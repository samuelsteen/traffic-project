/*
 * File Name: LightController.java
 * Author: Samuel Steen
 * Date: 04/19/2026
 * Purpose: Controls one traffic light direction thread, updates light colors,
 *          handles pause timing, and coordinates light changes for one
 *          intersection.
 */

public class LightController implements Runnable {

    private volatile boolean running = true;

    private final Intersection intersection;
    private final Main panel;

    private final LightAxis lightAxis;

    public enum LightAxis {
        VERTICAL,
        HORIZONTAL
    }

    public LightController(Intersection intersection, Main panel, LightAxis lightAxis) {
        this.intersection = intersection;
        this.panel = panel;
        this.lightAxis = lightAxis;
    }

    @Override
    public void run() {
        while (running) {
            synchronized (intersection) {
                setControlledLightToRedIfNotActive();
            }

            if (intersection.getActiveLightDirection() != getControlledDirection()) {
                try {
                    Thread.sleep(100);
                    continue;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            TrafficLight.LightColor currentColor = getCurrentLightColor();

            if (currentColor == TrafficLight.LightColor.RED) {
                /* Wait before turning green to let cars to get out of the intersection */
                if (!waitWithPauseCheck(2000)) {
                    break;
                }
            } else if (currentColor == TrafficLight.LightColor.YELLOW) {
                if (!waitWithPauseCheck(3000)) {
                    break;
                }
            } else {
                if (!waitWithPauseCheck(intersection.getTrafficLightDuration() * 1000)) {
                    break;
                }
            }

            synchronized (intersection) {
                if (lightAxis == LightAxis.VERTICAL) {
                    setNextLightVertical();
                } else {
                    setNextLightHorizontal();
                }
            }

            panel.repaint();
        }
    }

    private TrafficLight.LightColor getCurrentLightColor() {
        return (lightAxis == LightAxis.VERTICAL)
                ? intersection.getCurrentVerticalLight()
                : intersection.getCurrentHorizontalLight();
    }

    private void setNextLightVertical() {
        switch (intersection.getCurrentVerticalLight()) {
            case RED ->
                intersection.setVerticalLight(TrafficLight.LightColor.GREEN);
            case GREEN ->
                intersection.setVerticalLight(TrafficLight.LightColor.YELLOW);
            default -> {
                intersection.setVerticalLight(TrafficLight.LightColor.RED);
                intersection.setActiveLightDirection(Intersection.ActiveLightDirection.HORIZONTAL);
            }
        }
    }

    private void setNextLightHorizontal() {
        switch (intersection.getCurrentHorizontalLight()) {
            case RED ->
                intersection.setCurrentHorizontalLight(TrafficLight.LightColor.GREEN);
            case GREEN ->
                intersection.setCurrentHorizontalLight(TrafficLight.LightColor.YELLOW);
            default -> {
                intersection.setCurrentHorizontalLight(TrafficLight.LightColor.RED);
                intersection.setActiveLightDirection(Intersection.ActiveLightDirection.VERTICAL);
            }
        }
    }

    private void setControlledLightToRedIfNotActive() {
        if (isLightActive(lightAxis) || isLightRed(lightAxis)) {
            return;
        }

        if (lightAxis == LightAxis.VERTICAL) {
            intersection.setVerticalLight(TrafficLight.LightColor.RED);
        } else {
            intersection.setCurrentHorizontalLight(TrafficLight.LightColor.RED);
        }

        panel.repaint();
    }

    /*
    Checks to see of the provided light is red
     */
    private boolean isLightRed(LightAxis lightAxis) {
        if (lightAxis == LightAxis.VERTICAL) {
            return intersection.getCurrentVerticalLight() == TrafficLight.LightColor.RED;
        } else {
            return intersection.getCurrentHorizontalLight() == TrafficLight.LightColor.RED;
        }
    }

    /*
    Checks to see of the provided light is the active light in the intersection
     */
    private boolean isLightActive(LightAxis lightAxis) {
        if (lightAxis == LightAxis.VERTICAL) {
            return intersection.getActiveLightDirection() == Intersection.ActiveLightDirection.VERTICAL;
        }
        return intersection.getActiveLightDirection() == Intersection.ActiveLightDirection.HORIZONTAL;
    }

    /*
    Returns the direction of the light currently in control
     */
    private Intersection.ActiveLightDirection getControlledDirection() {
        return lightAxis == LightAxis.VERTICAL
                ? Intersection.ActiveLightDirection.VERTICAL
                : Intersection.ActiveLightDirection.HORIZONTAL;
    }

    private boolean waitWithPauseCheck(int totalMillis) {
        int elapsedTime = 0;

        while (elapsedTime < totalMillis && running) {
            try {

                if (Main.PAUSED) {
                    Thread.sleep(100);
                    continue;
                }
                Thread.sleep(100);
                elapsedTime += 100;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    /* 
    Returns whether any of the intersections lights are yellow
     */
    public boolean checkIfCurrentLightsAreYellow() {
        if (lightAxis == LightAxis.VERTICAL) {
            return intersection.getCurrentVerticalLight() == TrafficLight.LightColor.YELLOW;
        }
        return intersection.getCurrentHorizontalLight() == TrafficLight.LightColor.YELLOW;
    }

    public void stop() {
        running = false;
    }
}
