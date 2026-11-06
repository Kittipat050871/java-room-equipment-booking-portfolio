package main.model;

public class BookingDetail {
    private String exactStartTime;
    private String exactEndTime;
    private String timestamp;

    public BookingDetail(String timeRange) {
        // แยกเวลาจาก String "09:00 - 12:00" มาเก็บละเอียด
        if(timeRange != null && timeRange.contains("-")) {
            String[] times = timeRange.split("-");
            this.exactStartTime = times[0].trim();
            this.exactEndTime = times[1].trim();
        } else {
            this.exactStartTime = timeRange;
            this.exactEndTime = "";
        }
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public String getExactStartTime() { return exactStartTime; }
    public String getExactEndTime() { return exactEndTime; }
}