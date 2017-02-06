package edu.gatech.xpert;

public class X {
	public static final boolean DEBUG = true;
	public static final boolean DEBUG_LAYOUT = false;

	public static void debug(String message) {
		if(DEBUG){
			System.out.println(message);
		}
	}
}
