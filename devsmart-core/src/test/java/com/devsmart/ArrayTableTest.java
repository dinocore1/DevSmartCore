package com.devsmart;


import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class ArrayTableTest {

    @Test
    public void displayTest() {
        ArrayTable table = ArrayTable.createWithColumnsCopy(
                new int[]{123456, 1, 5},
                new double[]{5.3, 8.3, 10.3},
                new String[]{"e", "abcdefghijk", "c"}
        );
        System.out.println(table.toString());
    }

    @Test
    public void testTablePrimitive() {

        ArrayTable table = ArrayTable.createWithColumnsCopy(new int[]{1, 3, 5}, new double[]{5.3, 8.3, 10.3});

        assertEquals(3, table.getInt(1, 0));
        assertEquals(5, table.getInt(2, 0));
        assertEquals(5.3, table.getDouble(0, 1), 0.0000001);
    }

    @Test
    public void testAddRows() {
        ArrayTable table = ArrayTable.createWithColumnTypes(int.class, String.class);

        assertEquals(0, table.rows());
        assertEquals(2, table.columns());

        table.addRow(4, "first insert");
        assertEquals(1, table.rows());
        assertEquals(2, table.columns());

        table.addRow(3, "second insert");
        assertEquals(2, table.rows());
        assertEquals(2, table.columns());

        assertEquals(3, table.getInt(1, 0));
        assertEquals(4, table.getInt(0, 0));
        assertEquals("second insert", table.getObject(1, 1));
    }

    @Test
    public void testGrow() {
        ArrayTable table = ArrayTable.createWithColumnTypes(2, int.class, String.class);
        assertEquals(2, table.capacity());
        assertEquals(0, table.rows());
        assertEquals(2, table.columns());

        table.addRow(4, "first insert");
        assertEquals(1, table.rows());
        assertEquals(2, table.columns());
        assertEquals(2, table.capacity());

        table.addRow(3, "second insert");
        assertEquals(2, table.rows());
        assertEquals(2, table.columns());
        assertEquals(2, table.capacity());

        assertEquals(3, table.getInt(1, 0));
        assertEquals(4, table.getInt(0, 0));
        assertEquals("second insert", table.getObject(1, 1));

        table.addRow(5, null);
        assertEquals(3, table.rows());

        assertEquals(5, table.getInt(2, 0));
    }

    @Test
    public void testDeleteRow() {
        ArrayTable table = ArrayTable.createWithColumns(new int[]{1, 2, 3, 4, 5});

        assertEquals(5, table.rows());
        assertEquals(3, table.getInt(2, 0));
        table.deleteRow(2);
        assertEquals(4, table.rows());

        assertEquals(1, table.getInt(0, 0));
        assertEquals(2, table.getInt(1, 0));
        assertEquals(4, table.getInt(2, 0));
        assertEquals(5, table.getInt(3, 0));
    }

    @Test
    public void testWithInitialValues() {
        ArrayTable table = ArrayTable.createWithColumns(new int[]{1, 2, 3, 4, 5});
        assertEquals(5, table.rows());
        assertEquals(1, table.columns());
        assertTrue(table.capacity() >= 5);

        assertEquals(1, table.getInt(0, 0));
        assertEquals(2, table.getInt(1, 0));
        assertEquals(3, table.getInt(2, 0));
        assertEquals(4, table.getInt(3, 0));
        assertEquals(5, table.getInt(4, 0));
    }

    @Test
    public void testSort() {
        ArrayTable table = ArrayTable.createWithColumnsCopy(
                new int[]{3, 1, 5},
                new double[]{5.3, 8.3, 10.3},
                new String[]{"e", "a", "c"}
        );

        assertEquals("e", table.getObject(0, 2));
        assertEquals("a", table.getObject(1, 2));
        assertEquals("c", table.getObject(2, 2));

        table.sort(new ArrayTable.ChainedRowComparator.Builder(table)
                .byColumnAsc(0)
                .build());


        assertEquals(1, table.getInt(0, 0));
        assertEquals(3, table.getInt(1, 0));
        assertEquals(5, table.getInt(2, 0));

        assertEquals(8.3, table.getDouble(0, 1), 0.0000001);
        assertEquals(5.3, table.getDouble(1, 1), 0.0000001);
        assertEquals(10.3, table.getDouble(2, 1), 0.0000001);

        table.sort(new ArrayTable.ChainedRowComparator.Builder(table)
                .byColumnAsc(1)
                .build());

        assertEquals(3, table.getInt(0, 0));
        assertEquals(1, table.getInt(1, 0));
        assertEquals(5, table.getInt(2, 0));

        assertEquals(5.3, table.getDouble(0, 1), 0.0000001);
        assertEquals(8.3, table.getDouble(1, 1), 0.0000001);
        assertEquals(10.3, table.getDouble(2, 1), 0.0000001);

        table.sort(new ArrayTable.ChainedRowComparator.Builder(table)
                .byColumnAsc(2)
                .build());

        assertEquals("a", table.getObject(0, 2));
        assertEquals("c", table.getObject(1, 2));
        assertEquals("e", table.getObject(2, 2));
    }

    @Test
    public void sortTest2() {
        ArrayTable table = ArrayTable.createWithColumnTypes(int.class);

        for(int i=0;i<30;i++) {
            table.addRow(i);
        }
        table.addRow(3);
        table.addRow(5);

        table.shuffle(new Random(1));

        table.sort(new ArrayTable.ChainedRowComparator.Builder(table)
                .byColumnAsc(0)
                .build());

        int[] columnArray = (int[]) table.getColumn(0);
        assertTrue(Ordering.natural().isOrdered(Ints.asList(columnArray)));
    }

    @Test
    public void binarySearchTest() {

        //Java's binary search stops as soon as it finds the first key
        //in this case index 2 is returned. However for our purposes,
        //we want the index of the *FIRST* matching key
        int javabs = Arrays.binarySearch(new int[]{1, 3, 3, 5, 6}, 3);
        assertEquals(2, javabs);

        ArrayTable table = ArrayTable.createWithColumnsCopy(
                new int[]{1, 3, 5, 3, 6, 3}
        );
        table.sort(new ArrayTable.AscIntRowComparator(0));
        int r = table.floor(0, 3, 0, table.rows() - 1);
        assertEquals(1, r);

        r = table.ceil(0, 3, 0, table.rows() - 1);
        assertEquals(3, r);

        r = table.ceil(0, 4, 0, table.rows() - 1);
        assertEquals(-4, r);
    }

    @Test
    public void binarySearchTest2() {
        ArrayTable table = ArrayTable.createWithColumns(new int[]{1, 2, 3, 3, 3, 4, 5});

        assertEquals(4, table.ceil(0, 3, 0, table.rows()-1));
    }

    @Test
    public void MultikeySearchTest() {
        ArrayTable table = ArrayTable.createWithColumnTypes(int.class, String.class);
        for(int i=0;i<4;i++) {
            for(int j=0;j<3;j++) {
                String str = "" + (char)(65+j);
                table.addRow(i, str);
            }
        }

        ArrayTable.MultikeyBinarySearch bs = new ArrayTable.MultikeyBinarySearch.Builder()
                .addIntAsc(0)
                .addObj(1, Ordering.<String>natural().nullsFirst())
                .build();

        bs.setKey(0, 1);
        bs.setKey(1, "A");
        int i = bs.search(table);
        assertEquals(3, i);


    }
}
