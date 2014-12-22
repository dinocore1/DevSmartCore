package com.devsmart;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DoubleArrayAllocatorTest {

    @Test
    public void testBin() {
        int i = DoubleArrayAllocator.getBin2(20);
        assertEquals(5, i);

        i = DoubleArrayAllocator.getBin2(32);
        assertEquals(5, i);
    }
}
