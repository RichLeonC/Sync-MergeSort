//Viviana Acosta Romero 2020033583
//Melissa Alguera Castillo 2019056061
//Richard Leon Chinchilla 2019003759
//package com.mycompany.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

class Barrier {

    private final int totalThreads;
    private int waitingThreads = 0;

    public Barrier(int totalThreads) {
        this.totalThreads = totalThreads;
    }

    public synchronized void esperar() throws InterruptedException {
        waitingThreads++;

        if (waitingThreads < totalThreads) {
            wait();
        } else {
            notifyAll();
            waitingThreads = 0;
        }
    }
}

class Semaforo {

    private int permits;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public Semaforo(int permits) {
        this.permits = permits;
    }

    public void adquirir() throws InterruptedException {
        lock.lock();
        try {
            if (permits == 0) {
                condition.await();
            }
            permits--;
        } finally {
            lock.unlock();
        }
    }

    public void liberar() {
        lock.lock();
        try {
            permits++;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
}

class Monitor {

    private boolean ocupado = false;

    public synchronized void entrar() throws InterruptedException {
        while (ocupado) {
            wait();
        }
        ocupado = true;
    }

    public synchronized void salir() {
        ocupado = false;
        notify();
    }
}

class SyncLib {

    private Barrier barrera;
    private Semaforo semaforo;
    private Monitor monitor;

    public SyncLib(int capacity) {
        this.barrera = new Barrier(capacity);
        this.semaforo = new Semaforo(1);
        this.monitor = new Monitor();
    }

    public Barrier getBarrier() {
        return barrera;
    }

    public Semaforo getSemaforo() {
        return semaforo;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void waitBarrera() throws InterruptedException {
        barrera.esperar();
    }

    public void incrementSemaforo() throws InterruptedException {
        semaforo.adquirir();

    }

    public void decrementSemaforo() {
        semaforo.liberar();

    }

    public void entrarMonitor() throws InterruptedException {
        monitor.entrar();

    }

    public void salirMonitor() throws InterruptedException {
        monitor.salir();

    }

}

class MergeSort {

    // Assuming system has 4 logical processors
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private static final CyclicBarrier barrier = new CyclicBarrier(Runtime.getRuntime().availableProcessors());
    private static final Semaphore s = new Semaphore(Runtime.getRuntime().availableProcessors());

    // Custom Thread class with constructors
    private static class SortThreads extends Thread {

        SortThreads(Integer[] array, int begin, int end) {
            super(() -> {
                mergeSort(array, begin, end);
            });


            this.start();
        }
    }

    // Perform Threaded merge sort
    public static void threadedSort(Integer[] array) {
        System.out.println("MAX THREADS: " + MAX_THREADS);

        long time = System.currentTimeMillis();
        final int length = array.length;
        boolean exact = length % MAX_THREADS == 0;
        int maxlim = exact ? length / MAX_THREADS : length / (MAX_THREADS - 1);
        maxlim = maxlim < MAX_THREADS ? MAX_THREADS : maxlim;
        final ArrayList<SortThreads> threads = new ArrayList<>();
        for (int i = 0; i < length; i += maxlim) {
            int beg = i;
            int remain = (length) - i;
            int end = remain < maxlim ? i + (remain - 1) : i + (maxlim - 1);
            final SortThreads t = new SortThreads(array, beg, end);
            // Add the thread references to join them later
            threads.add(t);
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }
        for (int i = 0; i < length; i += maxlim) {
            int mid = i == 0 ? 0 : i - 1;
            int remain = (length) - i;
            int end = remain < maxlim ? i + (remain - 1) : i + (maxlim - 1);

            merge(array, 0, mid, end);

        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent for custom multi-threaded recursive merge_sort(): " + time + "ms");
    }

    // Typical recursive merge sort
    public static void mergeSort(Integer[] array, int begin, int end) {
        if (begin < end) {
            //  s.acquire();
            
            int mid = (begin + end) / 2;
            mergeSort(array, begin, mid);
            mergeSort(array, mid + 1, end);
            merge(array, begin, mid, end);
            //s.release();
        }

        }
        //Typical 2-way merge
    public static void merge(Integer[] array, int begin, int mid, int end) {
        Integer[] temp = new Integer[(end - begin) + 1];

        int i = begin, j = mid + 1;
        int k = 0;

        // Add elements from first half or second half based on whichever is lower,
        // do until one of the list is exhausted and no more direct one-to-one comparison could be made
        while (i <= mid && j <= end) {
            if (array[i] <= array[j]) {
                temp[k] = array[i];
                i += 1;
            } else {
                temp[k] = array[j];
                j += 1;
            }
            k += 1;
        }

        // Add remaining elements to temp array from first half that are left over
        while (i <= mid) {
            temp[k] = array[i];
            i += 1;
            k += 1;
        }

        // Add remaining elements to temp array from second half that are left over
        while (j <= end) {
            temp[k] = array[j];
            j += 1;
            k += 1;
        }

        for (i = begin, k = 0; i <= end; i++, k++) {
            array[i] = temp[k];
        }

    }

}

class MergeSortWithThreads {

    private static final SyncLib sync = new SyncLib(2);
    private static final Semaphore s = new Semaphore(2);

    //private static final CyclicBarrier barrier = new CyclicBarrier(Runtime.getRuntime().availableProcessors());
    public static void mergeSort(int[] array) throws InterruptedException {
        // sync.incrementSemaforo(); // Adquirir el semáforo antes de realizar la fusión
        //s.acquire();
        int length = array.length;
        if (length < 2) {
            return;
        }

        int mid = length / 2;
        int[] leftArray = Arrays.copyOfRange(array, 0, mid);
        int[] rightArray = Arrays.copyOfRange(array, mid, length);

        Thread leftThread = new Thread(() -> {
            try {
                mergeSort(leftArray);
            } catch (InterruptedException ex) {
                Logger.getLogger(MergeSortWithThreads.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        Thread rightThread = new Thread(() -> {
            try {
                mergeSort(rightArray);
            } catch (InterruptedException ex) {
                Logger.getLogger(MergeSortWithThreads.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        leftThread.start();
        rightThread.start();
        // sync.decrementSemaforo();

        try {
            leftThread.join();
            rightThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        merge(array, leftArray, rightArray);
    }

    public static void merge(int[] array, int[] leftArray, int[] rightArray) throws InterruptedException {
        s.acquire();

        int leftLength = leftArray.length;
        int rightLength = rightArray.length;

        int i = 0, j = 0, k = 0;

        while (i < leftLength && j < rightLength) {
            if (leftArray[i] < rightArray[j]) {
                array[k++] = leftArray[i++];
            } else {
                array[k++] = rightArray[j++];
            }
        }

        while (i < leftLength) {
            array[k++] = leftArray[i++];
        }

        while (j < rightLength) {
            array[k++] = rightArray[j++];
        }

        s.release();

    }
}

public class Sync {

    public static void main(String[] args) throws InterruptedException {
        Integer[] arr = {83, 86, 77, 15, 93, 35, 86, 92, 49, 21,
            62, 27, 90, 59, 63, 26, 40, 26, 72, 36};

        // SyncLib syncLib = new SyncLib(2); // Cambia el valor según la cantidad de hilos que deseas utilizar
        //  MergeSort merge = new MergeSort();
        MergeSort.threadedSort(arr);
        // MergeSortWithThreads.mergeSort(arr);

        System.out.println("Arreglo con Threads ordenado: " + Arrays.toString(arr));
    }
}
