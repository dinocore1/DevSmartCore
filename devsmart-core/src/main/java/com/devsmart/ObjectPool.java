package com.devsmart;

public class ObjectPool<T> {

    public interface PooledCreator<Q> {
        Q create();
    }

    private PooledCreator<T> mCreator;
    private Object[] mObjects;
    private int mFreeIndex;

    public ObjectPool(int poolSize, PooledCreator<T> creator) {
        mCreator = creator;
        mObjects = new Object[poolSize];
        mFreeIndex = -1;
    }

    public synchronized T borrow() {
        if (mFreeIndex < 0) {
            return mCreator.create();
        } else {
            T retval = (T) mObjects[mFreeIndex];
            mObjects[mFreeIndex] = null;
            mFreeIndex--;
            return retval;
        }
    }

    public synchronized void release(T obj) {
        if (obj == null) {
            return;
        }

        if (mFreeIndex >= mObjects.length - 1) {
            return;
        }

        if(isInFreeList(obj)){
            return;
        }

        mObjects[++mFreeIndex] = obj;
    }

    public synchronized boolean isInFreeList(T obj) {
        for(int i=0;i<mFreeIndex+1;i++){
            if(obj == mObjects[i]) {
                return true;
            }
        }

        return false;
    }
}