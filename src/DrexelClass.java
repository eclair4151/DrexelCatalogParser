import java.util.Comparator;

/**
 * Created by shemesht on 7/1/15.
 */
public class DrexelClass implements Comparable<DrexelClass>{
    public String courseType;
    public String courseNum;
    public String courseTitle;
    public String courseCredits;
    public String courseDescription;
    public String courseCollege;
    public String courseRepeatStatus;
    public String coursePrereqs;
    public String courseRestrictions;
    public String courseCoReqs;
    public String courseTermType;
    public String courseStudentType;

    @Override
    public String toString(){
        return courseType + "," + courseNum + "," + courseTitle.replace(",","") + "," + courseCredits + "," + courseType + " " + courseNum;
    }

    public String getSQLinsert()
    {
        String insertString = "'" + courseType + "', '" + courseNum + "', '" + courseTitle.replace("'","''") + "', '" + courseCredits + "', ";
        insertString+=(courseDescription==null)?"null, ":"'" + courseDescription.replace("'","''") +"', ";
        insertString+=(courseCollege==null)?"null, ":"'" + courseCollege.replace("'","''") +"', ";
        insertString+=(courseRepeatStatus==null)?"null, ":"'" + courseRepeatStatus.replace("'","''") +"', ";
        insertString+=(coursePrereqs==null)?"null, ":"'" + coursePrereqs.replace("'","''") +"', ";
        insertString+=(courseRestrictions==null)?"null, ":"'" + courseRestrictions.replace("'","''") +"', ";
        insertString+=(courseCoReqs==null)?"null, ":"'" + courseCoReqs.replace("'","''") +"', ";
        insertString += "'" + courseTermType + "', '" + courseStudentType + "'";

        return  insertString;
    }

    public String getSQLBriefinsert()
    {
        return "'" + courseType + "', '" + courseNum + "', '" + courseTitle.replace("'","''") + "', '" + courseCredits + "'";
    }

    @Override
    public int compareTo(DrexelClass o) {
        return (courseType + courseNum).compareTo(o.courseType + o.courseNum);
    }
}
