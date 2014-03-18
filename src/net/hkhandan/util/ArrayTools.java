package net.hkhandan.util;

/**
 * <p>Title: Object Recogniser</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author Hamed Khandan
 * @version 1.0
 */
public final class ArrayTools {
    private ArrayTools() {
    }

    public static String arrayToString(Object[] array, String open, String delimeter, String close) {
        return _arrayToString(array, open, delimeter, close).toString();
    }

    private static StringBuilder _arrayToString(Object[] array, String open, String delimeter, String close) {
        StringBuilder builder = new StringBuilder();
        builder.append(open);
        int l = array.length - 1;
        for(int i = 0; i <= l; i++) {
            builder.append(array[i].toString());
            if(i < l)
                builder.append(delimeter);
        }
        builder.append(close);
        return builder;
    }

    public static String toString2(double[][] array) {
        return _toString(array).toString();
    }

    private static StringBuilder _toString(double[][] array) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int l = array.length - 1;
        for(int i = 0; i <= l; i++) {
            builder.append(toString(array[i]));
            if(i < l)
                builder.append(", ");
        }
        builder.append("}");
        return builder;
    }

    public static String toString(int[] a) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for(int i = 0; i < a.length; i++) {
          buf.append(Integer.toString(a[i]));
          if(i < a.length - 1)
            buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }

    public static String toString(int[] a, int bias) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for(int i = 0; i < a.length; i++) {
          buf.append(Integer.toString(a[i] + bias));
          if(i < a.length - 1)
            buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }


    public static String toString(double[] a) {
        return toString(a, "%f");
    }
    
    public static String toString(double[] a, int p) {
        return toString(a, "%." + p + "f");
    }
    
    private static String toString(double[] a, String formatStr) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for(int i = 0; i < a.length; i++) {
          buf.append(String.format(formatStr, a[i]));
          if(i < a.length - 1)
            buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }

    public static String toString(Object[] a) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for(int i = 0; i < a.length; i++) {
          buf.append(a[i].toString());
          if(i < a.length - 1)
            buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }

    /**
    * toString
    *
    * @param speedProb long[]
    * @return Object
    */
    public static Object toString(long[] a) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for(int i = 0; i < a.length; i++) {
          buf.append(Double.toString(a[i]));
          if(i < a.length - 1)
            buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }

    public static void copy(double[] src, double[] dst) {
        int s = Math.min(src.length, dst.length);
        for(int i = 0; i < s; i++) {
          dst[i] = src[i];
        }
    }

    public static void copy(int[] src, int[] dst) {
        int s = Math.min(src.length, dst.length);
        for(int i = 0; i < s; i++) {
          dst[i] = src[i];
        }
    }

    public static int[] drop(int[] a, int n) {
        int[] result = new int[a.length - 1];
        for(int i = 0; i < n; i++)
          result[i] = a[i];
        for(int i = n; i < result.length; i++)
          result[i] = a[i + 1];
        return result;
    }

    public static void drop(int[] dst, int[] src, int n) {
        for(int i = 0; i < n; i++)
          dst[i] = src[i];
        for(int i = n; i < dst.length; i++)
          dst[i] = src[i + 1];
    }

    public static void append(int[] dst, int[] src1, int[] src2) {
        for(int i = 0; i < src1.length; i++)
          dst[i] = src1[i];
        for(int i = 0; i < src2.length; i++)
          dst[src1.length + i] = src2[i];
    }

    public static double sum(double[] ar) {
        double s = 0;
        for(int i = 0; i < ar.length; i++)
          s += ar[i];
        return s;
    }

    public static float sum(float[] ar) {
        float s = 0;
        for(int i = 0; i < ar.length; i++)
          s += ar[i];
        return s;
    }
    
    public static int sum(int[] ar) {
        int s = 0;
        for(int i = 0; i < ar.length; i++)
          s += ar[i];
        return s;
    }
    
    /**
    * mean
    *
    * @param res double[]
    * @return double
    */
    public static double mean(double[] res) {
        return sum(res)/res.length;
    }

    /**
    * @return true if class a is a subclass of class b
    */
    public static boolean isSubclass(Class a, Class b) {
        Class sup = b;
        do {
            if(sup.equals(a))
                return true;
            sup = sup.getSuperclass();
        } while(sup != null);
        return false;
    }
}
