package net.hkhandan.math;

import net.hkhandan.util.ArrayTools;

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
public class SuperMatrix {

  public static class SuperMatrixDimentionMissmatch extends Error {
    public SuperMatrixDimentionMissmatch() {
      super();
    }
    public SuperMatrixDimentionMissmatch(String msg) {
      super(msg);
    }
  }

  double[] buffer;
  int[]    dims;

  private SuperMatrix() {
    super();
  }

  /**
   * Creates a 1st rank tensor representing a vector of length
   * <code>vecLen</code>
   * @param vecLen int - Length of the vector represented by this
   * <code>SuperMatrix</code>
   */
  public SuperMatrix(int vecLen) {
    this(new int[]{vecLen});
  }

  public SuperMatrix(int[] dims) {
    this.dims = dims;
    buffer = new double[getMemSize(dims)];
  }

  public SuperMatrix(int[] dims, double[] values) {
    this(dims);
    set(values);
  }

  public SuperMatrix(int[] dims, double initValue) {
    this(dims);
    for(int i = 0; i < buffer.length; i++)
      buffer[i] = initValue;
  }

  public SuperMatrix(double value) {
    this(new int[]{1});
    buffer[0] = value;
  }

  public SuperMatrix(double[] values) {
    this(new int[]{values.length});
    set(values);
  }

  public double get(int[] address) {
    return buffer[decodeAddress(address, dims)];
  }

  public double element(int ... address) {
      return get(address);
  }

  public void set(int[] address, double value) {
    buffer[decodeAddress(address, dims)] = value;
  }

  public void set(double[] values) {
    for(int i = 0; i < values.length; i++)
      buffer[i] = values[i];
  }

  public int getRank() {
    //    return (dims[0] == 1)?0:dims.length;
    return dims.length;
  }

  private int getMemSize(int[] dims) {
    int s = 1;
    for(int i = 0; i < dims.length; i++)
      s *= dims[i];
    return s;
  }

  public int getMemSize() {
    return buffer.length;
  }

  public int[] getDimentions() {
    return (int[])dims.clone();
  }

  public void dropDimention(int d) {
    if(d >= getRank())
      return; /** @todo and perhaps throw an exception */

//    if(getRank() == 1) {
//     dims = new int[1];
//      dims[0] = 1;
//      double[] oldBuffer = buffer;
//      buffer = new double[1];
//      buffer[0] = oldBuffer[0];
//    } else {
      int[] oldDims = dims;
      dims = ArrayTools.drop(dims, d);

      double[] oldBuffer = buffer;
      buffer = new double[getMemSize(dims)];

      int[] srcAddress = new int[oldDims.length];
      int[] dstAddress = new int[dims.length];
      int size = buffer.length;
      for(int i = 0; i < size; i++) {
	encodeAddress(dstAddress, dims, i);
	shiftArray(srcAddress, dstAddress, d);
	srcAddress[d] = 0;
        //System.out.println(arrayToString(srcAddress) + ", " + arrayToString(dstAddress));
        /** @todo is this correct ? */
	set(dstAddress, oldBuffer[decodeAddress(srcAddress, oldDims)]);
//      }
    }
  }

  public void dropDimention() {
    dropDimention(dims.length - 1);
  }

  public void addDimention(int s) {
    int   rank  = getRank();

    int[] oldDims = dims;
    dims = new int[rank + 1];
    for(int i = 0; i < oldDims.length; i++)
      dims[i] = oldDims[i];
    dims[rank] = s;

    double[] oldBuffer = buffer;
    buffer = new double[getMemSize(dims)];

    int[] srcAddress = new int[oldDims.length];
    int[] dstAddress = new int[dims.length];
    int   size       = getMemSize(oldDims);
    for(int i = 0; i < size; i++) {
      encodeAddress(srcAddress, oldDims, i);
      for(int j = 0; j < s; j++) {
        encodeAddress(dstAddress, oldDims, i);
        dstAddress[rank] = j;
        set(dstAddress, oldBuffer[decodeAddress(srcAddress, oldDims)]);
      }
    }
  }

  /** @todo implement alterDimention method */
  public void alterDimention(int d, int s) {
    dims[d] = s;
  }

  public SuperMatrix add(SuperMatrix other) {
    SuperMatrix result = new SuperMatrix(getDimentions());
    for(int i = 0; i < buffer.length; i++)
      result.buffer[i] = buffer[i] + other.buffer[i];
    return result;
  }

  public SuperMatrix dot(SuperMatrix other, int mode) {
    if(other.getRank() == 1) {
      return contractedProduct(other.buffer, mode);
    } else {
      throw new IllegalArgumentException("Currently only vector inputs are supported.");
    }
  }

