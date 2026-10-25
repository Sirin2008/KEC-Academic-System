# KEC R2024 Student Academic & Continuous Evaluation System

## Requirements
- JDK 17 or later
- MySQL Server 8.x
- VS Code + Extension Pack for Java
- Maven (or use VS Code Java support to import the `pom.xml` project)

## Setup
1. Open MySQL Workbench or MySQL command line and execute `src/main/resources/kec_schema.sql`.
2. Edit `src/main/java/com/kec/academic/DBConnection.java`: set `PASSWORD` to your MySQL root password. If your MySQL username is not `root`, update `USER` too.
3. Open this folder in VS Code. Wait for Maven dependencies to finish downloading.
4. Run `com.kec.academic.MainApp` using the Run button, or from terminal run `mvn compile exec:java -Dexec.mainClass=com.kec.academic.MainApp` after adding the exec plugin, or run using the Java extension.

## Evaluation assumptions
- CIA = average of CIA1, CIA2, and assignment/quiz/lab mark; missing items count as zero until entered.
- Weighted CIA component = CIA average × CIA weight (THEORY 40%, PRACTICAL 60%, EMBEDDED 50%). Weighted ESE component = raw ESE × remaining weight.
- Attendance below 65% => SA; 65%–74.99% requires approved condonation; >=75% is eligible.
- Passing requires total >= 50 and the weighted ESE component meeting the supplied threshold. ESE absence => AB.
- SGPA is calculated from graded courses with completed ESE marks; pending ESE courses are excluded until marks are entered.

This is an educational implementation of the requirements supplied for the project; confirm institutional regulations with the department before treating results as official.
