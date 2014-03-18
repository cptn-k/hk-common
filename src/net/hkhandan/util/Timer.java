/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.util;

/**
 *
 * @author hamed
 */
public class Timer {
    long start = 0;
    long period = 0;

    String name = "";

    public Timer(String name) {
        start = System.currentTimeMillis();
        this.name = name;
    }

    public void stop() {
        period = System.currentTimeMillis() - start;
    }

    public long get() {
        return System.currentTimeMillis() - start;
    }

    public String toString() {
        if(period != 0)
            return "Timer " + name + " " + Long.toString(period) + "ms past.";
        return "Timer " + name + " " + Long.toString(System.currentTimeMillis() - start) + "ms past.";
    }
}
