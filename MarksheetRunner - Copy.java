// import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.URL;

import java.nio.file.*;
import java.nio.charset.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.*;

class MarksheetRunner
{
  private final ArrayList<USheet> uSheets = new ArrayList<USheet>();
  private Workbook workbook;

  private CellStyle headerStyle;
  private CellStyle defaultStyle;

  // buffer cell variable
  private Cell cell;
  
  MarksheetRunner() throws Exception
  {
    final String prefix = sampleLinkToPrefix(getSampleLink());

    workbook = new XSSFWorkbook();

    defaultStyle = workbook.createCellStyle();
    defaultStyle.setBorderBottom(BorderStyle.THIN);
    defaultStyle.setBorderTop(BorderStyle.THIN);
    defaultStyle.setBorderLeft(BorderStyle.THIN);
    defaultStyle.setBorderRight(BorderStyle.THIN);
    defaultStyle.setAlignment(HorizontalAlignment.CENTER);
    defaultStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    defaultStyle.setWrapText(true);

    final Font headerFont = workbook.createFont();
    headerFont.setBold(true);

    headerStyle = workbook.createCellStyle();
    headerStyle.cloneStyleFrom(defaultStyle);
    headerStyle.setFont(headerFont);
  
    String[] UIDs = Files.readString(Paths.get("en.txt"),  StandardCharsets.UTF_8).split("\n");
    for(String UID : UIDs){
      final Marksheet m = new Marksheet(prefix, UID.trim());

      Sheet sheet = getSheet(m);

      Row row = sheet.createRow(sheet.getLastRowNum() + 1);

      cell = row.createCell(0);
      cell.setCellValue(sheet.getLastRowNum() - 2);
      cell.setCellStyle(defaultStyle);

      cell = row.createCell(1);
      cell.setCellValue(m.getSeatNumber());
      cell.setCellStyle(defaultStyle);

      cell = row.createCell(2);
      cell.setCellValue(m.getEnrollmentNumber());
      cell.setCellStyle(defaultStyle);

      cell = row.createCell(3);
      cell.setCellValue(m.getName());
      cell.setCellStyle(defaultStyle);

      int i = 3;

      for(Course c : m.getCourses())
      {
        if(c.hasTheory()){
          cell = row.createCell(++i);
          try
	  {
            cell.setCellValue(c.getTheoryMarks().getESE().getValueAsInt());
          }
          catch(Exception e)
          {
            cell.setCellValue(c.getTheoryMarks().getESE().getValue());
          }
	  
          cell.setCellStyle(defaultStyle);

          cell = row.createCell(++i);
          try
	  {
            cell.setCellValue(c.getTheoryMarks().getPA().getValueAsInt());
          }
          catch(Exception e)
          {
            cell.setCellValue(c.getTheoryMarks().getPA().getValue());
          }
          cell.setCellStyle(defaultStyle);

          cell = row.createCell(++i);
          try
	  {
            cell.setCellValue(c.getTheoryMarks().getValueAsInt());
          }
          catch(Exception e)
          {
            cell.setCellValue(c.getTheoryMarks().getValue());
          }
          cell.setCellStyle(defaultStyle);
        }

        if(c.hasPracticals()){
          cell = row.createCell(++i);
          try
	  {
            cell.setCellValue(c.getPracticalMarks().getESE().getValueAsInt());
          }
          catch(Exception e)
          {
            cell.setCellValue(c.getPracticalMarks().getESE().getValue());
          }
	  
          cell.setCellStyle(defaultStyle);

          cell = row.createCell(++i);
          try
	  {
            cell.setCellValue(c.getPracticalMarks().getPA().getValueAsInt());
          }
          catch(Exception e)
          {
            cell.setCellValue(c.getPracticalMarks().getPA().getValue());
          }
          cell.setCellStyle(defaultStyle);

          cell = row.createCell(++i);
          try
	  {
            cell.setCellValue(c.getPracticalMarks().getValueAsInt());
          }
          catch(Exception e)
          {
            cell.setCellValue(c.getPracticalMarks().getValue());
          }
          cell.setCellStyle(defaultStyle);
        }
      cell = row.createCell(++i);
      try
      {
        cell.setCellValue(c.getCreditsAsInt());
      }
      catch(Exception e)
      {
        cell.setCellValue(c.getCredits());
      }    
      cell.setCellStyle(defaultStyle);
    }

      cell = row.createCell(++i);
      cell.setCellValue(m.getTotalCredits());
      cell.setCellStyle(headerStyle);

      cell = row.createCell(++i);
      cell.setCellValue(m.getTotalMarks());
      cell.setCellStyle(headerStyle);

      cell = row.createCell(++i);
      cell.setCellValue(Math.round(m.getPercentage() * 100.0) / 100.0);
      cell.setCellStyle(headerStyle);

      cell = row.createCell(++i);
      cell.setCellValue(m.getResultClass());
      cell.setCellStyle(headerStyle);
  }

  autoFitContentsOfWorkbook();

  FileOutputStream fos = new FileOutputStream("output.xlsx");
  workbook.write(fos);
  fos.close();
}

