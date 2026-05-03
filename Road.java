/*
 * File Name: Road.java
 * Author: Samuel Steen
 * Date: 04/19/2026
 * Purpose: Represents a directional road segment with lane markings.
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Road {

    public final static Color COLOR = Color.LIGHT_GRAY;

    private final int xCoordinate;
    private final int yCoordinate;

    private final int roadLength;
    private final int roadWidth;

    private final Direction direction;

    public static final int ROAD_THICKNESS = 100;
    public static final int ROAD_LENGTH = 450;

    public enum Direction {
        DOWN,
        RIGHT
    }

    public Road(int x, int y, Road.Direction direction) {
        this.xCoordinate = x;
        this.yCoordinate = y;
        this.direction = direction;

        if (direction == Road.Direction.DOWN) {
            this.roadWidth = ROAD_THICKNESS;
            this.roadLength = ROAD_LENGTH;
        } else {
            this.roadWidth = ROAD_LENGTH;
            this.roadLength = ROAD_THICKNESS;
        }
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public int getRoadLength() {
        return roadLength;
    }

    public int getRoadWidth() {
        return roadWidth;
    }

    public Direction getRoadDirection() {
        return direction;
    }

    /* 
    Draws the road using its stored width and length
     */
    public void drawRoad(Graphics graphics) {
        drawRoad(graphics, getRoadWidth(), getRoadLength());
    }

    /*
    Draws the road using the width and height passed in
     */
    public void drawRoad(Graphics graphics, int drawWidth, int drawHeight) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        int x = getXCoordinate();
        int y = getYCoordinate();
        Color roadColor = Road.COLOR;

        graphics2D.setColor(roadColor);
        graphics2D.fillRect(x, y, drawWidth, drawHeight);

        /* sets the color for the road double median lines */
        graphics2D.setColor(Color.YELLOW);

        if (direction == Direction.DOWN) {
            drawRoadLinesVertical(graphics2D, drawHeight, drawWidth);
        } else {
            drawRoadLinesHorizontal(graphics2D, drawHeight, drawWidth);
        }
    }

    /*
    Draws lane markings and borders for vertical roads
     */
    private void drawRoadLinesVertical(Graphics2D graphics2D, int drawHeight, int drawWidth) {
        int roadXCoordinate = getXCoordinate();
        int roadYCoordinate = getYCoordinate();

        /* Calculate center of road for lane placement */
        int centerX = roadXCoordinate + (drawWidth / 2);
        int lineGap = 6;
        int lineWidth = 3;

        graphics2D.fillRect(
                centerX - lineGap / 2 - lineWidth, roadYCoordinate,
                lineWidth, drawHeight
        );
        graphics2D.fillRect(
                centerX + lineGap / 2,
                roadYCoordinate, lineWidth, drawHeight
        );

        /* Calculate road edges for outlining */
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawLine(
                roadXCoordinate, roadYCoordinate, roadXCoordinate,
                roadYCoordinate + drawHeight
        );
        graphics2D.drawLine(
                roadXCoordinate + drawWidth - 1, roadYCoordinate,
                roadXCoordinate + drawWidth - 1,
                roadYCoordinate + drawHeight
        );
    }

    /*
    Draws lane markings and borders for horizontal roads
     */
    private void drawRoadLinesHorizontal(Graphics2D graphics2D, int drawHeight, int drawWidth) {
        int roadXCoordinate = getXCoordinate();
        int roadYCoordinate = getYCoordinate();

        /* Calculate center of road for lane placement */
        int centerY = roadYCoordinate + (drawHeight / 2);
        int lineGap = 6;
        int lineWidth = 3;

        graphics2D.fillRect(roadXCoordinate, centerY - lineGap / 2 - lineWidth, drawWidth, lineWidth);
        graphics2D.fillRect(roadXCoordinate, centerY + lineGap / 2, drawWidth, lineWidth);

        graphics2D.setColor(Color.BLACK);
        graphics2D.drawLine(roadXCoordinate, roadYCoordinate,
                roadXCoordinate + drawWidth, roadYCoordinate);

        graphics2D.drawLine(roadXCoordinate, roadYCoordinate + drawHeight - 1,
                roadXCoordinate + drawWidth, roadYCoordinate + drawHeight - 1);
    }
}
