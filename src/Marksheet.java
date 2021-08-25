package org.tpoly.msbte;

import java.util.*;
import java.io.*;
import java.net.*;

import java.nio.file.*;
import java.nio.charset.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.*;

// The Marksheet(Parser) class accepts an prefix and (valid) UID (i.e. seat number or enrollment number) and returns an logically accessible marksheet object.
public class Marksheet
{
    // The logically accessible HTML docment object that stores most of the info of the marksheet.
    private Document document;

    // A simple static helper function that can be used to get the prefix from the passed [sampleLink].
    public static String getPrefixFrom(String sampleLink)
    {
        try
	{
          sampleLink = new URL(sampleLink).getFile();
          return sampleLink.substring(1, sampleLink.indexOf("/", sampleLink.indexOf("/", 1) + 1)).trim();
        }
	catch (Exception e)
	{
            return "";
        }
    }


    public Marksheet(String prefix, String uid) throws Exception
    {
        /// Check if the raw data needed to create a Marksheet object exists in the cache
        //
	// While saving the raw data needed to create a Marksheet object (in the cache (as a file)), it is the named in the following format:
	// `enrollmentnumber-seatno.html`
	//
	// For example: "1705220123-123456.html"
	// 
	// The directory in which the cache is stored in depends on the prefix in the following format:
	// "cache/firstHalfOfPrefix/secondHalfOfPrefix/enrollmentnumber-seatno.html"
	//
	// eg. "cache/CRSLDNOV2020DISRESLIVE/2FRSRESFLS20LIVE/1705220123-123456.html" relative to the current directory.
	//
	// This makes it easier to know whether the given file is the cache we are looking for or not (instead of actually reading it's content)
        File[] cacheFiles = new File("./cache/" + prefix).listFiles(new FileFilter() {

            public boolean accept(File file) {
		// Split the file name into two parts based on the (one) hyphen that is assumed to be in it.
                String[] uids = file.getName().split("-");
		
		// If the number of hyphens is not equal to 1, then it certainly isn't the file we are looking for, so return false.
                if (uids.length != 2) return false;

		// If the first part of the filename is equal to the UID return true
                if (uids[0].equals(uid)) return true;

		// If the second part of the filename, except the file format is equal then return true else false
                return uid.equals(uids[1].substring(0, 6));
            }
        });

	// If the above operation has discovered the file we were looking for
        if (cacheFiles!=null && cacheFiles.length != 0) {

	    // Parse the contents of that file (the first file) into a Document object, that could be used to logically access the raw data/html file
	    String fileContents = String.join("\n", Files.readAllLines(Paths.get(cacheFiles[0].getPath()), StandardCharsets.UTF_8));
            document = Jsoup.parse(fileContents);
        }

        // Else retrieve the raw data of the marksheet over the network and cache it for future (re)use
        else
	{
	    // Declare a common variable to store the link
            String link = "";

	    // If the UID could be an valid enrollment number (based on the number of digits), assign an enrollmentnumber based link to the link variable
            if (uid.length() == 10) link = "https://msbte.org.in/" + prefix + "/EnrollmentNumber/" + uid.substring(0, 2) + '/' + uid + "Marksheet.html";

	    // If the UID could be an valid seat number (based on the number of digits), assign a seat no. based link to the link variable
            else if (uid.length() == 6) link = "https://msbte.org.in/" + prefix + "/SeatNumber/" + uid.substring(0, 2) + '/' + uid + "Marksheet.html";

	    // If it's neither then it is assumed that the link is invalid
            else throw new IllegalArgumentException("The passed UID "+uid+" is invalid! Please provide a valid enrollment/seat no.");

            try
	    {
		// Try to fetch the required document over the network
		document = Jsoup.connect(link).get();
            }
	    catch(UnknownHostException e)
	    {
		System.out.println("Could not connect to www.msbte.org. Please check that you are connected to the internet or if MSBTE's official server is not currently down.");
		throw e;
	    }
	    catch(HttpStatusException e)
	    {
	        throw new FileNotFoundException("Could not find marksheet for "+uid+'.'+" (Link: "+link+')');
	    }
	    catch (Exception e)
	    {
		// All the exceptions thrown by Jsoup.connect seem to be RuntimeException(s) so they aren't specified in the docs. So here's a workaround.
		System.out.println("The below error isn't recognized. Please let us know about it if you ");
		throw e;
            }

            // Cache it for future (re-use)

	    // Create a cache File (object representation)
            File cacheFile = new File("./cache/" + prefix + '/' + getEnrollmentNumberAsString() + '-' + getSeatNumberAsString() + ".html");
	    // Create the parent directories of the cache (if they do not exist)	    	   
            cacheFile.getParentFile().mkdirs();

	    // Write the html code (raw data) to that file (as a cache)
            FileWriter writer = new FileWriter(cacheFile);

	    // The string representation of the stored Document object is itself the raw data we are looking for
            writer.write(document.toString());

            writer.close();
        }
    }

    // The name of the below methods itself specify which element/content are they targeting at.

    // eg. of return value: Diploma In Computer Engineering
    String getCourseName() {
        return document.body().select("td").get(10).text();
    }

