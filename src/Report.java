/*
* A MSBTE marksheet parser and report generator.
* 
* A marksheet for a specific UID (enrollment no./seat no.) can be obtained as an (logically accessible) Java object by passing a prefix and the UID.
*
* eg. If this is the sample link `https://msbte.org.in/CRSLDNOV2020DISRESLIVE/2FRSRESFLS20LIVE/EnrollmentNumber/17/1705220125Marksheet.html` then * `CRSLDNOV2020DISRESLIVE/2FRSRESFLS20LIVE` is the prefix.
* 
* Libraries used:
*  - Jsoup (jsoup-1.13.1)   : This library is used to fetch
*  - Apache POI (poi-4.1.2) : 
*/

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

// The class that generates the report for a given set of UIDs
public class Report
{
    // Maintains a set of unique marksheets (eg. CO3I, CO5I, ME2I, ...)
    private final ArrayList<USheet> uSheets = new ArrayList<USheet>();

    // Used to get the set of UIDs that were found invalid during the generation of report
    private final List<String> invalidUIDs = new ArrayList<String>();

    // The variable that stores the workbook to which all the headers and entries will be written to, before actually generating the excel file.
    private Workbook workbook;

    // This variable stores the CellStyle for the headers of the excel sheet to be generated
    private CellStyle headerStyle;

    // This variable stores the CellStyle for the entries of the excel sheet to be generated
    private CellStyle defaultStyle;

    // A helper variable that is used to perform multiple method calls on a newly created cell
    private Cell cell;

    // A static method that can be used to generate a Report from the given sample link, UIDs and the file in which the main report is expected to be stored 
    // in.
    public static Report generate(String sampleLink, String[] UIDs, String outputFileName) throws IOException, InterruptedException
    {
      return new Report(null, sampleLink, UIDs, outputFileName);
    }

    // A static method that can be used to generate a Report from the given sample link and UIDs (Output file name: "output.xlsx")
    public static Report generate(String sampleLink, String[] UIDs) throws IOException, InterruptedException
    {
      return new Report(null, sampleLink, UIDs, "output.xlsx");
    }

    public static Report genAndObserve(String sampleLink, String[] UIDs, String outputFileName, ReportGeneratorObserver rgo) throws IOException, InterruptedException
    {
      return new Report(rgo, sampleLink, UIDs, outputFileName);
    }

    public static Report genAndObserve(String sampleLink, String[] UIDs, ReportGeneratorObserver rgo) throws IOException, InterruptedException
    {
      return new Report(rgo, sampleLink, UIDs, "output.xlsx");
    }