  private void autoFitContentsOfWorkbook() {
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
        final Sheet sheet = workbook.getSheetAt(i);
        if (sheet.getPhysicalNumberOfRows() > 0) {
            Row row = sheet.getRow(sheet.getFirstRowNum());
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
                sheet.autoSizeColumn(columnIndex);
            }
        }
    }
  }

  private Sheet getSheet(Marksheet m)
  {
    for(USheet u : uSheets) if(u.canAccept(m)) return u.getSheet(); 

    System.out.println("New Type!");

    Sheet sheet = workbook.createSheet(m.getCourseCode()+'-'+m.getExamType()+'-'+m.getBatchRange());

    Row row = sheet.createRow(0);
    Row TPRow = sheet.createRow(1);
    Row cHRow = sheet.createRow(2);

    (cell=row.createCell(0)).setCellValue("Sr No.");
    cell.setCellStyle(headerStyle);
    (cell=row.createCell(1)).setCellValue("Seat No.");
    cell.setCellStyle(headerStyle);
    (cell=row.createCell(2)).setCellValue("Enrollment No.");
    cell.setCellStyle(headerStyle);
    (cell=row.createCell(3)).setCellValue("Name of Candidate");
    cell.setCellStyle(headerStyle);

    for(int i=0; i<=3; i++)
    {
      TPRow.createCell(i).setCellStyle(headerStyle);
      cHRow.createCell(i).setCellStyle(headerStyle);
    }

    sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 0));
    sheet.addMergedRegion(new CellRangeAddress(0, 2, 1, 1));
    sheet.addMergedRegion(new CellRangeAddress(0, 2, 2, 2));
    sheet.addMergedRegion(new CellRangeAddress(0, 2, 3, 3));

    int i = 4;

    for(Course c : m.getCourses()){
      int rows = 0;

      if(c.hasTheory()){

        cell = TPRow.createCell(i+rows);
	cell.setCellStyle(headerStyle);
        try
        {
          cell.setCellValue("TH" + " (0-" + c.getTheoryMarks().getMaximumAsInt()+")");
        } catch(Exception e) {
          cell.setCellValue("TH" + " (0-" + c.getTheoryMarks().getMinimum()+'-'+c.getTheoryMarks().getMaximum()+")");
        }
        cell = TPRow.createCell(i+rows+1);
	cell.setCellStyle(headerStyle);
        cell = TPRow.createCell(i+rows+2);
	cell.setCellStyle(headerStyle);

        cell = cHRow.createCell(i+rows);
	cell.setCellStyle(headerStyle);
        try
	{
	  cell.setCellValue("ESE" + " ("+c.getTheoryMarks().getESE().getMinimumAsInt()+'-'+c.getTheoryMarks().getESE().getMaximumAsInt()+")");
	} catch(Exception e)
	{
	  cell.setCellValue("ESE" + " ("+c.getTheoryMarks().getESE().getMinimum()+'-'+c.getTheoryMarks().getESE().getMaximum()+")");
	}
        cell = cHRow.createCell(i+rows+1);
	cell.setCellStyle(headerStyle);
	try
	{
          cell.setCellValue("PA" + " ("+c.getTheoryMarks().getPA().getMinimumAsInt()+'-'+c.getTheoryMarks().getPA().getMaximumAsInt()+")");
	}
	catch(Exception e)
	{
          cell.setCellValue("PA" + " ("+c.getTheoryMarks().getPA().getMinimum()+'-'+c.getTheoryMarks().getPA().getMaximum()+")");
	}
        cell = cHRow.createCell(i+rows+2);
	cell.setCellStyle(headerStyle);
	try
	{
          cell.setCellValue("Total"+ " ("+c.getPracticalMarks().getMaximumAsInt()+")");
	}
	catch(Exception e)
	{
          cell.setCellValue("Total"+ " ("+c.getTheoryMarks().getMaximum()+")");
	}

        rows+=3;
      }

      if(c.hasPracticals()){

        cell = TPRow.createCell(i+rows);
	cell.setCellStyle(headerStyle);
	try
	{
          cell.setCellValue("PR" + " (0-" + c.getPracticalMarks().getMaximumAsInt()+")");
	}
	catch(Exception e)
	{
          cell.setCellValue("PR" + " (0-" + c.getPracticalMarks().getMaximum()+")");
	}
        cell = TPRow.createCell(i+rows+1);
	cell.setCellStyle(headerStyle);
        cell = TPRow.createCell(i+rows+2);
	cell.setCellStyle(headerStyle);

        cell = cHRow.createCell(i+rows);
	cell.setCellStyle(headerStyle);
	try
	{
          cell.setCellValue("ESE" + " ("+c.getPracticalMarks().getESE().getMinimumAsInt()+'-'+c.getPracticalMarks().getESE().getMaximumAsInt()+")");
	}
	catch(Exception e)
	{
          cell.setCellValue("ESE" + " ("+c.getPracticalMarks().getESE().getMinimum()+'-'+c.getPracticalMarks().getESE().getMaximum()+")");
	}
        cell = cHRow.createCell(i+rows+1);
	cell.setCellStyle(headerStyle);
	try
	{
          cell.setCellValue("PA" + " ("+c.getPracticalMarks().getPA().getMinimumAsInt()+'-'+c.getPracticalMarks().getPA().getMaximumAsInt()+")");
	}
	catch(Exception e)
	{
          cell.setCellValue("PA" + " ("+c.getPracticalMarks().getPA().getMinimum()+'-'+c.getPracticalMarks().getPA().getMaximum()+")");
	}
        cell = cHRow.createCell(i+rows+2);
	cell.setCellStyle(headerStyle);
	try
	{
          cell.setCellValue("Total"+ " ("+c.getPracticalMarks().getMaximumAsInt()+")");
	}
	catch(Exception e)
	{
          cell.setCellValue("Total"+ " ("+c.getPracticalMarks().getMaximum()+")");
	}


        rows+=3;
      }

      cell = TPRow.createCell(i+rows);
      cell.setCellStyle(headerStyle);
      cell.setCellValue("Credits");

      for(int ii=i+1; ii<=i+rows; ++ii) {
        cell = row.createCell(ii);
        cell.setCellStyle(headerStyle);
      }

      cell = row.createCell(i);
      cell.setCellValue(c.getTitle());
      cell.setCellStyle(headerStyle);

      sheet.addMergedRegion(new CellRangeAddress(0, 0, i, i+rows));

      sheet.addMergedRegion(new CellRangeAddress(1, 1, i, i+2));
      if(c.hasTheory()&&c.hasPracticals()) sheet.addMergedRegion(new CellRangeAddress(1, 1, i+3, i+5));    

      i += rows+1;
    }

    cell = row.createCell(i);
    cell.setCellStyle(headerStyle);
    cHRow.createCell(i).setCellStyle(headerStyle);

    cell = TPRow.createCell(i++);
    cell.setCellValue("Total Credits");
    cell.setCellStyle(headerStyle);
    row.createCell(i).setCellStyle(headerStyle);
    cHRow.createCell(i).setCellStyle(headerStyle);

    cell = TPRow.createCell(i++);
    cell.setCellValue("Total Marks ("+m.getTotalMaxMarks()+')');
    cell.setCellStyle(headerStyle);
    row.createCell(i).setCellStyle(headerStyle);
    cHRow.createCell(i).setCellStyle(headerStyle);

    cell = TPRow.createCell(i++);
    cell.setCellValue("Percentage");
    cell.setCellStyle(headerStyle);
    row.createCell(i).setCellStyle(defaultStyle);
    cHRow.createCell(i).setCellStyle(headerStyle);

    cell = TPRow.createCell(i);
    cell.setCellValue("Result");
    cell.setCellStyle(headerStyle);
    row.createCell(i).setCellStyle(defaultStyle);
    cHRow.createCell(i).setCellStyle(headerStyle);
 
    sheet.addMergedRegion(new CellRangeAddress(0, 0, i-3, i));        

    uSheets.add(new USheet(m, sheet));
   
    return sheet;
  }

  static public String getSampleLink(){
    return "https://msbte.org.in/CRSLDNOV2020DISRESLIVE/2FRSRESFLS20LIVE/EnrollmentNumber/17/1705220123Marksheet.html";
  }

  static String sampleLinkToPrefix(String link)
  {
    try {
      link = new URL(link).getFile();
      return link.substring(1, link.indexOf("/", link.indexOf("/", 1)+1)).trim();
    } catch(Exception e) {
      return "";
    }
  }

  public static void main(String[] args) throws Exception { new MarksheetRunner(); }
}

