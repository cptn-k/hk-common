/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.math;

/**
 *
 * @author hamed
 */
public class IntPair {
    public int x;
    public int y;

    public IntPair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distance(IntPair other) {
        return distance(other.x, other.y);
    }

    public double distance(int x, int y) {
        return Math.sqrt(HKMath.sqr(this.x - x) + HKMath.sqr(this.y - y));
    }

    @Override
    public boolean equals(Object o) {
        IntPair p = (IntPair)o;
        if(p.x == x && p.y == y)
            return true;
        return false;
    }

    public boolean nextTo(IntPair o) {
        if(Math.abs(o.x - x) <= 1 && Math.abs(o.y - y) <= 1)
            return true;
        return false;
    }

    public boolean inSquareProximity(IntPair o, int n) {
        if(Math.abs(o.x - x) <= n && Math.abs(o.y - y) <= n)
            return true;
        return false;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.x;
        hash = 19 * hash + this.y;
        return hash;
    }



    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
