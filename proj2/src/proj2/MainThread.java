package proj2;

public class MainThread{
	
	private Thread[] eThread = new Thread[4];
	private Thread[] sThread = new Thread[4];
	
	public MainThread(){
		for (int i = 0; i < 4; i++){
			eThread[i] = new EventThread(i + 1);
			sThread[i] = new ServeThread(i + 1, (EventThread) eThread[i]);
			
			sThread[i].start();
			eThread[i].start();
		}
		
		for (int i = 0; i < 4; i++){
			try {
				eThread[i].join();
//				System.out.println("Thread " + String.valueOf(i) + " joined.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
	public static void main(String[] args){
		MainThread t = new MainThread();
	}
}
