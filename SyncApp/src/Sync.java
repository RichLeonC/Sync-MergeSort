//Viviana Acosta Romero 2020033583
//Melissa Alguera Castillo 2019056061
//Richard Leon Chinchilla 2019003759
//package com.mycompany.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class Barrier {

    private final int totalThreads;
    private int waitingThreads = 0;

    public Barrier(int totalThreads) {
        this.totalThreads = totalThreads;
    }

    //Coloca todos los hilos en espera hasta que se llegue al contador esperado
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
 
    //Se utiliza para adiquirir un permiso del semaforo, si no hay permisos, el hilo que
    //llama a este metodo se bloqueara hasta que se libere un permiso
    public synchronized void adquirir() throws InterruptedException {
        lock.lock();
        try {
            if (permits == 0) {
                wait();
            }
            permits--;
        } finally {
            lock.unlock();
        }
    }

    //Sirve para liberar un permiso del seamaforo, incrementa el contador de permisos disponibles
    //luego con el notify desbloquea cualquier hilo que este en espera.
    public synchronized void liberar() {
        lock.lock();
        try {
            permits++;
            notifyAll();   
        } finally {
            lock.unlock();
        }
    }
}

class Monitor {

    private boolean ocupado = false;
    
    //Si esta ocupado, pone el hilo en espera
    public synchronized void entrar() throws InterruptedException {
        if (ocupado) {
            wait();
        }
        ocupado = true;
    }

    //Si no, desbloquea el hilo
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
        this.semaforo = new Semaforo(capacity);
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

    public void adquirir() throws InterruptedException {
        semaforo.adquirir();

    }

    public void liberar() {
        semaforo.liberar();

    }

    public void entrarMonitor() throws InterruptedException {
        monitor.entrar();

    }

    public void salirMonitor() throws InterruptedException {
        monitor.salir();

    }

}


//Algoritmo Merge Sort obtenido de GeeksforGeeks y adaptado al ejercicio requerido con la threads y la biblioteca sync
//Link: https://www.geeksforgeeks.org/merge-sort-using-multi-threading/
class MergeSort {

    
    private static final int MAX_THREADS = 2;
    private static final SyncLib sync = new SyncLib(MAX_THREADS);

    private static class SortThreads extends Thread {

        private Integer[] array;
        private int begin, end;

        SortThreads(Integer[] array, int begin, int end) {
            this.array = array;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void run() {
            
            try {
                sync.adquirir(); // Adquirir el semáforo
                mergeSort(array, begin, end);
                sync.liberar(); // Liberar el semáforo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void threadedSort(Integer[] array) {
        long time = System.currentTimeMillis();
        final int length = array.length;
        final ArrayList<SortThreads> threads = new ArrayList<>();
        for (int i = 0; i < MAX_THREADS; i++) {
            int begin = i * (length / MAX_THREADS);
            int end = (i + 1) * (length / MAX_THREADS) - 1;
            final SortThreads t = new SortThreads(array, begin, end);
            threads.add(t);
            t.start();
        }

        for (SortThreads t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }

        for (int i = 1; i < MAX_THREADS; i++) {
            merge(array, 0, i * (length / MAX_THREADS) - 1, (i + 1) * (length / MAX_THREADS) - 1);
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Time spent for custom multi-threaded merge_sort(): " + time + "ms");
    }

    public static void mergeSort(Integer[] array, int begin, int end) {
        if (begin < end) {
            int mid = (begin + end) / 2;
            mergeSort(array, begin, mid);
            mergeSort(array, mid + 1, end);
            merge(array, begin, mid, end);
        }
    }

    public static void merge(Integer[] array, int begin, int mid, int end) {
        Integer[] temp = new Integer[(end - begin) + 1];

        int i = begin, j = mid + 1;
        int k = 0;

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

        while (i <= mid) {
            temp[k] = array[i];
            i += 1;
            k += 1;
        }
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

public class Sync {

    public static Integer[] generateRandomArray(int length) {
        Integer[] array = new Integer[length];
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            array[i] = random.nextInt(1000); // Números aleatorios entre 0 y 999
        }
        return array;
    }

    public static void main(String[] args) throws InterruptedException {
        Integer[] arr = {83, 86, 77, 15, 93, 35, 86, 92, 49, 21,
            62, 27, 90, 59, 63, 26, 40, 26, 72, 36};
        Integer[] arr2 = generateRandomArray(10000);
        MergeSort.threadedSort(arr);

        System.out.println("Arreglo con Threads ordenado: " + Arrays.toString(arr));
    }
}