class USheet
{
  private Marksheet mSheet;
  private Sheet xlSheet;

  USheet(Marksheet mSheet, Sheet xlSheet)
  {
    this.mSheet = mSheet;
    this.xlSheet = xlSheet;
  }

  boolean canAccept(Marksheet sheet)
  {
    return mSheet.isCompatibleWith(sheet);
  }    

  Sheet getSheet()
  {
    return xlSheet;
  }
}

class Marksheet
{
 private Document document;

 Marksheet(String prefix, String uid) throws IOException {

   // Check if marksheet exists in cache
   File[] cacheFiles = new File("./cache/"+prefix).listFiles(new FileFilter() {

    public boolean accept(File file) {
        String[] uids = file.getName().split("-");
        if(uids.length!=2) return false;
        if(uids[0].equals(uid)) return true;
	return uid.equals(uids[1].substring(0,6));
    }

   });
   
   // If cache exists for the given uid, then just use that data to retrieve information
   if(cacheFiles.length!=0){
     if(cacheFiles.length>1) System.out.println("Please do not mangle with the cache to avoid unexpected behavior (UID: "+uid+')');
     // System.out.println("Taking "+uid+" from cache..");
     document = Jsoup.parse(Files.readString(Paths.get(cacheFiles[0].getPath()), StandardCharsets.UTF_8));
   }
   // Else retrieve the data for the marksheet over the network and cache it for future (re-)use
   else {

    // Fetch the required document over the net

    String link = "";

    if(uid.length()==10) link = "https://msbte.org.in/" + prefix + "/EnrollmentNumber/" + uid.substring(0,2) + '/'+uid+"Marksheet.html";
    else if(uid.length()==6) link = "https://msbte.org.in/" + prefix +"/SeatNumber/"+uid.substring(0,2)+'/'+uid+"Marksheet.html";
    else System.out.println("Invalid Enrollment No.\\Seat No. "+uid);

    // System.out.println("Downloading over the network.. (UID: "+uid+")");

    System.out.println(link);

    try {

    } catch(Exception e) {
      System.out.println("Could not find marksheet for "+uid+'.'+" (Link: "+link+')');
    }

    // Cache it for future (re-use)
    File cacheFile = new File("./cache/"+prefix+'/'+getEnrollmentNumberAsString()+'-'+getSeatNumberAsString()+".html");
    cacheFile.getParentFile().mkdirs();

    FileWriter writer = new FileWriter(cacheFile, StandardCharsets.UTF_8);
    writer.write(document.toString());
    writer.close();
  }

 }

