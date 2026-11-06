package main.model;

public class Booking {

	private int bookingId;
    private String studentId;
    private String roomName;
    private String date;
    private String timeRange;
    private String status;
    private String purpose;
    private String equipments;

    // [เพิ่มใหม่] ตามหลัก OOP
    private User user;              // Aggregation (Booking มี User แต่ User อยู่ได้ด้วยตัวเอง)
    private BookingDetail detail;   // Composition (Booking มี Detail และ Detail ตายพร้อม Booking)

    public Booking(int bookingId, String studentId, String roomName,
                   String date, String timeRange, String status,
                   String purpose, String equipments) {
        this.bookingId = bookingId;
        this.studentId = studentId;
        this.roomName = roomName;
        this.date = date;
        this.timeRange = timeRange;
        this.status = status;
        this.purpose = purpose;
        this.equipments = equipments;

        // [Composition] สร้างส่วนประกอบทันทีที่สร้าง Booking
        this.detail = new BookingDetail(timeRange);
    }

    // [Aggregation] Method สำหรับใส่ User เข้ามาทีหลัง
    public void setUser(User user) {
        this.user = user;
    }
    public User getUser() { return this.user; }

    // Getter สำหรับ Detail
    public BookingDetail getDetail() { return this.detail; }

    // ===== getters =====
    public int getBookingId() { return bookingId; }
    public String getStudentId() { return studentId; }
    public String getRoomName() { return roomName; }
    public String getDate() { return date; }
    public String getTimeRange() { return timeRange; }
    public String getStatus() { return status; }
    public String getPurpose() { return purpose; }
    public String getEquipments() { return equipments; }

    // ===== setters =====
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public void setDate(String date) { this.date = date; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
    public void setStatus(String status) { this.status = status; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setEquipments(String equipments) { this.equipments = equipments; }
}