    // eg. CO4I
    String getCourseCode() {
        return getSemesterCode();
    }

    String getSemesterName() {
        return document.body().select("td").get(8).text();
    }

    String getExaminationName() {
        return document.body().select("td").get(5).text();
    }

    String getResultClass() {
        return document.select("table").get(2).select("tr").get(2).select("td").get(1).text().trim();
    }

    String getBatchRange() {
        final int year = getYearOfExaminationAsInt();
        return (year - 1) + "-" + year % 100;
    }

    String getYearOfExamination() {
        return getExaminationName().split(" ")[1];
    }

    int getYearOfExaminationAsInt() {
        return Integer.parseInt(getYearOfExamination());
    }

    String getYearOfEnrollment() {
        return "20" + getEnrollmentNumberAsString().substring(0, 2);
    }

    String getSemesterCode() {
        return document.select("table").get(2).select("tr").get(2).select("td").get(0).text().trim().split("/")[2];
    }

    int getYearOfEnrollmentAsInt() {
        return Integer.parseInt(getYearOfEnrollment());
    }

    String getResultDate() {
        return document.select("table").get(2).select("td").get(0).text().split(" ")[2];
    }

    String getExamType() {
        return getExaminationName().split(" ")[0];
    }

    boolean isWinter() {
        return getExamType().equals("WINTER");
    }

    boolean isSummer() {
        return getExamType().equals("SUMMER");
    }

    String getName() {
        return document.body().select("td").get(1).text();
    }

    int getEnrollmentNumber() {
        return Integer.parseInt(getEnrollmentNumberAsString());
    }

    String getEnrollmentNumberAsString() {
        return document.body().select("td").get(3).text().trim();
    }

    int getSeatNumber() {
        return Integer.parseInt(getSeatNumberAsString());
    }

    String getSeatNumberAsString() {
        return document.body().select("td").get(7).text();
    }

    String getInstituteCode() {
        return getEnrollmentNumberAsString().substring(2, 6);
    }

    String getTotalMaxMarksAsString() {
        return document.select("table").get(2).select("tr").get(1).select("td").get(0).text().trim();
    }

    int getTotalMaxMarks() {
        return Integer.parseInt(getTotalMaxMarksAsString());
    }

    String getPercentageAsString() {
        return document.select("table").get(2).select("tr").get(1).select("td").get(1).text().trim();
    }

    float getPercentage() {
        return Float.parseFloat(getPercentageAsString());
    }

    String getTotalMarksAsString() {
        return document.select("table").get(2).select("tr").get(1).select("td").get(2).text().trim();
    }

    int getTotalMarks() {
        return Integer.parseInt(getTotalMarksAsString());
    }

    String getTotalCreditsAsString() {
        return document.select("table").get(2).select("tr").get(1).select("td").get(3).text().trim();
    }

    int getTotalCredits() {
        return Integer.parseInt(getTotalCreditsAsString());
    }

    boolean isCompatibleWith(Marksheet m)
    {
        if (this == m) return true;

        List <String > list1 = getCourseTitles(), list2 = m.getCourseTitles();

        if (list1.size() != list2.size()) return false;

        for (int i = 0; i < list1.size(); ++i) {
            if (!list1.get(i).equals(list2.get(i))) return false;
        }

        return true;
    }

    // Gets all the titles of the courses avaiable in this marksheet
    List <String> getCourseTitles() {
        ArrayList <String> titles = new ArrayList < String > ();
        List <Element> contents = document.select("tbody").get(1).select("tr");

        for (Element content: contents) {
            final String title = content.select("tr").get(0).select("td").get(0).text().trim();
            if (title.length() != 0) titles.add(title);
        }
        return titles;
    }

    // Gets all the courses available in this marksheet
    List<Course> getCourses() {

        ArrayList <Course> courses = new ArrayList <Course> ();
        List <Element> contents = document.select("tbody").get(1).select("tr"), content = new ArrayList<Element>();

        TH th = new TH();

        contents = contents.subList(2, contents.size());

        int i = 0;

        while (i < contents.size()) {
            content = contents.get(i).select("tr").get(0).select("td");

            String title = content.get(0).text();
            String credits = "";
            
            // Certain MSBTE marksheets do not have the credit column.
	    try
            {
	      credits = content.get(8).text();
            }
	    catch(Exception e) {}

            if (content.get(1).text().equals("TH")) {

		th = new TH();

                // TH
                final String thMax = content.get(6).text();
                final String thObt = content.get(7).text();

                // TH-ESE
                final ESE thESE = new ESE(content.get(3).text(), content.get(4).text(), content.get(5).text());

                content = contents.get(++i).select("tr").get(0).select("td"); // move to next content

                // TH-PA
                final PA thPA = new PA(content.get(3).text(), content.get(4).text(), content.get(5).text());

                content = contents.get(++i).select("tr").get(0).select("td"); // move to next content

                th = new TH(thESE, thPA, thMax, thObt);

                // Check if the next content is theory (if the first content is PR then this if block wouldn't have got executed)
                if (content.get(1).text().equals("TH")) {
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
	    th = null;
            courses.add(c);

            ++i;
        }

        return courses;
    }

    public int hashCode() {
        return getEnrollmentNumber();
    }
}