  public SuperMatrix dot(SuperMatrix other){
    return dot(other, dims.length - 1);
  }

  public SuperMatrix dot(double[] v, int mode) {
    return contractedProduct(v, mode);
  }

  public SuperMatrix dot(double[] v) {
    return contractedProduct(v);
  }


  /** @todo remove this method */
  public SuperMatrix contractedProduct(double[] v, int mode) {
    return contractedProduct(v, mode, false);
  }


  public SuperMatrix contractedProduct(double[] v, int mode, boolean debug) {
    /** @todo remove debugging stuff */
    int[] dims    = getDimentions();

    SuperMatrix result = new SuperMatrix(ArrayTools.drop(dims, mode));
    int[] resDims = result.getDimentions();
    int size = result.getMemSize();

    int[] resAddress = new int[resDims.length];
    int[] address    = new int[dims.length];
    int[] vecAddress = new int[1];
    int   n       = resDims.length;
    for(int i = 0; i < size; i++) {
      double sum = 0;
      encodeAddress(resAddress, resDims, i);
      shiftArray(address, resAddress, mode);
      if(debug) System.out.print(ArrayTools.toString(resAddress) + " = ");
      for(int j = 0; j < dims[mode]; j++) {
        address[mode] = j;
        vecAddress[0] = j;
        sum += get(address)*v[j];
        if(debug) System.out.print(ArrayTools.toString(address) + "*" + ArrayTools.toString(vecAddress) + " ");
        if(debug) System.out.print(get(address) + "*" + v[j]);
        if(debug) if(j < dims[mode] - 1)
          System.out.print(" + ");
      }
      result.set(resAddress, sum);
      if(debug) System.out.println(" = " + sum);
    }
    return result;
  }

  public SuperMatrix contractedProduct(double[] v) {
    return contractedProduct(v, dims.length - 1);
  }

  public SuperMatrix contractedProduct(SuperMatrix other) {
    return contractedProduct(other.buffer);
  }

  public SuperMatrix outerProduct(SuperMatrix other) {
    int[] resDims = new int[dims.length + other.dims.length];
    ArrayTools.append(resDims, dims, other.dims);

    int[] addr1   = new int[dims.length];
    int[] addr2   = new int[other.dims.length];
    int[] resAddr = new int[dims.length + other.dims.length];

    int s1 = getMemSize();
    int s2 = other.getMemSize();

    SuperMatrix res = new SuperMatrix(resDims);
    for(int i = 0; i < s1; i++) {
      encodeAddress(addr1, dims, i);
      for(int j = 0; j < s2; j++) {
        encodeAddress(addr2, other.dims, j);
        ArrayTools.append(resAddr, addr1, addr2);
        res.set(resAddr, get(addr1) * other.get(addr2));
      }
    }

    return res;
  }

  public SuperMatrix outerProduct(double[] other) {
    int[] resDims = new int[dims.length + 1];
    ArrayTools.copy(dims, resDims);
    resDims[dims.length] = other.length;

    int[] addr1   = new int[dims.length];
    int   addr2   = 0;
    int[] resAddr = new int[dims.length + 1];

    int s1 = getMemSize();
    int s2 = other.length;

    SuperMatrix res = new SuperMatrix(resDims);
    for(int i = 0; i < s1; i++) {
      encodeAddress(addr1, dims, i);
      for (int j = 0; j < s2; j++) {
        ArrayTools.copy(addr1, resAddr);
        resAddr[dims.length] = j;
        res.set(resAddr, get(addr1) * other[j]);
      }
    }

    return res;
  }

  public SuperMatrix contractedDivision(SuperMatrix v, int mode) {
    return contractedDivision(v.buffer, mode);
  }

  public SuperMatrix contractedDivision(double[] v, int mode) {
    int[] resDims = new int[dims.length + 1];
    shiftArray(resDims, dims, mode);
    resDims[mode] = v.length;
    SuperMatrix result = new SuperMatrix(resDims);
    int[] srcAddress = new int[dims.length];
    int[] dstAddress = new int[resDims.length];
    int   s = result.getMemSize();
    for(int i = 0; i < s; i++) {
      encodeAddress(dstAddress, resDims, i);
      ArrayTools.drop(srcAddress, dstAddress, mode);
      result.set(dstAddress, get(srcAddress)/v[dstAddress[mode]]);
    }
    return result;
  }

  public SuperMatrix contractedDivision(double[] v) {
    return contractedDivision(v, 0);
  }

  public SuperMatrix div(SuperMatrix other) {
    SuperMatrix res = new SuperMatrix(dims);
    int s = getMemSize();
    for(int i = 0; i < s; i++)
      res.buffer[i] = buffer[i] / other.buffer[i];
    return res;
  }

