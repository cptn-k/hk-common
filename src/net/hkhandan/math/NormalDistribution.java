/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hkhandan.math;

import static net.hkhandan.math.HKMath.sqr;

/**
 *
 * @author hamed
 */
public class NormalDistribution {
    private final static double CONST_COEF = 1/Math.sqrt(2 * Math.PI);
    
    private double µ;
    private double σ;
    
    private final Object spareMonitor = new Object();
    private double spareSample = 0;
    private boolean isSpareReady = false;
    
    /**
     * 
     * @param µ - Mean
     * @param σ - Standard deviation
     */
    public NormalDistribution(double µ, double σ) {
        this.µ = µ;
        this.σ = σ;
    }
    
    public double getMean() {
        return µ;
    }
    
    public double getStandardDeviation() {
        return σ;
    }
    
    public double pdf(double x) {
        return (1/(σ * 2*Math.PI)) * Math.exp(-sqr(x - µ)/(2*sqr(σ)));
    }
    
    public double generateRandomSample() {
        synchronized(spareMonitor) {
            if(isSpareReady) {
                isSpareReady = false;
                return spareSample;
            }
        }
        
        double u = Math.random();
        double v = Math.random();
        double coef = Math.sqrt(-2 * Math.log(u));
        double x = coef * Math.cos(2 * Math.PI * v) * σ + µ;
        
        synchronized(spareMonitor) {
            spareSample = coef * Math.sin(2 * Math.PI * v) * σ + µ;
            isSpareReady = true;
        }
        
        return x;
    }
    
    public static void main(String[] args) {
        NormalDistribution d = new NormalDistribution(10, 2);
        for(int i = 0; i < 100; i++) {
            System.out.println(d.generateRandomSample());
        }
    }
}
