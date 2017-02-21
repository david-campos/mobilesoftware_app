package com.campos.david.appointments;

public abstract class Util {
    /**
     * This function takes last element as pivot, places the pivot element at its correct
     * position in sorted array, and places all smaller (smaller than pivot) to left of
     * pivot and all greater elements to right of pivot
     */
    private static int partition(String[] arr, String[] arr1, int l, int h) {
        String x = arr[h];
        int i = (l - 1);
        for (int j = l; j <= h - 1; j++) {
            if (compare(arr[j], x) <= 0) {
                i++;
                // swap arr[i] and arr[j]
                swap(arr, arr1, i, j);
            }
        }
        // swap arr[i+1] and arr[h]
        swap(arr, arr1, i + 1, h);
        return (i + 1);
    }

    private static int compare(String str0, String str1) {
        byte[] strB0 = str0.getBytes();
        byte[] strB1 = str1.getBytes();
        for (int i = 0; i < str0.length(); i++) {
            if (strB0[i] < strB1[i])
                return -1;
            else if (strB0[i] > strB1[i])
                return +1;
        }
        if (strB0.length == strB1.length) {
            return 0;
        } else if (strB0.length > strB1.length) {
            return +1;
        } else {
            return -1;
        }
    }

    private static <T> void swap(T arr[], T arr1[], int i, int j) {
        T t = arr[i];
        T t1 = arr1[i];
        arr[i] = arr[j];
        arr1[i] = arr1[j];
        arr[j] = t;
        arr1[j] = t1;
    }


    /**
     * Sorts arr0 using iterative quick sort and changes arr1 at the same time
     *
     * @param arr0 String[]
     * @param arr1 String[]
     * @param l    int
     * @param h    int
     */
    public static void quickSort(String[] arr0, String[] arr1, int l, int h) {
        // create auxiliary stack
        int stack[] = new int[h - l + 1];
        // initialize top of stack
        int top = -1;
        // push initial values in the stack
        stack[++top] = l;
        stack[++top] = h;
        // keep popping elements until stack is not empty
        while (top >= 0) {
            // pop h and l
            h = stack[top--];
            l = stack[top--];
            // set pivot element at it's proper position
            int p = partition(arr0, arr1, l, h);
            // If there are elements on left side of pivot,
            // then push left side to stack
            if (p - 1 > l) {
                stack[++top] = l;
                stack[++top] = p - 1;
            }
            // If there are elements on right side of pivot,
            // then push right side to stack
            if (p + 1 < h) {
                stack[++top] = p + 1;
                stack[++top] = h;
            }
        }
    }
}
