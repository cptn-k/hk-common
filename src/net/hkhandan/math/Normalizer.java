/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.math;

/**
 *
 * @author hamed
 */
public class Normalizer {
    private double scale = 1;
    private double translation = 0;

    public Normalizer(double[] x) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        
        for(int i = x.length - 1; i >= 0; i--) {
            if(x[i] < min)
                min = x[i];
            if(x[i] > max)
                max = x[i];
        }

        scale = max - min;
        translation = min;

        for(int i = x.length - 1; i >= 0; i--)
            x[i] = (x[i] - translation)/scale;
    }

    public Normalizer(double[][] x) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        int s1 = x.length;
        int s2 = x[0].length;

        for(int i = 0; i < s1; i++) {
            for(int j = 0; j < s2; j++) {
                if(x[i][j] < min)
                    min = x[i][j];
                if(x[i][j] > max)
                    max = x[i][j];
            }
        }

        scale = max - min;
        translation = min;

        for(int i = 0; i < s1; i++)
            for(int j = 0; j < s2; j++)
            x[i][j] = (x[i][j] - translation)/scale;
    }

    public double normalize(double x) {
        return (x - translation)/scale;
    }

    public double denormalize(double x) {
        return x * scale + translation;
    }
}
