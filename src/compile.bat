@echo off

rem --- java(c) 8 ---
rem C:\Program Files\Android\Android Studio\jre\bin\javac

javac -d .. -cp ../lib/* *.java

IF "%ERRORLEVEL%" == "0" (
  echo Success.
) ELSE (
  echo Could not generate the required class files.
  echo Please solve the above mentioned errors in order to proceed.
)