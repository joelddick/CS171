package proj2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServeThread extends Thread {
	private int PORT_NO = 10000;
	private int myPort;
	private int siteNum;
	private Socket socket = null;
	ServerSocket serverSocket = null;
	private ObjectInputStream inStream = null;
	private boolean isRunning = false;
	private boolean checkNow = false;
	private EventThread parentThread;

	public ServeThread(int siteNum, EventThread et) {
		this.siteNum = siteNum;
		this.isRunning = true;
		this.myPort = PORT_NO + siteNum;
		parentThread = et;
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
		serverSocket = new ServerSocket(this.myPort);
		while (isRunning) {


			try {
				socket = serverSocket.accept();
				System.out.println("Connected");

				/*
				 * Receive log
				 */
				inStream = new ObjectInputStream(socket.getInputStream());

				int sourceSite = inStream.readInt();

				List<StringTime> log = new ArrayList<StringTime>();
				log = (List<StringTime>) inStream.readObject();

				/*
				 * Receive time table
				 */
				//inStream = new ObjectInputStream(socket.getInputStream());
				int[][] timeTable = new int[4][4];
				timeTable = (int[][]) inStream.readObject();


				/*
				 * Update timetable and log
				 */
				update(sourceSite, timeTable);
				update(log);
				
				/*
				 * Garbage collect
				 */
//				checkNow = !checkNow;
//				if(checkNow) {
					gargabeCollect();
//				}


			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	
	/*
	 * Synchronously garbage collect
	 */
	public synchronized void gargabeCollect() {
		
		int clearUntil = Integer.MAX_VALUE;
		
		// Find lowest common clock value
		synchronized (parentThread.timeTable) {
			for(int i = 0; i < 4; ++i) {
				if(parentThread.timeTable[i][siteNum-1] < clearUntil) {
					clearUntil = parentThread.timeTable[i][siteNum-1];
				}
			}
		}
		
		
		
		// use this clock value to garbage collect
		if(clearUntil != 0) {
			System.out.println("Clearing up until " + clearUntil + " at site " + this.siteNum);
			synchronized (parentThread.log) {
				for(int i = 0; i < parentThread.log.size(); ++i) {
					if(parentThread.log.get(i).clock <= clearUntil) {
						System.out.println("Removing " + parentThread.log.get(i).string + " at site "+this.siteNum);
						parentThread.log.remove(i);
					}
					
				}
			}
		}
		
	}
	

	/*
	 * Synchronously update TimeTable
	 */
	public synchronized void update(int sourceSite, int[][] timeTable) {
		// TODO Update EventThread.timeTable
		synchronized (parentThread.timeTable) {
			int[][] eventTimeTable = parentThread.timeTable;

			for(int i = 0; i < 4; i++){
				for(int j = 0; j < 4; j++){
					eventTimeTable[i][j] = Math.max(eventTimeTable[i][j], timeTable[i][j]);
				}
			}

			for(int k = 0; k < 4; k++){
				eventTimeTable[siteNum - 1][k] = Math.max(eventTimeTable[siteNum - 1][k], timeTable[sourceSite - 1][k]);
			}

			parentThread.timeTable = eventTimeTable;
		}
	}


	/*
	 * Synchronously update log
	 */
	public synchronized void update(List<StringTime> log) {
		// TODO Update EventThread.log
		synchronized (parentThread.log) {
			for(StringTime lst : log){
				boolean copy = true;
				for(StringTime rst : parentThread.log){
					if(lst.clock == rst.clock && lst.site == rst.site){
						copy = false;
					}
				}
				if(copy){
					parentThread.log.add(lst);
					synchronized (parentThread.msgs) {
						if(lst.string.substring(0, 4).equals("Post")){
							parentThread.msgs.add(Integer.valueOf(lst.string.substring(lst.string.indexOf("(") + 1, lst.string.indexOf(","))));
						}
						else if (lst.string.substring(0, 6).equals("Delete")){
							parentThread.msgs.remove(Integer.valueOf(lst.string.substring(lst.string.indexOf("(") + 1, lst.string.indexOf(")"))));
						}
					}
				}
			}
		}
	}


}





