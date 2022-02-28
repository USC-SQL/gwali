import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.Utils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestGwaliRanking {


    static class Fixtuple {
        String pattern;
        String replacement;

        Fixtuple(String pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }
    }
    
    private static final String TEST_CASES_PATHS_FILE_ = Config.RANK_CONFIG_FILE;

    private static final String TEMP_AUTO_FIX_FILE_NAME = "TEMP_AUTO_FIXED_FILE.html";

    private static HashMap<String, Fixtuple> fixes = new HashMap<>();

    private static FirefoxDriver baseLineDriver;
    private static FirefoxDriver putDriver;

    private static ArrayList<Integer> allRankings = new ArrayList<>();



    public static void main(String[] args) {
        Config.removeNonTagNodes = false;
        populateFixes();
        baseLineDriver = (FirefoxDriver) Utils.getNewFirefoxDriver();
        putDriver = (FirefoxDriver) Utils.getNewFirefoxDriver();
        try {
            String[] testCasesFileLines = readTestCasesPaths(TEST_CASES_PATHS_FILE_);
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
            DescriptiveStatistics rankingStats = new DescriptiveStatistics();
            System.out.println("Rankings of actual faulty elements:");
            for (Integer rank: allRankings) {
                System.out.println(rank);
                rankingStats.addValue(rank);
            }
            System.out.println("median ranking: " + rankingStats.getPercentile(50));
            System.out.println("average ranking:" + rankingStats.getMean());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTestCase(int idx,String testCasesBasePath, String baseLine, String put, ArrayList<String> failures) {
        String baselinePagePath = "file://" + testCasesBasePath + baseLine;
        String putPagePath = "file://" + testCasesBasePath + put ;

        System.out.println("Running Test Case: " + idx);
        System.out.println("baseline: "+baselinePagePath);
        System.out.println("put: "+ putPagePath);
        System.out.println("known Failures: ");
        for (String failure : failures ) {
            System.out.println(failure);
        }
        int smallestRank = -1;

        while(smallestRank < Integer.MAX_VALUE){
            baseLineDriver.get(baselinePagePath);
            putDriver.get(putPagePath);
            ArrayList<String> localizationResult = runGwali(baseLineDriver,putDriver);

            smallestRank = Integer.MAX_VALUE;
            String smallestXpath = "";
            for(String groundTruth: failures){
                int reportedRank = getReportedRank(groundTruth, localizationResult);
                if (reportedRank < smallestRank) {
                    smallestRank = reportedRank;
                    smallestXpath = groundTruth;
                }
            }
            if (smallestRank != Integer.MAX_VALUE){
                System.out.println("\t Assigned failure <"+smallestXpath+"> rank No: "+smallestRank);
                allRankings.add(smallestRank);
                System.out.println("\t Now fixing it...");
                putPagePath = fixFault(putPagePath,smallestXpath,put);
                failures.remove(smallestXpath);
                System.out.println("\t Remaining Failures:");
                for(String remainingFailure: failures){
                    System.out.println("\t\t "+remainingFailure);
                }
            }


        }

        if(failures.size() > 0){
            System.out.println("SOME FAILURES WERE NOT DETECTED!");
        }

        // REMOVE CREATED TEMP FILE....
        String putfilePath = putPagePath.replace("file://","");
        new File(putfilePath).delete();

    }

    private static int getReportedRank(String groundTruth, ArrayList<String> localizationResult) {
        // we might also use..
        //localizationResult.indexOf();
        int rank = 0;
        boolean found = false;
        groundTruth = Utils.normalizeXPATH(groundTruth);

        for (String reportedElement : localizationResult){
            reportedElement = Utils.normalizeXPATH(reportedElement);
            rank++;
            if(reportedElement.toUpperCase().contains(groundTruth.toUpperCase())){
                System.out.println("\t found <"+groundTruth+"> at line: "+rank);
                found = true;
                break;
            }
        }
        if(found == false){
            System.err.println("FAILURE <"+groundTruth+"> WAS NOT FOUND BY GWALI ... REPORTING SIZE OF LIST AS RANK");
        }
        return rank;
    }

    private static void replaceFileContent(Path filePath, Fixtuple ft) {
        String pattern = ft.pattern;
        String replacement = ft.replacement;

        Charset charset = StandardCharsets.UTF_8;

        try {
            String content = new String(Files.readAllBytes(filePath), charset);
            //first check if file contains the pattern to be fixed
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(content);
            if(!m.find()){
                System.err.println("COULD NOT FIND PATTERN <"+pattern+"> TO FIX!!");
            }
            else {
                content = content.replaceFirst(pattern, replacement);
                Files.write(filePath, content.getBytes(charset));
                System.out.println("\t\t\treplace " + pattern + " with " + replacement);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String fixFault(String pageToBeFixed, String xpath, String pagePath){
        String putfilePathString = pageToBeFixed.replace("file://","");
        Path putFilePath = Paths.get(putfilePathString);

        Path newPath = putFilePath.resolveSibling(TEMP_AUTO_FIX_FILE_NAME);
        if(!putFilePath.endsWith(TEMP_AUTO_FIX_FILE_NAME)) {
            try {
                Files.copy(putFilePath, newPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Fixtuple ft = getFixTuple(pagePath, xpath);

        replaceFileContent(newPath, ft);

        return "file://"+newPath.toString();
    }

    private static Fixtuple getFixTuple(String pagePath, String xpath) {
        return fixes.get(pagePath+xpath);
    }


    private static void populateFixes() {


        fixes.put("ScrapBook/data/linkedin-test/index.html"+"html/body/footer/div/div/div[3]/div/ul[1]/descendant::text()",  new Fixtuple("Pequeñas&nbsp;empresas&nbsp;","P"));
        fixes.put("ScrapBook/data/linkedin-test/index.html"+"html/body/footer/div/div/div[3]/div/nav/ul[2]/descendant::text()",  new Fixtuple("Universidades","Univ"));

        fixes.put("ScrapBook/data/googleEarth-test/index.html"+"html/body/div[5]/div[1]/div/div/div[1]/p[2]/descendant::text()",new Fixtuple("Сообщество\\s*Google Планета Земля","Сообщество"));
        fixes.put("ScrapBook/data/googleEarth-test/index.html"+"html/body/div[5]/div[1]/div/div/div[2]/p[2]/descendant::text()",new Fixtuple("Некоммерческих организаций","H"));

        fixes.put("ScrapBook/data/caLottery-test/index.html"+"html/body/form/div[5]/div/header/ul/li[6]/p[2]/descendant::text()",new Fixtuple("Iniciar sesión<\\/a>", "S</a>"));

        fixes.put("ScrapBook/data/rentalCars-test/index.html"+"html/body/div[8]/div/div[2]",new Fixtuple("Warum über rentalcars\\.com buchen\\?","über rentalcars.com buchen?"));

        fixes.put("ScrapBook/data/ixigo-test/index.html"+"html/body/div[3]/div[3]/div[5]/div[1]/div[1]/ul/descendant::text()",new Fixtuple("aggiungi il tuo hotel","aggiungi"));
        fixes.put("ScrapBook/data/ixigo-test/index.html"+"html/body/div[3]/div[3]/div[1]/div/div[2]/div[1]/div[3]/div[2]/div[1]/form/div[3]/div/div[1]/div[1]/div/div[1]/input/@value",new Fixtuple("input value=\"inserisci una città o un aeroporto\" autocomplete=\"off\" tabindex=\"4\"","input value=\"inserisci\" autocomplete=\"off\" tabindex=\"4\""));
        fixes.put("ScrapBook/data/ixigo-test/index.html"+"html/body/div[3]/div[3]/div[1]/div/div[2]/div[1]/div[3]/div[2]/div[1]/form/div[3]/div/div[1]/div[1]/div/div[2]/input/@value",new Fixtuple("input value=\"inserisci una città o un aeroporto\" autocomplete=\"off\" tabindex=\"5\"","input value=\"inserisci\" autocomplete=\"off\" tabindex=\"5\""));

        fixes.put("ScrapBook/data/qualitrol-test/index.html"+"html/body/form/div[3]/div[2]/div[1]/div/div[2]/ul/descendant::text()",new Fixtuple("Решения для промышленности","Ре"));

        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/footer/div/div/section/div/div[3]/div/descendant::text()",new Fixtuple("MyPlay Direct © 2015","M"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[1]/div[4]/div/div[1]/text()",new Fixtuple("http://www.myplaydirect.com/kiss\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://www.myplaydirect.com/kiss\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[2]/div[4]/div/div[1]/text()",new Fixtuple("http://ghostbustersstore.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://ghostbustersstore.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[3]/div[4]/div/div[1]/text()",new Fixtuple("http://www.popmarket.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://www.popmarket.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[4]/div[4]/div/div[1]/text()",new Fixtuple("http://friendstvshop.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://friendstvshop.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[5]/div[4]/div/div[1]/text()",new Fixtuple("http://www.breakingbadstore.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://www.breakingbadstore.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[6]/div[4]/div/div[1]/text()",new Fixtuple("http://www.myplaydirect.com/whitney-houston/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://www.myplaydirect.com/whitney-houston/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[7]/div[4]/div/div[1]/text()",new Fixtuple("http://shop.legendary.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://shop.legendary.com/\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[8]/div[4]/div/div[1]/text()",new Fixtuple("http://www.michaeljackson.com/us/store\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://www.michaeljackson.com/us/store\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));
        fixes.put("ScrapBook/data/mplay-test/index.html"+"html/body/div[1]/div[1]/div[1]/section/div[3]/div/section/div/div/div[2]/div[9]/div[4]/div/div[1]/text()",new Fixtuple("http://www.asapmobshop.com/home/all\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div>\\s+<div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para recibir alertas de la tienda","http://www.asapmobshop.com/home/all\" target=\"_top\">Tienda de Official</a></h3></div></span>  </div> <div class=\"views-field views-field-field-newsletter-list-id\">        <div class=\"field-content\"><div class=\"brand-detail-row newsletter-button\">Regístrese para"));

        fixes.put("ScrapBook/data/hightail-test/index.html"+"html/body/div[2]/div[2]/div[1]/div[4]/div/div/div/form/div[1]/div[3]/div[2]/p[2]/input/@value",new Fixtuple("Geben Sie Ihren Vor- und Nachnamen an \\(optional\\)","Geben Sie "));

        fixes.put("ScrapBook/data/flynas-test/index.html"+"html/body/form/div[18]/div[2]/div/div[4]/div/div/div/fieldset[4]/div[1]/span[3]/text()",new Fixtuple("11 Yaş üzeri","11 Yaş"));
        fixes.put("ScrapBook/data/flynas-test/index.html"+"html/body/form/div[18]/div[2]/div/descendant::text()",new Fixtuple("Rezervasyon Yapın","Rezervasyon"));

        fixes.put("ScrapBook/data/worldsBest-test/index.html"+"html/body/div[1]/ul/li[3]/a/font/font/text()",new Fixtuple("class=\"hyperlink_link hyperlink_link_1177022\" alt=\"Auszeichnungen\" href=\"#\" data-href=\"awards\"><font><font>Auszeichnungen","class=\"hyperlink_link hyperlink_link_1177022\" alt=\"Auszeichnungen\" href=\"#\" data-href=\"awards\"><font><font>Auszeic"));
        fixes.put("ScrapBook/data/worldsBest-test/index.html"+"html/body/div[1]/ul/li[9]/a/font/font/text()",new Fixtuple("class=\"hyperlink_link hyperlink_link_1177134\" alt=\"Medienzentrum\" href=\"#\" data-href=\"media-centre\"><font><font>Medienzentrum","class=\"hyperlink_link hyperlink_link_1177134\" alt=\"Medienzentrum\" href=\"#\" data-href=\"media-centre\"><font><font>Med"));
        fixes.put("ScrapBook/data/worldsBest-test/index.html"+"html/body/div[1]/div[4]/div/div[8]/ul/li[1]/p[3]/descendant::text()",new Fixtuple("tritt in die Fußstapfen des letztjährigen Siegers Azurmendi in Larrabetzu","tritt"));
        fixes.put("ScrapBook/data/worldsBest-test/index.html"+"html/body/div[1]/div[4]/div/div[8]/ul/li[4]/p/descendant::text()",new Fixtuple("dass die Jahre verbrachte er leitet die Küche an lokales Wahrzeichen Tetsuya die mehr","dass"));
        fixes.put("ScrapBook/data/worldsBest-test/index.html"+"html/body/div[1]/div[6]/ul/li[3]/a/font/font/text()",new Fixtuple("class=\"hyperlink_link hyperlink_link_1449052\" alt=\"Auszeichnungen\" href=\"#\" data-href=\"awards\"><font><font>Auszeichnungen","class=\"hyperlink_link hyperlink_link_1449052\" alt=\"Auszeichnungen\" href=\"#\" data-href=\"awards\"><font><font>Auszeic"));
        fixes.put("ScrapBook/data/worldsBest-test/index.html"+"html/body/div[1]/div[6]/ul/li[9]/a/font/font/text()",new Fixtuple("class=\"hyperlink_link hyperlink_link_1449134\" alt=\"Medienzentrum\" href=\"#\" data-href=\"media-centre\"><font><font>Medienzentrum","class=\"hyperlink_link hyperlink_link_1449134\" alt=\"Medienzentrum\" href=\"#\" data-href=\"media-centre\"><font><font>Med"));


        fixes.put("ScrapBook/data/museum-test/index.html"+"html/body/div[2]/div/footer/div/section[2]/div/form/span[2]/font/font/input",new Fixtuple("INSCRIVEZ-VOUS MAINTENANT","INSCRIVEZ"));
        fixes.put("ScrapBook/data/museum-test/index.html"+"html/body/div[2]/div/div[3]/nav/ul/descendant::text()",new Fixtuple("Notre recherche|Apprendre et Enseigner","it"));
        fixes.put("ScrapBook/data/museum-test/index.html"+"html/body/div[2]/div/div[3]/div[6]/div[2]/article[1]/section/a",new Fixtuple("Commencez à explorer","Commencez"));
        fixes.put("ScrapBook/data/museum-test/index.html"+"html/body/div[2]/div/div[3]/div[6]/div[2]/article[4]/section[1]/h1/descendant::text()",new Fixtuple("de cadeaux uniques","de"));
        fixes.put("ScrapBook/data/museum-test/index.html"+"html/body/div[2]/div/div[3]/div[6]/div[2]/article[4]/section[2]/h1/descendant::text()",new Fixtuple("avec les experts","avec"));


        fixes.put("ScrapBook/data/doctor-test/index.html"+"html/body/div[7]/div[1]/div[1]/ul/descendant::text()",new Fixtuple("financiamiento</a>","fin</a>"));

        fixes.put("ScrapBook/data/akamai-test/index.html"+"html/body/div[1]/div[3]/footer/div/p[1]/span[5]/descendant::text()",new Fixtuple("Privacidad y Políticas|Acerca de nuestros Anuncios","it"));

        fixes.put("ScrapBook/data/skype-test/index.html"+"html/body/div[1]/section[1]/div/form/span/input/@placeholder",new Fixtuple("Rechercher dans","R"));

        fixes.put("ScrapBook/data/els-test/index.html"+"html/body/div[2]/div[2]/div/div[3]/div[3]/h3/text()",new Fixtuple("Contactar um conselheiro","Contactar"));
        fixes.put("ScrapBook/data/els-test/index.html"+"html/body/div[2]/div[1]/div[2]/div/div[8]/div/div[2]/a/text()",new Fixtuple("Escolha o campus certo da ELS","Escolha"));

        fixes.put("ScrapBook/data/twitterHelp-test/index.html"+"html/body/div/nav[2]/div/div/div/div/div/ul/descendant::text()",new Fixtuple("Мобильные устройства и приложения","М"));

        fixes.put("ScrapBook/data/skyScanner-test/index.html"+"html/body/div[2]/div[3]/div[1]/div/div/section/form/section[1]/fieldset[1]/div[2]/input/@placeholder",new Fixtuple("Masukkan negara, bandar atau lapangan terbang","Masukkan negara"));

        fixes.put("ScrapBook/data/googleLogin-test/index.html"+"html/body/div/div[2]/div[2]/div[1]/form/div[1]/div/div[1]/div/div/input[1]/@placeholder",new Fixtuple("Εισαγάγετε τη διεύθυνση ηλεκτρονικού ταχυδρομείου σας","Εισαγάγετε "));

        fixes.put("ScrapBook/data/facebookLogin-test/index.html"+"html/body/div/div[2]/div[1]/div/div[1]/div/div/div[2]/div[2]/div/div/div/form[1]/div[1]/div[2]/div/div/div/text()",new Fixtuple("Адрес на електронна поща или мобилен номер","Адрес на електронна"));

        fixes.put("ScrapBook/data/westin-test/index.html"+"html/body/div[5]/div[1]/div[2]/div[1]/div/div[2]/div/ul/descendant::text()",new Fixtuple("Westin Well-Being","Westin"));

        fixes.put("ScrapBook/data/dmv-test/index.html"+"html/body/div[2]/div[4]/a[4]/span/text()",new Fixtuple("Información","Info"));
        fixes.put("ScrapBook/data/dmv-test/index.html"+"html/body/div[2]/div[5]/form[3]/div/descendant::text()",new Fixtuple("id=\"head_srch_l_lbl\">Este sitio","id=\"head_srch_l_lbl\">Este"));

        fixes.put("ScrapBook/data/designSponge-test/index.html"+"html/body/header/div[3]/div[2]/ul/descendant::text()",new Fixtuple("VIDA Y NEGOCIOS","VIDA"));

        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[4]/div/div/div/div/div/div[2]/form/fieldset[1]/input/@placeholder",new Fixtuple("digo postal o el aeropuerto","digo"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[2]/a/div[1]/div[1]/div[3]/text()",new Fixtuple("San Antonio Northwest - Medical Center","San Antonio Northwest"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[6]/a/div[1]/div[1]/div[3]/text()",new Fixtuple("Downtown \\(centro\\) - SoHo - Financial District \\(distrito financiero\\) zona","Downtown - SoHo - Financial District"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[10]/a/div[1]/div[1]/div[3]/text()",new Fixtuple("Londres Gatwick LGW zona","Londres"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[1]/a/div[1]/div[2]/div[2]/text()",new Fixtuple("MXN&nbsp;1,090","M&nbsp;1,090"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[2]/a/div[1]/div[2]/div[2]/text()",new Fixtuple("MXN&nbsp;1,849","M&nbsp;1,849"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[4]/a/div[1]/div[2]/div[2]/text()",new Fixtuple("MXN&nbsp;1,698","M&nbsp;1,698"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[5]/a/div[1]/div[2]/div[2]/text()",new Fixtuple("MXN&nbsp;2,030","M&nbsp;2,030"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[6]/a/div[1]/div[2]/div[2]/text()",new Fixtuple("MXN&nbsp;2,025","M&nbsp;2,025"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[7]/a/div[1]/div[2]/div[2]/text()",new Fixtuple("MXN&nbsp;1,118","M&nbsp;1,118"));
        fixes.put("ScrapBook/data/hotwire-test/index.html"+"html/body/div[4]/div[6]/div/ul/li[8]/a/div[1]/div[2]/div[2]/text()",new Fixtuple("MXN&nbsp;1,831","M&nbsp;1,831"));

    }

    private static String[] readTestCasesPaths(String testCasesPathsFile) throws IOException {
        Path filePath = new File(testCasesPathsFile).toPath();
        System.out.println(filePath);
        Charset charset = Charset.defaultCharset();
        List<String> stringList = Files.readAllLines(filePath, charset);
        System.out.println(stringList);
        return stringList.toArray(new String[]{});
    }

    private static ArrayList<String> runGwali(FirefoxDriver baseLineDriver, FirefoxDriver putDriver) {
        Gwali gwali = new Gwali(baseLineDriver,putDriver);
        ArrayList<String> gwaliResult = gwali.runGwali();
        System.out.println("\t\t Gwali Output:");
        for (String xpath: gwaliResult ) {
            System.out.println("\t\t\t" + xpath);
        }

        return gwaliResult;
    }



}
