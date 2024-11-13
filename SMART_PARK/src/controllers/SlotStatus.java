package controllers;

public class SlotStatus {
    private String status;
    private int count;

    public SlotStatus(String status, int count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public int getCount() {
        return count;
    }
}
