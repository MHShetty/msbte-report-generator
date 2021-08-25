package org.tpoly.msbte;

public class TH extends CHead
{
    // Used to just create an empty object
    TH() {
        super(null, null, null, null);
    }

    TH(ESE ese, PA pa, String max, String total) {
        super(ese, pa, max, total);
    }

    public String toString() {
        return "Theory:\n " + super.toString();
    }
}