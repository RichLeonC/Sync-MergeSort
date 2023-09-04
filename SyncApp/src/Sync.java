//Viviana Acosta Romero 2020033583
//Melissa Alguera Castillo 2019056061
//Richard Leon Chinchilla 2019003759
//package com.mycompany.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
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
        }

       else {
            notifyAll();
            waitingThreads = 0; 
        } 
   }
}

class Semaforo {

    private int r;
    private Semaphore s;

    public Semaforo() {
        this.r = 2;
    }

    public synchronized void increment() {
        while (isAvailable()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        r++;
        this.notifyAll();
    }

    public synchronized void decrement() {
        while (!isAvailable()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        r--;
        this.notifyAll();
    }

    private synchronized boolean isAvailable() {
        return r == 1;

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
        this.semaforo = new Semaforo();
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

    public void incrementSemaforo() {
        semaforo.increment();

    }

    public void decrementSemaforo() {
        semaforo.decrement();

    }

    public void entrarMonitor() throws InterruptedException {
        monitor.entrar();

    }

    public void salirMonitor() throws InterruptedException {
        monitor.salir();

    }

}

class MergeSort{
     
      // Assuming system has 4 logical processors
    private static final int MAX_THREADS = 4;
       
    // Custom Thread class with constructors
    private static class SortThreads extends Thread{
        SortThreads(Integer[] array, int begin, int end){
            super(()->{
                MergeSort.mergeSort(array, begin, end);
            });
            this.start();
        }
    }
     
      // Perform Threaded merge sort
    public static void threadedSort(Integer[] array){
        long time = System.currentTimeMillis();
        final int length = array.length;
        boolean exact = length%MAX_THREADS == 0;
        int maxlim = exact? length/MAX_THREADS: length/(MAX_THREADS-1);
        // if workload is less and no more than 1 thread is required for work, then assign all to 1 thread
        maxlim = maxlim < MAX_THREADS? MAX_THREADS : maxlim;
        // To keep track of threads
        final ArrayList<SortThreads> threads = new ArrayList<>();
        // Since each thread is independent to work on its assigned chunk,
        // spawn threads and assign their working index ranges
        // ex: for 16 element list, t1 = 0-3, t2 = 4-7, t3 = 8-11, t4 = 12-15
        for(int i=0; i < length; i+=maxlim){
            int beg = i;
            int remain = (length)-i;
            int end = remain < maxlim? i+(remain-1): i+(maxlim-1); 
            final SortThreads t = new SortThreads(array, beg, end);
            // Add the thread references to join them later
            threads.add(t);
        }
        for(Thread t: threads){
            try{
                  // This implementation of merge requires, all chunks worked by threads to be sorted first.
                // so we wait until all threads complete
                t.join();
            } catch(InterruptedException ignored){}
        }
        // System.out.println("Merging k-parts array, where m number of parts are distinctly sorted by each Threads of available MAX_THREADS="+MAX_THREADS);
        /*
          The merge takes 2 parts at a time and merges them into 1,
          then again merges the resultant into next part and so on...until end
          For MAXLIMIT = 2 (2 elements per thread where total threads = 4, in a total of 4*2 = 8 elements)
          list1 = (beg, mid); list2 = (mid+1, end);
          1st merge = 0,0,1 (beg, mid, end)
          2nd merge = 0,1,3 (beg, mid, end)
          3rd merge = 0,3,5 (beg, mid, end)
          4th merge = 0,5,7 (beg, mid, end)
        */
        for(int i=0; i < length; i+=maxlim){
            int mid = i == 0? 0 : i-1;
            int remain = (length)-i;
            int end = remain < maxlim? i+(remain-1): i+(maxlim-1);
            // System.out.println("Begin: "+0 + " Mid: "+ mid+ " End: "+ end + " MAXLIM = " + maxlim);
            merge(array, 0, mid, end);
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent for custom multi-threaded recursive merge_sort(): "+ time+ "ms");
    }
 
    // Typical recursive merge sort
    public static void mergeSort(Integer[] array, int begin, int end){
        if (begin<end){
            int mid = (begin+end)/2;
            mergeSort(array, begin, mid);
            mergeSort(array, mid+1, end);
            merge(array, begin, mid, end);
        }
    }
    
   
     
    //Typical 2-way merge
    public static void merge(Integer[] array, int begin, int mid, int end){
        Integer[] temp = new Integer[(end-begin)+1];
         
        int i = begin, j = mid+1;
        int k = 0;
 
        // Add elements from first half or second half based on whichever is lower,
        // do until one of the list is exhausted and no more direct one-to-one comparison could be made
        while(i<=mid && j<=end){
            if (array[i] <= array[j]){
                temp[k] = array[i];
                i+=1;
            }else{
                temp[k] = array[j];
                j+=1;
            }
            k+=1;
        }
 
        // Add remaining elements to temp array from first half that are left over
        while(i<=mid){
            temp[k] = array[i];
            i+=1; k+=1;
        }
         
        // Add remaining elements to temp array from second half that are left over
        while(j<=end){
            temp[k] = array[j];
            j+=1; k+=1;
        }
 
        for(i=begin, k=0; i<=end; i++,k++){
            array[i] = temp[k];
        }
    }
    
}

public class Sync {

    public static void main(String[] args) throws InterruptedException {
        Integer[] arr = {83, 86, 77, 15, 93, 35, 86, 92, 49, 21, 
         62, 27, 90, 59, 63, 26, 40, 26, 72, 36};

        SyncLib syncLib = new SyncLib(2); // Cambia el valor según la cantidad de hilos que deseas utilizar
        MergeSort merge = new MergeSort();
        MergeSort.threadedSort(arr);
     

        System.out.println("Arreglo ordenado: " + Arrays.toString(arr));
    }
}
