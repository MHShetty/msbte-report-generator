package org.tpoly.msbte;

public class Course
{
  private String title;
  private String credits;

  private TH th;
  private PR pr;

  Course(String title, TH theoryMarks, PR practicalMarks, String credits) {
    this.title = title;
    this.th = theoryMarks;
    this.pr = practicalMarks;
    this.credits = credits;
  }

  Course(String title, TH theoryMarks, String credits) {
   this.title = title;
   this.th = theoryMarks;
   this.credits = credits;
  }

    Course(String title, PR practicalMarks, String credits) {
        this.title = title;
        this.pr = practicalMarks;
    }

    TH getTheoryMarks() {
        return th;
    }

    PR getPracticalMarks() {
        return pr;
    }

    String getTitle() {
        return title;
    }

    String getCredits() {
        return credits;
    }

    int getCreditsAsInt() {
        return Integer.parseInt(credits);
    }

    boolean hasTheory() {
        return th != null;
    }

    boolean hasPracticals() {
        return pr != null;
    }

    public String toString() {
        StringBuilder s = new StringBuilder(getTitle() + " (Credits: " + credits + ")\n");

        if (hasTheory()) s.append(th.toString());
        if (hasPracticals()) s.append(pr.toString());

        return s.toString();
    }
}