/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.math;

/**
 *
 * @author hamed
 */
public class DoublePair {
    public final double x;
    public double y;
    
    public DoublePair(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
