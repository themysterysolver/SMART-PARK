public abstract class Person {
    private String name;
    private String id;

    public Person(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public abstract void bookParkingSlot(ParkingBookingSystem bookingSystem, Vehicle vehicle);

    public void displayInfo();

    // Getters and Setters
}
public class Admin extends Person {
    public Admin(String name, String id) {
        super(name, id);
    }

    public void allocateParkingSlot(ParkingLotDisplay parkingLot, int slotNumber, Vehicle vehicle);

    @Override
    public void bookParkingSlot(ParkingBookingSystem bookingSystem, Vehicle vehicle);

    public void generateReport(Report report, String period);
}
public class User extends Person {
    public User(String name, String id) {
        super(name, id);
    }

    @Override
    public void bookParkingSlot(ParkingBookingSystem bookingSystem, Vehicle vehicle);
}
public class ParkingLotDisplay {
    private int totalSlots;
    private Vehicle[] slots;

    public ParkingLotDisplay(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public void displayAvailableSlots();

    public boolean isSlotAvailable(int slotNumber);

    public void assignSlot(int slotNumber, Vehicle vehicle);

    public void freeSlot(int slotNumber);
}
public class ParkingBookingSystem {
    private ParkingLotDisplay parkingLot;

    public ParkingBookingSystem(ParkingLotDisplay parkingLot) {
        this.parkingLot = parkingLot;
    }

    public void bookSlotForPerson(Person person, Vehicle vehicle);

    public void allocateSlot(int slotNumber, Vehicle vehicle);
}
public class Vehicle {
    private String vehicleNumber;
    private String vehicleType;

    public Vehicle(String vehicleNumber, String vehicleType) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
    }

    public void displayVehicleInfo();

    // Getters and Setters
}
public class Billing {
    private Vehicle vehicle;
    private long duration;
    private double ratePerHour;
    private double totalAmount;

    public Billing(Vehicle vehicle, long duration, double ratePerHour) {
        this.vehicle = vehicle;
        this.duration = duration;
        this.ratePerHour = ratePerHour;
    }

    public void calculateBill();

    public void printBill();

    // Getters and Setters
}
public class Report {
    private List<Billing> billings;

    public Report(List<Billing> billings) {
        this.billings = billings;
    }

    public void generateCostReport(String period);

    public void printReport();
}
