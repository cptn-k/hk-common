package net.hkhandan.ai.adaptive;

// levenberg-marquardt in java
//
// To use this, implement the functions in the LMfunc interface.
//
// This library uses simple matrix routines from the JAMA java matrix package,
// which is in the public domain.  Reference:
//    http://math.nist.gov/javanumerics/jama/
// (JAMA has a matrix object class.  An earlier library JNL, which is no longer
// available, represented matrices as low-level arrays.  Several years
// ago the performance of JNL matrix code was better than that of JAMA,
// though improvements in java compilers may have fixed this by now.)
//
// One further recommendation would be to use an inverse based
// on Choleski decomposition, which is easy to implement and
// suitable for the symmetric inverse required here.  There is a choleski
// routine at idiom.com/~zilla.
//
// If you make an improved version, please consider adding your
// name to it ("modified by ...") and send it back to me
// (and put it on the web).
//
// ----------------------------------------------------------------
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Library General Public License for more details.
//
// You should have received a copy of the GNU Library General Public
// License along with this library; if not, write to the
// Free Software Foundation, Inc., 59 Temple Place - Suite 330,
// Boston, MA  02111-1307, USA.
//
// initial author contact info:
// jplewis  www.idiom.com/~zilla  zilla # computer.org,   #=at
//
// Improvements by:
// dscherba  www.ncsa.uiuc.edu/~dscherba
// Jonathan Jackson   j.jackson # ucl.ac.uk
// Hamed Khandan www.hkhandan.net
//
import java.util.Arrays;
import Jama.*;
import net.hkhandan.util.ArrayTools;

public abstract class LevenbergMarquardtEstimator {

    public static class Solution {

        public final double[] a;
        public final double lambda;
        public final double error;
        public final int iterations;

        public Solution(double[] a, double l, double e, int i) {
            this.a = a;
            lambda = l;
            error = e;
            iterations = i;
        }

        @Override
        public String toString() {
            return String.format(
                    "[parameters = %s; error = %f; iterations = %d]",
                    ArrayTools.toString(a, 4), error, iterations);
        }
    }

    public static class TrainingSetup {
        public double[][] x;
        public double[][] y;
        public double[] a;
        public double[] actual_a = null;
        public double[] s;
        public boolean[] vary;
        public double lambda = 0.001;
        public double termEpsilon = 0.01;
        public int maxIterations = 100;

        public TrainingSetup(double[][] x, double[][] y, double[] a, double[] s, boolean[] vary) {
            if (s == null) {
                s = new double[x.length];
                Arrays.fill(s, 1);
            }

            if (vary == null) {
                vary = new boolean[a.length];
                Arrays.fill(vary, true);
            }

            this.x = x;
            this.y = y;
            this.a = a;
            this.s = s;
            this.vary = vary;
        }

        public TrainingSetup(double[][] x, double[][] y, double[] a, double[] s) {
            this(x, y, a, s, null);
        }

        public TrainingSetup(double[][] x, double[][] y, double[] a) {
            this(x, y, a, null, null);
        }
    }

    /**
     * x is a single point, but domain may be mulidimensional
     */
    public abstract double f(double[] x, double[] a);

    /**
     * return the kth component of the gradient df(x,a)/da_k
     */
    public abstract double grad(double[] x, double[] a, int k);

    public LevenbergMarquardtEstimator() {
        // nothing
    }

    /**
     * calculate the current sum-squared-error
     * (Chi-squared is the distribution of squared Gaussian errors,
     * thus the name)
     */
    private double chiSquared(double[][] x, double[] a, double[][] y, double[] s) {
        int npts = y.length;
        double sum = 0.;

        for (int i = 0; i < npts; i++) {
            double d = y[i][0] - f(x[i], a);
            d = d / s[i];
            sum = sum + (d * d);
        }

        return sum;
    } //chiSquared

    public Solution solve(TrainingSetup data) throws Exception {
        return solve(data, 0);
    }

    public Solution solve(TrainingSetup data, int verbose) throws Exception {
        return solve(data.x, data.a, data.y, data.s, data.vary, data.lambda,
                data.termEpsilon, data.maxIterations, verbose);
    }

