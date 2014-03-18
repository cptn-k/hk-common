
package net.hkhandan.gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

public class PrallelCordinatesDiagram extends Component {
    /////////////////////////////////////////////////////
    // NESTED CLASS DEFINITIONS //
    public static class Datum {
        public int uid = -1;
        public int[] dimIndexes;
        public String name;
        public Color color;
        public double[] value;
        boolean emphesized = false;
        public boolean has(int index) {
            for(int i:dimIndexes)
                if(i == index)
                    return true;
            return false;
        }
        public double get(int index) {
            for(int i = dimIndexes.length - 1; i >= 0; i--)
                if(dimIndexes[i] == index)
                    return value[i];
            return 0;
        }
    }

    public static class Axis {
        public int uid = -1;
        public String label = "";

        public Axis(String l) {
            label = l;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Axis)
                return ((Axis)o).label.equals(label);
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.label != null ? this.label.hashCode() : 0);
            return hash;
        }
    }

    /////////////////////////////////////////////////////
    // CONSTANT DEFINITIONS //
    private final static Color[] SHADE = new Color[20];
    static {
            SHADE[0]  = new Color(255, 85 , 0  );
            SHADE[1]  = new Color(255, 170, 0  );
            SHADE[2]  = new Color(255, 255, 0  );
            SHADE[3]  = new Color(170, 255, 0  );
            SHADE[4]  = new Color(85 , 255, 0  );
            SHADE[5]  = new Color(0  , 255, 85 );
            SHADE[6]  = new Color(0  , 255, 170);
            SHADE[7]  = new Color(255, 153, 0  );
            SHADE[8]  = new Color(0  , 255, 255);
            SHADE[9]  = new Color(0  , 170, 255);
            SHADE[10] = new Color(0  , 85 , 255);
            SHADE[11] = new Color(85 , 0  , 255);
            SHADE[12] = new Color(170, 0  , 255);
            SHADE[13] = new Color(255, 0  , 255);
            SHADE[14] = new Color(255, 0  , 170);
            SHADE[15] = new Color(255, 0  , 85 );
    }

    private final static Font PLOT_NUMBER_FONT = new Font("Currier New", Font.PLAIN, 10);
    private final static int  MIN_CELL_SIZE = 15;

    private int idcounter    = 0;
    private int shadecounter = 0;

    private int     marginTop    = 14;
    private int     marginBottom = 20;
    private int     marginLeft   = 14;
    private int     marginRight  = 14;

    private double  scaleY     = 1;
    private double  translateY = 0;

    private double virtualBottom = 0;
    private double virtualTop    = 1;

    private int stepX = 1;

    private double stepY = 0.1;
    private String formatY = "%2.2f";


    private boolean fixedAxes = false;
    private boolean recalculateAxes = true;

    private ArrayList<Datum> data = new ArrayList<Datum>();
    private ArrayList<Axis>  axes = new ArrayList<Axis>();
                 //<UID    , Index  >//
    private HashMap<Integer, Integer> indexmap = new HashMap<Integer, Integer>();

    private int getAxisByLabel(String name) {
        for(int i = axes.size() - 1; i >= 0; i--)
            if(axes.get(i).label.equals(name))
                return i;
        return -1;
    }

    public Color getColor(int id) {
        return data.get(indexmap.get(id)).color;
    }

    public void unemphesize() {
        for(Datum d:data)
            d.emphesized = false;
    }

    public void emphesize(int id) {
        data.get(indexmap.get(id)).emphesized = true;
    }

    @Override
    public void doLayout() {
        setupAxes();
    }

    public void setupAxes() {
        int width = getWidth() - marginLeft - marginRight - 1;
        stepX = width/(axes.size()-1);
        
        if(virtualBottom == virtualTop)
            scaleY = 0.1;
        else
            scaleY = (getHeight() - marginTop - marginBottom - 1) / (virtualTop - virtualBottom);
        translateY = getHeight() - marginBottom - 1 + (int)(scaleY * virtualBottom);

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

    private int y_v2d(double y) {
        return (int)(-(y * scaleY) + translateY);
    }

    private int x_v2d(int x) {
        return marginLeft + x * stepX;
    }

    public int putPlot(double[] x, String[] labels, String name, Color c, int id) {
        if(recalculateAxes) {
            if(!fixedAxes) {
                virtualBottom = Double.MAX_VALUE;
                virtualTop = Double.MIN_VALUE;
            }
            recalculateAxes = false;
        }
        
        Datum datum = null;

        if(indexmap.containsKey(id))
            datum = data.get(indexmap.get(id));
        else {
            datum = new Datum();

            if(c == null) {
                datum.color = SHADE[shadecounter];
                shadecounter = (shadecounter + 1)%SHADE.length;
            }

            datum.uid = idcounter;
            idcounter++;

            datum.dimIndexes = new int[labels.length];

            for(int i = 0; i < x.length; i++) {
                String l = (labels == null)?"x" + Integer.toString(i + 1):labels[i];
                int index = getAxisByLabel(l);
                if(index == -1) {
                    axes.add(new Axis(l));
                    index = axes.size() - 1;
                }
                datum.dimIndexes[i] = index;
            }

            data.add(datum);
            indexmap.put(datum.uid, data.size() - 1);
        }

        if(name != null)
            datum.name = name;

        if(c != null)
            datum.color = c;

        datum.value = x;

        if(!fixedAxes) {
            for(double xi:x) {
                virtualTop = Math.max(virtualTop, xi);
                virtualBottom = Math.min(virtualBottom, xi);
            }
            setupAxes();
        }

        return datum.uid;
    }

    public void plot() {
        repaint();
        recalculateAxes = true;
    }

    @Override
    public void paint(Graphics g) {
        setupAxes();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

        paintAxes(g);
        paintData(g);
    }

    private void paintAxes(Graphics g) {
        int w = getWidth();
        int width = w - marginLeft - marginRight - 1;
        int height = getHeight() - marginTop - marginBottom - 1;

        g.setFont(PLOT_NUMBER_FONT);

        FontMetrics fm = g.getFontMetrics();
        int textY = marginTop + height + fm.getHeight();

        g.setColor(Color.lightGray);
        g.drawRect(marginLeft, marginTop, width-1, height);
        
        int bottom = marginTop + height;
        g.setColor(Color.DARK_GRAY);
        for(int i = axes.size() - 1; i >= 0; i--) {
            int x = x_v2d(i);
            g.drawLine(x, marginTop, x, bottom);

            String text = axes.get(i).label;
            int textW = fm.stringWidth(text);
            int textX = x - textW/2;
            if(textX < 0)
                textX = 4;
            if(textX + textW > w)
                textX = w - textW - 4;
            g.drawString(text , textX, textY);
        }

        double begin = ((int)(virtualBottom / stepY))*stepY;
        if(begin < virtualBottom)
            begin += stepY;
        int right = marginLeft + width;
        for(double y = begin; y <= virtualTop; y += stepY) {
            int yOnDevice = y_v2d(y);

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(marginLeft, yOnDevice, right, yOnDevice);
            try {
                String str = String.format(formatY, y);
                g.setColor(Color.GRAY);
                g.drawString(str, marginLeft + 2, yOnDevice - 2);
            } catch(Exception e) {
                System.out.println("failed y: " + y + " - " + formatY + " - " + e.toString());
                break;
            }
        }
        
    }

    private void paintData(Graphics g) {
        int nAxes = axes.size() - 1;
        for(int i = data.size() - 1; i >= 0; i--) {
            Datum d = data.get(i);
            g.setColor(d.color);
            int yOnDevice1 = 0;
            int yOnDevice2 = 0;
            boolean drawLine = false;
            int    lastXOnDevice = 0;
            for(int j = nAxes; j >= 0; j--) {
                if(d.has(j)) {
                    yOnDevice2 = y_v2d(d.get(j));
                    int xOnDevice = x_v2d(j);
                    g.fillOval(xOnDevice - 4, yOnDevice2 - 4, 8, 8);
                    if(drawLine)
                        g.drawLine(lastXOnDevice, yOnDevice1, xOnDevice, yOnDevice2);
                    if(drawLine && d.emphesized) {
                        g.drawLine(lastXOnDevice + 1, yOnDevice1, xOnDevice + 1, yOnDevice2);
                        g.drawLine(lastXOnDevice, yOnDevice1 + 1, xOnDevice, yOnDevice2 + 1);
                    }
                    yOnDevice1 = yOnDevice2;
                    lastXOnDevice = xOnDevice;
                    drawLine = true;
                }
            }
        }
    }

    public static void main(String[] args) {
        Frame f = new Frame("ParallerCordinates Test");
        f.setSize(600, 400);
        f.setLayout(new BorderLayout());

        PrallelCordinatesDiagram d = new PrallelCordinatesDiagram();
        f.add(d, BorderLayout.CENTER);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        f.setVisible(true);

        d.putPlot(new double[]{1, 2, 3, 4, 5}, new String[]{"x", "y", "w", "hello", "goodbye"}, "Data1", null, -1);
        d.putPlot(new double[]{3, 2, 1, 5}, new String[]{"y", "hello", "goodbye", "x"}, "Data1", null, -1);
      
    }
    

}
