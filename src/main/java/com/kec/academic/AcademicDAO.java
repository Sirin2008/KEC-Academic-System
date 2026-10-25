
package com.kec.academic;

import java.sql.*;

public class AcademicDAO {

    public void enroll(String roll, String course, int semester,
                       int total, int attended, boolean condonation)
            throws SQLException {

        if (total < 0 || attended < 0 || attended > total) {
            throw new IllegalArgumentException("Attendance values are invalid.");
        }

        String sql = "INSERT INTO enrollments(roll_no,course_code,semester,total_classes,classes_attended,medical_condonation) VALUES(?,?,?,?,?,?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            p.setString(1, roll);
            p.setString(2, course);
            p.setInt(3, semester);
            p.setInt(4, total);
            p.setInt(5, attended);
            p.setBoolean(6, condonation);
            p.executeUpdate();

            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next()) {
                    ensureMarksRow(c, r.getInt(1));
                }
            }
        }
    }

    private void ensureMarksRow(Connection c, int id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO assessment_marks(enrollment_id) VALUES(?)")) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }

    private int enrollmentId(String roll, String course, int sem)
            throws SQLException {

        String sql = "SELECT enrollment_id FROM enrollments WHERE roll_no=? AND course_code=? AND semester=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, roll);
            p.setString(2, course);
            p.setInt(3, sem);

            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return r.getInt(1);
                }
            }
        }

        throw new SQLException("Enrollment not found. Enroll the student in this course first.");
    }

    public void updateMarks(String roll, String course, int sem,
                            Double cia1, Double cia2, Double assignment,
                            Double ese, boolean absent) throws SQLException {

        if (absent && ese != null) {
            throw new IllegalArgumentException("For AB, leave ESE mark empty.");
        }

        validateMark(cia1);
        validateMark(cia2);
        validateMark(assignment);
        validateMark(ese);

        int id = enrollmentId(roll, course, sem);

        String sql = "UPDATE assessment_marks SET cia1=COALESCE(?,cia1),cia2=COALESCE(?,cia2),assignment_mark=COALESCE(?,assignment_mark),ese_mark=?,ese_absent=? WHERE enrollment_id=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            setNullable(p, 1, cia1);
            setNullable(p, 2, cia2);
            setNullable(p, 3, assignment);
            setNullable(p, 4, absent ? null : ese);
            p.setBoolean(5, absent);
            p.setInt(6, id);
            p.executeUpdate();
        }
    }

    private void validateMark(Double x) {
        if (x != null && (x < 0 || x > 100)) {
            throw new IllegalArgumentException("Marks must be between 0 and 100.");
        }
    }

    private void setNullable(PreparedStatement p, int index, Double x)
            throws SQLException {

        if (x == null) {
            p.setNull(index, Types.DECIMAL);
        } else {
            p.setDouble(index, x);
        }
    }

    public void updateAttendance(String roll, String course, int sem,
                                 int total, int attended, boolean condonation)
            throws SQLException {

        if (total < 0 || attended < 0 || attended > total) {
            throw new IllegalArgumentException("Attendance values are invalid.");
        }

        String sql = "UPDATE enrollments SET total_classes=?,classes_attended=?,medical_condonation=? WHERE roll_no=? AND course_code=? AND semester=?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, total);
            p.setInt(2, attended);
            p.setBoolean(3, condonation);
            p.setString(4, roll);
            p.setString(5, course);
            p.setInt(6, sem);

            if (p.executeUpdate() == 0) {
                throw new SQLException("Enrollment not found.");
            }
        }
    }

    private String grade(double total) {
        if (total >= 91) return "O";
        if (total >= 81) return "A+";
        if (total >= 71) return "A";
        if (total >= 61) return "B+";
        if (total >= 50) return "B";
        return "RA";
    }

    private int points(String g) {
        return switch (g) {
            case "O" -> 10;
            case "A+" -> 9;
            case "A" -> 8;
            case "B+" -> 7;
            case "B" -> 6;
            default -> 0;
        };
    }

    public void printMarkSheet(String roll, int sem) throws SQLException {

        String sql = "SELECT s.roll_no,s.student_name,s.department,e.semester,e.total_classes,e.classes_attended,e.medical_condonation,c.course_code,c.title,c.credits,c.course_type,m.cia1,m.cia2,m.assignment_mark,m.ese_mark,m.ese_absent FROM enrollments e JOIN students s ON s.roll_no=e.roll_no JOIN courses c ON c.course_code=e.course_code JOIN assessment_marks m ON m.enrollment_id=e.enrollment_id WHERE s.roll_no=? AND e.semester=? ORDER BY c.course_code";

        double weightedPoints = 0;
        double credits = 0;
        boolean found = false;

        System.out.println("\n================ KEC R2024 ACADEMIC MARK SHEET ================");
        System.out.printf("%-10s %-22s %-8s %-8s %-8s %-8s %-6s %-5s %-5s%n",
                "COURSE", "TITLE", "CIA/100", "ESE/100",
                "TOTAL", "ATTEND%", "GRADE", "GP", "STATUS");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, roll);
            p.setInt(2, sem);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    found = true;

                    String type = r.getString("course_type");
                    double credit = r.getDouble("credits");
                    int totalClasses = r.getInt("total_classes");
                    int attended = r.getInt("classes_attended");

                    double attendance = totalClasses == 0
                            ? 0 : 100.0 * attended / totalClasses;

                    Double a = nullable(r, "cia1");
                    Double b = nullable(r, "cia2");
                    Double d = nullable(r, "assignment_mark");
                    Double ese = nullable(r, "ese_mark");

                    boolean absent = r.getBoolean("ese_absent");
                    boolean cond = r.getBoolean("medical_condonation");

                    double ciaAverage = average(a, b, d);
                    double ciaWeight = type.equals("THEORY") ? 0.40
                            : type.equals("PRACTICAL") ? 0.60 : 0.50;

                    double eseWeight = 1 - ciaWeight;
                    double eseThreshold = type.equals("THEORY") ? 27
                            : type.equals("PRACTICAL") ? 20 : 22.5;

                    double ciaComponent = ciaAverage * ciaWeight;
                    double eseComponent = ese == null ? 0 : ese * eseWeight;
                    double total = ciaComponent + eseComponent;

                    String g;
                    String status;

                    if (attendance < 65) {
                        g = "SA";
                        status = "BARRED";
                    } else if (attendance < 75 && !cond) {
                        g = "SA";
                        status = "CONDONATION NEEDED";
                    } else if (absent) {
                        g = "AB";
                        status = "FAIL";
                    } else if (ese == null) {
                        g = "PENDING";
                        status = "ESE PENDING";
                    } else if (eseComponent < eseThreshold || total < 50) {
                        g = "RA";
                        status = "FAIL";
                    } else {
                        g = grade(total);
                        status = "PASS";
                    }

                    int gp = points(g);

                    if (!g.equals("PENDING")) {
                        weightedPoints += credit * gp;
                        credits += credit;
                    }

                    String ciaText = (a == null && b == null && d == null)
                            ? "-" : String.format("%.1f", ciaAverage);

                    String eseText = absent ? "AB"
                            : ese == null ? "-" : String.format("%.1f", ese);

                    String totalText = g.equals("PENDING")
                            ? "-" : String.format("%.1f", total);

                    System.out.printf(
                            "%-10s %-22.22s %-8s %-8s %-8s %-8.1f %-6s %-5d %-5s%n",
                            r.getString("course_code"),
                            r.getString("title"),
                            ciaText, eseText, totalText,
                            attendance, g, gp, status
                    );
                }
            }
        }

        if (!found) {
            System.out.println("No enrollments found for this roll number and semester.");
            return;
        }

        double sgpa = credits == 0 ? 0 : weightedPoints / credits;

        System.out.printf("SGPA: %.2f | Equivalent Percentage: %.2f%%%n",
                sgpa, sgpa * 10);

        System.out.println("Note: CIA component = average of CIA1, CIA2 and assignment mark × CIA weight; missing CIA entries are treated as zero until entered.");
        System.out.println("===============================================================\n");
    }

    private Double nullable(ResultSet r, String col) throws SQLException {
        double x = r.getDouble(col);
        return r.wasNull() ? null : x;
    }

    private double average(Double... values) {
        double sum = 0;
        for (Double v : values) {
            sum += (v == null ? 0 : v);
        }
        return sum / values.length;
    }

    public void printAttendanceDefaulters(int sem) throws SQLException {

        String sql = "SELECT s.roll_no,s.student_name,s.department,e.course_code,e.total_classes,e.classes_attended FROM enrollments e JOIN students s ON s.roll_no=e.roll_no WHERE e.semester=? AND e.total_classes>0 AND 100.0*e.classes_attended/e.total_classes<65 ORDER BY s.department,s.roll_no";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, sem);

            try (ResultSet r = p.executeQuery()) {
                System.out.println("\n--- ATTENDANCE DEFAULTERS (SA: below 65%) ---");

                while (r.next()) {
                    System.out.printf("%s | %-22s | %s | %s | %.1f%% | Grade: SA%n",
                            r.getString("roll_no"),
                            r.getString("student_name"),
                            r.getString("department"),
                            r.getString("course_code"),
                            100.0 * r.getInt("classes_attended")
                                    / r.getInt("total_classes"));
                }
            }
        }
    }

    public void printClassSummary(int sem) throws SQLException {

        String sql = "SELECT s.department,s.roll_no,e.total_classes,e.classes_attended,e.medical_condonation,c.credits,c.course_type,m.cia1,m.cia2,m.assignment_mark,m.ese_mark,m.ese_absent FROM enrollments e JOIN students s ON s.roll_no=e.roll_no JOIN courses c ON c.course_code=e.course_code JOIN assessment_marks m ON m.enrollment_id=e.enrollment_id WHERE e.semester=?";

        class Row {
            double pts = 0;
            double cr = 0;
            int pass = 0;
            int fail = 0;
        }

        java.util.Map<String, Row> map = new java.util.TreeMap<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, sem);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {

                    String dept = r.getString("department");
                    String type = r.getString("course_type");
                    Row row = map.computeIfAbsent(dept, k -> new Row());

                    int t = r.getInt("total_classes");
                    int a = r.getInt("classes_attended");

                    double att = t == 0 ? 0 : 100.0 * a / t;
                    boolean cond = r.getBoolean("medical_condonation");
                    boolean abs = r.getBoolean("ese_absent");

                    Double x = nullable(r, "cia1");
                    Double y = nullable(r, "cia2");
                    Double z = nullable(r, "assignment_mark");
                    Double ese = nullable(r, "ese_mark");

                    if (att < 65 || (att < 75 && !cond) || abs || ese == null) {
                        if (ese != null || abs || att < 65 || (att < 75 && !cond)) {
                            row.fail++;
                        }
                        continue;
                    }

                    double cw = type.equals("THEORY") ? 0.4
                            : type.equals("PRACTICAL") ? 0.6 : 0.5;

                    double total = average(x, y, z) * cw + ese * (1 - cw);
                    double threshold = type.equals("THEORY") ? 27
                            : type.equals("PRACTICAL") ? 20 : 22.5;

                    String g = (total >= 50 && ese * (1 - cw) >= threshold)
                            ? grade(total) : "RA";

                    int gp = points(g);
                    row.pts += r.getDouble("credits") * gp;
                    row.cr += r.getDouble("credits");

                    if (gp > 0) {
                        row.pass++;
                    } else {
                        row.fail++;
                    }
                }
            }
        }

        System.out.println("\n--- DEPARTMENT RESULT SUMMARY ---");
        System.out.printf("%-12s %-10s %-10s %-12s%n",
                "DEPARTMENT", "PASS", "FAIL/SA", "AVG SGPA");

        for (var e : map.entrySet()) {
            Row r = e.getValue();

            System.out.printf("%-12s %-10d %-10d %-12.2f%n",
                    e.getKey(), r.pass, r.fail,
                    r.cr == 0 ? 0 : r.pts / r.cr);
        }
    }

    public void printAllStudents(int semester) throws SQLException {

        String sql = """
            SELECT s.roll_no, s.student_name, s.department,
                   e.course_code, m.cia1, m.cia2,
                   m.assignment_mark, m.ese_mark, m.ese_absent,
                   e.total_classes, e.classes_attended
            FROM students s
            JOIN enrollments e ON s.roll_no = e.roll_no
            JOIN assessment_marks m ON e.enrollment_id = m.enrollment_id
            WHERE e.semester = ?
            ORDER BY s.roll_no, e.course_code
            """;

        System.out.println("\n================ ALL STUDENT ACADEMIC REPORT ================");
        System.out.printf("%-12s %-18s %-8s %-10s %-7s %-7s %-7s %-9s%n",
                "ROLL NO", "NAME", "DEPT", "COURSE",
                "CIA1", "CIA2", "ESE", "ATTEND%");

        int count = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, semester);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    count++;

                    int total = rs.getInt("total_classes");
                    int attended = rs.getInt("classes_attended");

                    double attendance = total == 0
                            ? 0 : 100.0 * attended / total;

                    String ese;

                    if (rs.getBoolean("ese_absent")) {
                        ese = "AB";
                    } else if (rs.getObject("ese_mark") == null) {
                        ese = "-";
                    } else {
                        ese = String.format("%.1f", rs.getDouble("ese_mark"));
                    }

                    String cia1 = rs.getObject("cia1") == null
                            ? "-" : rs.getString("cia1");

                    String cia2 = rs.getObject("cia2") == null
                            ? "-" : rs.getString("cia2");

                    System.out.printf(
                            "%-12s %-18.18s %-8s %-10s %-7s %-7s %-7s %-8.1f%%%n",
                            rs.getString("roll_no"),
                            rs.getString("student_name"),
                            rs.getString("department"),
                            rs.getString("course_code"),
                            cia1, cia2, ese, attendance
                    );
                }
            }
        }

        System.out.println("Total course enrollments displayed: " + count);
        System.out.println("=============================================================");
    }
}