    /**
     * Minimize E = sum {(y[k] - f(x[k],a)) / s[k]}^2
     * The individual errors are optionally scaled by s[k].
     * Note that LMfunc implements the value and gradient of f(x,a),
     * NOT the value and gradient of E with respect to a!
     *
     * @param x array of domain points, each may be multidimensional
     * @param y corresponding array of values
     * @param a the parameters/state of the model
     * @param vary false to indicate the corresponding a[k] is to be held fixed
     * @param s2 sigma^2 for point i
     * @param lambda blend between steepest descent (lambda high) and
     *	jump to bottom of quadratic (lambda zero).
     * 	Start with 0.001.
     * @param termepsilon termination accuracy (0.01)
     * @param maxiter	stop and return after this many iterations if not done
     * @param verbose	set to zero (no prints), 1, 2
     *
     * @return the new lambda for future iterations.
     *  Can use this and maxiter to interleave the LM descent with some other
     *  task, setting maxiter to something small.
     */
    public Solution solve(
            double[][] x,
            double[] a,
            double[][] y,
            double[] s,
            boolean[] vary,
            double lambda,
            double termepsilon,
            int maxiter,
            int verbose)
            throws Exception {
        int npts = y.length;
        int nparm = a.length;
        assert s.length == npts;
        assert x.length == npts;
        if (verbose > 0) {
            System.out.print("solve x[" + x.length + "][" + x[0].length + "]");
            System.out.print(" a[" + a.length + "]");
            System.out.println(" y[" + y.length + "]");
        }

        double e0 = chiSquared(x, a, y, s);
        //double lambda = 0.001;
        boolean done = false;

        // g = gradient, H = hessian, d = step to minimum
        // H d = -g, solve for d
        double[][] H = new double[nparm][nparm];
        double[] g = new double[nparm];
        //double[] d = new double[nparm];

        double[] oos2 = new double[s.length];
        for (int i = 0; i < npts; i++) {
            oos2[i] = 1. / (s[i] * s[i]);
        }

        int iter = 0;
        int term = 0;	// termination count test

        do {
            ++iter;

            // hessian approximation
            for (int r = 0; r < nparm; r++) {
                for (int c = 0; c < nparm; c++) {
                    for (int i = 0; i < npts; i++) {
                        if (i == 0) {
                            H[r][c] = 0.;
                        }
                        double[] xi = x[i];
                        H[r][c] += (oos2[i] * grad(xi, a, r) * grad(xi, a, c));
                    }  //npts
                } //c
            } //r

            // boost diagonal towards gradient descent
            for (int r = 0; r < nparm; r++) {
                H[r][r] *= (1. + lambda);
            }

            // gradient
            for (int r = 0; r < nparm; r++) {
                for (int i = 0; i < npts; i++) {
                    if (i == 0) {
                        g[r] = 0.;
                    }
                    double[] xi = x[i];
                    g[r] += (oos2[i] * (y[i][0] - f(xi, a)) * grad(xi, a, r));
                }
            } //npts

            // scale (for consistency with NR, not necessary)
            if (false) {
                for (int r = 0; r < nparm; r++) {
                    g[r] = -0.5 * g[r];
                    for (int c = 0; c < nparm; c++) {
                        H[r][c] *= 0.5;
                    }
                }
            }

            // solve H d = -g, evaluate error at new location
            //double[] d = DoubleMatrix.solve(H, g);
            double[] d = (new Matrix(H)).lu().solve(new Matrix(g, nparm)).getRowPackedCopy();
            //double[] na = DoubleVector.add(a, d);
            double[] na = (new Matrix(a, nparm)).plus(new Matrix(d, nparm)).getRowPackedCopy();
            double e1 = chiSquared(x, na, y, s);

            if (verbose > 0) {
                System.out.println("\n\niteration " + iter + " lambda = " + lambda);
                System.out.print("a = ");
                (new Matrix(a, nparm)).print(10, 2);
                if (verbose > 1) {
                    System.out.print("H = ");
                    (new Matrix(H)).print(10, 2);
                    System.out.print("g = ");
                    (new Matrix(g, nparm)).print(10, 2);
                    System.out.print("d = ");
                    (new Matrix(d, nparm)).print(10, 2);
                }
                System.out.print("e0 = " + e0 + ": ");
                System.out.print("moved from ");
                (new Matrix(a, nparm)).print(10, 2);
                System.out.print("e1 = " + e1 + ": ");
                if (e1 < e0) {
                    System.out.print("to ");
                    (new Matrix(na, nparm)).print(10, 2);
                } else {
                    System.out.println("move rejected");
                }
            }

            // termination test (slightly different than NR)
            if (Math.abs(e1 - e0) > termepsilon) {
                term = 0;
            } else {
                term++;
                if (term == 4) {
                    if (verbose > 0) {
                        System.out.println("terminating after " + iter + " iterations");
                    }
                    done = true;
                }
            }
            if (iter >= maxiter) {
                done = true;
            }

            // in the C++ version, found that changing this to e1 >= e0
            // was not a good idea.  See comment there.
            //
            if (e1 > e0 || Double.isNaN(e1)) { // new location worse than before
                lambda *= 10.;
            } else {		// new location better, accept new parameters
                lambda *= 0.1;
                e0 = e1;
                // simply assigning a = na will not get results copied back to caller
                for (int i = 0; i < nparm; i++) {
                    if (vary[i]) {
                        a[i] = na[i];
                    }
                }
            }

        } while (!done);

        return new Solution(a, lambda, e0, iter);
    } //solve

//===========================================================================//

