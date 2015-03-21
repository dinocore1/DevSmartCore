package com.devsmart;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DoubleArrayAllocatorTest {

    @Test
    public void testDoubleInsert() {
        DoubleArrayAllocator allocator = new DoubleArrayAllocator(100);

        double[] a = new double[5];
        double[] b;

        allocator.free(a);
        allocator.free(a);

        b = allocator.alloc(5);
        assertNotNull(b);
        assertTrue(a == b);

        double[] c = allocator.alloc(5);
        assertNotNull(c);
        assertTrue(c != a);
    }

    @Test
    public void test1() {

        DoubleArrayAllocator allocator = new DoubleArrayAllocator(100);

        double[] last = null;
        double[] retval = null;
        last = allocator.alloc(5);
        assertNotNull(last);
        assertTrue(last.length >= 5);
        allocator.free(last);

        retval = allocator.alloc(5);
        assertNotNull(retval);
        assertTrue(retval == last);

        allocator.free(retval);


        allocator.free(new double[6]);

        last = new double[8];
        allocator.free(last);
        //allocator.free(new double[7]);
        //allocator.free(new double[8]);

        retval = allocator.alloc(7);
        assertNotNull(retval);
        assertTrue(retval == last);

    }
}
