
import java.util.concurrent.Semaphore;



/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author acost
 */
public class Connection {
    private static Connection INSTANCE = new Connection();
    private int noOfConnections;
    private Semaphore semaphore = new Semaphore(10, true);
    
    private Connection(){};
    
    public static Connection getConnection(){
        return INSTANCE;
    }
    
    public void connect(){
        try{
            semaphore.acquire();
            
            synchronized(this){
                noOfConnections++;
                System.out.println("Current connection: " + noOfConnections);
                this.wait(2000);
            }
            
            
            synchronized(this){
                noOfConnections--;
                System.out.println("Current connection: " + noOfConnections);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }finally{
            if(semaphore != null){
                semaphore.release();
            }
        }
        
    }
}
