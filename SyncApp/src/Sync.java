//Viviana Acosta Romero 2020033583
//Melissa Alguera Castillo 2019056061
//Richard Leon Chinchilla 2019003759
//package com.mycompany.sync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Semaphore;

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

class MergeSort extends Thread {

    public MergeSort(){}
    public static void mergeSort(int[] arr, int inicio, int fin, SyncLib syncLib) {
    if (inicio < fin) {
        int medio = (inicio + fin) / 2;

        // Dividir la lista en dos
        mergeSort(arr, inicio, medio, syncLib);
        mergeSort(arr, medio + 1, fin, syncLib);

        // Fusionar las dos sublistas
        merge(arr, inicio, medio, fin, syncLib);
    }
}

    public static void parallelMergeSort(int[] arr, int inicio, int fin, SyncLib sync) {
        if (inicio < fin) {
            int medio = (inicio + fin) / 2;

            Thread leftThread = new Thread(() -> {
                parallelMergeSort(arr, inicio, medio, sync);
            });

            Thread rightThread = new Thread(() -> {
                parallelMergeSort(arr, medio + 1, fin, sync);
            });

            leftThread.start();
            rightThread.start();

            try {
                leftThread.join();
                rightThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            //System.out.println("Llego a la llamada del merge");
            
            try {
                sync.waitBarrera();
                sync.decrementSemaforo();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } 
           merge(arr, inicio, medio, fin,sync);
        }
    }

    private static void merge(int[] arr, int inicio, int medio, int fin, SyncLib sync) {
      //  System.out.println("Entro al merge");
        int n1 = medio - inicio + 1;
        int n2 = fin - medio;

        int[] left = new int[n1];
        int[] right = new int[n2];

        for (int i = 0; i < n1; i++) {
            left[i] = arr[inicio + i];
        }
        for (int i = 0; i < n2; i++) {
            right[i] = arr[medio + 1 + i];
        }

        int i = 0, j = 0;
        int k = inicio;

        while (i < n1 && j < n2) {
            if (left[i] <= right[j]) {
                arr[k] = left[i];
                i++;
            } else {
                arr[k] = right[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            arr[k] = left[i];
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = right[j];
            j++;
            k++;
        }

        // Sincroniza antes de salir
//        try {
//            sync.waitBarrera();
//            sync.decrementSemaforo();
//            
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

}

public class Sync {

    public static void main(String[] args) {
        int []array = {5,3,8,2,7,4,1};
        
        int threads = Runtime.getRuntime().availableProcessors();
        
        SyncLib sync = new SyncLib(threads);
        
        MergeSort merge = new MergeSort();
        System.out.println("Antes del parallel");
        merge.parallelMergeSort(array, 0, array.length-1, sync);
        
        System.out.println("Hola");
        for (int i:array) {
            System.out.println(i+" ");
        }
    }
}
