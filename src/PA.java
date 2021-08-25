package org.tpoly.msbte;

public class PA extends Marks
{
    PA(String max, String min, String obtained)
    {
      super(max, min, obtained);
    }

    public String toString()
    {
      return "PA:\n" + super.toString();
    }
}