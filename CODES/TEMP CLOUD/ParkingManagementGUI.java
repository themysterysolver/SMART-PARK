import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParkingManagementGUI extends JFrame {
    private JPanel slotPanel;
    private int totalSlots;

    public ParkingManagementGUI() {
        setTitle("Admin Parking Management");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        String input = JOptionPane.showInputDialog(this, "Enter the number of parking slots:");
        try {
            totalSlots = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Setting default to 10 slots.");
            totalSlots = 10; 
        }

      
        slotPanel = new JPanel();
        slotPanel.setLayout(new GridLayout(0, 4));  
        add(slotPanel, BorderLayout.CENTER);

        createParkingSlots(totalSlots);

        setVisible(true);
    }

    private void createParkingSlots(int totalSlots) {
        for (int i = 1; i <= totalSlots; i++) {
            JButton slotButton = new JButton("Slot " + i);
            slotButton.setBackground(Color.GREEN);
            slotButton.setActionCommand(String.valueOf(i));
            slotButton.addActionListener(new SlotButtonListener());
            slotPanel.add(slotButton);
        }
    }

    private class SlotButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String slotId = e.getActionCommand();
            JOptionPane.showMessageDialog(ParkingManagementGUI.this, "You clicked Slot " + slotId);
        }
    }

    public static void main(String[] args) {
        new ParkingManagementGUI();
    }
}