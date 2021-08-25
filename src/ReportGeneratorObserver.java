package org.tpoly.msbte;

public class ReportGeneratorObserver
{
  final static ReportGeneratorObserver DEFAULT = new ReportGeneratorObserver();

  // A method that is called when an invalid UID is found (either because the format is invalid or because a marksheet wasn't found on the server for the same)
  public void onInvalidUID(String invalidUID, Exception e){}

  // A method that is called when a new sheet is created (ie. When a different type of marksheet is found or the first sheet gets created)
  public void onSheetFound(String UID){}

  public void onMarksheetGenStart(String UID){}

  public void onMarksheetGenSuccess(String UID){}

  public void onMarksheetEntrySuccess(String UID){}

  public void onMarksheetEntryFailed(String UID){}

  public void onReportGenStart(){}

  public void onReportGenDone(String finalPath){System.out.println("Report Generated!");}

  public String onOutputFileLocked(String outputFileName){return null;}


  // This method will be called whenever there is an connection issue.
  // If this method returns true, then the report generation would retry fetching,
  // Else if it returns false, then it would stop the report generation and create a (excel) report from whatever it has (just in case if it stopped in between).
  // Else if it returns null, then it would force stop the entire process of report generation (without creating the excel sheet)
  public Boolean onConnectionIssue(String UID)
  {
    
    return true; // one could add something like a delay or wait for some user input
  }

  // This method will be called when a marksheet is not found on the server (after a successful transaction).
  // If this method returns true then the generator would skip the current marksheet.
  // Else if this method returns false then the generator would stop the generator and create a report from whatever it had.
  // Else this method returns null then the entire process of report generation would be force stopped.
  public Boolean onMarksheetNotFound(String UID)
  {
    return true;
  }
}