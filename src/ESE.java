package org.tpoly.msbte;

public class ESE extends Marks
{
    ESE(String max, String min, String obtained)
    {
      super(max, min, obtained);
    }

    public String toString() {
        return "ESE:\n" + super.toString();
    }
}