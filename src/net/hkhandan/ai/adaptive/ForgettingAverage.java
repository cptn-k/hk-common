/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.ai.adaptive;

/**
 *
 * @author hamed
 */
public class ForgettingAverage {
    private double[] values;
    private int count  = 0;
    private int index  = 0;
    private double avg = 0;

    public ForgettingAverage(int size) {
        values = new double[size];
    }

    public double incorporate(double d) {
        values[index] = d;
        if(count < values.length)
            count++;
        index = (index + 1)%values.length;

        double sum = 0;
        for(int i = 0; i < count; i++)
            sum += values[i];
        avg =  sum/count;

        return avg;
    }

    public double get() {
        return avg;
    }

    public void reset() {
        count = 0;
        index = 0;
        java.util.Arrays.fill(values, 0);
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "buffer=" + java.util.Arrays.toString(values) + ", count = " + count + ", index = " + index + ", average =" + avg;
    }

    public boolean isFilled() {
        return count == values.length;
    }

}
