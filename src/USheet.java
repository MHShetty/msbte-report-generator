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

// A helper class that is used to logically store a Marksheet and a (workbook) sheet.
//
// This class is used in the Report class to maintain a set of unique marksheets in order to ensure that a new sheet is created if one of the existing ones 
// isn't compatible (i.e. has a different set of subjects than the existing marksheets). If it is compatible, then a new row/entry is written onto that
// workbook sheet with the help of the obtained U(nique)Sheet.
public class USheet
{
    private Marksheet mSheet;
    private Sheet xlSheet;

    USheet(Marksheet mSheet, Sheet xlSheet) {
        this.mSheet = mSheet;
        this.xlSheet = xlSheet;
    }

    // Check if the passed marksheet is compatible with the stored marksheet
    boolean canAccept(Marksheet sheet) {
        return mSheet.isCompatibleWith(sheet);
    }

    // Get the sheet stored in this instance
    Sheet getSheet() {
        return xlSheet;
    }
}