import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.Utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.HashSet;
import java.io.IOException;
import java.util.HashMap;

class Subject {
    List<String> LTRFaultElements;
    String LTRTime;
    List<String> RTLFaultElements = new ArrayList<>();
    String RTLTime;
}

/**
 * Created by Fan Zhang on 2/12/2022.
 */
public class RunTestOnAllSubjects {

    public static void main(String[] args) {

        String basePath = Config.SUBJECTS_FOLDER;
        File folder = new File(basePath);
        String[] contents = folder.list();

        HashSet<String> fileNames = new HashSet<String>();

        assert contents != null;
        for (String file : contents) {
            int lastHyphenIdx = file.lastIndexOf("-");
            if (lastHyphenIdx != -1) {
                String newFile = file.substring(0, lastHyphenIdx);
                fileNames.add(newFile);
            }
        }


        HashMap<String, Subject> faultElements = new HashMap<>();



        for (String fileName : fileNames) {
            String refPath = "file:///" + basePath + fileName + "-ref/index.html";
            String testPath = "file:///" + basePath + fileName + "-test/index.html";


            Subject subject = new Subject();


            // Test the subject with LTR config
            Config.CHECK_RTL = false;

            FirefoxDriver refDriver = Utils.getNewFirefoxDriver();
            FirefoxDriver testDriver = Utils.getNewFirefoxDriver();

            refDriver.get(refPath);
            testDriver.get(testPath);


            Gwali gwali = new Gwali(refDriver, testDriver);
            List<String> LTRFaultElements = gwali.runGwali();
            subject.LTRTime = gwali.printIssues();

            subject.LTRFaultElements = LTRFaultElements;

            refDriver.quit();
            testDriver.quit();


            // Test the subject with RTL config
            Config.CHECK_RTL = true;

            refDriver = Utils.getNewFirefoxDriver();
            testDriver = Utils.getNewFirefoxDriver();

            refDriver.get(refPath);
            testDriver.get(testPath);

            gwali = new Gwali(refDriver, testDriver);
            subject.RTLFaultElements = gwali.runGwali();
            subject.RTLTime = gwali.printIssues();

            refDriver.quit();
            testDriver.quit();

            faultElements.put(fileName, subject);
        }

        try (CSVPrinter printer = new CSVPrinter(new FileWriter("faulty_elements.csv"), CSVFormat.EXCEL)) {
            printer.printRecord("FileName", "LTRFaultyElements", "LTRFaultyElementsSize", "LTRTime", "RTLFaultyElements", "RTLFaultyElementsLength", "RTLTime");
            faultElements.forEach((name, subject) -> {
                try {
                    printer.printRecord(name, subject.LTRFaultElements, subject.LTRFaultElements.size(), subject.LTRTime,
                                              subject.RTLFaultElements, subject.RTLFaultElements.size(), subject.RTLTime);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
