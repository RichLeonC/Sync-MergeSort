
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

class Semaforo {

    private int r;
    private Semaphore s;
    private static Semaforo INSTANCE = new Semaforo();
    
    public Semaforo() {
        this.r = 2;
        this.s = new Semaphore(10, true);
    }
    
    public static Semaforo getConnection(){
        return INSTANCE;
    }

    public synchronized void increment() {
            try {
                r++;
                System.out.println("Current: " + r);
            } catch (Exception ex) {
                Logger.getLogger(Semaforo.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    public void connect(){
        try{
            s.acquire();
            
            synchronized(this){
                r++;
                System.out.println("Current connection: " + r);
            }
            
           Thread.sleep(2);
            
            synchronized(this){
                r--;
                System.out.println("Current connection: " + r);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }finally{
            if(s != null){
                s.release();
            }
        }
    }

    public synchronized void decrement() {
        try {
            r--;
            System.out.println("Current: " + r);
        } catch (Exception ex) {
            Logger.getLogger(Semaforo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private synchronized boolean isAvailable() {
        return r == 1;

    }

}