  public SuperMatrix mul(SuperMatrix other) {
    SuperMatrix res = new SuperMatrix(dims);
    int s = getMemSize();
    for(int i = 0; i < s; i++)
      res.buffer[i] = buffer[i] * other.buffer[i];
    return res;
  }

  public void mulBy(double a) {
    for(int i = 0; i < buffer.length; i++)
      buffer[i] *= a;
  }

  public SuperMatrix sum(int mode) {
    int[] dims    = getDimentions();

    SuperMatrix result = new SuperMatrix(ArrayTools.drop(dims, mode));
    int[] resDims = result.getDimentions();
    int size = result.getMemSize();

    int[] resAddress = new int[resDims.length];
    int[] address    = new int[dims.length];
    int   n       = resDims.length;
    for(int i = 0; i < size; i++) {
      double sum = 0;
      encodeAddress(resAddress, resDims, i);
      shiftArray(address, resAddress, mode);
      for(int j = 0; j < dims[mode]; j++) {
        address[mode] = j;
        sum += get(address);
      }
      result.set(resAddress, sum);
    }
    return result;
  }

  private void printEquations(int[] address, int mode, SuperMatrix x, SuperMatrix b) {
    StringBuffer s = new StringBuffer();
    s.append("x" + ArrayTools.toString(address) + " = ");
    int[] dstAddress = new int[dims.length];
    int[] bAddr      = new int[1];
    shiftArray(dstAddress, address, mode);
    for(int i = 0; i < dims[mode]; i++) {
      dstAddress[mode] = i;
      bAddr[0] = i;
      s.append(b.get(bAddr));
      s.append(" * ");
      s.append("A");
      s.append(ArrayTools.toString(dstAddress, 1));
      if(i < dims[mode] - 1)
        s.append(" + ");
    }
    s.append(" = ");
    s.append(x.get(address));
    System.out.println(s.toString());
  }

  private void printEquations(int mode, SuperMatrix x, SuperMatrix b) {
    int[] address = new int[x.dims.length];
    for(int i = 0; i < x.getMemSize(); i++) {
      encodeAddress(address, x.dims, i);
      printEquations(address, mode, x, b);
    }
  }

  private static void shiftArray(int[] target, int[] source, int loc) {
    int diff = target.length - source.length;
    for(int i = 0; i < loc; i++)
      target[i] = source[i];
    for(int i = loc; i < source.length; i++)
      target[diff + i] = source[i];
  }

  private static void encodeAddress(int[] address, int[] dims, int n) {
    for(int i = 0; i < dims.length; i++) {
      address[i] = n % dims[i];
      n = n / dims[i];
    }
  }

  public void encodeAddress(int[] address, int n) {
    encodeAddress(address, this.dims, n);
  }

  private static int decodeAddress(int[] address, int[] dims) {
    int p = 1;
    int loc = 0;
    int l = dims.length;
    for(int i = 0; i < l; i++) {
      loc += address[i] * p;
      p *= dims[i];
    }
    return loc;
  }

// REMOVE THE FOLLOWING IMPLEMENTATIONS
/*
  private String toString(int[] dims, int offset) {
    if(dims.length == 0)
      return Double.toString(buffer[offset]);

    int[] nextDims    = ArrayUtils.drop(dims,    0);
    int step = getMemSize(nextDims);
    StringBuffer buf = new StringBuffer();
    buf.append("{");
    for(int i = 0; i < dims[0]; i++) {
      buf.append(toString(nextDims, offset + i * step));
      if(i < dims[0] - 1)
        buf.append(", ");
    }
    buf.append("}");
    return buf.toString();
  }

  public String toString() {
    return toString(dims, 0);
  }
*/

  private String toString(int[] addr, int n) {

    if(n == dims.length) {
      return Double.toString(get(addr));
    }

    StringBuffer buf = new StringBuffer();
    buf.append("{");
    for(int i = 0; i < dims[n]; i++) {
      addr[n] = i;
      buf.append(toString(addr, n + 1));
      if(i < dims[n] - 1)
        buf.append(", ");
    }
    buf.append("}");

    return buf.toString();
  }

  public String toString() {
    return toString((int[])getDimentions().clone(), 0);
  }

  public String toString2() {
    return toString() + " " + ArrayTools.toString(dims);
  }

  public String dump() {
    int s = getMemSize();
    StringBuffer res = new StringBuffer();
    int[] address = new int[dims.length];
    for(int i = 0; i < s; i++) {
      encodeAddress(address, dims, i);
      res.append(ArrayTools.toString(address));
      res.append(" = ");
      res.append(buffer[i]);
      res.append("\n");
    }
    return res.toString();
  }

