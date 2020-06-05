import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.Utils;

import org.openqa.selenium.firefox.FirefoxDriver;

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
import java.util.List;

/**
 * Created by alameer on 3/29/17.
 * this class is used to run GWALI in test cases and compute its precision and recall.
 * We need to make sure that it is within the acceptable limit (i.e. 91% precision and 100% recall)
 * whenever we make change to the logic of GWALI, this should be executed!
 *
 */


public class TestGwaliAccuracy {

    public static final String TEST_CASES_PATHS_FILE = "/home/alameer/workspace/gwaliTestCases/RQ1TestCases";
    public static final boolean STD_OUT = true;


    // Compute Detection Accuracy
    public static void main(String[] args){
    	if(!STD_OUT){
	    	try {
				System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("GWALI_Detection_Eval_Output.txt")),true));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	Config.strictMode = false;
        Config.normalizePath = false;
        int degrees[] = {60};
        float searchRadius[] = {200};

        for (float radius: searchRadius ) {
            for (int degree: degrees ) {
                Config.ANGEL_THRISHOLD = degree;
                Config.SEARCH_RADIUS =  radius;


                int TP = 0;
                int TN = 0;
                int FP = 0;
                int FN = 0;

                FirefoxDriver baseLineDriver = (FirefoxDriver) Utils.getNewFirefoxDriver();
                FirefoxDriver putDriver = (FirefoxDriver) Utils.getNewFirefoxDriver();

                try {
                    String[] testCases = readTestCasesPaths(TEST_CASES_PATHS_FILE);
                    String testCasesBasePath = testCases[1];
                    final int FIRST_TESTCASE_LINE = 2;

                    for (int i = FIRST_TESTCASE_LINE; i < testCases.length; i += 3) {
                        String baseLineURL = "file://" + testCasesBasePath + testCases[i];
                        String putURL = "file://" + testCasesBasePath + testCases[i + 1];
                        boolean expectedResult = Boolean.parseBoolean(testCases[i + 2]);

                        baseLineDriver.get(baseLineURL);
                        putDriver.get(putURL);

                        ArrayList<String> reportedIssues = runGwali(baseLineDriver, putDriver);
                        int noOfInconsistancy = reportedIssues.size();
                        boolean result = (noOfInconsistancy > 0);



                        System.out.print("Test Cases #"+(i+1)+":");
                        System.out.println("expected output: "+expectedResult+" - GWALI output: "+result);

                        if (expectedResult == true && result == true)
                            TP++;
                        if (expectedResult == false && result == true){
                            FP++;
                            System.out.println("false postive - reported elements by GWALI:");
                            System.out.println(reportedIssues);
                        }
                        if (expectedResult == true && result == false)
                            FN++;
                        if (expectedResult == false && result == false)
                            TN++;
                    }
                    System.out.println("setting degree: " + degree + " and radius: " + radius);
                    System.out.println("Here are some stats: ");
                    System.out.println("true +:" + TP);
                    System.out.println("true -:" + TN);
                    System.out.println("false +:" + FP);
                    System.out.println("false -:" + FN);
                    System.out.println("precision:" + (TP * 1.0 / (TP * 1.0 + FP * 1.0)));
                    System.out.println("recall   :" + (TP * 1.0 / (TP * 1.0 + FN * 1.0)));

                    baseLineDriver.quit();
                    putDriver.quit();
                    
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }
    }


    private static ArrayList<String> runGwali(FirefoxDriver baseLineDriver, FirefoxDriver putDriver) {
        Gwali gwali = new Gwali(baseLineDriver,putDriver);
        return gwali.runGwali();
    }


    private static String[] readTestCasesPaths(String testCasesPathsFile) throws IOException {
        Path filePath = new File(testCasesPathsFile).toPath();
        Charset charset = Charset.defaultCharset();
        List<String> stringList = Files.readAllLines(filePath, charset);
        String[] stringArray = stringList.toArray(new String[]{});
        return stringArray;
    }

}
