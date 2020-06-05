package edu.usc.performance;

import java.util.ArrayList;

public class TimeMeasurement {
	public static ArrayList<TimeStamp> execTimes = new ArrayList<TimeStamp>();
	public static class TimeStamp{
		public long start;
		public long end;
		public String phaseName;
		public TimeStamp(long start, String phaseName) {
			this.start = start;
			this.phaseName = phaseName;
		}
		
	}
	//Start measuring new phase time
	public static void tic(String phaseName){
		execTimes.add(new TimeStamp(System.currentTimeMillis(), phaseName));
	}
	
	//End measuring the time of last added phase
	public static void toc(){
		if(execTimes.size() > 0)
			execTimes.get(execTimes.size()-1).end = System.currentTimeMillis();
		else{
			System.err.println("need to tic before toc");
		}
	}
	
	//print measurement results in milliseconds
	public static String getMeasurementResults(){
		StringBuilder sb = new StringBuilder();
		for (TimeStamp timeStamp : execTimes) {
			long duration = timeStamp.end - timeStamp.start;
			sb.append("TimeFor:" + timeStamp.phaseName + "= " + duration + " ms\n");
		}
		return sb.toString();
	}

	public static void reset() {
		execTimes = new ArrayList<TimeStamp>();
	}

}
