package org.tpoly.msbte;

public class PR extends CHead
{
    PR(ESE ese, PA pa, String max, String total) {
        super(ese, pa, max, total);
    }

    public String toString() {
        return "Practicals:\n" + super.toString();
    }
}