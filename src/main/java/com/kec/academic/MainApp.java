package com.kec.academic;

import java.sql.SQLException;
import java.util.Scanner;

public class MainApp {
    private static final Scanner in = new Scanner(System.in);
    private static final StudentDAO students = new StudentDAO();
    private static final AcademicDAO academic = new AcademicDAO();

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println(" KONGU ENGINEERING COLLEGE - R2024 ACADEMIC SYSTEM");
        System.out.println("====================================================");
        while (true) {
            System.out.println("\n1. Add Student\n2. Add Course\n3. Enroll Student + Attendance\n4. Enter/Update CIA and ESE Marks\n5. Update Attendance / Condonation\n6. Student Semester Mark Sheet + SGPA\n7. Attendance Defaulter Report\n8. Department Result Summary\n0. Exit");
            int choice = number("Choose option: ");
            try {
                switch (choice) {
                    case 1 -> addStudent(); case 2 -> addCourse(); case 3 -> enroll();
                    case 4 -> marks(); case 5 -> attendance(); case 6 -> academic.printMarkSheet(read("Roll number: "), number("Semester: "));
                    case 7 -> academic.printAttendanceDefaulters(number("Semester: "));
                    case 8 -> academic.printClassSummary(number("Semester: "));
                    case 0 -> { System.out.println("Application closed."); return; }
                    default -> System.out.println("Invalid menu option.");
                }
            } catch (SQLException | IllegalArgumentException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }
    private static void addStudent() throws SQLException {
        String roll=read("Roll number (e.g. 24ITR001): ").toUpperCase();
        String name=read("Student name: "); String dept=read("Department (IT/CSE/ECE/MECH): ").toUpperCase();
        int sem=number("Current semester (1-8): "); students.addStudent(roll,name,dept,sem);System.out.println("Student added successfully.");
    }
    private static void addCourse() throws SQLException {
        String code=read("Course code (e.g. 24ITR31): ").toUpperCase();String title=read("Course title: ");double credits=decimal("Credits: ");
        String type=read("Course type (THEORY/PRACTICAL/EMBEDDED): ").toUpperCase();students.addCourse(code,title,credits,type);System.out.println("Course added successfully.");
    }
    private static void enroll() throws SQLException {
        String roll=read("Roll number: ").toUpperCase(),course=read("Course code: ").toUpperCase();int sem=number("Semester: ");
        int total=number("Total classes held: "),att=number("Classes attended: ");boolean cond=yes("Medical condonation approved? (y/n): ");
        academic.enroll(roll,course,sem,total,att,cond);System.out.println("Enrollment and initial attendance saved.");
    }
    private static void marks() throws SQLException {
        String roll=read("Roll number: ").toUpperCase(),course=read("Course code: ").toUpperCase();int sem=number("Semester: ");
        System.out.println("Leave a mark blank to keep its existing value. Enter AB for absent ESE.");
        Double c1=optionalMark("CIA Test 1 (0-100): "),c2=optionalMark("CIA Test 2 (0-100): "),ass=optionalMark("Assignment/Quiz/Lab mark (0-100): ");
        String eseText=read("ESE mark (0-100), AB, or blank to keep: ");boolean absent=eseText.equalsIgnoreCase("AB");Double ese=eseText.isBlank()||absent?null:parseMark(eseText);
        academic.updateMarks(roll,course,sem,c1,c2,ass,ese,absent);System.out.println("Assessment marks updated successfully.");
    }
    private static void attendance() throws SQLException {
        String roll=read("Roll number: ").toUpperCase(),course=read("Course code: ").toUpperCase();int sem=number("Semester: ");int total=number("Total classes held: "),att=number("Classes attended: ");boolean cond=yes("Medical condonation approved? (y/n): ");
        academic.updateAttendance(roll,course,sem,total,att,cond);System.out.println("Attendance updated successfully.");
    }
    private static Double optionalMark(String prompt){String s=read(prompt);return s.isBlank()?null:parseMark(s);}
    private static Double parseMark(String s){double d=Double.parseDouble(s);if(d<0||d>100)throw new IllegalArgumentException("Marks must be between 0 and 100.");return d;}
    private static String read(String p){System.out.print(p);return in.nextLine().trim();}
    private static int number(String p){return Integer.parseInt(read(p));}
    private static double decimal(String p){return Double.parseDouble(read(p));}
    private static boolean yes(String p){return read(p).equalsIgnoreCase("y");}
}
