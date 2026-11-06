package main.ui.user;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
// Import สำหรับ TextPrompt
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import main.MainUI;
import main.model.User;
import main.service.AuthService;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class RegisterUI extends JFrame {

    private JTextField idText;
    private JTextField nameText; // [เพิ่มใหม่]
    private JTextField emailText;
    private JTextField phoneText;
    private JPasswordField passText;
    private JPasswordField confirmText;
    private JButton registerButton;
    private JButton backButton;

    private AuthService authService;

    public RegisterUI() {
        // Setup พื้นฐาน
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());
        this.authService = new AuthService();

        setTitle("ลงทะเบียนนักศึกษา");
        setSize(1024, 850); // ** เพิ่มความสูงอีกนิดเพื่อให้ช่องพอดี **
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 1. พื้นหลัง (Background)
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        // 2. สร้าง Card สีขาว (กล่องกลาง)
        RoundedPanel card = new RoundedPanel(20, new Color(255, 255, 255, 200));
        
        card.setLayout(null); 
        // ** ปรับความสูงกล่องให้รับกับช่องกรอกที่เพิ่มขึ้น **
        card.setPreferredSize(new Dimension(450, 780)); 
        mainPanel.add(card);

        // ================= เนื้อหาใน Card =================

        // --- 1. ส่วนเพิ่มโลโก้ ---
        ImageIcon logoImg = new ImageIcon("images/SUT_logo_v_th_or-768x600.png");
        // ย่อรูปให้พอดี
        int logoW = 90; // ปรับขนาดโลโก้ตามต้องการ
        int logoH = (logoImg.getIconWidth() > 0) ? (int)((double)logoImg.getIconHeight()/logoImg.getIconWidth()*logoW) : 50;
        Image scaledLogo = logoImg.getImage().getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setBounds(180, 20, logoW, logoH); // จัดกึ่งกลาง
        card.add(logoLabel);
        
        // 2.2 ข้อความหัวเรื่อง
        JLabel titleLabel = new JLabel("ระบบจองห้องประชุมและอุปกรณ์", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Leelawadee UI", Font.PLAIN, 20));
        titleLabel.setBounds(25, 95, 400, 30);
        card.add(titleLabel);

        // --- 2. หัวข้อ "ลงทะเบียน" ---
        JLabel subtitleLabel = new JLabel("ลงทะเบียน", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Leelawadee UI", Font.BOLD, 28));
        subtitleLabel.setBounds(50, 125, 350, 40);
        card.add(subtitleLabel);

        // --- 3. สร้างช่องกรอกข้อมูล ---
        int startY = 180; 
        int gap = 65; // ลดระยะห่างลงนิดหน่อยเพื่อให้ใส่ได้ครบ

        // 1. รหัสนักศึกษา
        JPanel idPanel = createInputPanel(50, startY, "รหัสนักศึกษา");
        card.add(idPanel);
        idText = (JTextField) idPanel.getComponent(0);
        
        // 2. [เพิ่มใหม่] ชื่อผู้ใช้
        JPanel namePanel = createInputPanel(50, startY + gap, "ชื่อผู้ใช้ (Username/Name)");
        card.add(namePanel);
        nameText = (JTextField) namePanel.getComponent(0);

        // 3. อีเมล
        JPanel emailPanel = createInputPanel(50, startY + (gap * 2), "อีเมล");
        card.add(emailPanel);
        emailText = (JTextField) emailPanel.getComponent(0);

        // 4. เบอร์โทรศัพท์
        JPanel phonePanel = createInputPanel(50, startY + (gap * 3), "เบอร์โทรศัพท์");
        card.add(phonePanel);
        phoneText = (JTextField) phonePanel.getComponent(0);

        // 5. รหัสผ่าน
        JPanel passPanel = createPasswordPanel(50, startY + (gap * 4), "รหัสผ่าน");
        card.add(passPanel);
        passText = (JPasswordField) passPanel.getClientProperty("field");

        // 6. ยืนยันรหัสผ่าน
        JPanel confirmPanel = createPasswordPanel(50, startY + (gap * 5), "ยืนยันรหัสผ่าน");
        card.add(confirmPanel);
        confirmText = (JPasswordField) confirmPanel.getClientProperty("field");

        // --- 4. ปุ่มกด ---
        int btnY = startY + (gap * 6) + 10; 

        // ปุ่มลงทะเบียน
        registerButton = new JButton("ลงทะเบียน");
        registerButton.setBounds(50, btnY, 350, 45);
        registerButton.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(242, 101, 34)); // สีส้ม
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setBorder(BorderFactory.createEmptyBorder()); 
        card.add(registerButton);

        // ปุ่มกลับไปล็อกอิน
        backButton = new JButton("กลับไปล็อกอิน");
        backButton.setBounds(50, btnY + 55, 350, 45);
        backButton.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
        backButton.setForeground(Color.BLACK);
        backButton.setBackground(Color.WHITE);
        backButton.setBorder(BorderFactory.createLineBorder(new Color(242, 101, 34), 1)); // ขอบส้ม
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(backButton);


        // ================= Logic การทำงาน =================
        registerButton.addActionListener(e -> {
            String id = idText.getText().trim();
            String name = nameText.getText().trim(); // [เพิ่มใหม่]
            String email = emailText.getText().trim();
            String phone = phoneText.getText().trim();
            char[] passChars = passText.getPassword();
            char[] confirmChars = confirmText.getPassword();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || passChars.length == 0 || confirmChars.length == 0) {
                JOptionPane.showMessageDialog(this, "กรุณากรอกทุกช่อง", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!Arrays.equals(passChars, confirmChars)) {
                JOptionPane.showMessageDialog(this, "รหัสผ่านไม่ตรงกัน", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String passwordString = new String(passChars);
            // [แก้ไข] ส่ง name เข้าไปใน constructor
            User newUser = new User(id, passwordString, name, email, phone);
            boolean success = authService.register(newUser);

            if (success) {
                JOptionPane.showMessageDialog(this, "ลงทะเบียนสำเร็จ!");
                this.dispose();
                new MainUI().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "ลงทะเบียนล้มเหลว!\nรหัสนักศึกษา หรือ อีเมลนี้ อาจมีในระบบแล้ว", "ผิดพลาด", JOptionPane.ERROR_MESSAGE);
            }
            Arrays.fill(passChars, '0');
            Arrays.fill(confirmChars, '0');
        });

        backButton.addActionListener(e -> {
            this.dispose();
            new MainUI().setVisible(true);
        });
    }

    // ================= Helper Methods (คงเดิม) =================
    private JPanel createInputPanel(int x, int y, String placeholder) {
        RoundedPanel panel = new RoundedPanel(15, Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.setBounds(x, y, 350, 50);
        panel.setLayout(null);

        JTextField field = new JTextField();
        field.setBorder(null);
        field.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        field.setBounds(15, 10, 320, 30);
        
        TextPrompt prompt = new TextPrompt(placeholder, field);
        prompt.changeAlpha(0.5f);
        prompt.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));

        panel.add(field);
        return panel;
    }

    private JPanel createPasswordPanel(int x, int y, String placeholder) {
        RoundedPanel panel = new RoundedPanel(15, Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.setBounds(x, y, 350, 50);
        panel.setLayout(null);

        JPasswordField field = new JPasswordField();
        field.setBorder(null);
        field.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        field.setBounds(15, 10, 280, 30);
        
        TextPrompt prompt = new TextPrompt(placeholder, field);
        prompt.changeAlpha(0.5f);
        prompt.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));

        JToggleButton eyeBtn = new JToggleButton("👁");
        eyeBtn.setBounds(305, 10, 40, 30);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setFocusPainted(false);
        eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        char defaultChar = field.getEchoChar();
        eyeBtn.addActionListener(e -> {
            if (eyeBtn.isSelected()) {
                field.setEchoChar((char) 0);
            } else {
                field.setEchoChar(defaultChar);
            }
        });

        panel.add(field);
        panel.add(eyeBtn);
        panel.putClientProperty("field", field);
        
        return panel;
    }

    // ================= Inner Classes (คงเดิม) =================
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
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }

    class BackgroundPanel extends JPanel {
        private Image bgImage;
        public BackgroundPanel() {
            try {
                bgImage = new ImageIcon("images/พื้นหลัง.png").getImage();
            } catch (Exception e) {}
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    class TextPrompt extends JLabel implements FocusListener, DocumentListener {
        JTextComponent component;
        Document document;
        public TextPrompt(String text, JTextComponent component) {
            this.component = component;
            document = component.getDocument();
            setText(text);
            setFont(component.getFont());
            setForeground(Color.GRAY);
            setBorder(new EmptyBorder(component.getInsets()));
            setHorizontalAlignment(JLabel.LEADING);
            component.addFocusListener(this);
            document.addDocumentListener(this);
            component.setLayout(new BorderLayout());
            component.add(this);
            checkForPrompt();
        }
        public void changeAlpha(float alpha) { changeAlpha((int) (alpha * 255)); }
        public void changeAlpha(int alpha) {
            alpha = alpha > 255 ? 255 : alpha < 0 ? 0 : alpha;
            int red = getForeground().getRed();
            int green = getForeground().getGreen();
            int blue = getForeground().getBlue();
            setForeground(new Color(red, green, blue, alpha));
        }
        public void checkForPrompt() {
            if (document.getLength() > 0) { setVisible(false); return; }
            setVisible(true);
        }
        public void focusGained(FocusEvent e) { checkForPrompt(); }
        public void focusLost(FocusEvent e) { checkForPrompt(); }
        public void insertUpdate(DocumentEvent e) { checkForPrompt(); }
        public void removeUpdate(DocumentEvent e) { checkForPrompt(); }
        public void changedUpdate(DocumentEvent e) { checkForPrompt(); }
    }
}