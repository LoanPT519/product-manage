package k24dtcn519.loanpt.product.manage.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lớp quản lý kết nối đến cơ sở dữ liệu SQLite.
 * Cung cấp phương thức lấy kết nối và khởi tạo bảng.
 */
public class DBConnection {
    
    // Đường dẫn đến file database SQLite
    // File sẽ được tạo tự động nếu chưa tồn tại
//    private static final String DB_URL = "jdbc:sqlite:database/products.db";
//    static{
//        javax.swing.JOptionPane.showMessageDialog(null,"DB path = " + new java.io.File("database/products.db").getAbsolutePath() + "| "+DB_URL);
//    }

    private static final String DB_URL;
    static {
        String jarDir = new java.io.File(
                DBConnection.class.getProtectionDomain()
                                 .getCodeSource()
                                 .getLocation()
                                 .getPath())
                .getParent();

          DB_URL =  "jdbc:sqlite:" + new java.io.File("database/products.db").getAbsolutePath();
    }
    
    /**
     * Lấy kết nối đến database SQLite.
     * @return Connection object
     * @throws SQLException nếu không thể kết nối
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load SQLite JDBC driver (tùy chọn, JDBC 4.0+ tự động load)
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy SQLite JDBC Driver!");
            e.printStackTrace();
        }
        
        // Tạo và trả về kết nối
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * Khởi tạo bảng products nếu chưa tồn tại.
     * Gọi phương thức này khi khởi động ứng dụng.
     */
    public static void initializeDatabase() {
        String createTableSQL = 
            "CREATE TABLE IF NOT EXISTS products (" +
            "    product_id   TEXT PRIMARY KEY," +
            "    name         TEXT NOT NULL," +
            "    category     TEXT," +
            "    price        REAL NOT NULL CHECK(price > 0)," +
            "    quantity     INTEGER NOT NULL CHECK(quantity >= 0)" +
            ");";
        
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Lấy kết nối
            conn = getConnection();
            stmt = conn.createStatement();
            
            // Thực thi lệnh tạo bảng
            stmt.execute(createTableSQL);
            System.out.println("Database đã được khởi tạo thành công!");
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi khởi tạo database:");
            e.printStackTrace();
        } finally {
            // Đóng tài nguyên (quan trọng để tránh memory leak)
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Kiểm tra kết nối database (dùng để test).
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Kết nối database thành công!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối database:");
            e.printStackTrace();
        }
    }
}

