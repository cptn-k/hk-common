/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hkhandan.util;

/**
 *
 * @author hamed
 */
public class HsbaColor extends Color {
    public static RgbaColor[] convertToRgbaColor(HsbaColor[] colors) {
        RgbaColor[] result = new RgbaColor[colors.length];
        for(int i = colors.length - 1; i >= 0; i--) {
            result[i] = colors[i].convertToRgba();
        }
        return result;        
    }
        
    public HsbaColor(float h, float s, float l) {
        super(h, s, l, 1);
    }
    
    public HsbaColor(float h, float s, float l, float a) {
        super(h, s, l, a);
    }
    
    public float getHue() {
        return channel1;
    }
    
    public float getSaturation() {
        return channel2;
    }
    
    public float getLightness() {
        return channel3;
    }
    
    public float getAlpha() {
        return channel4;
    }
    
    private static float hueToRgb(float p, float q, float t) {
        if(t < 0)
            t += 1;
        
        if(t > 1)
            t -= 1;
        
        if(t < 1/6)
            return p + (q - p) * 6 * t;
        
        if(t < 1/2)
            return q;
        
        if(t < 2/3)
            return p + (q - p) * (2/3 - t) * 6;
        
        return p;
    }
    
    public RgbaColor convertToRgba() {
        if(channel2 == 0) {
            return new RgbaColor(channel3, channel3, channel3, channel4);
        } 
        
        float q = (channel3 < 0.5)?channel3 * (1 + channel2):channel3 + channel2 - channel3*channel2;
        float p = 2 * channel3 - q;
        return new RgbaColor(
                hueToRgb(p, q, channel1 + 1.0f/3.0f),
                hueToRgb(p, q, channel1),
                hueToRgb(p, q, channel1 - 1.0f/3.0f),
                channel4);
    }
}
