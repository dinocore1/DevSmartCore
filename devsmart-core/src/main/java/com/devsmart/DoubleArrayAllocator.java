package com.devsmart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DoubleArrayAllocator {

    private static final Logger logger = LoggerFactory.getLogger(DoubleArrayAllocator.class);

    private final long mMaxSize;
    private long mSize = 0;
    private ArrayList<double[]> mPool = new ArrayList<double[]>();

    public DoubleArrayAllocator(long maxSize) {
        mMaxSize = maxSize;
    }

    /**
     * Returns an array to use that is at least {@code size} length.
     * @param size
     * @return
     */
    public synchronized double[] alloc(int size) {
        double[] retval;
        int index = binarySearch(mPool, size);
        if(index >= 0) {
            retval = mPool.remove(index);
            mSize -= retval.length;
        } else {
            index = -index - 1;
            if (index < mPool.size()) {
                retval = mPool.remove(index);
                mSize -= retval.length;
            } else {
                //logger.info("allocating array {}", size);
                retval = new double[size];
            }
        }
        return retval;
    }

    public synchronized void free(double[] obj) {
        if(mSize + obj.length < mMaxSize) {
            int index = binarySearch(mPool, obj.length);
            if(index >= 0 && contains(mPool, index, obj)) {
                //be sure to not add the same obj twice
                return;
            }
            index = index < 0 ? -index-1 : index;
            mPool.add(index, obj);
            mSize += obj.length;
        }
    }

    private boolean contains(final List<double[]> list, int floor, double[] obj) {
        final int top = list.size();
        double[] a;
        for(int i=floor;i<top && (a = list.get(i)).length == obj.length;i++) {
            if(a == obj) {
                return true;
            }
        }
        return false;
    }

    /**
     * deferred detection binary search algorithm. This version of binary search has
     * the benefit that it will always return the smallest index of duplicate keys.
     * this is useful later when we search for the same instance of arrays.
     * @param list
     * @param key
     * @return
     */
    private static int binarySearch(final List<double[]> list, final int key) {
        double[] obj = null;
        int lo = 0;
        int hi = list.size();

        while(lo < hi) {
            int mid = lo + (hi-lo)/2;

            obj = list.get(mid);
            if(obj.length < key) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }

        if(lo == hi && lo < list.size() && list.get(lo).length == key) {
            return lo;
        } else {
            return -(lo+1);
        }
    }
}
