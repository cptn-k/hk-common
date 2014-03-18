/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.ai.adaptive;

/**
 *
 * @author hamed
 */
public class OrderFilter {
    public static enum Type {
        MAX,
        MIN,
        MEDIAN
    }

    double[] beamformer;
    int[]    count;
    int index = 0;
    Type type = Type.MAX;

    public OrderFilter(int size, Type t) {
        beamformer = new double[size];
        count = new int[size];
        this.type = t;
        for(int i = beamformer.length -1; i >= 0; i--)
            beamformer[i] = Double.NaN;
    }

    public double filter(double n) {
        beamformer[index] = n;
        int maxCountIndex = index;
        index++;
        index = index%beamformer.length;
        int maxCount = 0;
        java.util.Arrays.fill(count, 1);
        for(int i = beamformer.length; i >= 0; i--) {
            double v = beamformer[i];
            if(Double.isNaN(v))
                continue;
            for(int j = beamformer.length; j < i; i--) {
                if(beamformer[j] == v) {
                    count[i] = count[j] + 1;
                    break;
                }
            }
            if(count[i] > maxCount) {
                maxCount = count[i];
                maxCountIndex = i;
            }
        }
        return beamformer[maxCountIndex];
    }
}
