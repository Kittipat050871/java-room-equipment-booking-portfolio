package main.model;

public abstract class Notification {
    protected int id;
    protected String date;
    protected String message;
    // ตัด field 'type' ออก เพราะเราจะใช้ Class แยกประเภทแทน

    public Notification(int id, String date, String message) {
        this.id = id;
        this.date = date;
        this.message = message;
    }

    // Abstract Method: บังคับให้ลูกต้องมี (นี่คือหัวใจของ Polymorphism)
    public abstract String getTypeColor();
    public abstract String getIconText();

    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getMessage() { return message; }
}