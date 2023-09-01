//Viviana Acosta Romero 2020033583
//Melissa Alguera Castillo 2019056061
//Richard Leon Chinchilla 2019003759
package com.mycompany.sync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Semaphore;

class Barrier {

    private int capacity;
    private Semaphore s, exclude, counter;

    public Barrier(int capacity) {
        this.capacity = capacity;
        counter = new Semaphore(0);
        s = new Semaphore(0);
        exclude = new Semaphore(1);

    }

    public void espera() throws InterruptedException {
        exclude.acquire();
        if (counter.availablePermits() < capacity - 1) {
            counter.release();
            exclude.release();
            s.acquire();
        } else {
            exclude.release();
            System.out.println("RE;EASE ALL");
            for (int i = 0; i < capacity; i++) {
                s.release();
            }
        }
    }
}

class Semaforo {

    private int r;
    private Semaphore s;

    public Semaforo() {
        this.r = 1;
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
    
    private synchronized boolean isAvailable(){
        return r==1;
        
    }

}

class Monitor{
    synchronized void displayCounting(int n){
        for (int i = 0; i <= n; i++) {
            System.out.println(i);
        }
        try{
            Thread.sleep(500);
        }catch(Exception e){
            System.out.println(e);
        }
    }
}


abstract class Mutex{
    public abstract void espera()throws InterruptedException;
}


class SyncLib {

    private Barrier barrera;
    private Semaforo semaforo;
    private Monitor monitor;
    
    public Barrier getBarrier(){
        return barrera;
    }
    public Semaforo getSemaforo(){
        return semaforo;
    }
    public Monitor getMonitor(){
        return monitor;
    }

    public SyncLib(int capacity) {
       this.barrera = new Barrier(capacity);
       this.semaforo = new Semaforo();
       this.monitor = new Monitor(); 
    }



}

class MergeSort extends Thread{
    
    private static void sort(ArrayList<Integer> a,int threads){
       
    }
}

public class Sync {

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
