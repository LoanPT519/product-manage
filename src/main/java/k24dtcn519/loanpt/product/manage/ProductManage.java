package k24dtcn519.loanpt.product.manage;

import k24dtcn519.loanpt.product.manage.dao.DBConnection;
import k24dtcn519.loanpt.product.manage.ui.ProductFrame;

import javax.swing.*;

/**
 *
 * @author dhduy
 */
public class ProductManage {

    public static void main(String[] args) {
       // 1. Khởi tạo database (tạo bảng nếu chưa có)
        DBConnection.initializeDatabase();
        
        // 2. Khởi động giao diện Swing trên Event Dispatch Thread
        // Đây là best practice để đảm bảo thread-safety cho Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Thiết lập Look and Feel (giao diện hiện đại hơn)
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Tạo và hiển thị ProductFrame
                ProductFrame frame = new ProductFrame();
                frame.setVisible(true);
            }
        });
    }
}
