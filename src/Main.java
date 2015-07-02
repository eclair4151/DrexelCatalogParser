import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.io.*;

public class Main {


    public static void main(String[] args) throws IOException {

        Connection connection = null;
        Connection connectionBrief = null;

        try {


            connection = DriverManager.getConnection("jdbc:sqlite:DrexelClassdb.sqlite3");
            connectionBrief = DriverManager.getConnection("jdbc:sqlite:DrexelClassBriefdb.sqlite3");
            Statement statement = connection.createStatement();
            Statement statementBrief = connectionBrief.createStatement();

            statement.executeUpdate("drop table if exists DrexelClass");
            statementBrief.executeUpdate("drop table if exists DrexelClass");
            statement.executeUpdate("create table DrexelClass (id integer, courseType string, courseNum string, courseTitle string, courseCredits string, courseDescription string, courseCollege string, courseRepeatStatus, coursePrereqs string, courseRestrictions string, courseCoReqs string, courseTermType string, courseStudentType string)");
            statementBrief.executeUpdate("create table DrexelClass (courseType string, courseNum string, courseTitle string, courseCredits string)");

            List<DrexelClass> allclasses = new ArrayList<>(0);

            allclasses.addAll(loadClassesFromCatalog("quarter", "undergrad"));
            allclasses.addAll(loadClassesFromCatalog("quarter", "grad"));
            allclasses.addAll(loadClassesFromCatalog("semester", "undergrad"));
            allclasses.addAll(loadClassesFromCatalog("semester", "grad"));
            allclasses.addAll(genUnivClasses());


            System.out.println("sorting classes");
            Collections.sort(allclasses);


            System.out.println("writing classes to database");
            for (int i = 0; i < allclasses.size(); i++) {
                statement.executeUpdate("insert into DrexelClass values(" + (i + 1) + ", " + allclasses.get(i).getSQLinsert() + ")");
                statementBrief.executeUpdate("insert into DrexelClass values(" + allclasses.get(i).getSQLBriefinsert() + ")");
            }

        }
        catch (SQLException e)
        {
            System.out.println("Something went wrong!!: " + e.getMessage());
        }
        finally {
            try
            {
                connection.close();
                connectionBrief.close();
            }
            catch (SQLException e)
            {
                System.out.println("error closing database!");
            }

        }
        System.out.println("completed Successfully");
    }



    public static List<DrexelClass> loadClassesFromCatalog(String termType,String studentType) throws IOException{
        String url = "http://catalog.drexel.edu/coursedescriptions/" + termType + "/" + studentType + "/";
        System.out.println("Loading all catalog links for: " + url);
        Document doc = Jsoup.connect(url).timeout(0).get();
        Elements columns = doc.getElementsByClass("colfloat");
        List<String> alllinks = new ArrayList<String>(0);
        for (Element currentcolumn : columns) {
            Elements links = currentcolumn.getElementsByTag("a");
            for (Element link : links) {
                String thing = link.attr("href");
                alllinks.add("http://catalog.drexel.edu"+thing);
            }
        }

        List<DrexelClass> currentClasses = new ArrayList<DrexelClass>(0);
        for (String currentlink : alllinks) {
            currentClasses.addAll(loadClassesfromCollegeLink(currentlink,termType,studentType));
        }
        return currentClasses;
    }

    public static List<DrexelClass> loadClassesfromCollegeLink(String collegeLink,String termType,String studentType) throws IOException
    {
        Document doc = Jsoup.connect(collegeLink).timeout(0).get();
        System.out.println("Importing: " + collegeLink);
        Elements courses = doc.getElementsByClass("courseblock");

        List<DrexelClass> collegeClasses = new ArrayList<DrexelClass>(0);
        for (Element currentcourse : courses) {
            Element classblock = currentcourse.getElementsByClass("courseblocktitle").get(0).getElementsByTag("strong").get(0);

            DrexelClass newClass = new DrexelClass();
            newClass.courseTermType = termType;
            newClass.courseStudentType = studentType;

            String fullcode = classblock.getElementsByClass("cdspacing").get(0).text();
            newClass.courseType = fullcode.split("\u00a0")[0];
            newClass.courseNum = fullcode.split("\u00a0")[1];

            String credits = classblock.textNodes().get(0).toString();
            newClass.courseCredits = credits.replaceAll("\\s+", "");

            newClass.courseTitle = classblock.getElementsByClass("cdspacing").get(1).text();

            newClass.courseDescription = Jsoup.parse(currentcourse.getElementsByClass("courseblockdesc").get(0).textNodes().get(0).toString().replace("\n","").trim()).text();
            newClass.courseDescription = (newClass.courseDescription.equals(""))?null:newClass.courseDescription;

            //.getElementsByTag("b").get(0).textNodes().get(0).toString()
            for(Element property: currentcourse.getElementsByTag("b"))
            {
                String type = property.textNodes().get(0).toString();
                String value = Jsoup.parse(currentcourse.childNode(property.siblingIndex()+1).toString()).text();
                switch (type){
                    case "College/Department:":
                        newClass.courseCollege = value;
                        break;
                    case "Repeat Status:":
                        newClass.courseRepeatStatus = value;
                        break;
                    case "Prerequisites:":
                        newClass.coursePrereqs = value;
                        break;
                    case "Corequisite: ":
                        newClass.courseCoReqs = value;
                        break;
                    case "Restrictions:":
                        newClass.courseRestrictions = value;
                        break;
                }
            }
            collegeClasses.add(newClass);
        }
        return collegeClasses;
    }


    public static List<DrexelClass> genUnivClasses() throws IOException
    {
        List<String> univLinks = new ArrayList<>();
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univa/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univas/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univb/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/unive/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univg/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univi/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univpe/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univr/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univt/");
        univLinks.add("http://catalog.drexel.edu/coursedescriptions/quarter/undergrad/univx/");

        List<DrexelClass> univClasses = new ArrayList<>();
        for(String link:univLinks){
            univClasses.addAll(loadClassesfromCollegeLink(link,"quarter","undergrad"));
        }
        return univClasses;
    }

}
