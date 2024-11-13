package controllers;

import javafx.beans.property.*;

public class TransactionRecord {
    private final IntegerProperty transactionID;
    private final IntegerProperty slotID;
    private final StringProperty startDate;
    private final StringProperty endDate;
    private final DoubleProperty cost;
    private final StringProperty type;

    public TransactionRecord(int transactionID, int slotID, String startDate, String endDate, double cost, String type) {
        this.transactionID = new SimpleIntegerProperty(transactionID);
        this.slotID = new SimpleIntegerProperty(slotID);
        this.startDate = new SimpleStringProperty(startDate);
        this.endDate = new SimpleStringProperty(endDate);
        this.cost = new SimpleDoubleProperty(cost);
        this.type = new SimpleStringProperty(type);
    }

    public IntegerProperty transactionIDProperty() { return transactionID; }
    public IntegerProperty slotIDProperty() { return slotID; }
    public StringProperty startDateProperty() { return startDate; }
    public StringProperty endDateProperty() { return endDate; }
    public DoubleProperty costProperty() { return cost; }
    public StringProperty typeProperty() { return type; }

    public String getStartDate() { return startDate.get(); }
}
