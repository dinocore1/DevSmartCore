package com.devsmart;


public class DoubleArrayPool {

    public final int mMaxSize;
    final double[][] mArrays;
    final int mArraySize;
    int mFreeIndex;

    public DoubleArrayPool(int maxSize, int arraySize) {
        this.mMaxSize = maxSize;
        this.mArraySize = arraySize;
        this.mArrays = new double[maxSize][];
        this.mFreeIndex = -1;
    }


    public double[] borrow() {
        if(mFreeIndex < 0) {
            return new double[mArraySize];
        }

        return mArrays[mFreeIndex--];
    }

    public void release(double[] o) {
        if(o != null
                && o.length == mArraySize
                && mFreeIndex < mArrays.length-1
                && !isAlreadyInFreeList(o)) {

            mArrays[++mFreeIndex] = o;
        }
    }

    public int getPoolSize() {
        return mFreeIndex+1;
    }

    boolean isAlreadyInFreeList(double[] o) {
        for(int i=0;i<mFreeIndex+1;i++){
            if(mArrays[i] == o){
                return true;
            }
        }
        return false;
    }


}
