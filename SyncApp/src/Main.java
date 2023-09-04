
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

/**
 *
 * @author acost
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ExecutorService executorService = null;
        try {
            executorService = Executors.newCachedThreadPool();
            
            for (int i = 1; i <= 300; i++) {
                executorService.submit(new Runnable(){
                    @Override
                    public void run(){
                        Connection.getConnection().connect();
                    }
                });
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }finally{
            if(executorService != null){
                executorService.shutdown();
            }
        }
    }
    
}
