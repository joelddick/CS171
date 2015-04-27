package proj2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServeThread extends Thread {
	private int PORT_NO = 5000;
	private int myPort;
	private int siteNum;
	private Socket socket = null;
	ServerSocket serverSocket = null;
	private ObjectInputStream inStream = null;
	private boolean isRunning = false;

	public ServeThread(int siteNum) {
		this.siteNum = siteNum;
		this.isRunning = true;
		this.myPort = PORT_NO + siteNum;
	}

	@Override
	public void run() {
		// Start listening
		try {
			listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Listen and receive time tables and other information from other
	 * sites. Process these using synchronized objects.
	 */
	public void listen() throws IOException {
		
		/*
		 * While site hasn't been shut down, listen
		 */
		while (isRunning) {
			serverSocket = new ServerSocket(this.myPort);
			
			try {
				socket = serverSocket.accept();
				System.out.println("Connected");
				
				/*
				 * Receive time table
				 */
				inStream = new ObjectInputStream(socket.getInputStream());
				int[][] timeTable = new int[4][4];
				timeTable = (int[][]) inStream.readObject();
				
				/*
				 * Receive log
				 */
				inStream = new ObjectInputStream(socket.getInputStream());
				ArrayList<StringTime> log = new ArrayList<StringTime>();
				log = (ArrayList<StringTime>) inStream.readObject();
				
				/*
				 * Update timetable and log
				 */
				update(timeTable);
				update(log);
				
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	
	/*
	 * Synchronously update TimeTable
	 */
	public synchronized void update(int[][] timeTable) {
		// TODO Update EventThread.timeTable
		
	}
	
	
	/*
	 * Synchronously update log
	 */
	public synchronized void update(ArrayList<StringTime> log) {
		// TODO Update EventThread.log
		
	}
	
	
}