 String getCourseName()
 {
   return document.body().select("td").get(10).text();
 }

 String getCourseCode()
 {
   return getSemesterCode();
 }

 String getSemesterName()
 {
   return document.body().select("td").get(8).text();
 }

 String getExaminationName()
 {
   return document.body().select("td").get(5).text();
 }

 String getResultClass()
 {
   return document.select("table").get(2).select("tr").get(2).select("td").get(1).text().trim();
 }

 String getBatchRange()
 {
   final int year = getYearOfExaminationAsInt();
   return (year-1)+"-"+year%100;
 }

 String getYearOfExamination()
 {
   return getExaminationName().split(" ")[1];
 }

 int getYearOfExaminationAsInt()
 {
   return Integer.parseInt(getYearOfExamination());
 }

 String getYearOfEnrollment()
 {
   return "20"+getEnrollmentNumberAsString().substring(0,2);
 }

 String getSemesterCode()
 {
   return document.select("table").get(2).select("tr").get(2).select("td").get(0).text().trim().split("/")[2];
 }

 int getYearOfEnrollmentAsInt()
 {
   return Integer.parseInt(getYearOfEnrollment());
 }

 String getResultDate()
 {
   return document.select("table").get(2).select("td").get(0).text().split(" ")[2];
 }

 String getExamType()
 {
   return getExaminationName().split(" ")[0];
 }