    /**
     * solve for phase, amplitude and frequency of a sinusoid
     */
    static class LMSineTest extends LevenbergMarquardtEstimator {
        static final int PHASE = 0;
        static final int AMP = 1;
        static final int FREQ = 2;

        public double[] initial() {
            double[] a = new double[3];
            a[PHASE] = 0.;
            a[AMP] = 1.;
            a[FREQ] = 1.;
            return a;
        } //initial

        public double f(double[] x, double[] a) {
            return a[AMP] * Math.sin(a[FREQ] * x[0] + a[PHASE]);
        } //val

        public double grad(double[] x, double[] a, int a_k) {
            if (a_k == AMP) {
                return Math.sin(a[FREQ] * x[0] + a[PHASE]);
            } else if (a_k == FREQ) {
                return a[AMP] * Math.cos(a[FREQ] * x[0] + a[PHASE]) * x[0];
            } else if (a_k == PHASE) {
                return a[AMP] * Math.cos(a[FREQ] * x[0] + a[PHASE]);
            } else {
                assert false;
                return 0.;
            }
        } //grad

        public TrainingSetup testdata() {
            double[] a = new double[3];
            a[PHASE] = 0.111;
            a[AMP] = 1.222;
            a[FREQ] = 1.333;

            int npts = 10;
            double[][] x = new double[npts][1];
            double[][] y = new double[npts][1];
            double[] s = new double[npts];
            for (int i = 0; i < npts; i++) {
                x[i][0] = (double) i / npts;
                y[i][0] = f(x[i], a);
                s[i] = 1.;
            }

            TrainingSetup data = new TrainingSetup(x, y, initial(), s);
            data.actual_a = a;
            return data;
        } //test
    } //SineTest

//===========================================================================//

    /**
     * quadratic (p-o)'S'S(p-o)
     * solve for o, S
     * S is a single scale factor
     */
    public static class LMQuadEstimator extends LevenbergMarquardtEstimator {

        public double f(double[] x, double[] a) {
            assert a.length == 3;
            assert x.length == 2;

            double ox = a[0];
            double oy = a[1];
            double s = a[2];

            double sdx = s * (x[0] - ox);
            double sdy = s * (x[1] - oy);

            return sdx * sdx + sdy * sdy;
        } //val

        /**
         * z = (p-o)'S'S(p-o)
         * dz/dp = 2S'S(p-o)
         *
         * z = (s*(px-ox))^2 + (s*(py-oy))^2
         * dz/dox = -2(s*(px-ox))*s
         * dz/ds = 2*s*[(px-ox)^2 + (py-oy)^2]

         * z = (s*dx)^2 + (s*dy)^2
         * dz/ds = 2(s*dx)*dx + 2(s*dy)*dy
         */
        public double grad(double[] x, double[] a, int a_k) {
            assert a.length == 3;
            assert x.length == 2;
            assert a_k < 3 : "a_k=" + a_k;

            double ox = a[0];
            double oy = a[1];
            double s = a[2];

            double dx = (x[0] - ox);
            double dy = (x[1] - oy);

            if (a_k == 0) {
                return -2. * s * s * dx;
            } else if (a_k == 1) {
                return -2. * s * s * dy;
            } else {
                return 2. * s * (dx * dx + dy * dy);
            }
        } //grad

        public double[] initial() {
            double[] a = new double[3];
            a[0] = 0.05;
            a[1] = 0.1;
            a[2] = 1.0;
            return a;
        } //initial

        public TrainingSetup testdata() {
            int npts = 25;
            double[][] x = new double[npts][2];
            double[][] y = new double[npts][1];
            double[] s = new double[npts];
            double[] a = new double[3];

            a[0] = 0.;
            a[1] = 0.;
            a[2] = 0.9;

            int i = 0;
            for (int r = -2; r <= 2; r++) {
                for (int c = -2; c <= 2; c++) {
                    x[i][0] = c;
                    x[i][1] = r;
                    y[i][0] = f(x[i], a);
                    //System.out.println("Quad "+c+","+r+" -> "+y[i]);
                    s[i] = 1.;
                    i++;
                }
            }
            //System.out.print("quad x= ");
            //(new Matrix(x)).print(10, 2);

            //System.out.print("quad y= ");
            //(new Matrix(y,npts)).print(10, 2);

            TrainingSetup data = new TrainingSetup(x, y, initial(), s);
            data.actual_a = a;
            return data;
        } //testdata
    } //LMQuadEstimator

//===========================================================================//

