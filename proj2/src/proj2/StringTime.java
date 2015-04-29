package proj2;

import java.io.Serializable;

public class StringTime implements Serializable {
	public String string;
	public int time;
	
	// maybe change from time to...
	public int clock;
	public int site;  // totally ordered
	
	public StringTime(int i, String s, int siteNo){
		string = s;
		time = i;
		
		// using clock for totally ordered Lamport clocks
		clock = i;
		site = siteNo;
	}
}
