package proj2;

import java.io.Serializable;

public class StringTime implements Serializable {
	public String string;
	public int clock;
	public int site;  // totally ordered
	
	public StringTime(int i, String s, int siteNo){
		string = s;
		// using clock for totally ordered Lamport clocks
		clock = i;
		site = siteNo;
	}
}
