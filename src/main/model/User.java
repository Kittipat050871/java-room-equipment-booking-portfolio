package main.model;

public class User {
    private String studentId; // ใช้เป็น Login ID
    private String password;
    private String name;      // [เพิ่มใหม่] ชื่อผู้ใช้/ชื่อเล่น
    private String email;
    private String phone;
    private String role;      // User หรือ Admin

    // แก้ไข Constructor ให้รับ name เข้ามาด้วย
    public User(String studentId, String password, String name, String email, String phone) {
        this.studentId = studentId;
        this.password = password;
        this.name = name; // [เพิ่มใหม่]
        this.email = email;
        this.phone = phone;
        this.role = "User"; // Default
    }

    // เพิ่ม Getter / Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Getter ตัวอื่นๆ เหมือนเดิม...
    public String getStudentId() { return studentId; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}