    // A private method that is used to get the Sheet in which the data of the Marksheet can be filled to.
    //
    // This method helps in abstracting the entire process of retrieving the (excel) sheet the caller might be looking for.
    //
    // If a valid sheet for a marksheet does not exist then it would create one or get an existing one, which is compatible with the passed marksheet.
    private Sheet getSheet(Marksheet m)
    {
        // Check if the marksheet is compatible with one of the existing sheets
        for (USheet u: uSheets)
            if (u.canAccept(m)) return u.getSheet();

        // If it isn't then we'll create one and return it

        // Create a new sheet in the workbook of this report
        Sheet sheet = workbook.createSheet(m.getCourseCode() + '-' + m.getExamType() + '-' + m.getBatchRange());

        /// Now we'll design a header for the above excel sheet based on the details provided by the given marksheet object

        // Create and store the first three rows of the sheet
        final Row row = sheet.createRow(0);
        final Row TPRow = sheet.createRow(1);
        final Row cHRow = sheet.createRow(2);

        // Creating and writing the contents of the first four heads of the header
        (cell = row.createCell(0)).setCellValue("Sr No.");
        cell.setCellStyle(headerStyle);
        (cell = row.createCell(1)).setCellValue("Seat No.");
        cell.setCellStyle(headerStyle);
        (cell = row.createCell(2)).setCellValue("Enrollment No.");
        cell.setCellStyle(headerStyle);
        (cell = row.createCell(3)).setCellValue("Name of Candidate");
        cell.setCellStyle(headerStyle);

	// A counter variable that will be used to iterate through the columns of the sheet table (as long as required)
        int colNo = 0;

	// Merging the first three rows of the first four columns of the sheet while ensuring that they have a common (header)Style to avoid ambugity in the
	// style of the final cell obtained after merging.
        for (colNo = 0; colNo <= 3; ++colNo) {
	  TPRow.createCell(colNo).setCellStyle(headerStyle);
	  cHRow.createCell(colNo).setCellStyle(headerStyle);
	  sheet.addMergedRegion(new CellRangeAddress(0, 2, colNo, colNo));
        }

	// Iterating through all the courses of the marksheet to dynamically design a part of the header, based on the number and type of courses 
	// (i.e. TH + PR / TH / PR) of the given marksheet m.
        for (Course c : m.getCourses()) {

	    // A counter for rows, that generally spans across multiple rows (generally 3) for a given course.
            int rows = 0;

	    // Check if the course has theory, so as to build part of the header with it, if it has one.
            if (c.hasTheory()) {
	        
		// Creating and storing the first cell (of the second row) with the style headerStyle
		cell = TPRow.createCell(colNo);
		cell.setCellStyle(headerStyle);

		// Writing the TH header for the current course
                try {
                    cell.setCellValue("TH" + " (0-" + c.getTheoryMarks().getMaximumAsInt() + ")");
                } catch (Exception e) {
                    cell.setCellValue("TH" + " (0-" + c.getTheoryMarks().getMaximum() + ")");
                }

		// Setting the style of the the next two cells as headerStyle to ensure that there is no ambugity in styles while merging
                cell = TPRow.createCell(colNo + rows + 1);
                cell.setCellStyle(headerStyle);
                cell = TPRow.createCell(colNo + rows + 2);
                cell.setCellStyle(headerStyle);

		// Designing the (course head) row (ESE-PA-Total), that'll be below the cell described above.
		
		// Creating and setting the style for first cell (of this course on this row) to describe the common details of ESE
                cell = cHRow.createCell(colNo + rows);
                cell.setCellStyle(headerStyle);

		// Writing the ESE header for the TH section of this course
                try {
                    cell.setCellValue("ESE" + " (" + c.getTheoryMarks().getESE().getMinimumAsInt() + '-' + c.getTheoryMarks().getESE().getMaximumAsInt() + ")");
                } catch (Exception e) {
                    cell.setCellValue("ESE" + " (" + c.getTheoryMarks().getESE().getMinimum() + '-' + c.getTheoryMarks().getESE().getMaximum() + ")");
                }

                cell = cHRow.createCell(colNo + rows + 1);
                cell.setCellStyle(headerStyle);

		// Writing the PA header for the TH section of this course
                try {
                    cell.setCellValue("PA" + " (" + c.getTheoryMarks().getPA().getMinimumAsInt() + '-' + c.getTheoryMarks().getPA().getMaximumAsInt() + ")");
                } catch (Exception e) {
                    cell.setCellValue("PA" + " (" + c.getTheoryMarks().getPA().getMinimum() + '-' + c.getTheoryMarks().getPA().getMaximum() + ")");
                }

                cell = cHRow.createCell(colNo + rows + 2);
                cell.setCellStyle(headerStyle);

		// Writing the header for the total of the TH section of this course
                try {
                    cell.setCellValue("Total" + " (" + c.getPracticalMarks().getMaximumAsInt() + ")");
                } catch (Exception e) {
                    cell.setCellValue("Total" + " (" + c.getTheoryMarks().getMaximum() + ")");
                }

		// Moving three cells ahead (if the course didn't have theory then we would have still been at the start of the section of this course
                rows += 3;
            }

	    // Check if the course has practicals(PR), so as to build part of the header with it, if it has one.
            if (c.hasPracticals()) {

		// Create, design and store the cell that would describe the PR header of this course
                cell = TPRow.createCell(colNo + rows);
                cell.setCellStyle(headerStyle);

		// Writing the PR header for the current course
                try {
                    cell.setCellValue("PR" + " (0-" + c.getPracticalMarks().getMaximumAsInt() + ")");
                } catch (Exception e) {
                    cell.setCellValue("PR" + " (0-" + c.getPracticalMarks().getMaximum() + ")");
                }

		// Setting the style of the the next two cells as headerStyle to ensure that there is no ambiguity in styles while merging
                cell = TPRow.createCell(colNo + rows + 1);
                cell.setCellStyle(headerStyle);
                cell = TPRow.createCell(colNo + rows + 2);
                cell.setCellStyle(headerStyle);

		// Creating, storing and setting the style for the ESE header of the PR section of this course
                cell = cHRow.createCell(colNo + rows);
                cell.setCellStyle(headerStyle);

		// Writing the ESE header for the PR section of this course
                try {
                    cell.setCellValue("ESE" + " (" + c.getPracticalMarks().getESE().getMinimumAsInt() + '-' + c.getPracticalMarks().getESE().getMaximumAsInt() + ")");
                } catch (Exception e) {
                    cell.setCellValue("ESE" + " (" + c.getPracticalMarks().getESE().getMinimum() + '-' + c.getPracticalMarks().getESE().getMaximum() + ")");
                }

		// Creating, storing and setting the style for the PA header of the PR section of this course
                cell = cHRow.createCell(colNo + rows + 1);
                cell.setCellStyle(headerStyle);

		// Writing the PA header for the PR section of this course
                try {
                    cell.setCellValue("PA" + " (" + c.getPracticalMarks().getPA().getMinimumAsInt() + '-' + c.getPracticalMarks().getPA().getMaximumAsInt() + ")");
                } catch (Exception e) {
                    cell.setCellValue("PA" + " (" + c.getPracticalMarks().getPA().getMinimum() + '-' + c.getPracticalMarks().getPA().getMaximum() + ")");
                }

		// Creating, storing and setting the style for the ESE header of the PR section of this course
                cell = cHRow.createCell(colNo + rows + 2);
                cell.setCellStyle(headerStyle);

		// Writing the header for the total of the PR section of this course
                try {
                    cell.setCellValue("Total" + " (" + c.getPracticalMarks().getMaximumAsInt() + ")");
                } catch (Exception e) {
                    cell.setCellValue("Total" + " (" + c.getPracticalMarks().getMaximum() + ")");
                }

		// Moving three cells ahead within this course section
                rows += 3;
            }

	    // Rendering the last cell in the second row of this section to create a header for the credits of the current course
            cell = TPRow.createCell(colNo + rows);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("Credits");

	    // Setting a common style for all the remaining cells of the top-most row to avoid ambiguity while merging		
            for (int i = colNo + 1; i <= colNo + rows; ++i) {
                cell = row.createCell(i);
                cell.setCellStyle(headerStyle);
            }

	    // Rendering the title of the column in the first column of the sub-section of this course
            cell = row.createCell(colNo);
            cell.setCellValue(c.getTitle());
            cell.setCellStyle(headerStyle);

	    // Merging all the top-most title cells of this course as one cell
            sheet.addMergedRegion(new CellRangeAddress(0, 0, colNo, colNo + rows));

	    // Merging the first sub-section heading of this course (TH/PR) as one cell
            sheet.addMergedRegion(new CellRangeAddress(1, 1, colNo, colNo + 2));

	    // And the second sub-section heading of this course (PR) as another cell
            if (c.hasTheory() && c.hasPracticals()) sheet.addMergedRegion(new CellRangeAddress(1, 1, colNo + 3, colNo + 5));

	    // Placing the colNo counter variable after all the columns of the current course
            colNo += rows + 1;

	    // The control will now move to the next course (if any)
        }

	// Designing rest of the static header section

	// Setting the 
        cell = row.createCell(colNo);
        cell.setCellStyle(headerStyle);
        cHRow.createCell(colNo).setCellStyle(headerStyle);

	// Designing the header for Total Credits section
        cell = TPRow.createCell(colNo++);
        cell.setCellValue("Total Credits");
        cell.setCellStyle(headerStyle);
        row.createCell(colNo).setCellStyle(headerStyle);
        cHRow.createCell(colNo).setCellStyle(headerStyle);

	// Designing the header for Total Marks section
        cell = TPRow.createCell(colNo++);
        cell.setCellValue("Total Marks (" + m.getTotalMaxMarks() + ')');
        cell.setCellStyle(headerStyle);
        row.createCell(colNo).setCellStyle(headerStyle);
        cHRow.createCell(colNo).setCellStyle(headerStyle);

	// Designing the header for Percentage section
        cell = TPRow.createCell(colNo++);
        cell.setCellValue("Percentage");
        cell.setCellStyle(headerStyle);
        row.createCell(colNo).setCellStyle(defaultStyle);
        cHRow.createCell(colNo).setCellStyle(headerStyle);

	// Designing the header for Result (class) section
        cell = TPRow.createCell(colNo);
        cell.setCellValue("Result");
        cell.setCellStyle(headerStyle);
        row.createCell(colNo).setCellStyle(defaultStyle);
        cHRow.createCell(colNo).setCellStyle(headerStyle);

	// Merging all the cells at the top of the final section to make it look complete
        sheet.addMergedRegion(new CellRangeAddress(0, 0, colNo - 3, colNo));

	// Adding the newly created sheet to list of unique sheets
        uSheets.add(new USheet(m, sheet));

	// Return the sheet that was created, so that it can get it's first entry from marksheet m by the caller method 
        return sheet;
    }

/*
    // Gets the set of UIDs that were found invalid during the generation of the report
    public List<String> getInvalidUIDs(){
      return invalidUIDs;
    }
*/
    // A private constructor that creates a report from the given parameters
    private Report(ReportGeneratorObserver rgo, String sampleLink, String[] UIDs, String outputFileName) throws IOException, InterruptedException
    {
        if(rgo==null) rgo = ReportGeneratorObserver.DEFAULT;
       
	// Sanitize the entered file name get an appropriate workbook for it based on it's type
	// There are mainly two types of excel formats (XSSF (XML SpreadSheet Format) and HSSF (Horrible SpreadSheet Format)
	// xlsx files use XSSF and xls files use HSSF. HSSF is older than XSSF.
        if(outputFileName.endsWith(".xlsx")) workbook = new XSSFWorkbook();
        else if(outputFileName.endsWith(".xls")) workbook = new HSSFWorkbook();
        else {
	  outputFileName+=".xlsx";
	  workbook = new XSSFWorkbook();
	}

	// Check if the file can be overwritten/created and if it can be then lock it until the we successfully write to it or abort the program

	// Try to get the prefix from the sample link
        final String prefix = Marksheet.getPrefixFrom(sampleLink);

	if(prefix.isEmpty())
	{
	  // fos.close();
	  throw new IOException("Invalid sample link. (Could not get the prefix from the sample link)");
	}

	/// Initializing the style for the header cells (headerStyle) and entries (defaultStyle)
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

        int cUIDs = 0;

        ArrayList<Marksheet> marksheets = new ArrayList<Marksheet>();

	// Iterating through the array of the passed UIDs with the help of a for each loop
	forUIDs:
        for (String UID: UIDs) {

	    Marksheet m = null;

	    // This retry logic was created
            boolean retry = true;
	    while(retry)
	    {
              retry = false;

              try
	      {
		rgo.onMarksheetGenStart(UID);
		// Try to generate a marksheet for the given UID
	  	m = new Marksheet(prefix, UID.trim());
		rgo.onMarksheetGenSuccess(UID);
	      }
	      catch(UnknownHostException e)
	      { 
	  	Boolean t = rgo.onConnectionIssue(UID);
		if(t==true) retry = true;
		else if(t==false) break forUIDs;
		else throw new InterruptedException("The report generation was force stopped by the Observer.");
	      }
              catch(FileNotFoundException e)
	      {
		Boolean t = rgo.onMarksheetNotFound(UID);
		if(t==true) continue forUIDs;
		else if(t==false) break forUIDs;
		else throw new InterruptedException("The report generation was force stopped by the observer.");
	      }
	      catch(Exception e)
	      {
		// Add the current UID to the list of invalidUIDs if the creation for the current marksheet fails for any reason
		rgo.onInvalidUID(UID,e);
	  	continue forUIDs;
	      }

	    }

	    // Get a compatible sheet for the current marksheet
            final Sheet sheet = getSheet(m);

      	    rgo.onSheetFound(UID);

            try
            {

	    // Create a row (after the last row) to which all the required details of the marksheet will be written to
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);

	    /// Fill in all the details for the current student with the help of the parsed marksheet m

	    // Sr No.
            cell = row.createCell(0);
            cell.setCellValue(sheet.getLastRowNum() - 2);
            cell.setCellStyle(defaultStyle);

	    // Seat No.
            cell = row.createCell(1);
            cell.setCellValue(m.getSeatNumber());
            cell.setCellStyle(defaultStyle);

	    // Enrollment No.
            cell = row.createCell(2);
            cell.setCellValue(m.getEnrollmentNumber());
            cell.setCellStyle(defaultStyle);

	    // Name
            cell = row.createCell(3);
            cell.setCellValue(m.getName());
            cell.setCellStyle(defaultStyle);

	    // A counter variable i that acts as an counter for every column 
            int i = 3;

	    // Iterate through all the courses of the marksheet and (dynamically) fill in the relevant details
            for (Course c : m.getCourses()) {

		// Note: The try-catch pattern was used here as a marksheet could have special marks (eg. KT, grace marks, ...) that would have an prefix or 
		// postfix to represent it.

		// If the course has theory (which is guaranteed to be before PR; if it exists), then fill in all the details of it from the given marksheet
                if (c.hasTheory())
	        {
		  // ESE (TH)
                  cell = row.createCell(++i);
                  try {
                    cell.setCellValue(c.getTheoryMarks().getESE().getValueAsInt());
                  } catch (Exception e) {
                    cell.setCellValue(c.getTheoryMarks().getESE().getValue());
                  }
                  cell.setCellStyle(defaultStyle);

		  // PA (TH)
                  cell = row.createCell(++i);
                  try {
                    cell.setCellValue(c.getTheoryMarks().getPA().getValueAsInt());
                  } catch (Exception e) {
                    cell.setCellValue(c.getTheoryMarks().getPA().getValue());
                  }
                  cell.setCellStyle(defaultStyle);

 		  // Total (TH)
                  cell = row.createCell(++i);
                  try {
                    cell.setCellValue(c.getTheoryMarks().getValueAsInt());
                  }
		  catch(Exception e)
		  {
		     cell.setCellValue(c.getTheoryMarks().getValue());
                  }
                  cell.setCellStyle(defaultStyle);
                }

		// If the course has practicals, then fill in the details of it (i will ensure that everything is in order irrespective of what was
		// previously filled)
                if (c.hasPracticals())
		{
  		  // ESE (PR)
                  cell = row.createCell(++i);
                  try {
                    cell.setCellValue(c.getPracticalMarks().getESE().getValueAsInt());
                  } catch (Exception e) {
                    cell.setCellValue(c.getPracticalMarks().getESE().getValue());
                  }
                  cell.setCellStyle(defaultStyle);

  		  // PA (PR)
                  cell = row.createCell(++i);
                  try {
                    cell.setCellValue(c.getPracticalMarks().getPA().getValueAsInt());
                  } catch (Exception e) {
                    cell.setCellValue(c.getPracticalMarks().getPA().getValue());
                  }
                  cell.setCellStyle(defaultStyle);

  		  // Total (PR)
                  cell = row.createCell(++i);
                  try {
                    cell.setCellValue(c.getPracticalMarks().getValueAsInt());
                  } catch (Exception e) {
                    cell.setCellValue(c.getPracticalMarks().getValue());
                  }
                  cell.setCellStyle(defaultStyle);
                }

		// Credits (Course)
                cell = row.createCell(++i);
                try {
                    cell.setCellValue(c.getCreditsAsInt());
                } catch (Exception e) {
                    cell.setCellValue(c.getCredits());
                }
                cell.setCellStyle(defaultStyle);
            }

	    // Total Credits
            cell = row.createCell(++i);
            cell.setCellValue(m.getTotalCredits());
            cell.setCellStyle(headerStyle);

	    // Total Marks
            cell = row.createCell(++i);
            cell.setCellValue(m.getTotalMarks());
            cell.setCellStyle(headerStyle);

	    // Percentage
            cell = row.createCell(++i);
            cell.setCellValue(Math.round(m.getPercentage() * 100.0) / 100.0);
            cell.setCellStyle(headerStyle);

	    // Result (Class)
            cell = row.createCell(++i);
            cell.setCellValue(m.getResultClass());
            cell.setCellStyle(headerStyle);

	    rgo.onMarksheetEntrySuccess(UID);
            ++cUIDs;
          }
	  catch(Exception e)
          {
            rgo.onMarksheetEntryFailed(UID);
          }

          marksheets.add(m);
        }

        // Parth 123 - Use Find Parth to reach here

        ArrayList<String> courseCodes = new ArrayList<String>();

        for(Marksheet marksheet : marksheets)
        {
          System.out.println(marksheet);
        }

        

	// By default the contents of the workbook aren't sized with respect to their column heights, so this method shakes all created cells of every column
	// of all the sheets to ensure that everything in the generated excel file looks fine
        autoFitContentsOfWorkbook();

        FileOutputStream fos;

        while(true)
	{
    	  try
	  {
	    fos = new FileOutputStream(outputFileName);
	    break;
	  }
	  catch(Exception e)
	  {
	    String newFilePath = rgo.onOutputFileLocked(outputFileName);
	    if(newFilePath==null) throw e;
	    else {
	      outputFileName = newFilePath;
	      continue;
	    }
	  }
	}

	// Finally generate the excel file (report) we were looking for from it's object representation workbook.
        workbook.write(fos);

        // Close the file stream (or unlock/free the (excel) file)
        fos.close();

	// Notify the observer that the report generation is complete
        rgo.onReportGenDone(outputFileName);
    }

    // A method that auto-fits the contents of the [workbook] of this report
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

}