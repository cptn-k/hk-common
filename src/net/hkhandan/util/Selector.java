/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hkhandan.util;

/**
 *
 * @author hamed
 */
public class Selector<A, B> {
    private final A valueToCompare;
    private boolean release = false;
    private B valueToRelease = null;
            
    private Selector(A value, B defaultValue) {
        valueToCompare = value;
        valueToRelease = defaultValue;
    }
    
    public static <S, T> Selector<S, T> with(S value, T defaultValue) {
        return new Selector<S, T>(value, defaultValue);
    }
    
    public static <T> Selector<Object, T> with(T defaultValue) {
        return new Selector<Object, T>(null, defaultValue);
    }
    
    public Selector<A, B> when(A value) {
        return when(value == valueToCompare);
    }
    
    public Selector<A, B> when(boolean condition) {
        release = condition;
        return this;
    }
    
    public Selector<A, B> is(B value) {
        if(release)
            valueToRelease = value;
        return this;
    }
    
    public B end() {
        return valueToRelease;
    }
}
