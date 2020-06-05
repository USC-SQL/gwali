package edu.usc.main;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import edu.usc.config.Config;
import edu.usc.issuesfilter.LayoutIssuesFilterProcessor;
import edu.usc.layoutgraph.LayoutGraphBuilder;
import edu.usc.layoutissue.Issue;
import edu.usc.layoutissue.NodeSuspiciousness;
import edu.usc.layoutissue.SuspiciousnessScoreEvaluator;
import edu.usc.performance.TimeMeasurement;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;



public class Main {


	public static CommandLine readArgs(String[] args){
        Options options = new Options();
        Option ignore = Option.builder("i")
	      	    .required(false)
	      	    .longOpt("ignore")
	      	    .hasArg()
	      	    .optionalArg(false)
	      	    .desc("file containing list of the xpaths for nodes to be ignored")
	      	    .build();
        
        Option measureTime = Option.builder("m")
	      	    .required(false)
	      	    .longOpt("measureTime")
	      	    .hasArg()
	      	    .optionalArg(false)
	      	    .desc("file where the time measurement will be outputed")
	      	    .build();
        
        Option baseLine = Option.builder("b")
          	    .required(true)
           	    .longOpt("baseline")
           	    .hasArg(true)
           	    .optionalArg(false)
           	    .desc("url of the baseline page")
           	    .build();
        
        Option pageUnderTest = Option.builder("t")
         	    .required(true)
          	    .longOpt("pageundertesting")
          	    .hasArg(true)
          	    .optionalArg(false)
          	    .desc("url of the page under test")
          	    .build();
        
        Option detectionOutput = Option.builder("d")
        	    .required(true)
         	    .longOpt("detection")
         	    .hasArg(true)
         	    .optionalArg(false)
         	    .desc("file where the detection output will be written")
         	    .build();
        
        Option localizationOutput = Option.builder("l")
       	    .required(true)
        	    .longOpt("localization")
        	    .hasArg(true)
        	    .optionalArg(false)
        	    .desc("file where the localization output will be written")
        	    .build();

        options.addOption(baseLine);
        options.addOption(pageUnderTest);
        options.addOption(detectionOutput);
        options.addOption(localizationOutput);
        options.addOption(ignore);
        options.addOption(measureTime);
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
			cmd = parser.parse( options, args);
        
        
        
        } catch (ParseException e) {
        	// automatically generate the help statement
        	System.err.println("something went wrong when parsing args");
        	HelpFormatter formatter = new HelpFormatter();
        	formatter.printHelp( "LayoutTesting", options );
        	System.exit(0);
		}
		return cmd;
	}



	public static void main(String[] args){
		try {
			
			//start measuring time for loading the browser
			//end of measuring (i.e. toc()) is inside lt.checkLayoutGraphs()
			//TimeMeasurement.tic("loadingbrowser");

			
			Config.applyConfig();
			CommandLine cmd = readArgs(args);
			String baseline = cmd.getOptionValue("b");
			String PUT = cmd.getOptionValue("t");
			String detectOutFileName = cmd.getOptionValue("d");
			String localizeOutFileName = cmd.getOptionValue("l");
			
			File detectOutputFile = new File(detectOutFileName);
			PrintWriter detectOut = new PrintWriter(new FileWriter(detectOutputFile));
			
			File localizationOutputfile = new File(localizeOutFileName);
			PrintWriter localizeOut = new PrintWriter(new FileWriter(localizationOutputfile));

			
			LayoutGraphBuilder lt = new LayoutGraphBuilder(baseline,PUT);
			
			ArrayList<Issue> potentialLayoutIssues = lt.compareLayoutGraphs();
			
			//TimeMeasurement.toc();
			
			//TimeMeasurement.tic("FilteringPotentialIssues");
			

			
			
			System.out.println("Operation done.. potential Layout issues: " + potentialLayoutIssues.size() + " non filtered issues");
			
			
			//filter issues
			LayoutIssuesFilterProcessor filter = new LayoutIssuesFilterProcessor();
			ArrayList<Issue> filteredLayoutIssues = filter.filterissues(potentialLayoutIssues);
			//filter issues

			
			if(filteredLayoutIssues.size() > 0)
				detectOut.append("true\n");
			else
				detectOut.append("false\n");
			detectOut.close();

			
			//start: compute suspiciousness and add LCA for direction issues
			SuspiciousnessScoreEvaluator suspiciousnessComputer = new SuspiciousnessScoreEvaluator(filteredLayoutIssues);
			ArrayList<NodeSuspiciousness> nodesSuspiciousness = suspiciousnessComputer.computeSuspiciousnesses();
			//end: compute suspiciousness

			
			System.out.println("nodes suspiciousness:");
			for (NodeSuspiciousness ns : nodesSuspiciousness) {
				localizeOut.append(ns.toString()+"\n");
				System.out.println(ns);
			}
			localizeOut.close();
			//TimeMeasurement.toc();
			if(cmd.hasOption("m")){
				String timeMeasurementFileName = cmd.getOptionValue("m");
				File timeMeasurementFile = new File(timeMeasurementFileName);
				PrintWriter timeMeasurementOut = new PrintWriter(new FileWriter(timeMeasurementFile));
				timeMeasurementOut.append(TimeMeasurement.getMeasurementResults());
				timeMeasurementOut.append("\n");
				timeMeasurementOut.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
