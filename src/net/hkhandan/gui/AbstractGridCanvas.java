package net.hkhandan.gui;

import java.awt.*;
import java.util.ArrayList;
import net.hkhandan.math.IntPair;

abstract class AbstractGridCanvas extends Component {
    public static enum StrokeStyle {
        NORMAL,
        BOLD
    };
    
    private static class Group {
        public int top;
        public int left;
        public int bottom;
        public int right;
        public Color backgroundColor;
        public Color borderColor;
        public int spacing;
        boolean draw;
        public Group(int top, int left, int bottom, int right, int spacing, boolean draw) {
            this.top    = top;
            this.left   = left;
            this.bottom = bottom;
            this.right  = right;
            this.spacing = spacing;
            this.draw = draw;
        }
    }
    
    private static class Circle {
        public final int x;
        public final int y;
        public final int r;
        public final Color color;

        public Circle(int x, int y, int r, Color color) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.color = color;
        }               
    }
    
    private static class Connection {
        final int srcX;
        final int srcY;
        final int dstX;
        final int dstY;
        Color color;
        Stroke stroke;
        
        public Connection(int srcX, int srcY, int dstX, int dstY, Color color, Stroke stroke) {
            this.srcX = srcX;
            this.srcY = srcY;
            this.dstX = dstX;
            this.dstY = dstY;
            this.color = color;
            this.stroke = stroke;
        }
        
        public boolean matches(int srcX, int srcY, int dstX, int dstY) {
            return this.srcX == srcX && this.srcY == srcY && this.dstX == dstX && this.dstY == dstY;
        }
    };
    
    private static Stroke NORMAL_STROKE = new BasicStroke();
    private static Stroke BOLD_STROKE = new BasicStroke(3);
    
    private int width;
    private int height;
    private Dimension componentSize;
    
    private int[] verticalGroupSpacing;
    private int[] horizontalGroupSpacing;
    private int[] commulativeVerticalSpacing;
    private int[] commulativeHorizontalSpacing;
    
    private int cellSize;
    private int cellSpacing;
    private int cellDistance;
    
    private Color gridColor;
    private Color groupBorderColor;
    private Color groupBackgroundColor;
    
    private boolean drawGrid;
    
    private final ArrayList<Group> groups;
    private final Object groupSynchronizationMonitor = new Object();
    private final ArrayList<Connection> connections;
    private ArrayList<Circle> circles = null;
    
    abstract protected void drawCell(int col, int row, Graphics g);

    public AbstractGridCanvas(int width, int height, int cellsize) {
        this.cellSize = cellsize;
        internalSetGridSize(width, height);
        
        cellSpacing = 1;
        cellDistance = cellSpacing + cellSize;
        
        gridColor = Color.BLACK;
        groupBorderColor = Color.BLACK;
        groupBackgroundColor = Color.WHITE;
        
        drawGrid = false;
        groups = new ArrayList<Group>();
        connections = new ArrayList<Connection>();
        
        componentSize = getPreferredSize();
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
        cellDistance = cellSpacing + cellSize;
        evaluatePrefferedSize();
        repaint();
    }
    
    public int getCellSize() {
        return cellSize;
    }

    private void internalSetGridSize(int width, int height) {
        this.width = width;
        this.height = height;
        verticalGroupSpacing = new int[width + 2];
        horizontalGroupSpacing = new int[height + 2];
        commulativeVerticalSpacing = new int[width + 2];
        commulativeHorizontalSpacing = new int[height + 2];
        evaluatePrefferedSize();
        repaint();
    }
    
    private void evaluatePrefferedSize() {
        int h = height * cellSize + commulativeHorizontalSpacing[height];        
        int w = width * cellSize + commulativeVerticalSpacing[width];
        
        componentSize = new Dimension(w, h);
        
        this.setPreferredSize(componentSize);
    }
    
    public void setGridSize(int width, int height) {
        internalSetGridSize(width, height);
    }
    
    public void showGrid() {
        drawGrid = true;
        repaint();
    }
    
    public void hideGrid() {
        drawGrid = false;
        repaint();
    }
    
    public boolean isShowingGrid() {
        return drawGrid;
    }
    
    public void setGridColor(Color c) {
        gridColor = c;
    }
    
    public Color getGridColor() {
        return gridColor;
    }
    
    public void setGroupBorderColor(Color c) {
        groupBorderColor = c;
    }
    
    public Color getGroupBorderColor() {
        return groupBorderColor;
    }
    
    public void setGroupBackgroundColor(Color c) {
        groupBackgroundColor = c;
    }
    
    public Color getGroupBackgroundColor() {
        return groupBackgroundColor;
    }

    public void setCellSpacing(int s) {
        cellSpacing = s;
        cellDistance = cellSpacing + cellSize;
        recalculateCommulativeSpacings();
    }
    
    public void createGroup(int top, int left, int bottom, int right, final int spacing, boolean draw) {
        synchronized(groupSynchronizationMonitor) {
            groups.add(new Group(top, left, bottom, right, spacing, draw));
            horizontalGroupSpacing[top] = Math.max(horizontalGroupSpacing[top], spacing);
            horizontalGroupSpacing[bottom] = Math.max(horizontalGroupSpacing[bottom], spacing);
            verticalGroupSpacing[left] = Math.max(verticalGroupSpacing[left], spacing);
            verticalGroupSpacing[right] = Math.max(verticalGroupSpacing[right], spacing);
            recalculateCommulativeSpacings();
        }
        evaluatePrefferedSize();
    }
    
    private void recalculateCommulativeSpacings() {
        int sum = 0;
        for(int i = 0; i < height + 1; i++) {
            int step = cellSpacing;
            if(horizontalGroupSpacing[i] > 0)
                step = horizontalGroupSpacing[i] * 2 + 1;

            sum += step;

            commulativeHorizontalSpacing[i] = sum;
        }

        sum = 0;
        for(int i = 0; i < width + 1; i++) {
            int step = cellSpacing;
            if(verticalGroupSpacing[i] > 0)
                step = verticalGroupSpacing[i] * 2 + 1;

            sum += step;

            commulativeVerticalSpacing[i] = sum;
        }                
    }
    
    
    
    public void clearGroups() {
        groups.clear();
        recalculateCommulativeSpacings();
        evaluatePrefferedSize();
        repaint();
    }
    
    public void addCircle(int x, int y, int r, Color color) {
        if(circles == null) {
            circles = new ArrayList<Circle>();
        }
        
        circles.add(new Circle(x, y, r, color));
    }
    
    public void clearCircles() {
        if(circles != null) {
            circles.clear();
        }
    }
    
    public void removeCirlcesAt(int x, int y) {
        ArrayList<Circle> toRemove = new ArrayList<>();
        for(Circle c: circles) {
            if(c.x == x && c.y == y) {
                toRemove.add(c);
            }
        }
        circles.removeAll(toRemove);
    }
    
    public void createConnection(int srcX, int srcY, int dstX, int dstY, Color color, StrokeStyle strokeStyle) {
        Stroke stroke = NORMAL_STROKE
                ;
        switch(strokeStyle) {
            case NORMAL:
                stroke = NORMAL_STROKE;
                break;
                
            case BOLD:
                stroke = BOLD_STROKE;
                break;
        }
        
        for(Connection c : connections) {
            if(c.matches(srcX, srcY, dstX, dstY)) {
                c.color = color;
                c.stroke = stroke;
                return;
            }
        }
        
        connections.add(new Connection(srcX, srcY, dstX, dstY, color, stroke));
    }
    
    public void clearConnections() {
        connections.clear();
    }
    
    public void repaint(int x, int y) {
        //int s = size + 1;
        //this.repaint(x * s + 1, y * s + 1, s, s);
        this.repaint();
    }

    public int getX(int i) {
        return i * cellSize + commulativeVerticalSpacing[i];
    }

    public int getY(int j) {
        return j * cellSize + commulativeHorizontalSpacing[j];
    }

    @Override
    public void paint(Graphics g) {
        if(width == 0 || height == 0)
            return;
        
        int w = componentSize.width;
        int h = componentSize.height;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w - 1, h - 1);

        g.setColor(gridColor);

        if(drawGrid) {
            for(int x = 0; x <= w; x += cellDistance)
                g.drawLine(x, 0, x, h);
            for(int y = 0; y <= h; y += cellDistance)
                g.drawLine(0, y, w, y);
        }
        
        synchronized(groupSynchronizationMonitor) {
            for(Group group : groups) {
                if(!group.draw)
                    continue;

                int x = group.left * cellSize + commulativeVerticalSpacing[group.left] - group.spacing - 1;
                int y = group.top  * cellSize + commulativeHorizontalSpacing[group.top] - group.spacing - 1;
                
                int gw = (group.right - group.left) * cellSize
                        + commulativeVerticalSpacing[group.right - 1]
                        - commulativeVerticalSpacing[group.left]
                        + group.spacing*2 + 1;

                int gh = (group.bottom - group.top) * cellSize
                        + commulativeHorizontalSpacing[group.bottom - 1]
                        - commulativeHorizontalSpacing[group.top]
                        + group.spacing*2 + 1;

                g.setColor(groupBorderColor);
                g.drawRect(x, y, gw, gh);
            }
        }
        
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                drawCell(i, j, g.create(getX(i), getY(j), cellSize, cellSize));
                g.setColor(Color.RED);
            }
        }
        
        if(g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D)g;
            for(Connection c : connections) {
                g2d.setColor(c.color);
                g2d.setStroke(c.stroke);
                g.drawLine(getX(c.srcX) + cellSize/2, getY(c.srcY)  + cellSize/2, getX(c.dstX)  + cellSize/2, getY(c.dstY)  + cellSize/2);
                g.fillOval(getX(c.srcX) + cellSize/2 - 2, getY(c.srcY) + cellSize/2 - 2, 5, 5);
                g2d.setStroke(NORMAL_STROKE);
            }
        }
        
        if(circles != null) {
            for(Circle c: circles) {
                g.setColor(c.color);
                g.drawOval(getX(c.x) - c.r + cellSize/2, getY(c.y) - c.r + cellSize/2, c.r*2, c.r*2);
            }
        }        
    }
    
    public IntPair getCellAt(int x, int y) {
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                int cellX = getX(i);
                int cellY = getY(j);
                if(x >= cellX && y >= cellY && x < cellX + cellSize && y < cellY + cellSize)
                    return new IntPair(i, j);
            }
        }
        return null;
    }
}