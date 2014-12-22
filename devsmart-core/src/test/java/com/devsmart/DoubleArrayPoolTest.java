package com.devsmart;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DoubleArrayPoolTest {

    @Test
    public void testBorrowRelease() {
        DoubleArrayPool pool = new DoubleArrayPool(3, 10);
        double[] a = pool.borrow();
        assertNotNull(a);
        assertEquals(10, a.length);

        pool.release(a);
        assertEquals(1, pool.getPoolSize());

        double[] b = pool.borrow();
        assertNotNull(b);
        assertTrue(a == b);

        pool.release(b);
        assertEquals(1, pool.getPoolSize());
        pool.release(b);

        assertEquals(1, pool.getPoolSize());

        pool.release(new double[11]);
        assertEquals(1, pool.getPoolSize());

        pool.release(new double[10]);
        assertEquals(2, pool.getPoolSize());

        pool.release(new double[10]);
        assertEquals(3, pool.getPoolSize());

        pool.release(new double[10]);
        assertEquals(3, pool.getPoolSize());
    }


}
