import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.openqa.selenium.firefox.FirefoxDriver;

import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.Utils;


public class TestGwaliResultsetSize {

    private static final String TEST_CASES_PATHS_FILE = "C:\\Users\\fzhan\\code\\research\\gwali\\src\\test\\TestCases.txt";

    private static FirefoxDriver baseLineDriver;
    private static FirefoxDriver putDriver;

    private static ArrayList<Integer> allSizes;
    private static int found = 0;
    private static int nonfound = 0;
    public static final boolean STD_OUT = true;

    
    
	public static void main(String[] args) {
		
    	if(!STD_OUT){
	    	try {
				System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("GWALI_Result_Size_Eval_Output.txt")),true));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}


		
        Config.removeNonTagNodes = false;
		
        int degrees[] = {60};
        float searchRadius[] = {200};

        for (float radius: searchRadius ) {
            for (int degree: degrees ) {
            	
                Config.ANGEL_THRISHOLD = degree;
                Config.SEARCH_RADIUS =  radius;

            	found = 0;
            	nonfound = 0;
            	allSizes = new ArrayList<>();
                
		        baseLineDriver = (FirefoxDriver) Utils.getNewFirefoxDriver();
		        putDriver = (FirefoxDriver) Utils.getNewFirefoxDriver();
		        
		        
		        try {
		            String[] testCasesFileLines = readTestCasesPaths(TEST_CASES_PATHS_FILE);
		            String testCasesBasePath = testCasesFileLines[1];
		            final int FIRST_TESTCASE_LINE = 2;
		            int i = FIRST_TESTCASE_LINE;
		            while (i < testCasesFileLines.length) {
		            	
		                String baseLine = testCasesFileLines[i];
		                String PUT = testCasesFileLines[i+1];
		                int numberOfFailures = Integer.parseInt(testCasesFileLines[i+2]);
		                int failuresStartIdx = i+3;
		                int failuresEndIdx = failuresStartIdx + numberOfFailures;
		                String[] failures = Arrays.copyOfRange(testCasesFileLines,failuresStartIdx,failuresEndIdx);
		                ArrayList<String> failuresList = new ArrayList<>(Arrays.asList(failures));
		                runTestCase(i,testCasesBasePath,baseLine,PUT,failuresList);
		                i = failuresEndIdx;
		
		            }
		            DescriptiveStatistics sizeStats = new DescriptiveStatistics();
		            //System.out.println("Sizes of gwali report:");
		            for (Integer rank: allSizes) {
		                //System.out.println(rank);
		                sizeStats.addValue(rank);
		            }
                    System.out.println("setting degree: " + degree + " and radius: " + radius);
		            System.out.println("median size: " + sizeStats.getPercentile(50));
		            System.out.println("average size:" + sizeStats.getMean());
		            System.out.println("found Failures: " + found);
		            System.out.println("nonfound Failures:" + nonfound);
		            baseLineDriver.quit();
		            putDriver.quit();
		
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
            }
        }

	}
    private static String[] readTestCasesPaths(String testCasesPathsFile) throws IOException {
        Path filePath = new File(testCasesPathsFile).toPath();
        Charset charset = Charset.defaultCharset();
        List<String> stringList = Files.readAllLines(filePath, charset);
        return stringList.toArray(new String[]{});
    }
    
    private static ArrayList<String> runGwali(FirefoxDriver baseLineDriver, FirefoxDriver putDriver) {
        Gwali gwali = new Gwali(baseLineDriver,putDriver);
        ArrayList<String> gwaliResult = gwali.runGwali();
        return gwaliResult;
    }
    
    private static void runTestCase(int idx,String testCasesBasePath, String baseLine, String put, ArrayList<String> failures) {
        String baselinePagePath = "file://" + testCasesBasePath + baseLine;
        String putPagePath = "file://" + testCasesBasePath + put ;
        baseLineDriver.get(baselinePagePath);
        putDriver.get(putPagePath);
        ArrayList<String> localizationResult = runGwali(baseLineDriver,putDriver);
        
        allSizes.add(localizationResult.size());
        
        for(String groundTruth: failures){
            boolean failureReported = isElementReported(groundTruth, localizationResult);
            if (failureReported) {
            	found++;
            }
            else{
            	nonfound++;
            }
        }
    }

    private static boolean isElementReported(String groundTruth, ArrayList<String> localizationResult) {
        boolean found = false;
        groundTruth = Utils.normalizeXPATH(groundTruth);
        
        for (String reportedElement : localizationResult){
            reportedElement = Utils.normalizeXPATH(reportedElement);
            if(reportedElement.contains(groundTruth)){
                found = true;
                break;
            }
        }
        if(found == false){
            System.out.println("\t element <"+groundTruth+"> is not reported by GWALI");
            //System.out.println("\t\t reported elements");
            //for (String reportedElement : localizationResult) {
                //System.out.println("\t\t <"+reportedElement+">");
			//}
        }
        return found;
    }
}