 boolean isWinter()
 {
   return getExamType().equals("WINTER");
 }

 boolean isSummer()
 {
   return getExamType().equals("SUMMER");   
 }

 String getName()
 {
   return document.body().select("td").get(1).text();
 }

 int getEnrollmentNumber()
 {
   return Integer.parseInt(getEnrollmentNumberAsString());
 }

 String getEnrollmentNumberAsString()
 {
   return document.body().select("td").get(3).text().trim();
 }

 int getSeatNumber()
 {
   return Integer.parseInt(getSeatNumberAsString());
 }

 String getSeatNumberAsString()
 {
   return document.body().select("td").get(7).text();
 }

 String getInstituteCode()
 {
   return getEnrollmentNumberAsString().substring(2,6);
 }

 String getTotalMaxMarksAsString()
 {
   return document.select("table").get(2).select("tr").get(1).select("td").get(0).text().trim();
 }

 int getTotalMaxMarks()
 {
   return Integer.parseInt(getTotalMaxMarksAsString());
 }

 String getPercentageAsString()
 {
   return document.select("table").get(2).select("tr").get(1).select("td").get(1).text().trim();
 }

 float getPercentage()
 {
   return Float.parseFloat(getPercentageAsString());
 }

 String getTotalMarksAsString()
 {
   return document.select("table").get(2).select("tr").get(1).select("td").get(2).text().trim();
 }
 
 int getTotalMarks()
 {
   return Integer.parseInt(getTotalMarksAsString());
 }

 String getTotalCreditsAsString()
 {
   return document.select("table").get(2).select("tr").get(1).select("td").get(3).text().trim();
 }

 int getTotalCredits()
 {
   return Integer.parseInt(getTotalCreditsAsString());
 }

 boolean isCompatibleWith(Marksheet m)
 {
   if(this==m) return true;

   List<String> list1 = getCourseTitles(), list2 = m.getCourseTitles();

   if(list1.size()!=list2.size()) return false;

   for(int i=0; i<list1.size(); ++i)
   {
     if(!list1.get(i).equals(list2.get(i))) return false;
   }

   return true;
 }

 List<String> getCourseTitles()
 {
   ArrayList<String> titles = new ArrayList<String>();
   List<Element> contents = document.select("tbody").get(1).select("tr");
   
   for(Element content:contents)
   {
     final String title = content.select("tr").get(0).select("td").get(0).text().trim();
     if(title.length()!=0) titles.add(title);
   }
  return titles;
 }

 List<Course> getCourses()
 {
   ArrayList<Course> courses = new ArrayList<Course>();
   List<Element> contents = document.select("tbody").get(1).select("tr"), content = new ArrayList<Element>();
   TH th = new TH();

   contents = contents.subList(2, contents.size());

   int i = 0;

   while(i<contents.size())
   {
     content = contents.get(i).select("tr").get(0).select("td");

     String title = content.get(0).text();
     String credits = content.get(8).text();

     if(content.get(1).text().equals("TH")){

       // TH
       final String thMax = content.get(6).text();
       final String thObt = content.get(7).text();

       // TH-ESE
       final ESE thESE = new ESE(content.get(3).text(), content.get(4).text(), content.get(5).text());

       content = contents.get(++i).select("tr").get(0).select("td"); // move to next content

       // TH-PA
       final PA  thPA = new PA(content.get(3).text(), content.get(4).text(), content.get(5).text());

       content = contents.get(++i).select("tr").get(0).select("td"); // move to next content

       th = new TH(thESE, thPA, thMax, thObt);

       // Check if the next content is theory (if the first content is PR then this if block wouldn't have got executed)
       if(content.get(1).text().equals("TH")) {
	 Course c = new Course(title, th, credits);
	 courses.add(c);
         th = null;
         continue;
       }
     }

     // PR
     final String prMax = content.get(6).text();
     final String prObt = content.get(7).text();

     // PR-ESE
     final ESE prESE = new ESE(content.get(3).text(), content.get(4).text(), content.get(5).text());

     content = contents.get(++i).select("tr").get(0).select("td"); // move to next content

     // PR-PA
     final PA prPA = new PA(content.get(3).text(), content.get(4).text(), content.get(5).text());

     Course c = new Course(title, th, new PR(prESE, prPA, prMax, prObt), credits);
     courses.add(c);

     ++i;
   }

   return courses;
 }

