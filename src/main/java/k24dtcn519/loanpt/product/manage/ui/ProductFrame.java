package k24dtcn519.loanpt.product.manage.ui;
import k24dtcn519.loanpt.product.manage.dao.ProductDAO;
import k24dtcn519.loanpt.product.manage.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lớp JFrame chứa giao diện chính của ứng dụng quản lý sản phẩm.
 * Đã cập nhật:
 *  - Mã sản phẩm không chứa dấu (chỉ cho phép a-zA-Z0-9_-)
 *  - Giá là số dương và không vượt quá MAX_PRICE
 *  - Số lượng là số nguyên dương và không vượt quá MAX_QUANTITY
 *  - Hiển thị giá theo định dạng nhóm chữ số (10,000)
 *  - Thêm nút "Hướng dẫn sử dụng" để hiển thị quy tắc validate và các action CRUD
 */
public class ProductFrame extends JFrame {

    // Các thành phần giao diện
    private JTextField txtProductId;
    private JTextField txtName;
    private JTextField txtCategory;
    private JTextField txtPrice;
    private JTextField txtQuantity;

    private JTable productTable;
    private DefaultTableModel tableModel;

    private JButton btnDisplay;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnReset;
    private JButton btnHelp;

    // DAO để thao tác với database
    private ProductDAO productDAO;

    // Validation patterns và giới hạn
    // Chỉ cho phép chữ, số, gạch dưới và gạch ngang — KHÔNG cho phép dấu tiếng Việt hoặc ký tự đặc biệt khác
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    // SQLite bản thân không giới hạn chính xác kiểu REAL, nhưng để an toàn ta sử dụng giới hạn thực tế:
    // Giá tối đa sẽ sử dụng giá trị tối đa của signed 64-bit (mô phỏng giới hạn lưu trữ lớn). Thường trong ứng dụng ta sẽ đặt giới hạn nhỏ hơn.
    private static final double MAX_PRICE = 9_223_372_036_854_775_807.0; // approx signed long max
    private static final int MAX_QUANTITY = Integer.MAX_VALUE; // 2,147,483,647

    // Dùng NumberFormat để hiển thị tiền với phân cách hàng nghìn theo chuẩn Locale.US (10,000)
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(Locale.US);

