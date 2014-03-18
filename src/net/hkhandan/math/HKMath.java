/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.math;

import java.awt.Color;
import javax.swing.*;
import net.hkhandan.gui.CurveCanvas2D;

/**
 *
 * @author hamed
 */
public class HKMath {
    public static double sqr(double a) {
        return a * a;
    }
    public static double gaussian(double x, double mu, double sigma) {
        return Math.exp(-sqr(x - mu)/(2*sqr(sigma)));
    }

    public static double cube(double a) {
        return a * a * a;
    }

    public static double sech(double x) {
        return 1/Math.cosh(x);
    }

    public static double sech2(double x) {
        return 1 - sqr(Math.tanh(x));
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(sqr(x1 - x2) + sqr(y1 - y2));
    }
    
    /*
     * Uses Marsaglia algorithm to generate Gaussian pseudo random numbers.
     */
    public static double rndGaussian(double mu, double sigma) {
        do {
            double u = Math.random() * 2 - 1;
            double v = Math.random() * 2 - 1;
            double s = sqr(u) + sqr(v);
            if(s < 1)
                return v * Math.sqrt(-2 * Math.log(s) / s) * sigma + mu;
        } while(true);
    }

    public static double rndExponential(double mu) {
        double u = Math.random();
        return -mu * Math.log(-u + 1);
    }

    public static double rndPisitiveUniform() {
        double v;
        do {
            v = Math.random();
        } while(v == 0);
        return v;
    }

    /**
     *
     * @param alpha in (0, 2]
     * @param beta  in [-1, 1]
     * @param c     positive
     * @return
     */
    public static double rndLevy(double alpha, double beta, double c) {
        double omega = (Math.random() - 0.5) * Math.PI;

        double phi;
        do {
            phi = rndExponential(1);
        } while(phi == 0);

        double n1 = Math.sin(alpha * phi + Math.atan(beta * Math.tan(alpha * Math.PI/2)));
        double n2 = Math.pow(Math.cos((1 - alpha)*phi - Math.atan(beta * Math.tan(alpha * Math.PI/2))), 1/alpha - 1);
        double d  = Math.pow(Math.cos(Math.atan(beta*Math.tan(alpha*Math.PI/2))), 1/alpha) * Math.pow(Math.cos(phi), 1/alpha) * Math.pow(omega, 1/alpha - 1);
        return c * n1 * n2 /d;
    }

    public static void main(String[] args) {
        double[] x = new double[200];
        double[] y = new double[x.length];

        JFrame f = new JFrame();
        final CurveCanvas2D canvas = new CurveCanvas2D();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new java.awt.BorderLayout());
        f.setSize(600, 400);
        f.add(canvas, java.awt.BorderLayout.CENTER);

        for(int i = 0; i < x.length; i++) {
            x[i] = i;
            y[i] = 0;
        }
        for(int i = 0; i < 100000; i++) {
//            int rnd = (int)(random_exponential(1)*10);
            int rnd = (int)(rndLevy(0.5, 1, 10) + (x.length/2));
            if(rnd < x.length && rnd > 0)
                y[rnd]++;
        }

        canvas.putPlot(x, y, "levy", null, -1, true);

        f.setVisible(true);
    }
}
