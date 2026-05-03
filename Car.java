/*
 * File Name: Car.java
 * Author: Samuel Steen
 * Date: 04/19/2026
 * Purpose: Shows a car with position and direction.
 */
import java.awt.*;
import java.util.Random;

public class Car {

    private static int nextCarId = 1;

    private final Color color;
    private final int carId;

    private int xCoordinate;
    private int yCoordinate;

    private final int carLength;
    private final int carWidth;

    private final Direction direction;

    private int intersectionIndex;
    private int distanceFromSpawn;
    private Point spawnAnchor;

    private final int carSpeedMph;
    private double fractionalPixelDistance;

    private final Color[] colorLoop = {
        Color.RED,
        Color.BLUE,
        Color.DARK_GRAY,
        Color.MAGENTA,
        Color.orange,
        Color.PINK
    };

    /* Will determine which way the car is facing and moving */
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public Car(int xCoordinate, int yCoordinate, Direction direction) {
        this.carId = nextCarId++;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;

        Random rand = new Random();
        /* Speed is stored in miles per hour. */
        int randomSpeed = rand.nextInt(25) + 15;
        this.carSpeedMph = randomSpeed;
        this.fractionalPixelDistance = 0;

        /* set the car shape based off the direction it will move in. */
        if (direction == Car.Direction.DOWN || direction == Car.Direction.UP) {
            this.carLength = 40;
            this.carWidth = 20;
        } else {
            this.carLength = 20;
            this.carWidth = 40;
        }

        int randomIndex = rand.nextInt(this.colorLoop.length);
        this.color = this.colorLoop[randomIndex];
        this.direction = direction;
        this.intersectionIndex = -1;
        this.distanceFromSpawn = 0;
        this.spawnAnchor = new Point(xCoordinate, yCoordinate);
    }

    public int getCarId() {
        return this.carId;
    }

    public int getCarSpeed() {
        return this.carSpeedMph;
    }

    public void setPosition(int xCoordinate, int yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public void setIntersectionIndex(int intersectionIndex) {
        this.intersectionIndex = intersectionIndex;
    }

    public int getIntersectionIndex() {
        return intersectionIndex;
    }

    public void setDistanceFromSpawn(int distanceFromSpawn) {
        this.distanceFromSpawn = distanceFromSpawn;
    }

    public int getDistanceFromSpawn() {
        return distanceFromSpawn;
    }

    public void setSpawnAnchor(Point spawnAnchor) {
        this.spawnAnchor = spawnAnchor;
    }

    public Point getSpawnAnchor() {
        return spawnAnchor;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public int getCarLength() {
        return carLength;
    }

    public int getCarWidth() {
        return carWidth;
    }

    public Color getCarColor() {
        return color;
    }

    public Direction getCarDirection() {
        return direction;
    }

    /*
    Calculate the amount of pixels to move based off the cars speed.
     */
    public int calculatePixelMovement(double elapsedSeconds, double pixelsPerMeter) {
        double speedMetersPerSecond = this.carSpeedMph * 0.44704;
        double metersTraveled = speedMetersPerSecond * elapsedSeconds;
        double exactPixelDistance = metersTraveled * pixelsPerMeter + fractionalPixelDistance;

        int pixelMovement = (int) exactPixelDistance;
        fractionalPixelDistance = exactPixelDistance - pixelMovement;

        return pixelMovement;
    }

    public void drawCar(Graphics graphics) {

        Graphics2D graphics2D = (Graphics2D) graphics;

        int x = getXCoordinate();
        int y = getYCoordinate();

        int width = getCarWidth();
        int length = getCarLength();

        Direction carDirection = getCarDirection();

        /* Define arcs for rounded rectangles when making cars */
        int arcWidth = 10;
        int arcHeight = 10;

        Color carColor = getCarColor();

        graphics2D.setColor(carColor);
        graphics2D.fillRoundRect(x, y, width, length, arcWidth, arcHeight);

        drawWindshield(graphics, carDirection);
    }

    /*
    Defines and adds a windshield to the car
     */
    public void drawWindshield(Graphics graphics, Direction direction) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        Polygon windshield;

        /* positions the windshield based on cars direction */
        switch (direction) {
            case RIGHT ->
                windshield = buildWindshield(
                        0.35, 0.85, 0.5, 0.75,
                        0, 0,
                        0, 0,
                        -3, -5,
                        1, -1
                );

            case LEFT ->
                windshield = buildWindshield(
                        0.15, 0.85, 0.25, 0.5,
                        0, 0,
                        0, 0,
                        -1, 1,
                        -1, 0
                );

            case UP ->
                windshield = buildWindshield(
                        0.25, 0.5, 0.15, 0.85,
                        -1, 1,
                        0, 0,
                        0, 0,
                        0, 0
                );

            case DOWN ->
                windshield = buildWindshield(
                        0.5, 0.75, 0.01, 1,
                        4, -4,
                        -3, 3,
                        0, 0,
                        0, 0
                );

            default -> {
                return;
            }
        }

        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.fillPolygon(windshield);
    }

    /*
    Draws a windshield on car
     */
    private Polygon buildWindshield(
            double windshieldTopPadding,
            double windshieldBottomPadding,
            double windshieldLeftPadding,
            double windshieldRightPadding,
            int topLeftXOffset,
            int topRightXOffset,
            int bottomRightXOffset,
            int bottomLeftXOffset,
            int topLeftYOffset,
            int topRightYOffset,
            int bottomRightYOffset,
            int bottomLeftYOffset
    ) {

        /* calculates the 4 points to draw the windshield on the car */
        int windshieldTop = getYCoordinate() + (int) (getCarLength() * windshieldTopPadding);

        int windshieldBottom
                = getYCoordinate() + (int) (getCarLength() * windshieldBottomPadding);

        int windshieldLeft = getXCoordinate() + (int) (getCarWidth() * windshieldLeftPadding);

        int windshieldRight = getXCoordinate() + (int) (getCarWidth() * windshieldRightPadding);

        int[] xPoints = {
            windshieldLeft + topLeftXOffset,
            windshieldRight + topRightXOffset,
            windshieldRight + bottomRightXOffset,
            windshieldLeft + bottomLeftXOffset
        };

        int[] yPoints = {
            windshieldTop + topLeftYOffset,
            windshieldTop + topRightYOffset,
            windshieldBottom + bottomRightYOffset,
            windshieldBottom + bottomLeftYOffset
        };

        return new Polygon(xPoints, yPoints, 4);
    }
}
