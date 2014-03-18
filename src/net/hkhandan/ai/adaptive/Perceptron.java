package net.hkhandan.ai.adaptive;

import java.awt.Color;
import java.util.Random;
import net.hkhandan.math.HKMath;
import javax.swing.*;
import net.hkhandan.gui.CurveCanvas2D;



//  ┌──┐  ┌────────┐          ┌──┐  ┌────────┐          ┌──┐
//  │  │  │        │          │  │  │        │          │  │
//  │  │  │        │          │  <---        <-----------  │<--.
//  │  │  │        │      eta │  │  │        │      eta │  │   │
//  │  │  │        │       │  │  │  │        │       │  │  │   │
//  │  │  │        │      ┌∨,┐|  │  │        │      ┌∨,┐│  │   │
//  │  │  │      <--------│f1<-  │  │      <--------│f2<-  │   │
//  │  │  │        │      └∧╌┘│  │  │        │      └∧╌┘│  │   │
//  └──┘  │        │       │  └──┘  │        │       │  └──┘   │
// delta1 │        │       │ delta2 │        │       │ delta3  │
//  (N1)  │        │       │ (N2+1) │        │       │ (N3+1)  │
//        │        │       │        │        │       │         │
//    ┌────────────────────┘    ┌────────────────────┘         │
//  ┌─┴┐  │        │  ┌──┐    ┌─┴┐  │        │  ┌──┐    ┌──┐ ┌─┴─┐ ┌──┐
//  │  │  │        │  │  │    │  │  │        │  │  │    │┌┐│ │dif│ │  │
//  │  │  │        │  │  │    │  │  │        │  │  │    │||│ └─┬─┘ │  │
//  │  │  │        │  │  │┌╌╌┐│  │  │        │  │  │┌╌╌┐│┌┘│   │   │  │
//-->  --->        --->  ->f1->  --->        --->  ->f2->││-->─┴─<--  │
//  │  │  │        │  │  │└╌╌┘│  │  │        │  │  │└╌╌┘│┌┘│       │  │
//  │  │  │        │  │  │    │  │  │        │  │  │    │||│       │  │
//  │  │  │        │  │  │    │  │  │        │  │  │    │└┘│       │  │
//  └──┘  └────────┘  └──┘    └──┘  └────────┘  └──┘    └──┘       └──┘
// l1 = X    w12       s2      l2       w23      s3    l3 = Ŷ        Y
// (N1+1)  (N1+1xN2)  (N2)   (N2+1)  (N2+1xN3)  (N3)    (N3)
// └─────────┘ └────────────────────────┘ └─────────────────┘
//     L1                 L2                     L3

public abstract class Perceptron {
    public static class TrainingSetup {
        public final static double DEFAULT_ETA = 0.01;
        public final static double DEFAULT_TERM_ERROR = 0.02;
        public final static int    DEFAULT_MAX_ITERATIONS = 100;

        public final double[][] x;
        public final double[][] y;
        public final double     eta;
        public final double     terminationError;
        public final int        maxIterations;


        public TrainingSetup(double[][] x, double[][] y, double eta, double termError, int maxIterations) {
            this.x = x;
            this.y = y;
            this.terminationError = termError;
            this.maxIterations = maxIterations;
            this.eta = eta;
        }
        public TrainingSetup(double[][] x, double[][] y, double eta, double termError) {
            this(x, y, eta, termError, DEFAULT_MAX_ITERATIONS);
        }
        public TrainingSetup(double[][] x, double[][] y, double eta) {
            this(x, y, eta, DEFAULT_TERM_ERROR, DEFAULT_MAX_ITERATIONS);
        }
        public TrainingSetup(double[][] x, double[][] y) {
            this(x, y, DEFAULT_ETA, DEFAULT_TERM_ERROR, DEFAULT_MAX_ITERATIONS);
        }
    }

    public static class Result {
        public double[][] w12;
        public double[][] w23;
        public double error;
        public int    iterations;
        public Result(double[][] p1, double[][] p2, double p3, int p4) {
            w12        = p1;
            w23        = p2;
            error      = p3;
            iterations = p4;
        }
        
        @Override
        public String toString() {
            return "[error = " + error + ", iterations = " + iterations + "]";
        }
    }
    
    public static enum AF {
        LINEAR,
        SIGMOID,
        TANH
    }

     double[] l1;

