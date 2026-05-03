
/*
 * File Name: TrafficLight.java
 * Author: Samuel Steen
 * Date: 04/19/2026
 * Purpose: Represents a directional traffic light with red,
 *          yellow, and green states.
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

public class TrafficLight {

    final private int xCoordinate;
    final private int yCoordinate;

    private LightColor lightColor;
    private final Direction direction;

    public enum LightColor {
        RED,
        YELLOW,
        GREEN
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public TrafficLight(int xCoordinate, int yCoordinate, Direction direction) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.direction = direction;
        this.lightColor = LightColor.RED;
    }

    public int getXCoordinate() {
        return this.xCoordinate;
    }

    public int getYCoordinate() {
        return this.yCoordinate;
    }

    public LightColor getCurrentLightColor() {
        return this.lightColor;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public void setLightColor(LightColor lightColor) {
        this.lightColor = lightColor;
    }

    /*
    Draws the teaffic light based on the direction the light is facing
     */
    public void drawLight(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;

        boolean vertical = direction == Direction.LEFT || direction == Direction.RIGHT;

        int housingWidth = vertical ? 16 : 30;
        int housingHeight = vertical ? 30 : 16;

        Point housingPosition = calculateHousingPosition(housingWidth, housingHeight, vertical);

        int housingX = housingPosition.x;
        int housingY = housingPosition.y;

        /* The arc to create the rounded light housing */
        int arc = 14;

        graphics2D.setColor(new Color(70, 70, 70));
        graphics2D.fillRoundRect(housingX, housingY, housingWidth, housingHeight, arc, arc);

        graphics2D.setColor(Color.BLACK);
        graphics2D.drawRoundRect(housingX, housingY, housingWidth, housingHeight, arc, arc);

        int padding = 6;
        int diameter = vertical ? housingWidth - (padding * 2) : housingHeight - (padding * 2);

        if (vertical) {
            drawVerticalLight(graphics2D, housingX, housingY, housingWidth,
                    housingHeight, padding, diameter
            );
        } else {
            drawHorizontalLight(graphics2D, housingX, housingY, housingWidth,
                    housingHeight, padding, diameter
            );
        }
    }

    private Point calculateHousingPosition(int housingWidth, int housingHeight, boolean vertical) {
        if (vertical) {
            return new Point(
                    xCoordinate - (housingWidth / 2),
                    yCoordinate - housingHeight);
        } else {
            return new Point(
                    xCoordinate - housingWidth,
                    yCoordinate - (housingHeight / 2));
        }
    }

    private void drawVerticalLight(
            Graphics2D graphics2D,
            int housingX,
            int housingY,
            int housingWidth,
            int housingHeight,
            int padding,
            int diameter
    ) {

        int centerX = housingX + (housingWidth / 2);

        int redY = housingY + padding;
        int yellowY = housingY + ((housingHeight - diameter) / 2);
        int greenY = housingY + housingHeight - padding - diameter;

        drawBulb(graphics2D, centerX - diameter / 2, redY, diameter,
                lightColor == LightColor.RED ? Color.RED : new Color(90, 20, 20));
        drawBulb(graphics2D, centerX - diameter / 2, yellowY, diameter,
                lightColor == LightColor.YELLOW ? Color.YELLOW : new Color(110, 95, 20));
        drawBulb(graphics2D, centerX - diameter / 2, greenY, diameter,
                lightColor == LightColor.GREEN ? Color.GREEN : new Color(20, 85, 20));
    }

    private void drawHorizontalLight(
            Graphics2D graphics2D,
            int housingX,
            int housingY,
            int housingWidth,
            int housingHeight,
            int padding,
            int diameter
    ) {

        int centerY = housingY + (housingHeight / 2);

        int redX = housingX + padding;
        int yellowX = housingX + ((housingWidth - diameter) / 2);
        int greenX = housingX + housingWidth - padding - diameter;

        drawBulb(graphics2D, redX, centerY - diameter / 2, diameter,
                lightColor == LightColor.RED ? Color.RED : new Color(90, 20, 20));
        drawBulb(graphics2D, yellowX, centerY - diameter / 2, diameter,
                lightColor == LightColor.YELLOW ? Color.YELLOW : new Color(110, 95, 20));
        drawBulb(graphics2D, greenX, centerY - diameter / 2, diameter,
                lightColor == LightColor.GREEN ? Color.GREEN : new Color(20, 85, 20));
    }

    private void drawBulb(Graphics2D graphics2D, int x, int y, int diameter, Color color) {
        graphics2D.setColor(color);
        graphics2D.fillOval(x, y, diameter, diameter);

        graphics2D.setColor(Color.BLACK);
        graphics2D.drawOval(x, y, diameter, diameter);
    }
}
