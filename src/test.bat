@echo off

javac -d .. -cp ../lib/* *.java

IF "%ERRORLEVEL%" == "0" (
  cd ..
  run.bat
) ELSE (
  echo Could not generate the required class files.
  echo Please solve the above mentioned errors in order to proceed.
)

cd src