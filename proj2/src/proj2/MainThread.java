package proj2;

public class MainThread{
	
	private Thread[] eThread = new Thread[4];
	private Thread[] sThread = new Thread[4];
	
	public MainThread(){
		for (int i = 0; i < 4; i++){
			eThread[i] = new EventThread(i + 1);
			//sThread[i] = new ServeThread(i + 1);
			
			//sThread[i].start();
			eThread[i].start();
		}
		
		for (int i = 0; i < 4; i++){
			try {
				eThread[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		MainThread t = new MainThread();
	}
}