    private double[][] w12;
    private double[][] g12;
    private double[]   s2;
    private double[]   l2;

    private double[][] w23;
    private double[][] g23;
    private double[]   s3;
    private double[]   l3;
    
    private double[] delta3;

    private int N1;
    private int N2;
    private int N3;

    private AF f2 = AF.TANH;
    private AF f3 = AF.TANH;

    private boolean interrupted = false;
    private boolean isworking = false;

    private Random rnd = new Random();

    public abstract void iterationFeedback(Result r);

    public Perceptron(int N1, int N2, int N3) {
        this.N1 = N1;
        this.N2 = N2;
        this.N3 = N3;
        
        l1 = new double[N1 + 1];
        l1[N1] = 1;
        w12 = new double[N1 + 1][N2];
        g12 = new double[N1 + 1][N2];

        s2 = new double[N2];
        l2 = new double[N2 + 1];
        l2[N2] = 1;
        w23 = new double[N2 + 1][N3];
        g23 = new double[N2 + 1][N3];

        s3 = new double[N3];
        l3 = new double[N3];

        delta3 = new double[N3 + 1];
        
        reset();
    }

    public void reset() {
        rnd.setSeed(System.currentTimeMillis());
        for(int n1 = N1; n1 >= 0; n1--)
            for(int n2 = N2 - 1; n2 >= 0; n2--)
                w12[n1][n2] = (rnd.nextDouble() * 2 - 1)/20;
        for(int n2 = N2; n2 >= 0; n2--)
            for(int n3 = N3 - 1; n3 >= 0; n3--)
                w23[n2][n3] = (rnd.nextDouble() * 2 - 1)/20;
    }

    public static double f(AF fn, double x) {
        switch(fn) {
            case LINEAR: return x;
            case SIGMOID: return 2/(1 + Math.exp(-x)) - 1;
            case TANH: return Math.tanh(x);
        }
        return 0;
    }

    public static double df(AF fn, double x) {
        switch(fn) {
            case LINEAR : return 1;
            case SIGMOID: return Math.exp(x)/HKMath.sqr(Math.exp(x) + 1);
            case TANH: return HKMath.sech2(x);
        }
        return 0;
    }
    
    public double[] evaluate(double ... x) {
        for(int n1 = 0; n1 < N1; n1++)
            l1[n1] = x[n1];
        l1[N1] = 1;
        
        for(int n2 = 0; n2 < N2; n2++) {
            s2[n2] = 0;
            for(int n1 = 0; n1 <= N1; n1++) {
                s2[n2] += w12[n1][n2] * l1[n1];
            }
            l2[n2] = f(f2, s2[n2]);
        }
        l2[N2] = 1;

        for(int n3 = 0; n3 < N3; n3++) {
            s3[n3] = 0;
            for(int n2 = 0; n2 <= N2; n2++) {
                s3[n3] += w23[n2][n3] * l2[n2];
            }
            l3[n3] = f(f3, s3[n3]);
        }

        return l3;
    }

    public Result train(TrainingSetup setup) {
        return train(setup.x, setup.y, setup.eta, setup.terminationError, setup.maxIterations);
    }

    public Result train(double[][] x, double[][] y, double eta, double termError, int maxIterations) {
        if(isworking) {
            interrupted = true;
            while(isworking)
                try {
                wait(5);
            } catch (InterruptedException ex) {
                System.err.print("Perceptron wait interrupted.");
            }
        }
        double error = 0;
        int i = 0;
        Result r = new Result(w12, w23, 0, 0);
        isworking = true;
        for(i = 0; i < maxIterations; i++) {
            if(interrupted)
                break;

            error = train(x, y, eta);

            r.error = error;
            r.iterations = i;
            iterationFeedback(r);
            
            if(error < termError)
                break;
        }
        isworking = false;
        interrupted = false;
        return new Result(w12, w23, error, i);
    }
    
