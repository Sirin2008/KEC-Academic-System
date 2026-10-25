
package com.kec.academic;

import java.sql.SQLException;
import java.util.Scanner;

public class MainApp {

    private static final Scanner in = new Scanner(System.in);
    private static final StudentDAO students = new StudentDAO();
    private static final AcademicDAO academic = new AcademicDAO();

    public static void main(String[] args) {

        System.out.println("======================================================================");
        System.out.println("             KONGU ENGINEERING COLLEGE");
        System.out.println("             R2024 STUDENT ACADEMIC SYSTEM");
        System.out.println("======================================================================");
        System.out.println("  STUDENT MANAGEMENT | MARKS | ATTENDANCE | SGPA | REPORTS");
        System.out.println("======================================================================");

        while (true) {
            System.out.println("\n1. Add Student");
            System.out.println("2. Add Course");
            System.out.println("3. Enroll Student + Attendance");
            System.out.println("4. Enter/Update CIA and ESE Marks");
            System.out.println("5. Update Attendance / Condonation");
            System.out.println("6. Student Semester Mark Sheet + SGPA");
            System.out.println("7. Attendance Defaulter Report");
            System.out.println("8. Department Result Summary");
            System.out.println("9. All Student Academic Report");
            System.out.println("0. Exit");

            int choice;
            try {
                choice = number("Choose option: ");
            } catch (IllegalArgumentException ex) {
                System.out.println("Please enter a valid menu number.");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> addStudent();
                    case 2 -> addCourse();
                    case 3 -> enroll();
                    case 4 -> marks();
                    case 5 -> attendance();
                    case 6 -> academic.printMarkSheet(
                            read("Roll number: ").toUpperCase(),
                            number("Semester: "));
                    case 7 -> academic.printAttendanceDefaulters(
                            number("Semester: "));
                    case 8 -> academic.printClassSummary(
                            number("Semester: "));
                    case 9 -> academic.printAllStudents(
                            number("Semester: "));
                    case 0 -> {
                        System.out.println("Application closed.");
                        in.close();
                        return;
                    }
                    default -> System.out.println("Invalid menu option.");
                }
            } catch (SQLException | IllegalArgumentException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    private static void addStudent() throws SQLException {
        String roll = read("Roll number: ").toUpperCase();
        String name = read("Student name: ");
        String dept = read("Department (IT/CSE/ECE/MECH): ").toUpperCase();
        int sem = number("Current semester (1-8): ");

        students.addStudent(roll, name, dept, sem);
        System.out.println("Student added successfully.");
    }

    private static void addCourse() throws SQLException {
        String code = read("Course code: ").toUpperCase();
        String title = read("Course title: ");
        double credits = decimal("Credits: ");
        String type = read("Course type (THEORY/PRACTICAL/EMBEDDED): ")
                .toUpperCase();

        students.addCourse(code, title, credits, type);
        System.out.println("Course added successfully.");
    }

    private static void enroll() throws SQLException {
        String roll = read("Roll number: ").toUpperCase();
        String course = read("Course code: ").toUpperCase();
        int sem = number("Semester: ");
        int total = number("Total classes held: ");
        int attended = number("Classes attended: ");
        boolean cond = yes("Medical condonation approved? (y/n): ");

        academic.enroll(roll, course, sem, total, attended, cond);
        System.out.println("Enrollment and attendance saved.");
    }

    private static void marks() throws SQLException {
        String roll = read("Roll number: ").toUpperCase();
        String course = read("Course code: ").toUpperCase();
        int sem = number("Semester: ");

        System.out.println("Enter AB if the student was absent for ESE.");
        System.out.println("Leave a mark blank to keep its existing value.");

        Double c1 = optionalMark("CIA Test 1 (0-100): ");
        Double c2 = optionalMark("CIA Test 2 (0-100): ");
        Double ass = optionalMark("Assignment mark (0-100): ");

        String eseText = read("ESE mark (0-100), AB, or blank: ");
        boolean absent = eseText.equalsIgnoreCase("AB");
        Double ese = eseText.isBlank() || absent
                ? null : parseMark(eseText);

        academic.updateMarks(roll, course, sem,
                c1, c2, ass, ese, absent);

        System.out.println("Assessment marks updated successfully.");
    }

    private static void attendance() throws SQLException {
        String roll = read("Roll number: ").toUpperCase();
        String course = read("Course code: ").toUpperCase();
        int sem = number("Semester: ");
        int total = number("Total classes held: ");
        int attended = number("Classes attended: ");
        boolean cond = yes("Medical condonation approved? (y/n): ");

        academic.updateAttendance(
                roll, course, sem, total, attended, cond);

        System.out.println("Attendance updated successfully.");
    }

    private static Double optionalMark(String prompt) {
        String s = read(prompt);
        return s.isBlank() ? null : parseMark(s);
    }

    private static Double parseMark(String s) {
        try {
            double d = Double.parseDouble(s);
            if (d < 0 || d > 100) {
                throw new IllegalArgumentException(
                        "Marks must be between 0 and 100.");
            }
            return d;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Enter a number between 0 and 100.");
        }
    }

    private static String read(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    
private static int number(String prompt) {
    while (true) {
        String value = read(prompt);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            System.out.println(
                    "Invalid input! Please enter a whole number, like 3.");
        }
    }
}


    private static double decimal(String prompt) {
        try {
            return Double.parseDouble(read(prompt));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Please enter a valid number.");
        }
    }

    private static boolean yes(String prompt) {
        return read(prompt).equalsIgnoreCase("y");
    }
}
