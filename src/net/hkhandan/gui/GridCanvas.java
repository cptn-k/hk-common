package net.hkhandan.gui;

import java.awt.*;
import java.awt.event.*;
import net.hkhandan.math.IntPair;


public class GridCanvas extends AbstractGridCanvas {

////////////////////////////////////////////////////////////// INNER TYPES
    public enum Shape {
        RECT,
        FILLED_RECT,
        OVAL,
        FILLED_OVAL,
        TRIANGLE,
        FILLED_TRIANGLE,
        DIAMOND,
        FILLED_DIAMOND,
        NONE;
    }

    public static class Cell implements Cloneable {
        String text = null;
        public Color foreground = Color.GRAY;
        public Color background = Color.WHITE;
        public Color textColor  = Color.BLACK;
        public Shape shape = Shape.RECT;
        boolean redraw = false;

        @Override
        public Object clone() {
            Cell c = new Cell();
            c.text = text;
            c.foreground = foreground;
            c.background = background;
            c.textColor = textColor;
            c.shape = shape;
            c.redraw = redraw;
            return c;
        }
    }

////////////////////////////////////////////////////////////// FIELDS
    private Cell[][] cells;
    private Cell     defaultCell = new Cell();
    private Polygon  triangle;
    private Polygon  diamond;
    private int w = 0;
    private int h = 0;

    public GridCanvas(int width, int height, int cellsize) {
        super(width, height, cellsize);
        this.w = width;
        this.h = height;
        cells = new Cell[width][height];
        
        setCellSize(cellsize);
    }
    
    @Override
    public final void setCellSize(int cellsize) {
        int c = cellsize/2;
        int a_half = (int)(cellsize/Math.tan(Math.PI/3)/2);
        int h_less = (int)(h * Math.sqrt(2)/2);
        diamond = new Polygon(new int[]{c, h, c, 0, c}, new int[]{0, c, h, c, 0}, 5);
        triangle = new Polygon(new int[]{c, c - a_half, c + a_half, c}, new int[]{(h - h_less)/2, h_less, h_less, (h - h_less)/2}, 4);
        super.setCellSize(cellsize);
    }

    @Override
    public void setGridSize(int width, int height) {
        super.setGridSize(width, height);
        Cell[][] newCells = new Cell[width][height];
        int mw = Math.min(w, width);
        int mh = Math.min(h, height);
        for(int i = 0; i < mw; i++)
            System.arraycopy(cells[i], 0, newCells[i], 0, mh);
        cells = newCells;
        w = width;
        h = height;
    }

    public void setText(int i, int j, String text) {
        set(i, j);
        cells[i][j].text = text;
    }

    public void setForeground(int i, int j, Color c) {
        set(i, j);
        cells[i][j].foreground = c;
    }

    public void setBackgournd(int i, int j, Color c) {
        set(i, j);
        cells[i][j].background = c;
    }

    public void setTextColor(int i, int j, Color c) {
        set(i, j);
        cells[i][j].textColor = c;
    }

    public void setShape(int i, int j, Shape s) {
        set(i, j);
        cells[i][j].shape = s;
    }

    private void set(int i, int j) {
        if(cells[i][j] == null)
            cells[i][j] = (Cell)defaultCell.clone();
    }

    public void setDefault(Cell c) {
        defaultCell = c;
    }

    public void clear(int x, int y) {
        cells[x][y] = null;
        //repaint(x, y);
    }

    public void clear() {
        for(int x = 0; x < w; x++)
            for(int y = 0; y < h; y++)
                cells[x][y] = null;
        repaint();
    }

    @Override
    public void drawCell(int col, int row, Graphics g) {
        Cell cell = defaultCell;
        if(cells[col][row] != null)
            cell = cells[col][row];

        int s = getCellSize();
                
        if(cells[col][row] != null) {
            g.setColor(cell.background);
            g.fillRect(0, 0, s, s);
        }

        g.setColor(cell.foreground);
        switch(cell.shape) {
            case RECT:
                g.drawRect(0, 0, s, s); break;
            case FILLED_RECT:
                g.fillRect(0, 0, s, s); break;
            case OVAL:
                g.drawOval(0, 0, s, s); break;
            case FILLED_OVAL:
                g.fillOval(0, 0, s, s); break;
            case TRIANGLE:
                g.drawPolygon(triangle); break;
            case FILLED_TRIANGLE:
                g.fillPolygon(triangle); break;
            case DIAMOND:
                g.drawPolygon(diamond); break;
            case FILLED_DIAMOND:
                g.fillPolygon(diamond); break;
        }

        String text = cell.text;
        if(text != null) {
            g.setColor(cell.textColor);

            FontMetrics fm = g.getFontMetrics();
            int x = s/2 - fm.stringWidth(text)/2;
            int y = s/2 + fm.getHeight()/2 - 2;

            g.drawString(text , x, y);
        }
    }

    public static void main(String[] args) {
        Frame f = new Frame("GridCanvas Test");
        f.setLayout(new BorderLayout());

        GridCanvas testCanvas = new GridCanvas(20, 10, 30);
        testCanvas.createGroup(1, 1, 4, 5, 1, true);
        testCanvas.createGroup(4, 2, 6, 5, 1, true);
        testCanvas.setGroupBackgroundColor(Color.GREEN);
        f.add(testCanvas, BorderLayout.CENTER);
        
        testCanvas.setText(2, 2, "Hi!");

        f.setSize(400, 300);
        f.setVisible(true);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
    }
}
