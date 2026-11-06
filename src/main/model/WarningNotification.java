package main.model;

public class WarningNotification extends Notification {

    public WarningNotification(int id, String date, String message) {
        super(id, date, message);
    }

    @Override
    public String getTypeColor() {
        return "#e74c3c"; // สีแดง
    }

    @Override
    public String getIconText() {
        return "⚠️"; // ไอคอนตกใจ
    }
}