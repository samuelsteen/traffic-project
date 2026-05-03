/*
 * File Name: Clock.java
 * Author: Samuel Steen
 * Date: 04/19/2026
 * Purpose: Runs a clock on a separate thread, updates the current time
 *          every second, and displays it in the GUI.
 */

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;

public class Clock implements Runnable {

    private volatile boolean running = true;

    private final JLabel label;

    @Override
    public void run() {

        while (running) {
            while (Main.PAUSED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }
            try {
                updateTime();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public Clock(JLabel label) {
        this.label = label;
    }

    private void updateTime() {
        LocalTime now = LocalTime.now();
        String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss a"));
        label.setText(time);

    }

    public void stop() {
        running = false;
    }
}