  public static void main(String[] args) {
    testVecOuterProduct();
  }

  public static void seminarTest() {
    SuperMatrix grass = new SuperMatrix(
      new int[]{2, 2, 2},
      new double[]{0.99, 0.01, 0.9, 0.1, 0.8, 0.2, 0, 1} );
    SuperMatrix sprinkler = new SuperMatrix(
      new int[]{2, 2},
      new double[] {0.01, 0.99, 0.4, 0.6} );
    SuperMatrix sprinkler0 = sprinkler.contractedProduct(new double[]{0.2, 0.8});
    SuperMatrix grassSpr0   = grass.contractedProduct(new double[]{0.2, 0.8}, 1);
    SuperMatrix lSpr1       = grassSpr0.contractedProduct(new double[]{0.7, 0.3}, 0);
    SuperMatrix sprinkler1n = sprinkler0.mul(lSpr1);
    SuperMatrix sprinkler1d = sprinkler0.contractedProduct(lSpr1);

    System.out.println(sprinkler1n);
    System.out.println(sprinkler1d);
  }

  public static void testOuterProduct() {
    SuperMatrix a = new SuperMatrix(new double[]{0, 1});
    SuperMatrix b = new SuperMatrix(new double[]{1, 0, 0, 0});
    SuperMatrix c = new SuperMatrix(new double[]{0, 1, 0});

    SuperMatrix res = b;
    System.out.println(res);

    res = res.outerProduct(c);
    System.out.println(res);

    res = res.outerProduct(a);
    System.out.println(res);

    res = res.sum(2);
    System.out.println(res);

    res = res.sum(1);
    System.out.println(res);
  }

  public static void testVecOuterProduct() {
    double[] a = new double[]{1, 2};
    double[] b = new double[]{3, 4, 5, 6};
    double[] c = new double[]{9, 3, 7};

    SuperMatrix res = new SuperMatrix(1d);
    System.out.println(res.dump());

    res = res.outerProduct(a);
    System.out.println(res.dump());

    res = res.outerProduct(b);
    System.out.println(res.dump());

    res = res.outerProduct(c);
    System.out.println(res.dump());

    res.dropDimention(0);
    System.out.println(res.dump());
  }

  public static void testDivision() {
    int[] dims = {2, 2};
    SuperMatrix mat = new SuperMatrix(dims);
    for(int i = 0; i < mat.getMemSize(); i++)
      mat.buffer[i] = i;
    SuperMatrix e = new SuperMatrix(new double[]{10, 20});
    SuperMatrix d = new SuperMatrix(new double[]{30, 40, 50, 60 , 70, 80});
    SuperMatrix c = new SuperMatrix(new double[]{90, 100, 110});
    SuperMatrix b = new SuperMatrix(new double[]{120, 130});
    SuperMatrix res = mat;

    System.out.println(res);

    res = res.contractedProduct(e.buffer, 1, true);
    System.out.println(res);

    res = res.contractedDivision(e.buffer, 1);
    System.out.println(res);

    res = res.contractedDivision(e.buffer, 0);
    System.out.println(res);
  }

  private static void testBackwardResoning() {
    int[] dims = {3, 2};
    SuperMatrix mat = new SuperMatrix(dims);
    SuperMatrix b = new SuperMatrix(new double[]{1, 2, 3});
    mat.set(new double[]{
            1, 2, 3,
            4, 5, 6
    });
    SuperMatrix x = mat.dot(b, 0);
    System.out.println(x.toString());
    mat.printEquations(0, x, b);
  }

  private static void testDropDimention() {
    int[] dims = {2, 2, 3, 5, 2};
    SuperMatrix mat = new SuperMatrix(dims);
    for(int i = 0; i < mat.getMemSize(); i++)
      mat.buffer[i] = i;

    //while(mat.getRank() > 0) {
      System.out.println(mat.toString());
      mat.dropDimention();
    //}

    System.out.println(mat.toString());
  }

  private static void testDotProduct() {
    int[] dims = {2, 2, 3};
    SuperMatrix mat = new SuperMatrix(dims);
    mat.set(new double[]{
            0.5, 0.5, 0.5,
            0.5, 0.5, 0.5,
            0.5, 0.5, 0.3333333333333333,
            0.6666666666666666, 0.5, 0.5
    });

    System.out.println("mat = " + mat.toString());
    mat = mat.dot(new double[]{0, 1, 0});
    System.out.println("mat = " + mat.toString());
    mat = mat.dot(new double[]{0, 1});
    System.out.println("mat = " + mat.toString());
  }


}
