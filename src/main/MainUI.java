package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import main.service.AuthService;
import main.ui.admin.AdminMainMenu;
import main.ui.user.RegisterUI;

public class MainUI extends JFrame {

    private JTextField userText;
    private JPasswordField passText;
    private JButton loginButton;
    private JButton registerButton;
    private JButton forgotButton; // เพิ่มปุ่มลืมรหัสผ่าน
    private AuthService authService;

    public MainUI() {
        // ตั้งค่า UI และ Font ภาษาไทย
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Font thaiFont = new Font("Leelawadee UI", Font.PLAIN, 14);
            UIManager.put("OptionPane.messageFont", thaiFont);
            UIManager.put("OptionPane.buttonFont", thaiFont);
        } catch (Exception e) { e.printStackTrace(); }
        
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());
        
        this.authService = new AuthService();

        setTitle("ระบบจองห้องประชุมและอุปกรณ์ - เข้าสู่ระบบ");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout()); 
        setContentPane(mainPanel);

        // แผ่นการ์ดตรงกลาง
        RoundedPanel card = new RoundedPanel(20, new Color(255, 255, 255, 200));
        card.setLayout(null);
        card.setPreferredSize(new Dimension(500, 480)); // เพิ่มความสูงเล็กน้อย
        mainPanel.add(card);

        // --- โลโก้ ---
        ImageIcon logoImg = new ImageIcon("images/SUT_logo_v_th_or-768x600.png");
        int logoW = 90; 
        int logoH = (logoImg.getIconWidth() > 0) ? (int)((double)logoImg.getIconHeight()/logoImg.getIconWidth()*logoW) : 50;
        Image scaledLogo = logoImg.getImage().getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setBounds(210, 20, logoW, logoH); 
        card.add(logoLabel);

        JLabel titleLabel = new JLabel("ระบบจองห้องประชุมและอุปกรณ์", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Leelawadee UI", Font.PLAIN, 18));
        titleLabel.setBounds(50, 95, 400, 30);
        card.add(titleLabel);

        JLabel subTitleLabel = new JLabel("เข้าสู่ระบบ", SwingConstants.CENTER);
        subTitleLabel.setFont(new Font("Leelawadee UI", Font.BOLD, 32));
        subTitleLabel.setBounds(50, 115, 400, 50);
        card.add(subTitleLabel);

        // --- ช่องกรอกข้อมูล ---
        JPanel userPanel = createInputPanel(50, 180, "ชื่อผู้ใช้ / รหัสนักศึกษา", "👤");
        card.add(userPanel);
        userText = (JTextField) userPanel.getComponent(1);

        JPanel passPanel = createInputPanel(50, 240, "รหัสผ่าน", "🔒");
        passPanel.remove(1); 
        passText = new JPasswordField();
        passText.setBorder(null);
        passText.setBackground(new Color(230, 230, 230));
        passText.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        passText.setBounds(45, 10, 320, 30);
        new TextPrompt("รหัสผ่าน", passText).changeAlpha(0.5f);
        passPanel.add(passText); 
        card.add(passPanel);

        // --- ปุ่มลืมรหัสผ่าน (เพิ่มใหม่) ---
        forgotButton = new JButton("ลืมรหัสผ่าน?");
        forgotButton.setBounds(330, 290, 120, 25);
        forgotButton.setFont(new Font("Leelawadee UI", Font.PLAIN, 12));
        forgotButton.setForeground(new Color(242, 101, 34));
        forgotButton.setContentAreaFilled(false);
        forgotButton.setBorderPainted(false);
        forgotButton.setFocusPainted(false);
        forgotButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(forgotButton);

        // --- ปุ่มหลัก ---
        loginButton = new JButton("เข้าสู่ระบบ");
        loginButton.setBounds(85, 325, 330, 45);
        loginButton.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(242, 101, 34)); 
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(loginButton);

        registerButton = new JButton("ยังไม่มีบัญชี? ลงทะเบียน");
        registerButton.setBounds(85, 385, 330, 40);
        registerButton.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        registerButton.setForeground(Color.BLACK);
        registerButton.setBackground(new Color(220, 220, 220)); 
        registerButton.setBorder(BorderFactory.createLineBorder(new Color(242, 101, 34), 1)); 
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(registerButton);

        // --- Logic การทำงาน ---
        loginButton.addActionListener(e -> {
            String user = userText.getText().trim();
            String pass = new String(passText.getPassword());

            if (authService.loginAdmin(user, pass)) {
                new AdminMainMenu(user).setVisible(true); 
                dispose();
            } else if (authService.login(user, pass)) {
                authService.updateUserStatus(user, "Active"); 
                AuthService.currentLoggedInStudentId = user;
                new MainMenu().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง", "แจ้งเตือน", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            new RegisterUI().setVisible(true);
            dispose();
        });

        forgotButton.addActionListener(e -> showForgotPasswordDialog());
    }

    // ระบบลืมรหัสผ่านแบบจำลอง Pop-up
    private void showForgotPasswordDialog() {
        String input = JOptionPane.showInputDialog(this, 
            "กรุณากรอกเบอร์โทรศัพท์หรืออีเมลที่ลงทะเบียนไว้\n(ระบบจะส่งรหัส OTP จำลองให้ท่าน)", 
            "ลืมรหัสผ่าน", JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) return;

        int mockOTP = (int)(Math.random() * 900000) + 100000;

        JOptionPane.showMessageDialog(this, 
            "ระบบได้ส่งรหัส OTP ไปยัง " + input + " แล้ว\nรหัสยืนยันของคุณคือ: " + mockOTP, 
            "ส่งรหัสยืนยันสำเร็จ", JOptionPane.INFORMATION_MESSAGE);

        String otpIn = JOptionPane.showInputDialog(this, "กรุณากรอกรหัส OTP 6 หลัก:");

        if (String.valueOf(mockOTP).equals(otpIn)) {
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(this, pf, "กรุณาตั้งรหัสผ่านใหม่:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (okCxl == JOptionPane.OK_OPTION) {
                String newPass = new String(pf.getPassword());
                if (newPass.length() >= 4) {
                	boolean success = authService.resetPassword(input, newPass);
                    // ที่นี่คุณสามารถเพิ่ม authService.resetPassword(input, newPass) ได้ในอนาคต
                    JOptionPane.showMessageDialog(this, "เปลี่ยนรหัสผ่านสำเร็จ! กรุณาเข้าสู่ระบบด้วยรหัสผ่านใหม่");
                } else {
                    JOptionPane.showMessageDialog(this, "รหัสผ่านสั้นเกินไป", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (otpIn != null) {
            JOptionPane.showMessageDialog(this, "รหัส OTP ไม่ถูกต้อง", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createInputPanel(int x, int y, String placeholder, String iconText) {
        RoundedPanel panel = new RoundedPanel(15, new Color(230, 230, 230)); 
        panel.setBounds(x, y, 400, 50); 
        panel.setLayout(null);
        JLabel icon = new JLabel(iconText, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        icon.setBounds(5, 0, 40, 50);
        panel.add(icon);
        JTextField field = new JTextField();
        field.setBorder(null);
        field.setBackground(new Color(230, 230, 230));
        field.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        field.setBounds(45, 10, 320, 30);
        new TextPrompt(placeholder, field).changeAlpha(0.5f);
        panel.add(field);
        return panel;
    }

    // Helper Classes สำหรับ UI
    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;
        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    class BackgroundPanel extends JPanel {
        private Image bgImage;
        public BackgroundPanel() {
            try { bgImage = new ImageIcon("images/พื้นหลัง.png").getImage(); } catch (Exception e) {}
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    class TextPrompt extends JLabel implements FocusListener, DocumentListener {
        private JTextComponent component;
        private Document document;

        public TextPrompt(String text, JTextComponent component) {
            this.component = component;
            this.document = component.getDocument();
            setText(text);
            setFont(component.getFont());
            setBorder(new EmptyBorder(component.getInsets()));
            component.addFocusListener(this);
            document.addDocumentListener(this);
            component.setLayout(new BorderLayout());
            component.add(this);
            checkForPrompt();
        }
        public void changeAlpha(float alpha) {
            int r=getForeground().getRed(), g=getForeground().getGreen(), b=getForeground().getBlue();
            setForeground(new Color(r, g, b, (int)(alpha*255)));
        }
        private void checkForPrompt() { setVisible(document.getLength() == 0); }
        public void focusGained(FocusEvent e) { checkForPrompt(); }
        public void focusLost(FocusEvent e) { checkForPrompt(); }
        public void insertUpdate(DocumentEvent e) { checkForPrompt(); }
        public void removeUpdate(DocumentEvent e) { checkForPrompt(); }
        public void changedUpdate(DocumentEvent e) { checkForPrompt(); }
    }
}