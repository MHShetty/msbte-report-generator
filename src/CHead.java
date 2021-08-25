package org.tpoly.msbte;

// The C(ourse)Head class represents a Course Head (ESE and PA)
public class CHead extends Marks
{
    private ESE ese;
    private PA pa;

    CHead(ESE ese, PA pa, String max, String total) {
        super(max, total);
        this.ese = ese;
        this.pa = pa;
    }

    ESE getESE() {
        return ese;
    }

    PA getPA() {
        return pa;
    }

    public String toString() {
        StringBuilder s = new StringBuilder(ese.toString() + "\n " + pa + "\n ");
        s.append("Overall:\n");
        s.append("  Maximum - " + getMaximum() + "\n");
        s.append("  Obtained - " + getValue() + "\n");
        return s.toString();
    }
}