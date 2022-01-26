[1mdiff --git a/org/tpoly/msbte/CHead.class b/org/tpoly/msbte/CHead.class[m
[1mindex 46b6aaa..634c16a 100644[m
Binary files a/org/tpoly/msbte/CHead.class and b/org/tpoly/msbte/CHead.class differ
[1mdiff --git a/org/tpoly/msbte/Course.class b/org/tpoly/msbte/Course.class[m
[1mindex 7383928..2645f66 100644[m
Binary files a/org/tpoly/msbte/Course.class and b/org/tpoly/msbte/Course.class differ
[1mdiff --git a/org/tpoly/msbte/ESE.class b/org/tpoly/msbte/ESE.class[m
[1mindex 4d8ff17..a05ed72 100644[m
Binary files a/org/tpoly/msbte/ESE.class and b/org/tpoly/msbte/ESE.class differ
[1mdiff --git a/org/tpoly/msbte/Marks.class b/org/tpoly/msbte/Marks.class[m
[1mindex 417a98c..df7221f 100644[m
Binary files a/org/tpoly/msbte/Marks.class and b/org/tpoly/msbte/Marks.class differ
[1mdiff --git a/org/tpoly/msbte/Marksheet$1.class b/org/tpoly/msbte/Marksheet$1.class[m
[1mindex 78f0303..2be6605 100644[m
Binary files a/org/tpoly/msbte/Marksheet$1.class and b/org/tpoly/msbte/Marksheet$1.class differ
[1mdiff --git a/org/tpoly/msbte/Marksheet.class b/org/tpoly/msbte/Marksheet.class[m
[1mindex c4c51fd..730e66d 100644[m
Binary files a/org/tpoly/msbte/Marksheet.class and b/org/tpoly/msbte/Marksheet.class differ
[1mdiff --git a/org/tpoly/msbte/PA.class b/org/tpoly/msbte/PA.class[m
[1mindex 5062640..194e6e6 100644[m
Binary files a/org/tpoly/msbte/PA.class and b/org/tpoly/msbte/PA.class differ
[1mdiff --git a/org/tpoly/msbte/PR.class b/org/tpoly/msbte/PR.class[m
[1mindex d190b20..28d13e2 100644[m
Binary files a/org/tpoly/msbte/PR.class and b/org/tpoly/msbte/PR.class differ
[1mdiff --git a/org/tpoly/msbte/Report.class b/org/tpoly/msbte/Report.class[m
[1mindex 00f379a..dd53333 100644[m
Binary files a/org/tpoly/msbte/Report.class and b/org/tpoly/msbte/Report.class differ
[1mdiff --git a/org/tpoly/msbte/ReportGeneratorObserver.class b/org/tpoly/msbte/ReportGeneratorObserver.class[m
[1mindex fbaf24d..7f27686 100644[m
Binary files a/org/tpoly/msbte/ReportGeneratorObserver.class and b/org/tpoly/msbte/ReportGeneratorObserver.class differ
[1mdiff --git a/org/tpoly/msbte/TH.class b/org/tpoly/msbte/TH.class[m
[1mindex 5189a44..d54dea7 100644[m
Binary files a/org/tpoly/msbte/TH.class and b/org/tpoly/msbte/TH.class differ
[1mdiff --git a/org/tpoly/msbte/USheet.class b/org/tpoly/msbte/USheet.class[m
[1mindex dc01a1b..827ef06 100644[m
Binary files a/org/tpoly/msbte/USheet.class and b/org/tpoly/msbte/USheet.class differ
[1mdiff --git a/src/Marksheet.java b/src/Marksheet.java[m
[1mindex 1bc82ce..d2117ae 100644[m
[1m--- a/src/Marksheet.java[m
[1m+++ b/src/Marksheet.java[m
[36m@@ -71,15 +71,15 @@[m [mpublic class Marksheet[m
 [m
 	// If the above operation has discovered the file we were looking for[m
         if (cacheFiles!=null && cacheFiles.length != 0) {[m
[31m-[m
 	    // Parse the contents of that file (the first file) into a Document object, that could be used to logically access the raw data/html file[m
[31m-	    String fileContents = String.join("\n", Files.readAllLines(Paths.get(cacheFiles[0].getPath()), StandardCharsets.UTF_8));[m
[32m+[m	[32m    String fileContents = new String(Files.readAllBytes(cacheFiles[0].toPath()));[m
             document = Jsoup.parse(fileContents);[m
         }[m
 [m
         // Else retrieve the raw data of the marksheet over the network and cache it for future (re)use[m
         else[m
 	{[m
[32m+[m[32m            System.out.println(uid);[m
 	    // Declare a common variable to store the link[m
             String link = "";[m
 [m
[1mdiff --git a/src/Report.java b/src/Report.java[m
[1mindex fefb3b9..8a75925 100644[m
[1m--- a/src/Report.java[m
[1m+++ b/src/Report.java[m
[36m@@ -401,8 +401,9 @@[m [mpublic class Report[m
 	      }[m
 	      catch(Exception e)[m
 	      {[m
[32m+[m[32m                System.out.println("Unhandled error while generating " + UID + "..." + e);[m
 		// Add the current UID to the list of invalidUIDs if the creation for the current marksheet fails for any reason[m
[31m-		rgo.onInvalidUID(UID,e);[m
[32m+[m		[32mrgo.onInvalidUID(UID, e);[m
 	  	continue forUIDs;[m
 	      }[m
 [m
[1mdiff --git a/src/test.bat b/src/test.bat[m
[1mindex 868d5c1..2231944 100644[m
[1m--- a/src/test.bat[m
[1m+++ b/src/test.bat[m
[36m@@ -5,6 +5,7 @@[m [mjavac -d .. -cp ../lib/* *.java[m
 IF "%ERRORLEVEL%" == "0" ([m
   cd ..[m
   run.bat[m
[32m+[m[32m  cd src[m
 ) ELSE ([m
   echo Could not generate the required class files.[m
   echo Please solve the above mentioned errors in order to proceed.[m
