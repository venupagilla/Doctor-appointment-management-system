import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class DoctorAppointmentSystem {
    private JFrame frame;
    private JPanel panel;
    private Connection connection;

    public DoctorAppointmentSystem() {
        try {
            connection = DBConnection.getConnection();
            frame = new JFrame("Doctor Appointment System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            panel = new JPanel();
            panel.setLayout(new GridLayout(5, 1, 10, 10));
            frame.add(panel);
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Connection Failed!");
        }
    }

    private void initialize() {
        JButton btnRegisterDoctor = new JButton("Register Doctor");
        btnRegisterDoctor.addActionListener(e -> registerDoctor());

        JButton btnViewDoctors = new JButton("View Doctors");
        btnViewDoctors.addActionListener(e -> viewDoctors());

        JButton btnBookAppointment = new JButton("Book Appointment");
        btnBookAppointment.addActionListener(e -> bookAppointment());

        JButton btnCancelAppointment = new JButton("Cancel Appointment");
        btnCancelAppointment.addActionListener(e -> cancelAppointment());

        JButton btnViewAppointments = new JButton("View Appointments");
        btnViewAppointments.addActionListener(e -> viewAppointments());

        panel.add(btnRegisterDoctor);
        panel.add(btnViewDoctors);
        panel.add(btnBookAppointment);
        panel.add(btnCancelAppointment);
        panel.add(btnViewAppointments);

        frame.setVisible(true);
    }

    private void registerDoctor() {
        String name = JOptionPane.showInputDialog("Enter Doctor's Name:");
        String specialization = JOptionPane.showInputDialog("Enter Specialization:");
        String contact = JOptionPane.showInputDialog("Enter Contact Number:");

        try (CallableStatement cs = connection.prepareCall("{CALL RegisterDoctor(?, ?, ?)}")) {
            cs.setString(1, name);
            cs.setString(2, specialization);
            cs.setString(3, contact);
            cs.execute();
            JOptionPane.showMessageDialog(null, "Doctor Registered Successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error registering doctor: " + e.getMessage());
        }
    }

    private void bookAppointment() {
        try {
            JComboBox<String> doctorDropdown = new JComboBox<>();
            Map<String, Integer> doctorMap = new HashMap<>();

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT doctor_id, name, specialization FROM Doctors")) {
                while (rs.next()) {
                    String doctorName = rs.getString("name") + " (" + rs.getString("specialization") + ")";
                    int doctorId = rs.getInt("doctor_id");
                    doctorDropdown.addItem(doctorName);
                    doctorMap.put(doctorName, doctorId);
                }
            }

            if (doctorMap.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No doctors available!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String patientName = JOptionPane.showInputDialog("Enter Patient's Name:");
            int patientAge = getValidAge();
            String patientContact = getValidContactNumber();
            String date = getValidDate();

            JOptionPane.showMessageDialog(null, doctorDropdown, "Select Doctor", JOptionPane.QUESTION_MESSAGE);
            String selectedDoctor = (String) doctorDropdown.getSelectedItem();
            int doctorId = doctorMap.get(selectedDoctor);

            try (CallableStatement cs = connection.prepareCall("{CALL BookAppointment(?, ?, ?, ?, ?)}")) {
                cs.setString(1, patientName);
                cs.setInt(2, patientAge);
                cs.setString(3, patientContact);
                cs.setInt(4, doctorId);
                cs.setDate(5, Date.valueOf(date));
                cs.execute();
                JOptionPane.showMessageDialog(null, "Appointment Booked Successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error booking appointment: " + e.getMessage());
        }
    }

    private void cancelAppointment() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT appointment_id, p.name AS patient_name, d.name AS doctor_name, a.date " +
                    "FROM Appointments a JOIN Doctors d ON a.doctor_id = d.doctor_id " +
                    "JOIN Patients p ON a.patient_id = p.patient_id");

            StringBuilder appointmentsList = new StringBuilder("Appointments:\n");
            while (rs.next()) {
                appointmentsList.append("Appointment ID: ").append(rs.getInt("appointment_id"))
                        .append(", Patient: ").append(rs.getString("patient_name"))
                        .append(", Doctor: ").append(rs.getString("doctor_name"))
                        .append(", Date: ").append(rs.getDate("date")).append("\n");
            }

            if (appointmentsList.length() == 0) {
                JOptionPane.showMessageDialog(null, "No appointments to cancel.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String appointmentToCancel = JOptionPane.showInputDialog(null, appointmentsList.toString() +
                    "\nEnter the Appointment ID to cancel:");
            int appointmentId = Integer.parseInt(appointmentToCancel);

            try (CallableStatement cs = connection.prepareCall("{CALL CancelAppointment(?)}")) {
                cs.setInt(1, appointmentId);
                cs.execute();
                JOptionPane.showMessageDialog(null, "Appointment Canceled Successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error canceling appointment: " + e.getMessage());
        }
    }

    private void viewDoctors() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Doctors")) {
            StringBuilder doctors = new StringBuilder("Doctors:\n");
            while (rs.next()) {
                doctors.append("ID: ").append(rs.getInt("doctor_id"))
                        .append(", Name: ").append(rs.getString("name"))
                        .append(", Specialization: ").append(rs.getString("specialization"))
                        .append(", Contact: ").append(rs.getString("contact")).append("\n");
            }
            JOptionPane.showMessageDialog(null, doctors.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewAppointments() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT a.appointment_id, p.name AS patient_name, d.name AS doctor_name, a.date " +
                     "FROM Appointments a JOIN Doctors d ON a.doctor_id = d.doctor_id " +
                     "JOIN Patients p ON a.patient_id = p.patient_id")) {
            StringBuilder appointments = new StringBuilder("Appointments:\n");
            while (rs.next()) {
                appointments.append("Appointment ID: ").append(rs.getInt("appointment_id"))
                        .append(", Patient: ").append(rs.getString("patient_name"))
                        .append(", Doctor: ").append(rs.getString("doctor_name"))
                        .append(", Date: ").append(rs.getDate("date")).append("\n");
            }
            JOptionPane.showMessageDialog(null, appointments.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getValidAge() {
        int age = -1;
        while (age <= 0) {
            try {
                String input = JOptionPane.showInputDialog("Enter Patient's Age:");
                age = Integer.parseInt(input);
                if (age <= 0) {
                    JOptionPane.showMessageDialog(null, "Age must be a positive number. Please try again.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input! Please enter a valid age.");
            }
        }
        return age;
    }

    private String getValidContactNumber() {
        String contact = "";
        while (contact.isEmpty() || !contact.matches("\\d{10}")) {
            contact = JOptionPane.showInputDialog("Enter Patient's Contact (10 digits):");
            if (contact.isEmpty() || !contact.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(null, "Invalid contact number! Please enter a 10-digit number.");
            }
        }
        return contact;
    }

    private String getValidDate() {
        String date = "";
        while (true) {
            date = JOptionPane.showInputDialog("Enter Appointment Date (YYYY-MM-DD):");
            if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                try {
                    Date enteredDate = Date.valueOf(date);
                    Date currentDate = new Date(System.currentTimeMillis());
                    if (enteredDate.before(currentDate)) {
                        JOptionPane.showMessageDialog(null, "Appointment date must be in the future. Please try again.");
                    } else {
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid date format. Please enter the date in YYYY-MM-DD format.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid date format. Please enter the date in YYYY-MM-DD format.");
            }
        }
        return date;
    }

    public static void main(String[] args) {
        new DoctorAppointmentSystem();
    }
}
