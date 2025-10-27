import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LibraryGUIWithTable extends JFrame {
    private JTextField txtBookID, txtTitle, txtAuthor;
    private JButton btnAdd, btnIssue, btnReturn, btnView;
    private JTable table;
    private DefaultTableModel tableModel;

    private static final String DB_URL = "jdbc:sqlite:library.db";

    public LibraryGUIWithTable() {
        setTitle("Library Management System");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.add(new JLabel("Book ID:"));
        txtBookID = new JTextField();
        inputPanel.add(txtBookID);

        inputPanel.add(new JLabel("Title:"));
        txtTitle = new JTextField();
        inputPanel.add(txtTitle);

        inputPanel.add(new JLabel("Author:"));
        txtAuthor = new JTextField();
        inputPanel.add(txtAuthor);

        btnAdd = new JButton("Add Book");
        btnIssue = new JButton("Issue Book");
        btnReturn = new JButton("Return Book");
        btnView = new JButton("View All Books");

        JPanel btnPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        btnPanel.add(btnAdd);
        btnPanel.add(btnIssue);
        btnPanel.add(btnReturn);
        btnPanel.add(btnView);

        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Available"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(inputPanel, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        createDatabase();

        btnAdd.addActionListener(e -> addBook());
        btnIssue.addActionListener(e -> issueBook());
        btnReturn.addActionListener(e -> returnBook());
        btnView.addActionListener(e -> viewBooks());

        setVisible(true);
    }

    private void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "author TEXT," +
                    "available INTEGER)");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void addBook() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO books(id, title, author, available) VALUES(?, ?, ?, 1)")) {
            int id = Integer.parseInt(txtBookID.getText());
            ps.setInt(1, id);
            ps.setString(2, txtTitle.getText());
            ps.setString(3, txtAuthor.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            txtBookID.setText(""); txtTitle.setText(""); txtAuthor.setText("");
            viewBooks();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void issueBook() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE books SET available=0 WHERE id=? AND available=1")) {
            int id = Integer.parseInt(txtBookID.getText());
            ps.setInt(1, id);
            int updated = ps.executeUpdate();
            JOptionPane.showMessageDialog(this, updated > 0 ? "Book issued" : "Book not available");
            txtBookID.setText("");
            viewBooks();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void returnBook() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE books SET available=1 WHERE id=?")) {
            int id = Integer.parseInt(txtBookID.getText());
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book returned");
            txtBookID.setText("");
            viewBooks();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void viewBooks() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("available") == 1 ? "Yes" : "No"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryGUIWithTable::new);
    }
}
