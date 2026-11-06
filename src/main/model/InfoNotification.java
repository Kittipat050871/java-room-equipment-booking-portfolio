package main.model;

public class InfoNotification extends Notification {

    public InfoNotification(int id, String date, String message) {
        super(id, date, message);
    }

    @Override
    public String getTypeColor() {
        return "#2ecc71"; // สีเขียว
    }

    @Override
    public String getIconText() {
        return "ℹ️"; // ไอคอนตัว i
    }
}