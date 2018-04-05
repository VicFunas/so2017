package multithread;

public class threads implements Runnable {

    public void run() {
//        System.out.println("Hello from a thread!");
    	String fisico = "0";
    	//fisico = Integer.toHexString(Integer.parseInt(fisico, 16)+1);
    	for (int i = 0; i < 255; i++) {
    		fisico = String.format("%3S",Integer.toString(Integer.parseInt(fisico, 16)+1, 16)).replace(' ','0');
    		System.out.println(fisico);	
    		
    	}
    }

    public static void main(String args[]) {
        (new Thread(new threads())).start();
    }

}
