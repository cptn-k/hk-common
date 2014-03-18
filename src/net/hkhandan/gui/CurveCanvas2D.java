package net.hkhandan.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class CurveCanvas2D extends JComponent {
    /////////////////////////////////////////////////////
    // NESTED CLASS DEFINITIONS //
    private static class Curve {
        public int uid = -1;
        public String name = "";
        public Color color;
        public double[] x = new double[400];
        public double[] y = new double[400];
        public int count;
    }

    /////////////////////////////////////////////////////
    // CONSTANT DEFINITIONS //
    private final static Color[] SHADE = new Color[20];
    static {
            SHADE[0]  = Color.BLUE;
            SHADE[1]  = Color.GREEN;
            SHADE[2]  = Color.RED;
            SHADE[3]  = Color.BLACK;
            SHADE[4]  = Color.CYAN;
            SHADE[5]  = Color.MAGENTA;
            SHADE[6]  = Color.LIGHT_GRAY;
            SHADE[7]  = Color.GRAY;
            SHADE[8]  = Color.YELLOW;
            SHADE[9]  = new Color(0  , 170, 255);
            SHADE[10] = new Color(0  , 85 , 255);
            SHADE[11] = new Color(85 , 0  , 255);
            SHADE[12] = new Color(170, 0  , 255);
            SHADE[13] = new Color(170, 0  , 85);
            SHADE[14] = new Color(255, 0  , 170);
            SHADE[15] = new Color(255, 0  , 85 );
    }

    private final static Font PLOT_NUMBER_FONT = new Font("Currier New", Font.PLAIN, 10);
    private final static int MIN_CELL_SIZE = 15;


    private int idcounter    = 0;
    private int shadecounter = 0;

    private int     marginTop    = 14;
    private int     marginBottom = 14;
    private int     marginLeft   = 14;
    private int     marginRight  = 14;

    private double  translateX = 0;
    private double  translateY = 0;

    private double  scaleX = 1;
    private double  scaleY = 1;

    private double virtualBottomLeftX = 0;
    private double virtualBottomLeftY = 0;
    private double virtualTopRightX = 1;
    private double virtualTopRightY = 1;

    private double stepX = 0.1;
    private double stepY = 0.1;
    private String formatX = "%2.2f";
    private String formatY = "%2.2f";
    private int xLabelWidth = -1;

    
    private boolean fixX = false;
    private boolean fixY = false;
    private boolean recalculateAxes = true;

    private int emphesized = -1;

    private ArrayList<Curve> curvelist = new ArrayList<Curve>();

                 //<UID    , Index  >//
    private HashMap<Integer, Integer> indexmap = new HashMap<Integer, Integer>();

    public CurveCanvas2D() {
        setBackground(Color.WHITE);
    }

    @Override
    public void doLayout() {
        setupXAxis();
        setupYAxis();
    }
    
    public void clear() {
        curvelist.clear();
        indexmap.clear();
        
        translateX = 0;
        translateY = 0;

        scaleX = 1;
        scaleY = 1;

        virtualBottomLeftX = 0;
        virtualBottomLeftY = 0;
        virtualTopRightX = 1;
        virtualTopRightY = 1;

        stepX = 0.1;
        stepY = 0.1;
        formatX = "%2.2f";
        formatY = "%2.2f";
        xLabelWidth = -1;


        fixX = false;
        fixY = false;
        recalculateAxes = true;

        emphesized = -1;        
        
        setupXAxis();
        setupYAxis();
    }

    private void setupXAxis() {
        if(virtualBottomLeftX == virtualTopRightX)
            scaleX = 0.1;
        else
            scaleX = (getWidth() - marginLeft - marginRight) / (virtualTopRightX - virtualBottomLeftX);

        if(Double.isInfinite(scaleX))
            scaleX = 0.1;

        translateX = marginLeft - (int)(scaleX * virtualBottomLeftX);

        double virtualMinCellSize = MIN_CELL_SIZE/scaleX;
        double n = Math.floor(Math.log10(virtualMinCellSize)) + 1;

        if(n > 100)
            n = 2;
        
        stepX = Math.pow(10, n);
        if(stepX/2 > virtualMinCellSize) {
            stepX /= 2;
            n--;
        }

        if(stepX/2 > virtualMinCellSize) { 
            stepX /= 2;
            n--;
        }

        if(n >= 0)
            formatX = "%.0f";
        else
            formatX = "%." + -(int)n + "f";

        xLabelWidth = -1;
    }

    private void setupYAxis() {
        if(virtualBottomLeftY == virtualTopRightY)
            scaleY = 0.1;
        else
            scaleY = (getHeight() - marginTop - marginBottom - 1) / (virtualTopRightY - virtualBottomLeftY);

        if(Double.isInfinite(scaleY))
            scaleY = 0.1;

        translateY = getHeight() - marginBottom - 1 + (int)(scaleY * virtualBottomLeftY);

        double virtualMinCellSize = MIN_CELL_SIZE/scaleY;
        double n = Math.floor(Math.log10(virtualMinCellSize)) + 1;

        if(Double.isInfinite(n))
            n = 2;

        stepY = Math.pow(10, n);
        if(stepY/2 > virtualMinCellSize) {
            stepY /= 2;
            n--;
        }

        if(stepY/2 > virtualMinCellSize) {
            stepY /= 2;
            n--;
        }

        if(n >= 0)
            formatY = "%.0f";
        else
            formatY = "%." + -(int)n + "f";
    }    

    private int x_v2d(double x) {
        return (int)(x * scaleX + translateX);
    }

    private int y_v2d(double y) {
        return (int)(-(y * scaleY) + translateY);
    }

    private double x_d2v(int x) {
        return (x - translateX)/scaleX;
    }

    private double y_d2v(int y) {
        return -(y - translateY)/scaleY;
    }

    private Curve getCurve(int uid) {
        return curvelist.get(indexmap.get(uid));
    }

    public void emphesize(int uid) {
        emphesized = uid;
        repaint();
    }

    public void setXAxis(double begin, double end) {
        virtualBottomLeftX = begin;
        virtualTopRightX = end;
        setupXAxis();
        fixX = true;
    }

    public void setYAxis(double begin, double end) {
        virtualBottomLeftY = begin;
        virtualTopRightY = end;
        setupYAxis();
        fixY = true;
    }

    public void unfixXAsis() {
        fixX = false;
    }

    public void unfixYAsis() {
        fixY = false;
    }

    public int putPlot(double[] x, double[] y, String name, Color c, int id, boolean updateAxis) {
        return putPlot(x, y, Math.min(x.length, y.length), name, c, id, updateAxis);
    }
    
    public int putPlot(double[] x, double[] y, int count, String name, Color c, int id, boolean updateAxis) {
        if(recalculateAxes) {
            if(!fixX) {
                virtualBottomLeftX = Double.MAX_VALUE;
                virtualTopRightX = Double.MIN_VALUE;
            }
            if(!fixY) {
                virtualBottomLeftY = Double.MAX_VALUE;
                virtualTopRightY = Double.MIN_VALUE;
            }
            recalculateAxes = false;
        }

        Curve curve = null;

        if(indexmap.containsKey(id))
            curve = curvelist.get(indexmap.get(id));
        else {
            curve = new Curve();
            
            if(c == null) {
                curve.color = SHADE[shadecounter];
                shadecounter = (shadecounter + 1)%SHADE.length;
            }
            curve.uid = (id < 0)?idcounter:id;
            while(idcounter <= curve.uid)
                idcounter++;

            curvelist.add(curve);
            indexmap.put(curve.uid, curvelist.size() - 1);
        }

        if(name != null)
            curve.name = name;
        
        if(c != null)
            curve.color = c;

        if(curve.x.length < count) {
            curve.x = new double[(int)(count * 1.5)];
            curve.y = new double[(int)(count * 1.5)];
        }
        
        for(int i = count - 1; i >= 0; i--) {
            curve.x[i] = x[i];
            curve.y[i] = y[i];
        }
        
        curve.count = count;

        if(updateAxis) {
            if(!fixX) {
                for(int i = count - 1; i >= 0; i--) {
                    double oneX = x[i];
                    virtualBottomLeftX = Math.min(virtualBottomLeftX, oneX);
                    virtualTopRightX   = Math.max(virtualTopRightX  , oneX);
                }
                setupXAxis();
            }

            if(!fixY) {
                for(int i = count - 1; i >= 0; i--) {
                    double oneY = y[i];
                    virtualBottomLeftY = Math.min(virtualBottomLeftY, oneY);
                    virtualTopRightY   = Math.max(virtualTopRightY  , oneY);
                }
                setupYAxis();
            }
        }

        return curve.uid;
    }

    public void plot() {
        repaint();
        recalculateAxes = true;
    }

    //     DeviceOrigin       <--Width-->
    //      O---------------------------------------------+
    //   ∧  |                                             |
    //   |  |                                             |
    //   h  |   ∧                                         |
    //   e  |   |                                         |
    //   i  |   |                                         |
    //   g  |   |                                         |
    //   h  |   |                                         |
    //   t  |   O----------------------------------->     |
    //   |  |                                             |
    //   ∨  |                                             |
    //      O---------------------------------------------+
    //     Actual Origin
    //
    // virtual.origin.x = margin-top;
    // virtual.origin.y = devide.height - margin-bottom;
    // virtual-to-device-scale.x = (device.width - margin-left - margin-right)/virtual.width;
    // virtual-to-device-scale.y = (device.height - margin-top - margin-bottom)/virtual.height;
    // p-device.x = p-virtual.x * virtual-to-device-scale.x + virtual.origin.x;
    // p-device.y = -(p-virtual.y * virtual-to-device-scale.y) + virtual.origin.y;

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

        paintAxes(g);

        paintCurves(g);
    }

    private void paintAxes(Graphics g) {
        try {
        if(xLabelWidth == -1) {
            FontMetrics metrics = g.getFontMetrics(PLOT_NUMBER_FONT);
            xLabelWidth = metrics.stringWidth(String.format(formatX, virtualTopRightX));
            xLabelWidth += xLabelWidth/20;
        }
        }catch(Exception e) {
            System.out.println(formatX);
        }

        int width  = getWidth()  - marginLeft - marginRight - 1;
        int height = getHeight() - marginTop  - marginBottom - 1;

        g.setFont(PLOT_NUMBER_FONT);
        g.setColor(Color.GRAY);
        g.drawRect(marginLeft - 1, marginTop - 1, width, height);
        
        // X Grid -------------
        double begin = ((int)(virtualBottomLeftX / stepX))*stepX;
        int bottom = marginTop + height;
        double lastLabelX = Double.NEGATIVE_INFINITY;
        for(double x = begin; x < virtualTopRightX; x += stepX) {
            int xOnDevice = x_v2d(x);

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(xOnDevice, marginTop, xOnDevice, bottom);

            if(lastLabelX + xLabelWidth < xOnDevice) {
                String str = String.format(formatX, x);
                g.setColor(Color.GRAY);
                g.drawString(str, xOnDevice + 2, bottom + 12);
                lastLabelX = xOnDevice;
            }
        }

        // Y Grid -------------
        begin = ((int)(virtualBottomLeftY / stepY))*stepY;
        if(begin < virtualBottomLeftY)
            begin += stepY;
        int right = marginLeft + width;
        for(double y = begin; y < virtualTopRightY; y += stepY) {
            int yOnDevice = y_v2d(y);
            
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(marginLeft, yOnDevice, right, yOnDevice);
            String str = String.format(formatY, y);
            g.setColor(Color.GRAY);
            g.drawString(str, marginLeft + 2, yOnDevice - 2);
        }
    }

    private void paintCurves(Graphics g) {
        for(Curve c:curvelist) {
            g.setColor(c.color);
            boolean isSelected = (c.uid == emphesized);
            for(int i = c.count - 1; i > 0; i--) {
                if(Double.isNaN(c.y[i]) || Double.isNaN(c.y[i-1]))
                    continue;
                g.drawLine(x_v2d(c.x[i]), y_v2d(c.y[i]), x_v2d(c.x[i - 1]), y_v2d(c.y[i - 1]));
                if(isSelected) {
                    g.drawLine(x_v2d(c.x[i]), y_v2d(c.y[i]) - 1, x_v2d(c.x[i - 1]), y_v2d(c.y[i - 1]) - 1);
                    g.drawLine(x_v2d(c.x[i]), y_v2d(c.y[i]) + 1, x_v2d(c.x[i - 1]), y_v2d(c.y[i - 1]) + 1);
                    g.drawLine(x_v2d(c.x[i]) - 1, y_v2d(c.y[i]), x_v2d(c.x[i - 1]) - 1, y_v2d(c.y[i - 1]));
                    g.drawLine(x_v2d(c.x[i]) + 1, y_v2d(c.y[i]), x_v2d(c.x[i - 1]) + 1, y_v2d(c.y[i - 1]));
                }
            }
        }
    }

    public Color getColor(int index) {
        return curvelist.get(index).color;
    }
}
