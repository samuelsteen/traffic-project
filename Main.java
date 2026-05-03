/*
 * File Name: Main.java
 * Author: Samuel Steen
 * Date: 04/19/2026
 * Purpose: Creates the main GUI, controls simulation, and renders
 *          intersections, traffic lights, cars, and the clock display.
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Main extends JPanel {

    private static final ArrayList<Intersection> intersections = new ArrayList<>();
    private static final ArrayList<Car> cars = new ArrayList<>();
    private static final ArrayList<LightController> lightControllers = new ArrayList<>();
    private static final ArrayList<CarController> carControllers = new ArrayList<>();

    private JPanel carInfoPanel;
    private boolean simulationStarted;

    /* whether the simulation is paused */
    public static volatile boolean PAUSED = true;
    private final int MAXCARS = 10;

    public static void main(String[] args) {

        Main panel = new Main();
        createAndShowUI(panel);

        spawnInitialThreeCars(panel);

        panel.repaint();
    }

    private static void spawnInitialThreeCars(Main panel) {
        for (int i = 0; i < 3; i++) {
            panel.spawnCar();
        }
    }

    private static void createAndShowUI(Main panel) {

        JFrame window = new JFrame("Traffic Sim");
        panel.setBackground(Color.WHITE);

        addInitialIntersectionsToList();

        panel.setIntersectionNeighbors();

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(panel, BorderLayout.CENTER);
        rootPanel.add(panel.createControlPanel(), BorderLayout.SOUTH);

        JPanel infoJPanel = panel.setUpInfoPanel();
        rootPanel.add(infoJPanel, BorderLayout.EAST);
        panel.carInfoPanel = panel.createCarInfoPanel();
        rootPanel.add(panel.carInfoPanel, BorderLayout.WEST);
        window.add(rootPanel);
        window.setSize(1200, 400);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setVisible(true);
    }

    private JPanel createCarInfoPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(100, 40));
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    /*
    Creates the card to display each cars information
     */
    private JPanel createCarInfoCard(Car car) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.add(new JLabel("Car " + car.getCarId()));
        card.add(new JLabel("X: " + car.getXCoordinate()));
        card.add(new JLabel("Y: " + car.getYCoordinate()));
        card.add(new JLabel("Speed: " + car.getCarSpeed() + " MPH"));
        card.add(new JLabel(" "));

        return card;
    }

    private void updateCarInfoPanel() {
        if (carInfoPanel == null) {
            return;
        }
        carInfoPanel.removeAll();

        synchronized (cars) {
            for (Car car : cars) {
                carInfoPanel.add(createCarInfoCard(car));
            }
        }

        carInfoPanel.revalidate();
        carInfoPanel.repaint();
    }

    /*
    Sets LightControllers for each direction in the intersection and starts each on its own thread
     */
    private void setUpLightControllers() {
        for (Intersection intersection : intersections) {
            startLightControllersForIntersection(intersection);
        }
    }

    private void startLightControllersForIntersection(Intersection intersection) {
        LightController horizontalLightController = new LightController(
                intersection,
                this,
                LightController.LightAxis.HORIZONTAL
        );

        LightController verticalLightController = new LightController(
                intersection,
                this,
                LightController.LightAxis.VERTICAL
        );

        synchronized (lightControllers) {
            lightControllers.add(horizontalLightController);
            lightControllers.add(verticalLightController);
        }

        Thread horizontalLightControllerThread = new Thread(horizontalLightController);
        Thread verticalLightControllerThread = new Thread(verticalLightController);

        horizontalLightControllerThread.start();
        verticalLightControllerThread.start();
    }

    private JPanel setUpInfoPanel() {

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);

        JLabel clock = setUpClock();

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        infoPanel.add(clock);
        infoPanel.setBackground(Color.WHITE);

        clock.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));
        wrapper.add(infoPanel, BorderLayout.NORTH);

        return wrapper;
    }

    /*
    Sets up the clock to show current timestamp
     */
    private JLabel setUpClock() {
        JLabel clock = new JLabel("HH:MM", SwingConstants.LEFT);
        clock.setPreferredSize(new Dimension(120, 40));
        startClockThread(clock);
        return clock;
    }

    private void startClockThread(JLabel clock) {
        Clock clock1 = new Clock(clock);
        Thread clockThread = new Thread(clock1);
        clockThread.start();
    }

    /*
    Adds control buttons to the control panel
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 6));
        createPauseButton(controlPanel);
        createContinueButton(controlPanel);
        createStartButton(controlPanel);
        createStopButton(controlPanel);
        createAddCarButton(controlPanel);
        createAddIntersectionButton(controlPanel);
        return controlPanel;
    }

    private void createAddIntersectionButton(JPanel controlPanel) {
        JButton addIntersectionButton = new JButton("Add Intersection");
        addIntersectionButton.addActionListener(event -> {
            Intersection intersection = new Intersection(50, 100);
            intersections.add(intersection);
            setIntersectionNeighbors();

            if (simulationStarted) {
                startLightControllersForIntersection(intersection);
            }
            repaint();
        });

        controlPanel.add(addIntersectionButton);
    }

    private void createAddCarButton(JPanel controlPanel) {
        JButton addCarButton = new JButton("Add Car");
        addCarButton.addActionListener(event -> {
            spawnCar();
            repaint();
        });
        controlPanel.add(addCarButton);
    }

    private void createPauseButton(JPanel controlPanel) {
        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(event -> {
            if (simulationStarted) {
                Main.PAUSED = true;
            }
        });
        controlPanel.add(pauseButton);
    }

    private void createContinueButton(JPanel controlPanel) {
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(event -> {
            if (simulationStarted) {
                Main.PAUSED = false;
            }
        });
        controlPanel.add(continueButton);
    }

    private void createStartButton(JPanel controlPanel) {
        JButton startButton = new JButton("Start");

        startButton.addActionListener(event -> {
            if (!simulationStarted) {
                simulationStarted = true;
                setUpLightControllers();
                repaint();
            }
            Main.PAUSED = false;
        });

        controlPanel.add(startButton);
    }

    private void createStopButton(JPanel controlPanel) {
        JButton stopButton = new JButton("Stop");

        stopButton.addActionListener(event -> {
            stopThreads();
            Main.PAUSED = true;
            simulationStarted = false;
            clearAllLists();
            repaint();
        });

        controlPanel.add(stopButton);
    }

    private void stopThreads() {
        for (LightController lightController : lightControllers) {
            lightController.stop();
        }
        for (CarController carController : carControllers) {
            carController.stop();
        }
    }

    private void clearAllLists() {
        synchronized (cars) {
            cars.clear();
        }
        synchronized (carControllers) {
            carControllers.clear();
        }
        synchronized (lightControllers) {
            lightControllers.clear();
        }
    }

    /* 
    Returns whether the intersection has another intersection on its right side 
     */
    private static boolean hasRightNeighbor(Intersection intersection) {
        int index = intersections.indexOf(intersection);
        return index < intersections.size() - 1;
    }

    /* 
    Sets whether each intersection has neighboring intersections 
     */
    private void setIntersectionNeighbors() {
        for (Intersection intersection : intersections) {
            intersection.setLeftNeighbor(hasLeftNeighbor(intersection));
            intersection.setRightNeighbor(hasRightNeighbor(intersection));
        }
    }

    /*
    Returns whether the intersection has another intersection on its left side
     */
    private static boolean hasLeftNeighbor(Intersection intersection) {
        int index = intersections.indexOf(intersection);
        return index > 0;
    }

    private static void addInitialIntersectionsToList() {
        for (int i = 0; i < 3; i++) {
            intersections.add(new Intersection(100, 100));
        }
    }

    /*
    Selects a spawnpoint for car and makes sure it is a valid option before spawning
     */
    void spawnCar() {
        /* Make sure there are valid intersection befor spawning cars */
        if (intersections.isEmpty() || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        /* Make sure there will only be up to the max amount of cars */
        if (cars.size() >= MAXCARS) {
            return;
        }

        Random rand = new Random();
        int attempts = 0;
        int maxAttempts = 20;

        while (attempts < maxAttempts) {

            int randomIntersectionIndex = rand.nextInt(intersections.size());
            Intersection intersection = intersections.get(randomIntersectionIndex);
            Car.Direction carDirection = getValidSpawnDirection(rand, intersection);
            Point spawnPoint = getCarSpawnPoint(
                    randomIntersectionIndex,
                    intersection,
                    carDirection);

            if (trySpawningCar(spawnPoint, carDirection, randomIntersectionIndex)) {
                return;
            }

            attempts++;
        }
    }

    /*
    Checks the intended coordinates to see if there is already a car there.
    Returns true if the spot is available and returns false if not
     */
    private boolean trySpawningCar(Point spawnPoint,
            Car.Direction carDirection, int randomIntersectionIndex
    ) {
        boolean blocked = checkIfCarIsBlocked(spawnPoint);
        if (!blocked) {

            Car car = new Car(spawnPoint.x, spawnPoint.y, carDirection);

            car.setIntersectionIndex(randomIntersectionIndex);
            car.setDistanceFromSpawn(0);
            car.setSpawnAnchor(spawnPoint);

            CarController carController = new CarController(this, car, cars, intersections);
            Thread carControllerThread = new Thread(carController);

            carControllers.add(carController);
            carControllerThread.start();

            synchronized (cars) {
                cars.add(car);
            }

            return true;
        }

        return false;
    }

    /*
    Gets a valid spawn direction for car and returns the correct direction for the car to travel
     */
    private Car.Direction getValidSpawnDirection(Random rand, Intersection intersection) {

        Car.Direction carDirection;

        /* Get the list of valid car directions */
        Car.Direction[] directions = Car.Direction.values();
        int randomDirectionsIndex = rand.nextInt(directions.length);
        HashMap<Car.Direction, Boolean> neighbors = getValidSpawnDirections(intersection);

        while (true) {
            if (neighbors.get(directions[randomDirectionsIndex])) {
                carDirection = directions[randomDirectionsIndex];
                break;
            }
            randomDirectionsIndex = rand.nextInt(directions.length);
        }
        return carDirection;
    }

    /*
    Checks to make sure spawn coordinates are available
     */
    private boolean checkIfCarIsBlocked(Point spawnPoint) {
        synchronized (cars) {
            for (Car existingCar : cars) {
                if (existingCar.getXCoordinate() == spawnPoint.x
                        && existingCar.getYCoordinate() == spawnPoint.y) {
                    return true;
                }
            }
        }

        return false;
    }

    /* 
    Gets cars spawn point from the intersection
     */
    private Point getCarSpawnPoint(
            int randomIntersectionIndex,
            Intersection intersection,
            Car.Direction carDirection
    ) {

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int intersectionWidth = panelWidth / intersections.size();
        int intersectionHeight = panelHeight;

        int intersectionX = randomIntersectionIndex * intersectionWidth;
        int intersectionY = 0;

        Point spawnPoint = intersection.getCarSpawnPoint(
                carDirection,
                intersectionX,
                intersectionY,
                intersectionWidth,
                intersectionHeight
        );

        return spawnPoint;
    }

    /* 
    Checks the chosen intersection and returns which directions are spawnable
     */
    private HashMap<Car.Direction, Boolean> getValidSpawnDirections(
            Intersection intersection) {

        HashMap<Car.Direction, Boolean> neighbors = new HashMap<>();

        neighbors.put(Car.Direction.UP, true);
        neighbors.put(Car.Direction.DOWN, true);
        neighbors.put(Car.Direction.RIGHT, !intersection.checkIfHasLeftNeighbor());
        neighbors.put(Car.Direction.LEFT, !intersection.checkIfHasRightNeighbor());

        return neighbors;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int intersectionWidth = panelWidth / intersections.size();
        int intersectionHeight = panelHeight;

        for (int i = 0; i < intersections.size(); i++) {
            Intersection intersection = intersections.get(i);

            int xCoordinate = i * intersectionWidth;
            int yCoordinate = 0;

            intersection.drawIntersection(graphics, xCoordinate,
                    yCoordinate, intersectionWidth, intersectionHeight);
        }

        drawCars(graphics, panelWidth, panelHeight);
        drawLights(graphics, panelWidth, panelHeight);
        updateCarInfoPanel();
    }

    /*
    Draws the intersection light to the intesections
     */
    private void drawLights(Graphics graphics, int panelWidth, int panelHeight) {
        int intersectionWidth = panelWidth / intersections.size();
        int intersectionHeight = panelHeight;

        for (int i = 0; i < intersections.size(); i++) {
            Intersection intersection = intersections.get(i);

            int xCoordinate = i * intersectionWidth;
            int yCoordinate = 0;

            int roadThickness = Road.ROAD_THICKNESS + 1;
            int centerX = xCoordinate + (intersectionWidth / 2);
            int centerY = yCoordinate + (intersectionHeight / 2);

            int verticalRoadX = centerX - (roadThickness / 2);
            int horizontalRoadY = centerY - (roadThickness / 2);

            intersection.drawIntersectionLights(graphics, verticalRoadX, horizontalRoadY, roadThickness);
        }
    }

    /*
    Draws cars to their points
     */
    private void drawCars(Graphics graphics, int panelWidth, int panelHeight) {
        synchronized (cars) {
            for (Car car : cars) {
                Point spawnPoint = calculateCarSpawnAnchor(car, panelWidth, panelHeight);

                car.setSpawnAnchor(spawnPoint);

                int carX = spawnPoint.x;
                int carY = spawnPoint.y;

                switch (car.getCarDirection()) {
                    case UP ->
                        carY -= car.getDistanceFromSpawn();
                    case DOWN ->
                        carY += car.getDistanceFromSpawn();
                    case LEFT ->
                        carX -= car.getDistanceFromSpawn();
                    case RIGHT ->
                        carX += car.getDistanceFromSpawn();
                }

                car.setPosition(carX, carY);
                car.drawCar(graphics);
            }
        }
    }

    /*
    Calculates and returns the spawnpoint of car
     */
    private Point calculateCarSpawnAnchor(Car car, int panelWidth, int panelHeight) {
        int carIntersectionWidth = panelWidth / intersections.size();
        int carIntersectionHeight = panelHeight;

        int carIntersectionX = car.getIntersectionIndex() * carIntersectionWidth;
        int carIntersectionY = 0;

        Intersection carIntersection = intersections.get(car.getIntersectionIndex());

        return carIntersection.getCarSpawnPoint(
                car.getCarDirection(),
                carIntersectionX,
                carIntersectionY,
                carIntersectionWidth,
                carIntersectionHeight);
    }
}
