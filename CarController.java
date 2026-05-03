/*
 * File Name: CarController.java
 * Author: Samuel Steen
 * Date: 04/24/2026
 * Purpose: Controls one car thread, updates car movement, checks traffic
 *          lights, stops cars from crashing, and deletes cars that leave
 *          the screen.
 */

import java.util.ArrayList;

public class CarController implements Runnable {

    private volatile boolean running = true;

    private final ArrayList<Intersection> intersections;
    private final ArrayList<Car> cars;

    private final Main panel;
    private final Car controlledCar;

    private long lastUpdateTime = System.nanoTime();

    private static final double METERS_BETWEEN_TRAFFIC_LIGHTS = 1000.0;
    private static final int THREAD_SLEEP_MILLIS = 40;
    private static final double SIMULATION_TIME_SCALE = 20.0;
    private final int MINIMUM_GAP_BETWEEN_CARS = 12;

    public CarController(
            Main panel,
            Car controlledCar,
            ArrayList<Car> cars,
            ArrayList<Intersection> intersections
    ) {
        this.intersections = intersections;
        this.controlledCar = controlledCar;
        this.panel = panel;
        this.cars = cars;
    }

    @Override
    public void run() {
        while (running) {

            if (Main.PAUSED) {
                try {
                    lastUpdateTime = System.nanoTime();
                    Thread.sleep(100);
                    continue;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            try {
                long now = System.nanoTime();
                double elapsedSeconds = ((now - lastUpdateTime) / 1_000_000_000.0) * SIMULATION_TIME_SCALE;

                lastUpdateTime = now;

                moveCars(elapsedSeconds);
                Thread.sleep(THREAD_SLEEP_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void moveCars(double elapsedSeconds) {
        int panelWidth = panel.getWidth();
        int currentIntersectionWidth = panelWidth / intersections.size();
        int currentIntersectionIndex = getCurrentIntersectionIndex(currentIntersectionWidth);
        Intersection currentIntersection = intersections.get(currentIntersectionIndex);
        int stopLine = calculateStopLine(currentIntersectionIndex, currentIntersectionWidth);

        synchronized (currentIntersection) {
            TrafficLight.LightColor currentLightColor = currentIntersection
                    .getDirectionalLightColor(controlledCar.getCarDirection());

            /* calculates how many pixels the car should move based off car speed and intersection size */
            double pixelsPerMeter = currentIntersectionWidth / METERS_BETWEEN_TRAFFIC_LIGHTS;
            int carSpeed = controlledCar.calculatePixelMovement(elapsedSeconds, pixelsPerMeter);

            if (carSpeed <= 0) {
                return;
            }

            ArrayList<Car> carsInSameLane = getCarsInSameLane(currentIntersectionIndex);

            boolean shouldStopForLight = shouldCarStop(currentLightColor, stopLine, carSpeed);
            boolean shouldStopForCarAhead = shouldStopForCarAhead(carsInSameLane, carSpeed);

            if (!shouldStopForLight && !shouldStopForCarAhead) {
                controlledCar.setDistanceFromSpawn(
                        controlledCar.getDistanceFromSpawn() + carSpeed
                );
            }
        }

        removeUnrenderedCars();
        this.panel.repaint();
    }

    /*
    Gets the index of the interesection the car is currently in from the intersection ArrayList
     */
    private int getCurrentIntersectionIndex(int currentIntersectionWidth) {
        int currentIntersectionIndex = controlledCar.getXCoordinate() / currentIntersectionWidth;
        return Math.max(
                0,
                Math.min(currentIntersectionIndex, intersections.size() - 1));
    }

    /*
    Calculates the stop line position for the current intersection
    based on the car’s direction and the intersection’s screen bounds
     */
    private int calculateStopLine(int currentIntersectionIndex, int currentIntersectionWidth) {
        int panelHeight = panel.getHeight();
        int currentIntersectionHeight = panelHeight;
        int currentIntersectionX = currentIntersectionIndex * currentIntersectionWidth;
        int currentIntersectionY = 0;

        return intersections.get(currentIntersectionIndex).getStopLine(
                controlledCar.getCarDirection(),
                currentIntersectionX,
                currentIntersectionY,
                currentIntersectionWidth,
                currentIntersectionHeight);
    }

    /*
    Returns all cars in the same lane as the current car
     */
    private ArrayList<Car> getCarsInSameLane(int currentIntersectionIndex) {
        ArrayList<Car> sameLaneCars = new ArrayList<>();

        synchronized (cars) {
            for (Car car : cars) {
                if (isSameLaneCar(car, currentIntersectionIndex)) {
                    sameLaneCars.add(car);
                }
            }
        }
        return sameLaneCars;
    }

    private int getCarFront(Car controlledCar) {
        return switch (controlledCar.getCarDirection()) {
            case DOWN ->
                controlledCar.getYCoordinate() + controlledCar.getCarLength();
            case UP ->
                controlledCar.getYCoordinate();
            case RIGHT ->
                controlledCar.getXCoordinate() + controlledCar.getCarWidth();
            case LEFT ->
                controlledCar.getXCoordinate();
        };
    }

    /*
    Returns whether the checked car is in the same lane as the controlled car
     */
    private boolean isSameLaneCar(Car otherCar, int currentIntersectionIndex) {
        if (otherCar == controlledCar) {
            return false;
        }

        if (otherCar.getCarDirection() != controlledCar.getCarDirection()) {
            return false;
        }

        if (isCarIsVertical(controlledCar.getCarDirection())) {
            return otherCar.getIntersectionIndex() == currentIntersectionIndex;
        }
        return true;
    }

    private boolean isCarIsVertical(Car.Direction direction) {
        return direction == Car.Direction.UP || direction == Car.Direction.DOWN;
    }

    private boolean shouldStopForCarAhead(ArrayList<Car> sameLaneCars, int carSpeed) {
        Car nearestCarAhead = getNearestCarAhead(sameLaneCars);

        if (nearestCarAhead == null) {
            return false;
        }

        int currentGap = getCurrentGapBetweenCars(nearestCarAhead);
        return currentGap - carSpeed < MINIMUM_GAP_BETWEEN_CARS;
    }

    /*
    Returns the current gap between the currently being controlled and teh car infront of it
     */
    private int getCurrentGapBetweenCars(Car nearestCarAhead) {
        int currentControlledCarFront = getCarFront(controlledCar);
        int otherCarBack = getCarBack(nearestCarAhead);
        return switch (controlledCar.getCarDirection()) {
            case DOWN, RIGHT ->
                otherCarBack - currentControlledCarFront;
            case UP, LEFT ->
                currentControlledCarFront - otherCarBack;
        };
    }

    private Car getNearestCarAhead(ArrayList<Car> sameLaneCars) {
        Car nearestCarAhead = null;

        for (Car otherCar : sameLaneCars) {
            int otherCarPosition = getDirectionalPosition(otherCar);

            int controlledCarPosition = getDirectionalPosition(controlledCar);
            boolean isAhead = isOtherCarAhead(controlledCarPosition, otherCarPosition);
            if (!isAhead) {
                continue;
            }

            if (nearestCarAhead == null) {
                nearestCarAhead = otherCar;
                continue;
            }

            boolean isOtherCarCloser = isCloserToControlledCar(otherCarPosition, nearestCarAhead);
            if (isOtherCarCloser) {
                nearestCarAhead = otherCar;
            }
        }

        return nearestCarAhead;
    }

    /*
    Returns if the car being checked is closer than the the current nearest to the controlled car
     */
    private boolean isCloserToControlledCar(int otherCarPosition, Car nearestCarAhead) {
        int nearestPosition = getDirectionalPosition(nearestCarAhead);
        return switch (controlledCar.getCarDirection()) {
            case DOWN, RIGHT ->
                otherCarPosition < nearestPosition;
            case UP, LEFT ->
                otherCarPosition > nearestPosition;
        };
    }

    private boolean isOtherCarAhead(int controlledCarPosition, int otherCarPosition) {
        return switch (controlledCar.getCarDirection()) {
            case DOWN, RIGHT ->
                otherCarPosition > controlledCarPosition;
            case UP, LEFT ->
                otherCarPosition < controlledCarPosition;
        };
    }

    private int getDirectionalPosition(Car car) {
        return switch (car.getCarDirection()) {
            case DOWN, UP ->
                car.getYCoordinate();
            case RIGHT, LEFT ->
                car.getXCoordinate();
        };
    }

    private int getCarBack(Car car) {
        return switch (car.getCarDirection()) {
            case DOWN ->
                car.getYCoordinate();
            case UP ->
                car.getYCoordinate() + car.getCarLength();
            case RIGHT ->
                car.getXCoordinate();
            case LEFT ->
                car.getXCoordinate() + car.getCarWidth();
        };
    }

    private boolean shouldCarStop(
            TrafficLight.LightColor currentLightColor,
            int stopLine,
            int carSpeed
    ) {

        int carFront = getCarFront(controlledCar);

        if (currentLightColor != TrafficLight.LightColor.RED) {
            return false;
        }

        boolean movingForward = controlledCar.getCarDirection() == Car.Direction.DOWN
                || controlledCar.getCarDirection() == Car.Direction.RIGHT;

        if (movingForward) {
            return carFront < stopLine && carFront + carSpeed >= stopLine;
        } else {
            return carFront > stopLine && carFront - carSpeed <= stopLine;
        }
    }

    /*
    Remove car when it leaves the screen
     */
    private void removeUnrenderedCars() {
        synchronized (cars) {
            int beforeSize = cars.size();
            cars.removeIf(car -> {
                int x = car.getXCoordinate();
                int y = car.getYCoordinate();
                int width = car.getCarWidth();
                int length = car.getCarLength();

                return (x + width < 0
                        || x > this.panel.getWidth()
                        || y + length < 0
                        || y > this.panel.getHeight());
            });
            int afterSize = cars.size();

            if (afterSize < beforeSize) {

                // a car was removed
                panel.spawnCar();

            }
        }
    }

    public void stop() {
        running = false;
    }
}
