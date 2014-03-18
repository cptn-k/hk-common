/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.math;

import net.hkhandan.util.TextTable;
import java.util.*;

/**
 *
 * @author hamed
 */
public class LineSegmentFunction {
    private ArrayList<DoublePair> points = new ArrayList<DoublePair>(100);

    public abstract class Integrator {
        public abstract double integrand(double x);
        public double f(double x) {
            return get(x);
        }

        public void init(double ... params) {
            // nothing
        }

        public double integral(double lbound, double ubound) {
            double sum = 0;
            int size = points.size();
            if(size == 0)
                return Double.NaN;
            if(Double.isInfinite(lbound))
                lbound = points.get(0).x;
            if(Double.isInfinite(ubound))
                ubound = points.get(points.size() - 1).x;
            
            int lindex = getAfter(lbound);
            int uindex = getAfter(ubound) - 1;

            if(lindex == size || uindex == 0)
                return Double.NaN;

            double xleft = points.get(lindex).x;
            double xright = points.get(uindex).x;

//            System.out.println("" + lbound + "-->" + xleft + " / " + integrand(lbound) + "-->" + integrand(xleft));
            sum += ((integrand(xleft) + integrand(lbound))*(xleft - lbound))/2;

            for(int i = lindex; i < uindex; i++) {
                double x1 = points.get(i).x;
                double x2 = points.get(i + 1).x;
//                System.out.println("" + x1 + "-->" + x2 + " / " + integrand(x1) + "-->" + integrand(x2));
                sum += ((integrand(x1) + integrand(x2))*(x2 - x1))/2;
            }
            
//            System.out.println("" + xright + "-->" + ubound + " / " + integrand(xright) + "-->" + integrand(ubound));
            sum += ((integrand(xright) + integrand(ubound))*(ubound - xright))/2;

            return sum;
        }
    }
    
    public abstract class CustomMax {
        public abstract double g(double x);
        public double f(double x) {
            return get(x);
        }

        public double max(double lbound, double rbound) {
            if(lbound < getLeftBound())
                lbound = getLeftBound();
            if(rbound > getRightBound())
                rbound = getRightBound();

            int i = 0;
            int s = points.size();
            double max = Double.NEGATIVE_INFINITY;
            int    maxIndex = -1;
            
            for(i = 0; i < s; i++) {
                if(points.get(i).x >= lbound)
                    break;
            }

            for(; i < s; i++) {
                double x = points.get(i).x;
                if(x > rbound)
                    break;

                double y = g(x);
                if(y > max) {
                    max = y;
                    maxIndex = i;
                }
            }

            if(maxIndex == -1)
                return Double.NaN;
            return points.get(maxIndex).x;
        }
    }

    public LineSegmentFunction() {
        // empty
    }

    public DoublePair addPoint(double x, double y) {
        if(points.isEmpty()) {
            DoublePair newPair = new DoublePair(x, y);
            points.add(newPair);
            return newPair;
        }
        int s = points.size();
        int l = 0;
        for(l = 0; l < s; l++) {
            if(points.get(l).x == x) {
                //System.out.println("equals");
                points.get(l).y = y;
                return points.get(l);
            }

            if(points.get(l).x > x)
                break;
        }

        DoublePair newPair = new DoublePair(x, y);
        points.add(l, newPair);
        return newPair;
    }

    public double get(double x) {
        int s = points.size();

        if(s == 0)
            return Double.NaN;
        
        int l = 0;
        for(l = 0; l < s; l++) {
            if(points.get(l).x > x)
                break;
        }

        if(l == 0 || l == s) {
            if(x == points.get(s - 1).x)
                return points.get(s - 1).y;
            return Double.NaN;
        }
        
        DoublePair left = points.get(l-1);
        DoublePair right = points.get(l);
        return (x - left.x) * ((right.y - left.y)/(right.x - left.x)) + left.y;
    }

    public double[] enumerateX() {
        double[] x = new double[points.size()];
        for(int i = x.length - 1; i >= 0; i--)
            x[i] = points.get(i).x;
        return x;
    }
    
    public double getLeftBound() {
        return points.get(0).x;
    }

    public double getRightBound() {
        return points.get(points.size() - 1).x;
    }

    private int getAfter(double x) {
        int s = points.size();
        int l = 0;
        for(l = 0; l < s; l++) {
            if(points.get(l).x > x)
                break;
        }
        return l;
    }

    public void clear() {
        points.clear();
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public int getSize() {
        return points.size();
    }

    public String dump() {
        TextTable tbl = new TextTable(3);
        tbl.setRow("i", "x", "y");
        tbl.hLine();
        int i = 0;
        for(DoublePair p:points)
            tbl.setRow(Integer.toString(i++), Double.toString(p.x), Double.toString(p.y));
        return tbl.toString();
    }

    public void print() {
        System.out.println("X");
        for(DoublePair p:points)
            System.out.println(p.x);
        System.out.println("\nY");
        for(DoublePair p:points)
            System.out.println(p.y);
    }

    public static void main(String[] args) {
        LineSegmentFunction fn = new LineSegmentFunction();
        for(double x = -6; x <= 6; x += 0.3)
            fn.addPoint(x, Math.sin(x));
        LineSegmentFunction.Integrator integrator = fn.new Integrator() {
          public double integrand(double x) {
              return f(x)*Math.exp(-x);
          }
        };
        LineSegmentFunction.CustomMax max = fn.new CustomMax() {
            @Override
            public double g(double x) {
                return f(x);
            }
        };
        System.out.println(fn.dump());
        //System.out.println("Integral = " + integrator.integral(9, Double.POSITIVE_INFINITY));
        System.out.println("Max = " + max.max(0, Double.POSITIVE_INFINITY));
    }

}
