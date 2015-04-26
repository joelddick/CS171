package proj2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.omg.PortableInterceptor.USER_EXCEPTION;

public class EventThread extends Thread{
	private int siteNum;
	private int arrayNum;
	private int[][] timeTable = new int[4][4];
	private ArrayList<StringTime> log = new ArrayList<StringTime>();
	private HashSet<Integer> msgs = new HashSet<Integer>();
	
	public EventThread(int siteNum){
		this.siteNum = siteNum;
		arrayNum = siteNum - 1;
	}
	
	public void run(){
		String dir = System.getProperty("user.dir");
		String path = dir + "\\user" + String.valueOf(siteNum) + ".txt";
		try{
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String line;
			
			while ((line = br.readLine()) != null){
				processEvent(line);
			}
			br.close();
			fr.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void processEvent(String event){
		// Determine event in string length order.
		if (event.substring(0, 4).equals("Post")){
			// Get message id.
			int msgId = Integer.valueOf(event.substring(5, event.indexOf(' ', 5)));
			// Get message.
			String message = event.substring(event.indexOf(' ', 5) + 1);
			// Truncate message if too long.
			if (message.length() > 140){
				message = message.substring(0, 140);
			}
			
			// Add message id to set of known messages.
			msgs.add((Integer) msgId);
			
			// Increment time table.
			timeTable[arrayNum][arrayNum] += 1;
			
			// Create log entry.
			String temp = "Post(" + String.valueOf(msgId) + ",\"" + message + "\")";
			int time = timeTable[arrayNum][arrayNum];
			
			// Add log entry to log with local time.
			log.add(new StringTime(time, temp));
		}
		else if (event.substring(0, 4).equals("Idle")){
			int idleTime = Integer.valueOf(event.substring(5));
			try {
				Thread.sleep(idleTime * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else if (event.substring(0, 5).equals("Share")){
			int userId = Integer.valueOf(event.substring(6));
			
			// TODO: Implement Sharing
		}
		else if (event.substring(0, 6).equals("Delete")){
			int msgId = Integer.valueOf(event.substring(7));
			
			if (msgs.contains((Integer) msgId)){
				msgs.remove((Integer) msgId);
			}
			
			// Increment time table.
			timeTable[arrayNum][arrayNum] += 1;
						
			// Create log entry.
			String temp = "Delete(" + String.valueOf(msgId) + ")";
			int time = timeTable[arrayNum][arrayNum];
						
			// Add log entry to log with local time.
			log.add(new StringTime(time, temp));
		}
		else if (event.substring(0, 8).equals("ShowBlog")){
			String temp = "Blog: ";
			for (Integer i : msgs){
				temp = temp + String.valueOf(i) + ",";
			}
			temp = temp.substring(0, temp.length() - 1);
			
			// TODO: Print temp
			System.out.println(temp);
		}
		else if (event.substring(0, 10).equals("PrintState")){
			String tempLog = "Log: {";
			for (StringTime i : log){
				tempLog = tempLog + i.string + ",";
			}
			tempLog = tempLog.substring(0, tempLog.length()-1) + "}";
			
			String[] tempTable = new String[4];
			for (int i = 0; i < 4; i++){
				String temp = "|";
				for (int j = 0; j < 4; j++){
					temp = temp + String.valueOf(timeTable[i][j]) + " ";
				}
				temp = temp.substring(0, temp.length() - 1) + "|";
				tempTable[i] = temp;
			}
			
			// TODO: Print temp
			System.out.println(tempLog);
			for (int i = 0; i < 4; i++){
				System.out.println(tempTable[i]);
			}
		}
	}
}
