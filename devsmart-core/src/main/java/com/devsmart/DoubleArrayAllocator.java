package com.devsmart;

public class DoubleArrayAllocator {


    private static DoubleArrayAllocator mSingleton;
    public synchronized static DoubleArrayAllocator get() {
        if(mSingleton == null){
            mSingleton = createDefault();
        }
        return mSingleton;
    }

    public static DoubleArrayAllocator createDefault() {
        DoubleArrayAllocator retval = new DoubleArrayAllocator();
        for(int i=0;i<13;i++){
            retval.ensureBinSize(i, 10);
        }
        return retval;
    }

    DoubleArrayPool[] bins = new DoubleArrayPool[13];

    /**
     * ensures at least a pool of size maximumCapacity for each
     * allocation bin.
     * @param bin
     * @param maxCapacity
     */
    public synchronized void ensureBinSize(int bin, int maxCapacity) {
        int binArraySize = 1 << bin;
        if(bins[bin] == null || bins[bin].mMaxSize < maxCapacity) {
            bins[bin] = new DoubleArrayPool(maxCapacity, binArraySize);
        }
    }

    public synchronized double[] alloc(int size) {
        int bin = getBin2(size);
        DoubleArrayPool pool = bins[bin];
        double[] retval = pool.borrow();
        return retval;
    }

    public synchronized void free(double[] o) {
        if(o == null) {
            return;
        }
        int bin = getBin2(o.length);
        DoubleArrayPool pool = bins[bin];
        pool.release(o);
    }

    static int getBin2(int size) {
        int retval = 1;
        size--;
        while((size >>= 1) > 0) {
            retval++;
        }
        return retval;
    }

    static int getBin(int size) {
        size--;
        size |= size >> 1;
        size |= size >> 2;
        size |= size >> 4;
        size |= size >> 8;
        size |= size >> 16;
        size++;
        return size;
    }
}
