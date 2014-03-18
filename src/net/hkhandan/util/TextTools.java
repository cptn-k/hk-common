/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan.util;

import java.lang.StringBuilder;
import java.util.Arrays;

/**
 *
 * @author hamed
 */
public class TextTools {
    public static String indent(String src, int cols) {
        StringBuilder builder = new StringBuilder();
        char[] spaces = new char[cols];
        Arrays.fill(spaces, ' ');
        builder.append(spaces);
        for(char ch:src.toCharArray()) {
            builder.append(ch);
            if(ch == '\n')
                builder.append(spaces);
        }
        return builder.toString();
    }
    
    public static String indent2(String src, int cols) {
        StringBuilder builder = new StringBuilder();
        char[] spaces = new char[cols];
        Arrays.fill(spaces, ' ');
        for(char ch:src.toCharArray()) {
            builder.append(ch);
            if(ch == '\n')
                builder.append(spaces);
        }
        return builder.toString();
    }

    public static String indent(String src, String token) {
        int cols = src.lastIndexOf(token) + token.length();
        return indent(src, cols);
    }
}
