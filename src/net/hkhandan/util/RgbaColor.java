/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hkhandan.util;

/**
 *
 * @author hamed
 */
public class RgbaColor extends Color {
    private final static String[] strColorList = {"#000000", "#00FF00", 
        "#0000FF", "#FF0000",  "#01FFFE", "#FFA6FE", "#FFDB66", "#006401", 
        "#010067", "#95003A",  "#007DB5", "#FF00F6", "#FFEEE8", "#774D00",
        "#90FB92", "#0076FF",  "#D5FF00", "#FF937E", "#6A826C", "#FF029D",
        "#FE8900", "#7A4782", "#7E2DD2", "#85A900", "#FF0056", "#A42400",
        "#00AE7E", "#683D3B", "#BDC6FF", "#263400", "#BDD393", "#00B917",
        "#9E008E", "#001544", "#C28C9F", "#FF74A3", "#01D0FF", "#004754",
        "#E56FFE", "#788231", "#0E4CA1", "#91D0CB", "#BE9970", "#968AE8",
        "#BB8800", "#43002C", "#DEFF74", "#00FFC6", "#FFE502", "#620E00",
        "#008F9C", "#98FF52", "#7544B1", "#B500FF", "#00FF78", "#FF6E41",
        "#005F39", "#6B6882", "#5FAD4E", "#A75740", "#A5FFD2", "#FFB167",
        "#009BFF", "#E85EBE"};
    
    public static RgbaColor[] DISTINCT_COLORS = convertFromHtmlHex(strColorList);
    
    public static RgbaColor[] convertFromHtmlHex(String[] colors) {
        RgbaColor[] result = new RgbaColor[colors.length];
        for(int i = colors.length - 1; i >= 0; i--) {
            result[i] = new RgbaColor(colors[i]);
        }
        return result;
    }
    
    public static HsbaColor[] convertToHsla(RgbaColor[] colors) {
        HsbaColor[] result = new HsbaColor[colors.length];
        for(int i = colors.length - 1; i >= 0; i--) {
            result[i] = colors[i].convertToHsla();
        }
        return result;
    }
    
    public static java.awt.Color[] convertToAwtColor(RgbaColor[] colors) {
        java.awt.Color[] result = new java.awt.Color[colors.length];
        for(int i = colors.length - 1; i >= 0; i--) {
            result[i] = colors[i].convertToAwtColor();
        }
        return result;
    }
    
    public RgbaColor(float r, float g, float b) {
        super(r, g, b, 1);
    }
    
    public RgbaColor(float r, float g, float b, float a) {
        super(r, g, b, a);
    }
    
    public RgbaColor(String htmlHex) {
        super(Integer.valueOf(htmlHex.substring(1, 3), 16)/255f,
                Integer.valueOf(htmlHex.substring(3, 5), 16)/255f,
                Integer.valueOf(htmlHex.substring(5, 7), 16)/255f,
                1);
    }
    
    public float getRed() {
        return channel1;
    }
    
    public float getGreen() {
        return channel2;
    }
    
    public float getBlue() {
        return channel3;
    }
    
    public float getAlpha() {
        return channel4;
    }
    
    public HsbaColor convertToHsla() {
        float cMax = Math.max(channel1, Math.max(channel2, channel3));
        float cMin = Math.min(channel1, Math.min(channel2, channel3));
        float delta = cMax - cMin;
        
        float h = 0;
        if(cMax == channel1) {
            h = 60 * (int)Math.round((channel2 - channel3)/delta) % 6;
        } else if(cMax == channel2) {
            h = 60 * (channel3 - channel1)/delta + 2;
        } else {
            h = 60 * (channel1 - channel2)/delta + 4;
        }
        
        h = h/360;
        
        float l = (cMax + cMin)/2;
        
        float s = (delta == 0)?0:(delta/(1 - Math.abs(2*l - 1)));
        
        return new HsbaColor(h, s, l, channel4);
    }
    
    public java.awt.Color convertToAwtColor() {
        return new java.awt.Color(channel1, channel2, channel3, channel4);
    }
}
