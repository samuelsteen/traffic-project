/*
 * File Name: Intersection.java
 * Author: Samuel Steen
 * Date: 04/19/2026
 * Purpose: Represents a traffic intersection, manages traffic lights,
 *          sets a stop line and spawn point calculations, and handles
 *          drawing of roads, lights, and intersection layout.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Random;

public class Intersection {

    private final int intersectionWidth;

    private boolean hasRightNeighbor = false;
    private boolean hasLeftNeighbor = false;

    private final int xCoordinate;
    private final int yCoordinate;

    private TrafficLight.LightColor verticalLight = TrafficLight.LightColor.GREEN;
    private TrafficLight.LightColor horizontalLight = TrafficLight.LightColor.RED;

    private ActiveLightDirection activeLightDirection;

    private final int trafficLightDuration;

    public enum ActiveLightDirection {
        VERTICAL,
        HORIZONTAL
    }

    public Intersection(int xCoordinate, int yCoordinate) {
        /* Add 1 pixel to size to cover the roads black outline in the intersections middle */
        this.intersectionWidth = Road.ROAD_THICKNESS + 1;

        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;

        Random rand = new Random();
        int randomLightDuration = rand.nextInt(7) + 5;
        this.trafficLightDuration = randomLightDuration;

        chooseStartingLightSet();
    }

    public int getXCoordinate() {
        return this.xCoordinate;
    }

    public int getYCoordinate() {
        return this.yCoordinate;
    }

    public void setRightNeighbor(boolean hasRightNeighbor) {
        this.hasRightNeighbor = hasRightNeighbor;
    }

    public void setLeftNeighbor(boolean hasLeftNeighbor) {
        this.hasLeftNeighbor = hasLeftNeighbor;
    }

    public boolean checkIfHasRightNeighbor() {
        return hasRightNeighbor;
    }

    public boolean checkIfHasLeftNeighbor() {
        return hasLeftNeighbor;
    }

    public int getTrafficLightDuration() {
        return this.trafficLightDuration;
    }

    public int getIntersectionWidth() {
        return this.intersectionWidth;
    }

    /* 
    Intersection is a square so width and height are the same.
    This is for readability.
     */
    public int getIntersectionHeight() {
        return this.intersectionWidth;
    }

    private void chooseStartingLightSet() {
        Random rand = new Random();
        int randomColorChoiceSeed = rand.nextInt(2);

        if (randomColorChoiceSeed == 0) {
            this.verticalLight = TrafficLight.LightColor.GREEN;
            this.horizontalLight = TrafficLight.LightColor.RED;
            this.activeLightDirection = ActiveLightDirection.VERTICAL;
        } else {
            this.verticalLight = TrafficLight.LightColor.RED;
            this.horizontalLight = TrafficLight.LightColor.GREEN;
            this.activeLightDirection = ActiveLightDirection.HORIZONTAL;
        }
    }

    public void drawIntersection(
            Graphics graphics, int xCoordinate, int yCoordinate,
            int intersectionPanelWidth, int intersectionPanelHeight) {

        Graphics2D graphics2D = (Graphics2D) graphics;

        int roadThickness = this.intersectionWidth;

        int centerX = xCoordinate + (intersectionPanelWidth / 2);
        int centerY = yCoordinate + (intersectionPanelHeight / 2);

        int verticalRoadX = centerX - (roadThickness / 2);
        int horizontalRoadY = centerY - (roadThickness / 2);

        drawRoadsToIntersection(
                graphics2D,
                xCoordinate,
                yCoordinate,
                verticalRoadX,
                horizontalRoadY,
                intersectionPanelHeight,
                intersectionPanelWidth,
                roadThickness);

        Color color = Road.COLOR;
        graphics2D.setColor(color);

        /*
        Set the intersection main square. +1 so the horizontal roads upper BLACK
        border is not shown
         */
        graphics2D.fillRect(verticalRoadX, horizontalRoadY - 1, roadThickness, roadThickness + 1);
        drawStopLines(graphics2D, verticalRoadX, horizontalRoadY, roadThickness);
    }

    /*
    Returns the current light color for the direction car is traveling in
     */
    public TrafficLight.LightColor getDirectionalLightColor(Car.Direction carDirection) {
        if (carDirection == Car.Direction.DOWN || carDirection == Car.Direction.UP) {
            return getCurrentVerticalLight();
        }
        return getCurrentHorizontalLight();
    }

    public synchronized TrafficLight.LightColor getCurrentVerticalLight() {
        return this.verticalLight;
    }

    public synchronized void setCurrentVerticalLight(TrafficLight.LightColor lightColor) {
        this.verticalLight = lightColor;
    }

    public synchronized void setVerticalLight(TrafficLight.LightColor lightColor) {
        this.verticalLight = lightColor;
    }

    public synchronized TrafficLight.LightColor getCurrentHorizontalLight() {
        return this.horizontalLight;
    }

    public synchronized void setCurrentHorizontalLight(TrafficLight.LightColor lightColor) {
        this.horizontalLight = lightColor;
    }

    public synchronized ActiveLightDirection getActiveLightDirection() {
        return this.activeLightDirection;
    }

    public synchronized void setActiveLightDirection(ActiveLightDirection activeLightDirection) {
        this.activeLightDirection = activeLightDirection;
    }

    /*
    Changes which color light is lit based on the last light color
     */
    public void advanceLightCycle() {
        if (verticalLight == TrafficLight.LightColor.GREEN) {
            verticalLight = TrafficLight.LightColor.YELLOW;
            horizontalLight = TrafficLight.LightColor.RED;

        } else if (verticalLight == TrafficLight.LightColor.YELLOW) {
            verticalLight = TrafficLight.LightColor.RED;
            horizontalLight = TrafficLight.LightColor.GREEN;

        } else if (horizontalLight == TrafficLight.LightColor.GREEN) {
            verticalLight = TrafficLight.LightColor.RED;
            horizontalLight = TrafficLight.LightColor.YELLOW;

        } else {
            verticalLight = TrafficLight.LightColor.GREEN;
            horizontalLight = TrafficLight.LightColor.RED;
        }
    }

    /*
    Adds roads to the interesection square to create the full intersection
     */
    private void drawRoadsToIntersection(
            Graphics2D graphics2D,
            int xCoordinate,
            int yCoordinate,
            int verticalRoadX,
            int horizontalRoadY,
            int intersectionPanelHeight,
            int intersectionPanelWidth,
            int roadThickness
    ) {
        Road verticalRoad = new Road(verticalRoadX, yCoordinate, Road.Direction.DOWN);
        Road horizontalRoad = new Road(xCoordinate, horizontalRoadY, Road.Direction.RIGHT);

        verticalRoad.drawRoad(graphics2D, roadThickness, intersectionPanelHeight);
        horizontalRoad.drawRoad(graphics2D, intersectionPanelWidth, roadThickness);
    }

    private void drawStopLines(Graphics2D graphics2D,
            int verticalRoadX, int horizontalRoadY, int roadThickness
    ) {
        int stopLineThickness = Math.max(3, roadThickness / 20);
        int stopLineOffset = Math.max(8, roadThickness / 10);

        graphics2D.setColor(Color.WHITE);

        drawTopStopLine(graphics2D, verticalRoadX, horizontalRoadY,
                roadThickness, stopLineThickness, stopLineOffset
        );

        drawBottomStopLine(graphics2D, verticalRoadX, horizontalRoadY,
                roadThickness, stopLineThickness, stopLineOffset
        );

        drawLeftStopLine(graphics2D, verticalRoadX, horizontalRoadY,
                roadThickness, stopLineThickness, stopLineOffset
        );

        drawRightStopLine(graphics2D, verticalRoadX, horizontalRoadY,
                roadThickness, stopLineThickness, stopLineOffset
        );
    }

    private void drawTopStopLine(Graphics2D graphics2D, int verticalRoadX, int horizontalRoadY,
            int roadThickness, int stopLineThickness, int stopLineOffset
    ) {
        graphics2D.fillRect(
                verticalRoadX + 1,
                horizontalRoadY - stopLineOffset,
                roadThickness / 2 - 15,
                stopLineThickness
        );
    }

    private void drawBottomStopLine(Graphics2D graphics2D, int verticalRoadX, int horizontalRoadY,
            int roadThickness, int stopLineThickness, int stopLineOffset
    ) {
        graphics2D.fillRect(
                verticalRoadX + (roadThickness / 2) + 15,
                horizontalRoadY + roadThickness + stopLineOffset - stopLineThickness,
                roadThickness / 2 - 16,
                stopLineThickness
        );
    }

    private void drawLeftStopLine(Graphics2D graphics2D, int verticalRoadX, int horizontalRoadY,
            int roadThickness, int stopLineThickness, int stopLineOffset
    ) {
        graphics2D.fillRect(
                verticalRoadX - stopLineOffset,
                horizontalRoadY + (roadThickness / 2) + 16,
                stopLineThickness,
                roadThickness / 3
        );
    }

    private void drawRightStopLine(Graphics2D graphics2D, int verticalRoadX, int horizontalRoadY,
            int roadThickness, int stopLineThickness, int stopLineOffset
    ) {
        graphics2D.fillRect(
                verticalRoadX + roadThickness + stopLineOffset - stopLineThickness,
                horizontalRoadY + 1,
                stopLineThickness,
                roadThickness / 2 - 16
        );
    }

    /*
    gets the coords of the stopline so the cars know where to stop at during red lights
     */
    public int getStopLine(Car.Direction direction, int xCoordinate,
            int yCoordinate, int intersectionPanelWidth, int intersectionPanelHeight
    ) {
        int roadThickness = this.intersectionWidth;
        int centerX = xCoordinate + (intersectionPanelWidth / 2);
        int centerY = yCoordinate + (intersectionPanelHeight / 2);
        int verticalRoadX = centerX - (roadThickness / 2);
        int horizontalRoadY = centerY - (roadThickness / 2);
        int stopLineOffset = Math.max(8, roadThickness / 10);

        return switch (direction) {
            case DOWN ->
                horizontalRoadY - stopLineOffset;
            case UP ->
                horizontalRoadY + roadThickness + stopLineOffset;
            case RIGHT ->
                verticalRoadX - stopLineOffset;
            case LEFT ->
                verticalRoadX + roadThickness + stopLineOffset;
        };
    }

    public void drawIntersectionLights(Graphics graphics, int verticalRoadX,
            int horizontalRoadY, int roadThickness
    ) {
        /* Get the centerd coordinates for the lights to sit in the middle of the road */
        int centeredHorizontalLight = verticalRoadX + (roadThickness / 2) + 15;
        int centeredVerticalLight = horizontalRoadY + (roadThickness / 2) + 15;

        TrafficLight upperLight = new TrafficLight(
                centeredHorizontalLight,
                horizontalRoadY,
                TrafficLight.Direction.UP);

        upperLight.setLightColor(verticalLight);
        upperLight.drawLight(graphics);

        TrafficLight lowerLight = new TrafficLight(
                centeredHorizontalLight,
                horizontalRoadY + roadThickness,
                TrafficLight.Direction.DOWN);

        lowerLight.setLightColor(verticalLight);
        lowerLight.drawLight(graphics);

        TrafficLight leftLight = new TrafficLight(
                verticalRoadX,
                centeredVerticalLight,
                TrafficLight.Direction.LEFT);

        leftLight.setLightColor(horizontalLight);
        leftLight.drawLight(graphics);

        TrafficLight rightLight = new TrafficLight(
                verticalRoadX + roadThickness,
                centeredVerticalLight,
                TrafficLight.Direction.RIGHT);

        rightLight.setLightColor(horizontalLight);
        rightLight.drawLight(graphics);
    }

    public Point getCarSpawnPoint(Car.Direction direction, int xCoordinate,
            int yCoordinate, int intersectionPanelWidth, int intersectionPanelHeight
    ) {
        int roadThickness = this.intersectionWidth;

        int centerX = xCoordinate + (intersectionPanelWidth / 2);
        int centerY = yCoordinate + (intersectionPanelHeight / 2);

        int laneOffset = roadThickness / 4;

        boolean verticalCar = direction == Car.Direction.UP || direction == Car.Direction.DOWN;
        int carWidth = verticalCar ? 20 : 40;
        int carLength = verticalCar ? 40 : 20;

        int spawnOffset = Math.max(8, roadThickness / 10);

        /* Get the center of the lanes */
        int leftLaneX = centerX - laneOffset;
        int rightLaneX = centerX + laneOffset;
        int upperLaneY = centerY - laneOffset;
        int lowerLaneY = centerY + laneOffset;

        return switch (direction) {
            case UP ->
                new Point(
                rightLaneX - (carWidth / 2),
                yCoordinate + intersectionPanelHeight - (carLength + 5) - spawnOffset
                );
            case DOWN ->
                new Point(
                leftLaneX - (carWidth / 2),
                yCoordinate + spawnOffset
                );
            case LEFT ->
                new Point(
                xCoordinate + intersectionPanelWidth - carWidth - spawnOffset,
                upperLaneY - (carLength / 2)
                );
            case RIGHT ->
                new Point(
                xCoordinate + spawnOffset,
                lowerLaneY - (carLength / 2)
                );
        };
    }
}