    /**
     * Constructor - Khởi tạo giao diện.
     */
    public ProductFrame() {
        productDAO = new ProductDAO();
        initComponents();
        loadProductsToTable(); // Tải dữ liệu ban đầu
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents() {
        // Thiết lập JFrame
        setTitle("Quản Lý Sản Phẩm - JDBC + Swing - Phạm Thị Loan - K24DTNCN519");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình
        setLayout(new BorderLayout(10, 10));

        // === PANEL TRÊN: Form nhập liệu ===
        JPanel topPanel = createFormPanel();
        add(topPanel, BorderLayout.NORTH);

        // === PANEL GIỮA: JTable hiển thị danh sách ===
        JPanel centerPanel = createTablePanel();
        add(centerPanel, BorderLayout.CENTER);

        // === PANEL DƯỚI: Các nút chức năng ===
        JPanel bottomPanel = createButtonPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Tạo panel chứa form nhập liệu.
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Thông Tin Sản Phẩm"));

        // Mã sản phẩm
        panel.add(new JLabel("Mã Sản Phẩm:"));
        txtProductId = new JTextField();
        panel.add(txtProductId);

        // Tên sản phẩm
        panel.add(new JLabel("Tên Sản Phẩm:"));
        txtName = new JTextField();
        panel.add(txtName);

        // Danh mục
        panel.add(new JLabel("Danh Mục:"));
        txtCategory = new JTextField();
        panel.add(txtCategory);

        // Giá
        panel.add(new JLabel("Giá:"));
        txtPrice = new JTextField();
        panel.add(txtPrice);

        // Số lượng
        panel.add(new JLabel("Số Lượng:"));
        txtQuantity = new JTextField();
        panel.add(txtQuantity);

        return panel;
    }

    /**
     * Tạo panel chứa JTable.
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Danh Sách Sản Phẩm"));

        // Tạo table model với các cột
        // Lưu ý: cột Giá sẽ hiển thị dưới dạng chuỗi đã format
        String[] columnNames = {"Mã SP", "Tên SP", "Danh Mục", "Giá", "Số Lượng"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép edit trực tiếp trên table
            }
        };

        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Xử lý sự kiện chọn hàng trong table
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedProduct();
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo panel chứa các nút chức năng.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Nút Hiển thị
        btnDisplay = new JButton("Hiển Thị");
        btnDisplay.addActionListener(e -> handleDisplay());
        panel.add(btnDisplay);

        // Nút Thêm
        btnAdd = new JButton("Thêm");
        btnAdd.addActionListener(e -> handleAdd());
        panel.add(btnAdd);

        // Nút Cập nhật
        btnUpdate = new JButton("Cập Nhật");
        btnUpdate.addActionListener(e -> handleUpdate());
        panel.add(btnUpdate);

        // Nút Xóa
        btnDelete = new JButton("Xóa");
        btnDelete.addActionListener(e -> handleDelete());
        panel.add(btnDelete);

        // Nút Reset
        btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> handleReset());
        panel.add(btnReset);

        // Nút Hướng dẫn sử dụng
        btnHelp = new JButton("Hướng dẫn sử dụng");
        btnHelp.addActionListener(e -> showHelpDialog());
        panel.add(btnHelp);

        return panel;
    }

    // ========== XỬ LÝ CÁC NÚT CHỨC NĂNG ==========

    /**
     * Xử lý nút "Hiển Thị" - Tải lại danh sách sản phẩm.
     */
    private void handleDisplay() {
        loadProductsToTable();
        JOptionPane.showMessageDialog(this, "Đã tải lại danh sách sản phẩm!",
                "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Xử lý nút "Thêm" - Thêm sản phẩm mới.
     */
    private void handleAdd() {
        try {
            // Validate dữ liệu đầu vào
            if (!validateInput()) {
                return;
            }

            String productId = txtProductId.getText().trim();

            // Kiểm tra trùng mã sản phẩm
            if (productDAO.findById(productId) != null) {
                JOptionPane.showMessageDialog(this,
                        "Mã sản phẩm đã tồn tại! Vui lòng nhập mã khác.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tạo đối tượng Product từ form
            Product product = createProductFromForm();

            // Thêm vào database
            boolean success = productDAO.insertProduct(product);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Thêm sản phẩm thành công!",
                        "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadProductsToTable(); // Tải lại table
                handleReset(); // Xóa form
            } else {
                JOptionPane.showMessageDialog(this,
                        "Thêm sản phẩm thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý nút "Cập Nhật" - Cập nhật thông tin sản phẩm.
     */
    private void handleUpdate() {
        try {
            // Kiểm tra có chọn hàng nào không
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn sản phẩm cần cập nhật!",
                        "Cảnh Báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate dữ liệu
            if (!validateInput()) {
                return;
            }

            // Tạo đối tượng Product từ form
            Product product = createProductFromForm();

            // Cập nhật trong database
            boolean success = productDAO.updateProduct(product);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Cập nhật sản phẩm thành công!",
                        "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadProductsToTable(); // Tải lại table
                handleReset();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cập nhật sản phẩm thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý nút "Xóa" - Xóa sản phẩm đã chọn.
     */
    private void handleDelete() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn sản phẩm cần xóa!",
                    "Cảnh Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy mã sản phẩm từ table
        String productId = (String) tableModel.getValueAt(selectedRow, 0);

        // Xác nhận trước khi xóa
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa sản phẩm này?",
                "Xác Nhận Xóa",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = productDAO.deleteProduct(productId);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Xóa sản phẩm thành công!",
                        "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadProductsToTable();
                handleReset();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Xóa sản phẩm thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xử lý nút "Reset" - Xóa dữ liệu trên form.
     */
    private void handleReset() {
        txtProductId.setText("");
        txtName.setText("");
        txtCategory.setText("");
        txtPrice.setText("");
        txtQuantity.setText("");

        productTable.clearSelection();
        txtProductId.requestFocus(); // Đưa con trỏ về ô đầu tiên
    }

    // ========== CÁC PHƯƠNG THỨC HELPER ==========

    /**
     * Tải danh sách sản phẩm từ database lên JTable.
     */
    private void loadProductsToTable() {
        // Xóa hết dữ liệu cũ trong table
        tableModel.setRowCount(0);

        // Lấy danh sách từ database
        List<Product> products = productDAO.getAllProducts();

        // Thêm từng sản phẩm vào table, định dạng giá
        for (Product p : products) {
            String formattedPrice = formatPrice(p.getPrice());
            Object[] row = {
                    p.getProductId(),
                    p.getName(),
                    p.getCategory(),
                    formattedPrice,
                    p.getQuantity()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Hiển thị thông tin sản phẩm đã chọn lên form.
     */
    private void displaySelectedProduct() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow >= 0) {
            txtProductId.setText((String) tableModel.getValueAt(selectedRow, 0));
            txtName.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtCategory.setText((String) tableModel.getValueAt(selectedRow, 2));

            // Giá trong table được format là String, cần unformat khi đưa lên form
            Object priceObj = tableModel.getValueAt(selectedRow, 3);
            String priceStr = priceObj == null ? "" : priceObj.toString().replace(",", "");
            txtPrice.setText(priceStr);

            txtQuantity.setText(String.valueOf(tableModel.getValueAt(selectedRow, 4)));
        }
    }

    /**
     * Tạo đối tượng Product từ dữ liệu trên form.
     */
    private Product createProductFromForm() {
        Product product = new Product();
        product.setProductId(txtProductId.getText().trim());
        product.setName(txtName.getText().trim());
        product.setCategory(txtCategory.getText().trim());

        // Giá và số lượng đã được validate trước đó
        double price = Double.parseDouble(txtPrice.getText().trim());
        int quantity = Integer.parseInt(txtQuantity.getText().trim());

        product.setPrice(price);
        product.setQuantity(quantity);
        return product;
    }

    /**
     * Validate dữ liệu nhập từ form.
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    private boolean validateInput() {
        // Kiểm tra trống
        if (txtProductId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Mã sản phẩm không được để trống!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtProductId.requestFocus();
            return false;
        }

        // Kiểm tra mã sản phẩm không có dấu (chỉ ASCII letters, digits, _ , -)
        String pid = txtProductId.getText().trim();
        if (!PRODUCT_ID_PATTERN.matcher(pid).matches()) {
            JOptionPane.showMessageDialog(this,
                    "Mã sản phẩm chỉ được chứa chữ (a-z, A-Z), số (0-9), gạch dưới (_) hoặc gạch ngang (-). Không được có dấu hoặc ký tự đặc biệt.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtProductId.requestFocus();
            return false;
        }

        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tên sản phẩm không được để trống!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        // Kiểm tra giá
        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            if (price <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Giá phải lớn hơn 0!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtPrice.requestFocus();
                return false;
            }
            if (price > MAX_PRICE) {
                JOptionPane.showMessageDialog(this,
                        "Giá vượt quá giá trị tối đa cho phép: " + CURRENCY_FORMAT.format(MAX_PRICE),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Giá phải là số!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtPrice.requestFocus();
            return false;
        }

        // Kiểm tra số lượng
        try {
            long quantityLong = Long.parseLong(txtQuantity.getText().trim());
            if (quantityLong < 0) {
                JOptionPane.showMessageDialog(this,
                        "Số lượng phải >= 0!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtQuantity.requestFocus();
                return false;
            }
            if (quantityLong > MAX_QUANTITY) {
                JOptionPane.showMessageDialog(this,
                        "Số lượng vượt quá giới hạn tối đa: " + MAX_QUANTITY,
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtQuantity.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng phải là số nguyên!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtQuantity.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Format giá thành chuỗi có phân cách hàng nghìn (ví dụ 10000 -> 10,000)
     */
    private String formatPrice(double price) {
        return CURRENCY_FORMAT.format(price);
    }

    /**
    * Hiển thị hộp thoại hướng dẫn sử dụng với đầy đủ hướng dẫn cho:
    * - Quy tắc nhập liệu
    * - Thêm
    * - Cập nhật
    * - Xóa
    */
    private void showHelpDialog() {

        StringBuilder sb = new StringBuilder();

        sb.append("📘 HƯỚNG DẪN SỬ DỤNG\n\n");

        // ====== QUY TẮC NHẬP LIỆU ======
        sb.append("⚙️ QUY TẮC NHẬP LIỆU:\n");
        sb.append("1. Mã sản phẩm:\n");
        sb.append("   - Bắt buộc nhập\n");
        sb.append("   - Chỉ chứa a-z, A-Z, 0-9, _ hoặc -\n");
        sb.append("   - Không chứa dấu tiếng Việt hoặc ký tự đặc biệt\n\n");

        sb.append("2. Tên sản phẩm:\n");
        sb.append("   - Bắt buộc nhập\n\n");

        sb.append("3. Giá:\n");
        sb.append("   - Phải là số dương > 0\n");
        sb.append("   - Tối đa: " + CURRENCY_FORMAT.format(MAX_PRICE) + "\n");
        sb.append("   - Hiển thị ở bảng theo dạng: 10,000\n\n");

        sb.append("4. Số lượng:\n");
        sb.append("   - Phải là số nguyên >= 0\n");
        sb.append("   - Tối đa: " + MAX_QUANTITY + "\n\n");

        // ====== HƯỚNG DẪN THÊM ======
        sb.append("🟩 THÊM SẢN PHẨM:\n");
        sb.append(" - Nhập đầy đủ các trường: Mã, Tên, Danh Mục, Giá, Số Lượng\n");
        sb.append(" - Mã sản phẩm không được trùng với mã đã có trong hệ thống\n");
        sb.append(" - Nhấn nút \"Thêm\" để lưu sản phẩm vào database\n\n");

        // ====== HƯỚNG DẪN CẬP NHẬT ======
        sb.append("🟦 CẬP NHẬT SẢN PHẨM:\n");
        sb.append(" - Chọn 1 sản phẩm trong bảng để tải dữ liệu lên form\n");
        sb.append(" - Sửa thông tin cần thay đổi\n");
        sb.append(" - Nhấn \"Cập Nhật\" để lưu thay đổi vào database\n\n");

        // ====== HƯỚNG DẪN XÓA ======
        sb.append("🟥 XÓA SẢN PHẨM:\n");
        sb.append(" - Chọn 1 sản phẩm trong bảng\n");
        sb.append(" - Nhấn nút \"Xóa\"\n");
        sb.append(" - Xác nhận đồng ý khi popup hiện lên\n");
        sb.append(" - Sản phẩm sẽ bị xóa khỏi database\n\n");

        // ====== HƯỚNG DẪN KHÁC ======
        sb.append("📌 CÁC NÚT KHÁC:\n");
        sb.append(" - Hiển Thị: tải lại toàn bộ danh sách sản phẩm từ database\n");
        sb.append(" - Reset: xóa sạch thông tin trên form và bỏ chọn trên bảng\n\n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 450));

        JOptionPane.showMessageDialog(this, scrollPane, "Hướng dẫn sử dụng", JOptionPane.INFORMATION_MESSAGE);
    }
}