    /**
     * Replicate the example in NR, fit a sum of Gaussians to data.
     * y(x) = \sum B_k exp(-((x - E_k) / G_k)^2)
     * minimize chisq = \sum { y[j] - \sum B_k exp(-((x_j - E_k) / G_k)^2) }^2
     *
     * B_k, E_k, G_k are stored in that order
     *
     * Works, results are close to those from the NR example code.
     */
    static class LMGaussTest extends LevenbergMarquardtEstimator {

        static double SPREAD = 0.001; 	// noise variance

        public double f(double[] x, double[] a) {
            assert x.length == 1;
            assert (a.length % 3) == 0;

            int K = a.length / 3;
            int i = 0;

            double y = 0.;
            for (int j = 0; j < K; j++) {
                double arg = (x[0] - a[i + 1]) / a[i + 2];
                double ex = Math.exp(-arg * arg);
                y += (a[i] * ex);
                i += 3;
            }

            return y;
        } //val

        /**
         * <pre>
         * y(x) = \sum B_k exp(-((x - E_k) / G_k)^2)
         * arg  =  (x-E_k)/G_k
         * ex   =  exp(-arg*arg)
         * fac =   B_k * ex * 2 * arg
         *
         * d/dB_k = exp(-((x - E_k) / G_k)^2)
         *
         * d/dE_k = B_k exp(-((x - E_k) / G_k)^2) . -2((x - E_k) / G_k) . -1/G_k
         *        = 2 * B_k * ex * arg / G_k
         *   d/E_k[-((x - E_k) / G_k)^2] = -2((x - E_k) / G_k) d/dE_k[(x-E_k)/G_k]
         *   d/dE_k[(x-E_k)/G_k] = -1/G_k
         *
         * d/G_k = B_k exp(-((x - E_k) / G_k)^2) . -2((x - E_k) / G_k) . -(x-E_k)/G_k^2
         *       = B_k ex -2 arg -arg / G_k
         *       = fac arg / G_k
         *   d/dx[1/x] = d/dx[x^-1] = -x[x^-2]
         */
        public double grad(double[] x, double[] a, int a_k) {
            assert x.length == 1;

            // i - index one of the K Gaussians
            int i = 3 * (a_k / 3);

            double arg = (x[0] - a[i + 1]) / a[i + 2];
            double ex = Math.exp(-arg * arg);
            double fac = a[i] * ex * 2. * arg;

            if (a_k == i) {
                return ex;
            } else if (a_k == (i + 1)) {
                return fac / a[i + 2];
            } else if (a_k == (i + 2)) {
                return fac * arg / a[i + 2];
            } else {
                System.err.println("bad a_k");
                return 1.;
            }

        } //grad

        public double[] initial() {
            double[] a = new double[6];
            a[0] = 4.5;
            a[1] = 2.2;
            a[2] = 2.8;

            a[3] = 2.5;
            a[4] = 4.9;
            a[5] = 2.8;
            return a;
        } //initial

        public TrainingSetup testdata() {
            int npts = 100;
            double[][] x = new double[npts][1];
            double[][] y = new double[npts][0];
            double[] s = new double[npts];
            double[] a = new double[6];

            a[0] = 5.0;	// values returned by initial
            a[1] = 2.0;	// should be fairly close to these
            a[2] = 3.0;
            a[3] = 2.0;
            a[4] = 5.0;
            a[5] = 3.0;

            for (int i = 0; i < npts; i++) {
                x[i][0] = 0.1 * (i + 1);	// NR always counts from 1
                y[i][0] = f(x[i], a);
                s[i] = SPREAD * y[i][0];
                //System.out.println(i + ": x,y= " + x[i][0] + ", " + y[i]);
            }

            TrainingSetup data = new TrainingSetup(x, y, initial(), s);
            data.actual_a = a;
            return data;
        } //testdata
    } //LMGaussTest

//===========================================================================//

    public static void main(String[] cmdline) {
        //LMQuadEstimator f = new LMQuadEstimator();
        LMSineTest f = new LMSineTest();	// works
        //LMGaussTest f = new LMGaussTest();	// works

        TrainingSetup data = f.testdata();

        try {
            Solution s = f.solve(f.testdata());
            System.out.println(s);
            System.out.println("Actual Paramerters = " + ArrayTools.toString(data.actual_a, 4));
        } catch (Exception ex) {
            System.err.println("Exception caught: " + ex.getMessage());
            System.exit(1);
        }

        System.exit(0);
    } //main
} // public class LevenbergMarquardtEstimator

