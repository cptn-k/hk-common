/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.util;

/**
 *
 * @author hamed
 */
public class TextTable {
    String[][] cells;
    int[] max;
    int[] limit;
    int row = 0;
    int cols = 0;

    public TextTable(int cols) {
        cells = new String[500][cols];
        max = new int[cols];
        limit = new int[cols];
        this.cols = cols;
        clr();
    }

    public void setLimit(int l) {
        java.util.Arrays.fill(limit, l);
    }

    public void cr() {
        row++;
    }

    public void hLine() {
        cells[row][0] = null;
        cr();
    }

    public void clr() {
        row = 0;
        for(int i = 0; i < max.length; i++)
            max[i] = 0;
    }

    public void set(int col, String value) {
        set(row, col, value);
    }

    private void set(int row, int col, String value) {
        cells[row][col] = value;
        max[col] = Math.max(value.length(), max[col]);
    }

    public void setRow(Object... args) {
        int l = Math.min(cols, args.length);
        for(int i = 0; i < l; i++) {
            if(args[i] == null)
                set(row, i, "");
            else
                set(row, i, args[i].toString());
        }
        cr();
    }

    private void appendHLine(StringBuffer buff) {
        //buff.append('+');
        for(int j = 0; j < cols; j++) {
            for(int l = 0; l < max[j]; l++)
                buff.append('=');
            buff.append("  ");
        }
        buff.append('\n');
    }

    @Override
    public String toString() {
        return getStringBuffer().toString();
    }

    public StringBuffer getStringBuffer() {
        StringBuffer buff = new StringBuffer();
        //appendHLine(buff);
        for(int i = 0; i < row; i++) {
            if(cells[i][0] == null) {
                appendHLine(buff);
                continue;
            }
            //buff.append('|');
            for(int j = 0; j < cols; j++) {
                if(cells[i][j] == null)
                    cells[i][j] = "[null]";
                int l = cells[i][j].length();
                buff.append(cells[i][j]);
                for(; l < max[j]; l++)
                    buff.append(' ');
                buff.append("  ");
            }
            buff.append('\n');
        }
        //appendHLine(buff);
        return buff;
    }

    public static void main(String[] args) {
        TextTable t = new TextTable(4);
        t.setRow("123132", "afsa", "sasdgasd", "asd");
        t.hLine();
        t.setRow("sdghst", "asdff", "3asg", "mujdvbed");
        t.setRow("ok", "dfdeasdf", "sdd", "fs");
        System.out.print(t.toString());
    }
}
