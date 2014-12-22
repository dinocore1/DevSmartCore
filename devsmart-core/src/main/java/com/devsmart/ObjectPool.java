package com.devsmart;

public class ObjectPool<T extends ObjectPool.Pooled> {
    private PooledCreator mCreator;
    private Pooled[] mObjects;
    private int mFreeIndex;

    public ObjectPool(int poolSize, PooledCreator creator) {
        mCreator = creator;
        mObjects = new Pooled[poolSize];
        mFreeIndex = -1;
    }

    public synchronized T borrow() {
        if (mFreeIndex < 0) {
            return (T) mCreator.create();
        }

        return (T) mObjects[mFreeIndex--];
    }

    public synchronized void release(Pooled obj) {
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

    public synchronized boolean isInFreeList(Pooled obj) {
        for(int i=0;i<mFreeIndex+1;i++){
            if(obj == mObjects[i]) {
                return true;
            }
        }

        return false;
    }

    public interface Pooled {
    }

    public interface PooledCreator {
        Pooled create();
    }
}