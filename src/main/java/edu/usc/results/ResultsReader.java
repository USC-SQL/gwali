package edu.usc.results;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.usc.config.Config;
import edu.usc.layoutgraph.edge.NeighborEdge;

public class ResultsReader {

	public static void main(String[] args) throws IOException{
		//mergeTwoFiles();
		readData();
	}
	
	public static void readData() throws IOException
	{
		File inFile;
		FileInputStream fileIn;
		ObjectInputStream objectIn = null;
		String fileName = Config.ANALYSIS_RESULTS_FILE; 
		AnalysisResultSummary summary = new AnalysisResultSummary();
		
		try {
			inFile = new File(fileName);

			fileIn = new FileInputStream(inFile);

			objectIn = new ObjectInputStream(fileIn);

	        while(true){
				TestCaseResult testCaseResult =  (TestCaseResult) objectIn.readObject();
				if(!testCaseResult.isExecFailed() 
						&& testCaseResult.getBaselineSize() != 47
						&& testCaseResult.getPutSize() != 47
						&& testCaseResult.getMatchRatio() > 0.95){
					summary.AddTCResult(testCaseResult);
					testCaseResult.logResultForExcel();
				}
			}
	        
        } catch(EOFException e){
        	System.out.println("reached end of file "+fileName);
        	
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		objectIn.close();
		summary.printResultSummary();
	}
	
	
	public static void mergeTwoFiles() throws IOException{
			File inFile;
			FileInputStream fileIn;
			ObjectInputStream objectIn = null;
			
			String[] files = {"./results/TCResults_1_380.ser" , "./results/TCResults_381_453.ser"}; 
			File outFile = new File("./results/TCResults_1_453.ser");
			FileOutputStream fos = new FileOutputStream(outFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			for (String fileName : files) {
				try {
					inFile = new File(fileName);

					fileIn = new FileInputStream(inFile);

				
					objectIn = new ObjectInputStream(fileIn);

			        while(true){
						TestCaseResult testCaseResult =  (TestCaseResult) objectIn.readObject();
						oos.writeObject(testCaseResult);
					}
			        
		        } catch(EOFException e){
		        	System.out.println("reached end of file "+fileName);
		        	
		        } catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				objectIn.close();
			}
			oos.close();

	}
	
	public int getRelationshipsSize(NeighborEdge edge){
		int size = 0;
		if(edge.isContains())
			size++;
		if(edge.isIntersect())
			size++;
		if(edge.isLeftEdgeAligned())
			size++;
		if(edge.isRightEdgeAligned())
			size++;
		if(edge.isTopEdgeAligned())
			size++;
		if(edge.isBottomEdgeAligned())
			size++;
		if(edge.isLeftRight())
			size++;
		if(edge.isRightLeft())
			size++;
		if(edge.isTopBottom())
			size++;
		if(edge.isBottomTop())
			size++;
		return size;
	}
}