 public int hashCode()
 {
   return getEnrollmentNumber();
 }
}

class Course
{
  private String title;
  private String credits;

  private TH th;
  private PR pr;

  Course(String title, TH theoryMarks, PR practicalMarks, String credits)
  {
    this.title = title;
    this.th = theoryMarks;
    this.pr = practicalMarks;
    this.credits = credits;
  }

  Course(String title, TH theoryMarks, String credits)
  {
    this.title = title;
    this.th = theoryMarks;
    this.credits = credits;
  }

  Course(String title, PR practicalMarks, String credits)
  {
    this.title = title;
    this.pr = practicalMarks;
  }

  TH getTheoryMarks()
  {
    return th;    
  }

  PR getPracticalMarks()
  {
    return pr;    
  }

  String getTitle()
  {
    return title;
  }

  String getCredits()
  {
    return credits;
  }

  int getCreditsAsInt()
  {
    return Integer.parseInt(credits);
  }
 
  boolean hasTheory()
  {
    return th!=null;
  }

  boolean hasPracticals()
  {
    return pr!=null;
  }

  public String toString()
  {
     StringBuilder s = new StringBuilder(getTitle() + " (Credits: "+credits+")\n");

     if(hasTheory()) s.append(th.toString());
     if(hasPracticals()) s.append(pr.toString());

     return s.toString();
  }
}

class Marks
{
  private String max;
  private String min;
  private String value;

  Marks(){}

  Marks(String max, String min, String value)
  {
    this.max = max;
    this.min = min;
    this.value = value;
  }

  Marks(String max, String value)
  {
    this.max = max;
    this.value = value;
  }

  String getMaximum()
  {
    return max;
  }

  String getMinimum()
  {
    return min;
  }

  String getValue()
  {
    return value;
  }

  int getMaximumAsInt()
  {
    return Integer.parseInt(max);
  }

  int getMinimumAsInt()
  {
    return Integer.parseInt(min);
  }

  int getValueAsInt()
  {
    return Integer.parseInt(value);
  }

  public String toString()
  {
    StringBuilder s = new StringBuilder("  Maximum - " + max +"\n");
    if(min!=null) s.append("  Minimum - " + min +"\n");
    s.append("  Obtained - " + value +"\n");
    return s.toString();
  }
}

class CHead extends Marks
{
  private ESE ese;
  private PA pa;

  CHead(ESE ese, PA pa, String max, String total)
  {
    super(max, total);
    this.ese = ese;
    this.pa = pa;
  }

  ESE getESE(){
    return ese;
  }

  PA getPA(){
    return pa;
  }

  public String toString()
  {
    StringBuilder s = new StringBuilder(ese.toString()+"\n "+pa+"\n ");
    s.append("Overall:\n");
    s.append("  Maximum - "+getMaximum()+"\n");
    s.append("  Obtained - "+getValue()+"\n");
    return s.toString();
  }
}

class TH extends CHead
{
  // Used to just create an empty object
  TH(){super(null, null, null, null);}

  TH(ESE ese, PA pa, String max, String total)
  {
    super(ese, pa, max, total);
  }

  public String toString()
  {
    return "Theory:\n "+super.toString();
  }
}

class PR extends CHead
{
  PR(ESE ese, PA pa, String max, String total)
  {
    super(ese, pa, max, total);
  }

  public String toString()
  {
    return "Practicals:\n"+super.toString();
  }
}

class PA extends Marks
{
  PA(String max, String min, String obtained)
  {
    super(max, min, obtained);
  }

  public String toString()
  {
    return "PA:\n"+super.toString();
  }
}

class ESE extends Marks
{
  ESE(String max, String min, String obtained)
  {
    super(max, min, obtained);
  }

  public String toString()
  {
    return "ESE:\n"+super.toString();
  }
}
