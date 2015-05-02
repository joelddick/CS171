package proj2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EventThread extends Thread {
	private int siteNum;
	private int arrayNum;
	private int PORT_NO = 10000;
	public int[][] timeTable = new int[4][4];
	public List<StringTime> log = (List<StringTime>) Collections
			.synchronizedList(new ArrayList<StringTime>());
	public Set<Integer> msgs = (Set<Integer>) Collections.synchronizedSet(new HashSet<Integer>());
	private PrintWriter writer;

	public EventThread(int siteNum) {
		this.siteNum = siteNum;
		arrayNum = siteNum - 1;
	}

	public void run() {
		String dir = System.getProperty("user.dir");
		String path = dir + "/user" + String.valueOf(siteNum) + ".txt";
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String line;

			writer = new PrintWriter("output" + String.valueOf(siteNum) + ".txt");

			while ((line = br.readLine()) != null) {
				processEvent(line);
			}
			br.close();
			fr.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/*
	 * Shares log and TT with destSite
	 */
	public void share(int destSite) throws UnknownHostException, IOException {
		Socket socket = new Socket("localHost", PORT_NO+destSite);
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		outStream.flush();

		outStream.writeInt(siteNum);
		shareLog(destSite, socket, outStream);
		shareTT(destSite, socket, outStream);

		socket.close();
	}


	/*
	 * Shares TT with destSite
	 */
	public void shareTT(int destSite, Socket socket, ObjectOutputStream outStream) throws UnknownHostException, IOException {
		synchronized (timeTable) {
			outStream.writeObject(timeTable);
		}
	}


	/*
	 * Check local TimeTable for what we know that destSite knows
	 * about sites 1-4 (excluding itself)
	 *
	 * Using these clock values, create Log copy with all entries with
	 * clock value greater than TT[destSite,i]
	 */
	public void shareLog(int destSite, Socket socket, ObjectOutputStream outStream) throws UnknownHostException, IOException {
		List<StringTime> sendLog = (List<StringTime>) Collections.synchronizedList(new ArrayList<StringTime>());
		synchronized (timeTable) {
			for(StringTime st : log) {
				if(st.clock > timeTable[destSite-1][st.site-1]) {
					// If we have in our local log an event with greater clock value
					// than what destSite knows about according to OUR TT, add to sendLog

					sendLog.add(st);
				}
			}
		}

		/*
		 * Send sendLog to destSite
		 */
		outStream.writeObject(sendLog);
	}


	public void processEvent(String event) throws UnknownHostException, IOException {

		// Determine event in string length order.
		if (event.substring(0, 4).equals("Post")) {
			// Get message id.
			int msgId = Integer.valueOf(event.substring(5,
					event.indexOf(' ', 5)));
			// Get message.
			String message = event.substring(event.indexOf(' ', 5) + 1);
			// Truncate message if too long.
			if (message.length() > 140) {
				message = message.substring(0, 140);
			}

			synchronized (msgs) {
				// Add message id to set of known messages.
				msgs.add((Integer) msgId);
			}

			synchronized (timeTable) {
				// Increment time table.
				timeTable[arrayNum][arrayNum] += 1;

				// Create log entry.
				String temp = "post(" + String.valueOf(msgId) + ",\"" + message
						+ "\")";
				int time = timeTable[arrayNum][arrayNum];

				// Add log entry to log with local time.
				log.add(new StringTime(time, temp, siteNum));
			}
			writer.write("Post " + String.valueOf(msgId)+"\n");
		} else if (event.substring(0, 4).equals("Idle")) {
			int idleTime = Integer.valueOf(event.substring(5));
			try {
				Thread.sleep(idleTime * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			writer.write("Idle " + String.valueOf(idleTime) + " seconds"+"\n");
		} else if (event.substring(0, 5).equals("Share")) {
			// TODO: Implement Sharing
			int destSite = Integer.parseInt(event.substring(6));
			share(destSite);
			writer.write("Share " + String.valueOf(destSite)+"\n");
		} else if (event.substring(0, 6).equals("Delete")) {
			if(siteNum == 3){
//				System.out.println(String.valueOf(msgs));
			}
			int msgId = Integer.valueOf(event.substring(7));
			boolean update = false;
			synchronized (msgs) {
				if (msgs.contains((Integer) msgId)) {
					msgs.remove((Integer) msgId);
					update = true;
				}
			}
			if(update){
				synchronized (timeTable) {
					// Increment time table.
					timeTable[arrayNum][arrayNum] += 1;

					// Create log entry.
					String temp = "delete(" + String.valueOf(msgId) + ")";
					int time = timeTable[arrayNum][arrayNum];

					// Add log entry to log with local time.
					log.add(new StringTime(time, temp, siteNum));
				}
				writer.write("Delete " + String.valueOf(msgId)+"\n");
			}
			else{
				writer.write("Delete " + String.valueOf(msgId) + " failed"+"\n");
			}
		} else if (event.substring(0, 8).equals("ShowBlog")) {
			synchronized (msgs) {
				String temp = "Blog: ";
				for (Integer i : msgs) {
					temp = temp + String.valueOf(i) + ",";
				}
				temp = temp.substring(0, temp.length() - 1);
				writer.write(temp+"\n");
			}
		} else if (event.substring(0, 10).equals("PrintState")) {
			String tempLog = "Log: {";
			for (StringTime i : log) {
				tempLog = tempLog + i.string + ",";
			}
			if(!tempLog.substring(5, 6).equals("{")){
				tempLog = tempLog.substring(0, tempLog.length() - 1) + "}";
			}
			else {
				tempLog += "}";
			}

			String[] tempTable = new String[4];
			for (int i = 0; i < 4; i++) {
				String temp = "| ";
				synchronized (timeTable) {
					for (int j = 0; j < 4; j++) {
						temp = temp + String.valueOf(timeTable[i][j]) + " ";
					}
				}
				temp = temp.substring(0, temp.length() - 1) + "|";
				tempTable[i] = temp;
			}

			// TODO: Print temp
			writer.write(tempLog+"\n");
			for (int i = 0; i < 4; i++) {
				writer.write(tempTable[i]+"\n");
			}
		}
	}
}