    private double train(double[][] x, double[][] y, double eta) {
        int count = x.length;
        double delta = 0;
        double error = 0;
        double totalError = 0;
        
        for(int n1 = 0; n1 <= N1; n1++)
            for(int n2 = 0; n2 < N2; n2++)
                g12[n1][n2] = 0;

        for(int n2 = 0; n2 <= N2; n2++)
            for(int n3 = 0; n3 < N3; n3++)
                g23[n2][n3] = 0;

        for(int i = 0; i < count; i++) {
            evaluate(x[i]);
            for(int n3 = 0; n3 < N3; n3++) {
                error = y[i][n3] -  l3[n3];
                totalError += HKMath.sqr(error);
                delta3[n3] = df(f3, s3[n3]) * error;
                for(int n2 = 0; n2 <= N2; n2++) {
                    g23[n2][n3] += l2[n2] * delta3[n3];
                }
            }

            for(int n2 = 0; n2 < N2; n2++) {
                error = 0;
                for(int n3 = 0; n3 < N3; n3++)
                    error += w23[n2][n3] * delta3[n3];

                delta = df(f2, s2[n2]) * error;
                for(int n1 = 0; n1 <= N1; n1++) {
                    g12[n1][n2] += l1[n1] * delta;
                }
            }
        }

        for(int n1 = 0; n1 <= N1; n1++)
            for(int n2 = 0; n2 < N2; n2++)
                w12[n1][n2] += eta * g12[n1][n2]/count;

        for(int n2 = 0; n2 <= N2; n2++)
            for(int n3 = 0; n3 < N3; n3++)
                w23[n2][n3] += eta * g23[n2][n3]/count;

        return Math.sqrt(totalError / (2 * count * N3));
    }

    public void interrupt() {
        if(isworking)
            interrupted = true;
    }

    public double error(double[] x, double[] y) {
        double[] yhat = evaluate(x);
        double sum = 0;
        for(int i = yhat.length - 1; i >= 0; i--)
            sum += HKMath.sqr(yhat[i] - y[i]);
        return Math.sqrt(sum);
    }

    public static double testFunction(double x) {
        return (Math.sin(6 * x/3) + Math.cos(3 * x/5))/6 + 0.5;
    }

    public static void main(String[] args) {
        double[][] x = new double[40][1];
        double[][] y = new double[40][1];
        final double[] xTest = new double[40];
        double[] yTest = new double[40];
        for(int i = 0; i < 40; i++) {
            x[i][0] = (double)i/4 - 5;
            y[i][0] = testFunction(x[i][0]);
            xTest[i] = x[i][0];
            yTest[i] = y[i][0];
        }


        JFrame f = new JFrame();
        final CurveCanvas2D canvas = new CurveCanvas2D();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new java.awt.BorderLayout());
        f.setSize(600, 400);
        f.add(canvas, java.awt.BorderLayout.CENTER);
        canvas.putPlot(xTest, yTest, "actual", Color.blue, -1, true);

        Perceptron p = new Perceptron(1, 10, 1) {

            @Override
            public void iterationFeedback(Result r) {
                if(r.iterations % 500 != 0)
                    return;

//                System.out.println("l1 = ");
//                System.out.println(net.hkhandan.util.ArrayTools.toString(l1));
//
//                System.out.println("w12 = ");
//                (new Jama.Matrix(r.w12)).print(4, 4);
//
//                System.out.println("s2 = ");
//                System.out.println(net.hkhandan.util.ArrayTools.toString(s2));
//
//                System.out.println("l2 = ");
//                System.out.println(net.hkhandan.util.ArrayTools.toString(l2));
//
//
//                System.out.println("w23 = ");
//                (new Jama.Matrix(r.w23)).print(4, 4);
//
//                System.out.println("s3 = ");
//                System.out.println(net.hkhandan.util.ArrayTools.toString(s3));
//
//                System.out.println("l3 = ");
//                System.out.println(net.hkhandan.util.ArrayTools.toString(l3));

                //                System.out.println("Error = " + r.error);


                System.out.println("epoch= " + r.iterations + ", error = " + r.error);
                
                double[] yHat  = new double[xTest.length];
                for(int i = 0; i < xTest.length; i++) {
                    yHat[i] = evaluate(new double[]{xTest[i]})[0];
                }
                canvas.putPlot(xTest, yHat, "estimate", Color.red, -1, true);
            }

        };

        p.train(x, y, 0.3, 0.01, 100000);

        double[] yHat = new double[xTest.length];
        for (int i = 0; i < xTest.length; i++) {
            yHat[i] = p.evaluate(new double[]{xTest[i]})[0];
        }
        int id = canvas.putPlot(xTest, yHat, "estimate", Color.MAGENTA, -1, true);
        canvas.emphesize(id);

        f.setVisible(true);
        

    }
}
