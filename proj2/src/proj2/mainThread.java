package proj2;

public class MainThread{
	
	public static void main(String[] args){
		(new EventThread(1)).start();
		(new EventThread(2)).start();
		(new EventThread(3)).start();
		(new EventThread(4)).start();
	}
}
