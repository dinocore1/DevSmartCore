package com.devsmart;


import com.google.common.collect.Ordering;

import java.lang.reflect.Array;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class ArrayTable {

    private static final int DEFAULT_INITIAL_SIZE = 16;

    private interface SwapImpl {
        void swap(Object array, int i, int j);
    }

    private static SwapImpl INT_SWAP = new SwapImpl() {
        @Override
        public void swap(Object array, int i, int j) {
            int temp = Array.getInt(array, i);
            Array.setInt(array, i, Array.getInt(array, j));
            Array.setInt(array, j, temp);
        }
    };

    public static SwapImpl LONG_SWAP = new SwapImpl() {
        @Override
        public void swap(Object array, int i, int j) {
            long temp = Array.getLong(array, i);
            Array.setLong(array, i, Array.getLong(array, j));
            Array.setLong(array, j, temp);
        }
    };

    private static SwapImpl FLOAT_SWAP = new SwapImpl() {
        @Override
        public void swap(Object array, int i, int j) {
            float temp = Array.getFloat(array, i);
            Array.setFloat(array, i, Array.getFloat(array, j));
            Array.setFloat(array, j, temp);
        }
    };

    private static SwapImpl DOUBLE_SWAP = new SwapImpl() {
        @Override
        public void swap(Object array, int i, int j) {
            double temp = Array.getDouble(array, i);
            Array.setDouble(array, i, Array.getDouble(array, j));
            Array.setDouble(array, j, temp);
        }
    };

    private static SwapImpl BOOLEAN_SWAP = new SwapImpl() {
        @Override
        public void swap(Object array, int i, int j) {
            boolean temp = Array.getBoolean(array, i);
            Array.setBoolean(array, i, Array.getBoolean(array, j));
            Array.setBoolean(array, j, temp);
        }
    };

    private static SwapImpl OBJ_SWAP = new SwapImpl() {
        @Override
        public void swap(Object array, int i, int j) {
            Object temp = Array.get(array, i);
            Array.set(array, i, Array.get(array, j));
            Array.set(array, j, temp);
        }
    };

    public interface RowComparator {
        int compare(ArrayTable table, int rowa, int rowb);
    }

    public interface Function<T> {
        T transform(ArrayTable t, int row);
    }

    public static class AscIntRowComparator implements RowComparator {

        private final int mColumn;

        public AscIntRowComparator(int column) {
            mColumn = column;
        }

        @Override
        public int compare(ArrayTable table, int rowa, int rowb) {
            return table.getInt(rowa, mColumn) - table.getInt(rowb, mColumn);
        }

        @Override
        public String toString() {
            return String.format("%d ASC", mColumn);
        }
    }

    public static class DescIntRowComparator implements RowComparator {

        private final int mColumn;

        public DescIntRowComparator(int column) {
            mColumn = column;
        }

        @Override
        public int compare(ArrayTable table, int rowa, int rowb) {
            return table.getInt(rowb, mColumn) - table.getInt(rowa, mColumn);
        }

        @Override
        public String toString() {
            return String.format("%d DESC", mColumn);
        }
    }

    public static class AscDoubleRowComparator implements RowComparator {

        private final int mColumn;

        public AscDoubleRowComparator(int column) {
            mColumn = column;
        }

        @Override
        public int compare(ArrayTable table, int rowa, int rowb) {
            return Double.compare(table.getDouble(rowa, mColumn), table.getDouble(rowb, mColumn));
        }

        @Override
        public String toString() {
            return String.format("%d ASC", mColumn);
        }
    }

    public static class DescDoubleRowComparator implements RowComparator {

        private final int mColumn;

        public DescDoubleRowComparator(int column) {
            mColumn = column;
        }

        @Override
        public int compare(ArrayTable table, int rowa, int rowb) {
            return Double.compare(table.getDouble(rowb, mColumn), table.getDouble(rowa, mColumn));
        }

        @Override
        public String toString() {
            return String.format("%d DESC", mColumn);
        }
    }

    public static class ObjRowComparator<T> implements RowComparator {

        private final int mColumn;
        private final Comparator<T> mComparator;

        public ObjRowComparator(int column, Comparator<T> comparator) {
            mColumn = column;
            mComparator = comparator;
        }

        @Override
        public int compare(ArrayTable table, int rowa, int rowb) {
            T objA = table.getObject(rowa, mColumn);
            T objB = table.getObject(rowb, mColumn);
            return mComparator.compare(objA, objB);
        }

        public Comparator getComparator() {
            return mComparator;
        }
    }

    public static class StringRowComparator extends ObjRowComparator<String> {

        public StringRowComparator(int column) {
            super(column, Ordering.<String>natural()
                    .nullsFirst());
        }
    }

    public static class ChainedRowComparator implements RowComparator {

        public static class Builder {

            private final ArrayTable mRefTable;
            private ArrayList<RowComparator> mSteps = new ArrayList<RowComparator>();

            public Builder(ArrayTable refTable) {
                mRefTable = refTable;
            }

            public Builder add(RowComparator step) {
                mSteps.add(step);
                return this;
            }

            public Builder byColumnAsc(int column) {
                if (mRefTable.mColumnTypes[column] == int.class) {
                    add(new AscIntRowComparator(column));
                } else {
                    add(new ObjRowComparator(column, Ordering.natural()));
                }

                return this;
            }

            public Builder byColumnDesc(int column) {
                if (mRefTable.mColumnTypes[column] == int.class) {
                    add(new DescIntRowComparator(column));
                } else {
                    add(new ObjRowComparator(column, Ordering.natural().reverse()));
                }

                return this;
            }

            public ChainedRowComparator build() {
                return new ChainedRowComparator(mSteps.toArray(new RowComparator[mSteps.size()]));
            }
        }

        private RowComparator[] mCompareSteps;

        public ChainedRowComparator(RowComparator... steps) {
            mCompareSteps = steps;
        }

        @Override
        public int compare(ArrayTable table, int rowa, int rowb) {
            int i = 0;
            int retval = 0;
            while (retval == 0 && i < mCompareSteps.length) {
                retval = mCompareSteps[i++].compare(table, rowa, rowb);
            }

            return retval;
        }
    }

    public static class MultikeyBinarySearch {

        public static class Builder {

            private ArrayList<BinarySearchStep> mSteps = new ArrayList<BinarySearchStep>();

            public Builder addIntAsc(int column) {
                mSteps.add(new IntBinarySearchStep(column));
                return this;
            }

            public Builder addLongAsc(int column) {
                mSteps.add(new LongBinarySearchStep(column));
                return this;
            }

            public Builder addDoubleAsc(int column) {
                mSteps.add(new DoubleBinarySearchStep(column));
                return this;
            }

            public Builder addFloatAsc(int column) {
                mSteps.add(new FloatBinarySearchStep(column));
                return this;
            }

            public Builder addObj(int column, Comparator<?> comparator) {
                mSteps.add(new ObjBinarySearchStep(column, comparator));
                return this;
            }

            public MultikeyBinarySearch build() {
                MultikeyBinarySearch retval = new MultikeyBinarySearch();
                retval.mSteps = mSteps.toArray(new BinarySearchStep[mSteps.size()]);
                return retval;
            }
        }

        private BinarySearchStep[] mSteps;

        public void setKey(int step, int key) {
            mSteps[step].setKey(key);
        }

        public void setKey(int step, long key) {
            mSteps[step].setKey(key);
        }

        public void setKey(int step, float key) {
            mSteps[step].setKey(key);
        }

        public void setKey(int step, double key) {
            mSteps[step].setKey(key);
        }

        public void setKey(int step, boolean key) {
            mSteps[step].setKey(key);
        }

        public void setKey(int step, Object key) {
            mSteps[step].setKey(key);
        }

        public int search(ArrayTable table) {
            return search(table, 0, table.rows() - 1);
        }

        public int search(ArrayTable table, int low, int hi) {

            while (low <= hi) {
                int mid = (low + hi) >>> 1;

                int r = 0;
                for (int i = 0; i < mSteps.length; i++) {
                    r = mSteps[i].compare(table, mid);
                    if (r != 0) {
                        break;
                    }
                }

                if (r < 0) {
                    low = mid + 1;
                } else if (r > 0) {
                    hi = mid - 1;
                } else {
                    return mid;
                }
            }

            return -(low + 1);
        }

    }

    public static abstract class BinarySearchStep {

        final int mColumn;

        BinarySearchStep(int column) {
            mColumn = column;
        }

        public void setKey(int key) {
        }

        public void setKey(long key) {
        }

        public void setKey(float key) {
        }

        public void setKey(double key) {
        }

        public void setKey(Object key) {
        }

        public abstract int compare(ArrayTable table, int row);
    }

    public static class IntBinarySearchStep extends BinarySearchStep {

        int mKey;

        IntBinarySearchStep(int column) {
            super(column);
        }

        @Override
        public void setKey(int key) {
            mKey = key;
        }

        @Override
        public int compare(ArrayTable table, int row) {
            final int value = table.getInt(row, mColumn);
            return Integer.compare(value, mKey);
        }
    }

    public static class LongBinarySearchStep extends BinarySearchStep {

        private long mKey;

        LongBinarySearchStep(int column) {
            super(column);
        }

        @Override
        public void setKey(long key) {
            mKey = key;
        }

        @Override
        public int compare(ArrayTable table, int row) {
            final long value = table.getLong(row, mColumn);
            return Long.compare(value, mKey);
        }
    }

    public static class DoubleBinarySearchStep extends BinarySearchStep {

        private double mKey;

        DoubleBinarySearchStep(int column) {
            super(column);
        }

        @Override
        public void setKey(double key) {
            mKey = key;
        }

        @Override
        public int compare(ArrayTable table, int row) {
            final double value = table.getDouble(row, mColumn);
            return Double.compare(value, mKey);
        }
    }

    public static class FloatBinarySearchStep extends BinarySearchStep {

        private float mKey;

        FloatBinarySearchStep(int column) {
            super(column);
        }

        @Override
        public void setKey(float key) {
            mKey = key;
        }

        @Override
        public int compare(ArrayTable table, int row) {
            final float value = table.getFloat(row, mColumn);
            return Float.compare(value, mKey);
        }
    }

    public static class ObjBinarySearchStep<T> extends BinarySearchStep {

        private T mKey;
        private Comparator<T> mComparator;

        ObjBinarySearchStep(int column, Comparator<T> comparator) {
            super(column);
            mComparator = comparator;
        }

        @Override
        public void setKey(Object key) {
            mKey = (T) key;
        }

        @Override
        public int compare(ArrayTable table, int row) {
            final T value = table.getObject(row, mColumn);
            return mComparator.compare(value, mKey);
        }
    }


    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private Class[] mColumnTypes;
    private SwapImpl[] mColumnSwapImpl;
    private String[] mColumnNames;
    private Object[] mColumns;
    private int mRows = 0;
    private int mCapacity = 0;
    private RowComparator mComparator;

    public static ArrayTable createWithColumnsCopy(Object... initialColumnValues) {
        ArrayTable retval = new ArrayTable();
        retval.mColumnTypes = new Class<?>[initialColumnValues.length];
        retval.mColumns = new Object[initialColumnValues.length];
        retval.mColumnSwapImpl = new SwapImpl[initialColumnValues.length];
        retval.mColumnNames = new String[initialColumnValues.length];

        for (int i = 0; i < initialColumnValues.length; i++) {
            Object columnArray = initialColumnValues[i];
            checkArgument(columnArray.getClass().isArray(),
                    "column " + i + " is not an array type");

            if (i == 0) {
                retval.mRows = Array.getLength(columnArray);
                retval.mCapacity = retval.mRows;
            } else {
                checkArgument(retval.mRows == Array.getLength(columnArray),
                        "column " + i + " does not contain " + retval.mRows + " rows");
            }

            retval.mColumnTypes[i] = collateColumnTypes(columnArray.getClass().getComponentType());
            retval.mColumnSwapImpl[i] = getSwapImpl(retval.mColumnTypes[i]);
            retval.mColumns[i] = Array.newInstance(retval.mColumnTypes[i], retval.mRows);
            System.arraycopy(columnArray, 0, retval.mColumns[i], 0, retval.mRows);
        }
        return retval;
    }

    public static ArrayTable createWithColumns(Object... initialColumnValues) {
        ArrayTable retval = new ArrayTable();
        retval.mColumnTypes = new Class<?>[initialColumnValues.length];
        retval.mColumns = new Object[initialColumnValues.length];
        retval.mColumnSwapImpl = new SwapImpl[initialColumnValues.length];
        retval.mColumnNames = new String[initialColumnValues.length];

        for (int i = 0; i < initialColumnValues.length; i++) {
            Object columnArray = initialColumnValues[i];
            checkArgument(columnArray.getClass().isArray(),
                    "column " + i + " is not an array type");

            if (i == 0) {
                retval.mRows = Array.getLength(columnArray);
                retval.mCapacity = retval.mRows;
            } else {
                checkArgument(retval.mRows == Array.getLength(columnArray),
                        "column " + i + " does not contain " + retval.mRows + " rows");
            }

            retval.mColumnTypes[i] = collateColumnTypes(columnArray.getClass().getComponentType());
            retval.mColumnSwapImpl[i] = getSwapImpl(retval.mColumnTypes[i]);
            retval.mColumns[i] = columnArray;
        }

        return retval;
    }

    public static ArrayTable createWithColumnTypes(int initialSize, Class<?>... types) {
        checkArgument(initialSize >= 0);
        ArrayTable retval = new ArrayTable();
        retval.mColumnTypes = new Class[types.length];
        retval.mColumnSwapImpl = new SwapImpl[types.length];
        retval.mColumns = new Object[types.length];
        retval.mColumnNames = new String[types.length];

        for (int i = 0; i < types.length; i++) {
            retval.mColumnTypes[i] = collateColumnTypes(types[i]);
            retval.mColumnSwapImpl[i] = getSwapImpl(types[i]);
            retval.mColumns[i] = Array.newInstance(types[i], initialSize);
        }

        retval.mCapacity = initialSize;

        return retval;
    }

    public static ArrayTable createWithColumnTypes(Class<?>... types) {
        return createWithColumnTypes(DEFAULT_INITIAL_SIZE, types);
    }

    private ArrayTable() {
    }

    private static Class<?> collateColumnTypes(Class<?> input) {
        if (int.class == input || Integer.class == input) {
            return int.class;
        } else if (long.class == input || Long.class == input) {
            return long.class;
        } else if (float.class == input || Float.class == input) {
            return float.class;
        } else if (double.class == input || Double.class == input) {
            return double.class;
        } else if (boolean.class == input || Boolean.class == input) {
            return boolean.class;
        } else {
            return input;
        }
    }

    private static SwapImpl getSwapImpl(Class<?> classType) {
        if (classType == int.class) {
            return INT_SWAP;
        } else if (classType == long.class) {
            return LONG_SWAP;
        } else if (classType == float.class) {
            return FLOAT_SWAP;
        } else if (classType == double.class) {
            return DOUBLE_SWAP;
        } else if (classType == boolean.class) {
            return BOOLEAN_SWAP;
        } else {
            return OBJ_SWAP;
        }
    }

    public int columns() {
        return mColumnTypes.length;
    }

    public int rows() {
        return mRows;
    }

    public Class<?> getColumnType(int column) {
        return mColumnTypes[column];
    }

    public String getColumnName(int column) {
        return mColumnNames[column];
    }

    public void setColumnName(int column, String name) {
        mColumnNames[column] = name;
    }

    public Object getColumn(int column) {
        final Class classType = mColumnTypes[column];
        if (classType == int.class) {
            return Arrays.copyOf((int[]) mColumns[column], mRows);
        } else if (classType == long.class) {
            return Arrays.copyOf((long[]) mColumns[column], mRows);
        } else if (classType == float.class) {
            return Arrays.copyOf((float[]) mColumns[column], mRows);
        } else if (classType == double.class) {
            return Arrays.copyOf((double[]) mColumns[column], mRows);
        } else if (classType == boolean.class) {
            return Arrays.copyOf((boolean[]) mColumns[column], mRows);
        } else {
            return Arrays.copyOf((Object[]) mColumns[column], mRows);
        }
    }

    /**
     * the total number of rows currently allocated.
     *
     * @return
     */
    public int capacity() {
        return mCapacity;
    }

    public void clear() {
        for (int i = 0; i < mColumnTypes.length; i++) {
            if (!mColumnTypes[i].isPrimitive()) {
                for (int j = 0; j < mRows; j++) {
                    // let GC recycle memory
                    Array.set(mColumns[i], j, null);
                }
            }
        }
        mRows = 0;
    }

    public void copy(ArrayTable copy) {
        mColumnTypes = Arrays.copyOf(copy.mColumnTypes, copy.mColumnTypes.length);
        mColumnNames = Arrays.copyOf(copy.mColumnNames, copy.mColumnNames.length);
        mColumnSwapImpl = Arrays.copyOf(copy.mColumnSwapImpl, copy.mColumnSwapImpl.length);
        mComparator = copy.mComparator;
        mRows = copy.mRows;
        mCapacity = copy.mCapacity;
        mColumns = new Object[mColumnTypes.length];
        for (int i = 0; i < mColumns.length; i++) {
            mColumns[i] = Array.newInstance(mColumnTypes[i], mCapacity);
            System.arraycopy(copy.mColumns[i], 0, mColumns[i], 0, mRows);
        }
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - mCapacity > 0) {
            grow(minCapacity);
        }
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    private void grow(int minCapacity) {
        int oldCapacity = mCapacity;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }

        for (int i = 0; i < mColumnTypes.length; i++) {
            Object newarray = Array.newInstance(mColumnTypes[i], newCapacity);
            System.arraycopy(mColumns[i], 0, newarray, 0, mRows);
            mColumns[i] = newarray;
        }

        mCapacity = newCapacity;
    }

    /**
     * adds a new row to the bottom of the table.
     *
     * @param data
     */
    public void addRow(Object... data) {
        checkArgument(data != null && data.length == mColumnTypes.length);

        ensureCapacity(mRows + 1);
        for (int i = 0; i < mColumnTypes.length; i++) {
            if (mColumnTypes[i] == int.class) {
                int[] column = (int[]) mColumns[i];
                column[mRows] = ((Number) data[i]).intValue();
            } else if (mColumnTypes[i] == long.class) {
                long[] column = (long[]) mColumns[i];
                column[mRows] = ((Number) data[i]).longValue();
            } else if (mColumnTypes[i] == float.class) {
                float[] column = (float[]) mColumns[i];
                column[mRows] = ((Number) data[i]).floatValue();
            } else if (mColumnTypes[i] == double.class) {
                double[] column = (double[]) mColumns[i];
                column[mRows] = ((Number) data[i]).doubleValue();
            } else if (mColumnTypes[i] == boolean.class) {
                boolean[] column = (boolean[]) mColumns[i];
                column[mRows] = ((Boolean) data[i]).booleanValue();
            } else {
                Object[] column = (Object[]) mColumns[i];
                column[mRows] = data[i];
            }
        }

        mRows++;
    }

    /**
     * Inserts the specified row at the specified position in this table.
     * Shifts the rows currently at that position (if any) and any subsequent
     * rows down (adds one to their indices).
     *
     * @param index index at which the specified row is to be inserted
     * @param data  row data
     */
    public void insertAt(int index, Object... data) {
        checkArgument(data != null && data.length == mColumnTypes.length);
        ensureCapacity(mRows + 1);

        for (int i = 0; i < mColumnTypes.length; i++) {
            Object column = mColumns[i];
            System.arraycopy(column, index, column, index + 1, mRows - index);

            if (mColumnTypes[i] == int.class) {
                Array.setInt(column, index, ((Number) data[i]).intValue());
            } else if (mColumnTypes[i] == long.class) {
                Array.setLong(column, index, ((Number) data[i]).longValue());
            } else if (mColumnTypes[i] == float.class) {
                Array.setFloat(column, index, ((Number) data[i]).floatValue());
            } else if (mColumnTypes[i] == double.class) {
                Array.setDouble(column, index, ((Number) data[i]).doubleValue());
            } else if (mColumnTypes[i] == boolean.class) {
                Array.setBoolean(column, index, ((Boolean) data[i]).booleanValue());
            } else {
                Array.set(column, index, data[i]);
            }
        }

        mRows++;
    }

    public void deleteRow(int index) {
        checkState(index >= 0 && index < mRows, "invalid row");

        int numMoved = mRows - index - 1;
        for (int i = 0; i < mColumnTypes.length; i++) {
            if (numMoved > 0) {
                System.arraycopy(mColumns[i], index + 1, mColumns[i], index, numMoved);
            }

            if (!mColumnTypes[i].isPrimitive()) {
                Object[] column = (Object[]) mColumns[i];
                column[mRows - 1] = null; // clear to let GC do its work
            }
        }

        mRows--;
    }

    public int getInt(int row, int column) {
        return Array.getInt(mColumns[column], row);
    }

    public void setInt(int row, int column, int value) {
        Array.setInt(mColumns[column], row, value);
    }

    public long getLong(int row, int column) {
        return Array.getLong(mColumns[column], row);
    }

    public void setLong(int row, int column, long value) {
        Array.setLong(mColumns[column], row, value);
    }

    public float getFloat(int row, int column) {
        return Array.getFloat(mColumns[column], row);
    }

    public void setFloat(int row, int column, float value) {
        Array.setFloat(mColumns[column], row, value);
    }

    public double getDouble(int row, int column) {
        return Array.getDouble(mColumns[column], row);
    }

    public void setDouble(int row, int column, double value) {
        Array.setDouble(mColumns[column], row, value);
    }

    public boolean getBoolean(int row, int column) {
        return Array.getBoolean(mColumns[column], row);
    }

    public void setBoolean(int row, int column, boolean value) {
        Array.setBoolean(mColumns[column], row, value);
    }

    public <T> T getObject(int row, int column) {
        return (T) Array.get(mColumns[column], row);
    }

    public void setObject(int row, int column, Object value) {
        Array.set(mColumns[column], row, value);
    }

    public int floor(int column, int key, int min, int max) {
        int[] columnArray = (int[]) mColumns[column];
        return floorBinarySearch(columnArray, key, min, max);
    }

    public int ceil(int column, int key, int min, int max) {
        int[] columnArray = (int[]) mColumns[column];
        return ceilBinarySearch(columnArray, key, min, max);
    }

    public int binarySearch(int column, int key) {
        int[] columnArray = (int[]) mColumns[column];
        return Arrays.binarySearch(columnArray, 0, mRows, key);
    }

    private int floorBinarySearch(int[] array, final int key, int low, int hi) {
        while (low < hi) {
            //int mid = low + (hi - low) / 2;
            int mid = (low + hi) >>> 1;

            if (array[mid] < key) {
                low = mid + 1;
            } else {
                hi = mid;
            }
        }

        return low == hi && array[low] == key ? low : -(low + 1);
    }

    private int ceilBinarySearch(int[] array, final int key, int low, int hi) {
        while (low < hi) {
            //int mid = low + (hi - low) / 2;
            int mid = (low + hi + 1) >>> 1;

            if (array[mid] > key) {
                hi = mid - 1;
            } else {
                low = mid;
            }
        }

        return low == hi && array[hi] == key ? low : -(low + 1);
    }

    public int floor(int column, double key, int min, int max) {
        double[] columnArray = (double[]) mColumns[column];
        return floorBinarySearch(columnArray, key, min, max);
    }

    public int ceil(int column, double key, int min, int max) {
        double[] columnArray = (double[]) mColumns[column];
        return ceilBinarySearch(columnArray, key, min, max);
    }

    public int binarySearch(int column, double key) {
        double[] columnArray = (double[]) mColumns[column];
        return Arrays.binarySearch(columnArray, 0, mRows, key);
    }

    private int floorBinarySearch(double[] array, final double key, int low, int hi) {
        while (low < hi) {
            //int mid = low + (hi - low) / 2;
            int mid = (low + hi) >>> 1;

            if (array[mid] < key) {
                low = mid + 1;
            } else {
                hi = mid;
            }
        }

        return low == hi && array[low] == key ? low : -(low + 1);
    }

    private int ceilBinarySearch(double[] array, final double key, int low, int hi) {
        while (low < hi) {
            //int mid = low + (hi - low) / 2;
            int mid = (low + hi + 1) >>> 1;

            if (array[mid] > key) {
                hi = mid - 1;
            } else {
                low = mid;
            }
        }

        return low == hi && array[hi] == key ? low : -(low + 1);
    }

    public <T> int floor(int column, T key, int min, int max, Comparator<T> comparator) {
        T[] columnArray = (T[]) mColumns[column];
        return floorBinarySearch(columnArray, key, min, max, comparator);
    }

    private <T> int ceil(int column, T key, int min, int max, Comparator<T> comparator) {
        T[] columnArray = (T[]) mColumns[column];
        return ceilBinarySearch(columnArray, key, min, max, comparator);
    }

    private <T> int floorBinarySearch(T[] array, final T key, int low, int hi, Comparator<T> comparator) {
        while (low < hi) {
            //int mid = low + (hi - low) / 2;
            int mid = (low + hi) >>> 1;

            if (comparator.compare(array[mid], key) < 0) {
                low = mid + 1;
            } else {
                hi = mid;
            }
        }

        return low == hi && comparator.compare(array[low], key) == 0 ? low : -(low + 1);
    }

    private <T> int ceilBinarySearch(T[] array, final T key, int low, int hi, Comparator<T> comparator) {
        while (low < hi) {
            //int mid = low + (hi - low) / 2;
            int mid = (low + hi + 1) >>> 1;

            if (comparator.compare(array[mid], key) > 0) {
                hi = mid - 1;
            } else {
                low = mid;
            }
        }

        return low == hi && comparator.compare(array[low], key) == 0 ? low : -(low + 1);
    }

    public void swap(int rowi, int rowj) {
        for (int i = 0; i < mColumnTypes.length; i++) {
            mColumnSwapImpl[i].swap(mColumns[i], rowi, rowj);
        }
    }

    /**
     * sort rows based on {@code comparator}
     *
     * @param comparator
     */
    public void sort(RowComparator comparator) {
        mComparator = comparator;
        resort();
    }

    /**
     * shuffle rows randomly.
     *
     * @param random
     */
    public void shuffle(Random random) {
        //using Fisher-Yates shuffle algorithm
        for (int i = mRows - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            swap(i, j);
        }
    }

    /**
     * shuffle the rows randomly.
     */
    public void shuffle() {
        shuffle(new Random());
    }

    /**
     * sorts all the rows again using the same RowComparator set previously
     * with {@code sort()}
     *
     * @see ArrayTable#sort(RowComparator)
     */
    public void resort() {
        if (mComparator != null) {
            heapSort();
        }
    }

    private void heapSort() {
        int n = mRows - 1;

        heapify();
        for(int i=n;i>0;i--){
            swap(0, i);
            n = n - 1;
            maxheap(0, n);
        }
    }

    private void heapify() {
        int n = mRows - 1;
        for(int i=n/2;i>=0;i--) {
            maxheap(i, n);
        }
    }

    private void maxheap(int i, int n) {

        int left = 2*i;
        int right = 2*i + 1;
        int max = i;
        if(left <= n && mComparator.compare(this, left, i) > 0) {
            max = left;
        }

        if(right <=n && mComparator.compare(this, right, max) > 0) {
            max = right;
        }

        if(max != i) {
            swap(i, max);
            maxheap(max, n);
        }
    }


    @Override
    public String toString() {
        final int MAX_DISPLAY_ROWS = 5;
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < Math.min(mRows, MAX_DISPLAY_ROWS); i++) {
            buf.append("[");
            for (int j = 0; j < mColumnTypes.length; j++) {
                if (mColumnTypes[j] == int.class) {
                    buf.append(String.format("%-6d ", getInt(i, j)));
                } else if (mColumnTypes[j] == double.class) {
                    buf.append(String.format("%-6.4g ", getDouble(i, j)));
                } else {
                    buf.append(String.format("%-6.6s ", getObject(i, j)));
                }
            }

            buf.append("],\n");
        }

        if (mRows > MAX_DISPLAY_ROWS) {
            buf.append("...");
        }

        return buf.toString();
    }

    public <T> Iterable<T> iterate(final Function<T> f) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {

                    int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < rows();
                    }

                    @Override
                    public T next() {
                        T retval = f.transform(ArrayTable.this, i);
                        i++;
                        return retval;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove is not supported");
                    }
                };
            }
        };
    }
}
