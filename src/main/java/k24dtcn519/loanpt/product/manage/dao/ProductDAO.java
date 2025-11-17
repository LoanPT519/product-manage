/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package k24dtcn519.loanpt.product.manage.dao;
import k24dtcn519.loanpt.product.manage.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO (Data Access Object) để thực hiện các thao tác CRUD
 * (Create, Read, Update, Delete) với bảng products.
 */
public class ProductDAO {
    
    /**
     * Lấy tất cả sản phẩm từ database.
     * @return Danh sách các sản phẩm
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_id";
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            // Duyệt qua từng dòng kết quả
            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getString("product_id"));
                product.setName(rs.getString("name"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getDouble("price"));
                product.setQuantity(rs.getInt("quantity"));
                
                products.add(product);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách sản phẩm:");
            e.printStackTrace();
        } finally {
            // Đóng tài nguyên
            closeResources(rs, stmt, conn);
        }
        
        return products;
    }
    
    /**
     * Thêm một sản phẩm mới vào database.
     * @param product Sản phẩm cần thêm
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean insertProduct(Product product) {
        String sql = "INSERT INTO products (product_id, name, category, price, quantity) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Tắt auto-commit để sử dụng transaction
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getProductId());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setInt(5, product.getQuantity());
            
            int rowsAffected = pstmt.executeUpdate();
            
            // Commit transaction nếu thành công
            conn.commit();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // Rollback nếu có lỗi (đảm bảo tính nhất quán - Consistency)
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction bị rollback do lỗi!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            javax.swing.JOptionPane.showMessageDialog(null,"Lỗi khi thêm sản phẩm:" +e);

            System.err.println("Lỗi khi thêm sản phẩm:");
            e.printStackTrace();
            return false;
        } finally {
            // Đóng tài nguyên
            closeResources(null, pstmt, conn);
        }
    }
    
    /**
     * Cập nhật thông tin sản phẩm.
     * @param product Sản phẩm với thông tin mới
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, category = ?, price = ?, quantity = ? " +
                     "WHERE product_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getCategory());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setString(5, product.getProductId());
            
            int rowsAffected = pstmt.executeUpdate();
            
            conn.commit(); // Commit nếu thành công
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Lỗi khi cập nhật sản phẩm:");
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, pstmt, conn);
        }
    }
    
    /**
     * Xóa sản phẩm theo mã sản phẩm.
     * @param productId Mã sản phẩm cần xóa
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean deleteProduct(String productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            conn.commit(); // Commit nếu thành công
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Lỗi khi xóa sản phẩm:");
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, pstmt, conn);
        }
    }
    
    /**
     * Tìm sản phẩm theo mã (dùng để kiểm tra trùng).
     * @param productId Mã sản phẩm
     * @return Product nếu tìm thấy, null nếu không tìm thấy
     */
    public Product findById(String productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getString("product_id"));
                product.setName(rs.getString("name"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getDouble("price"));
                product.setQuantity(rs.getInt("quantity"));
                return product;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm sản phẩm:");
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        
        return null;
    }
    
    /**
     * Đóng các tài nguyên JDBC (ResultSet, Statement, Connection).
     * Phương thức helper để tránh code lặp lại.
     */
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

