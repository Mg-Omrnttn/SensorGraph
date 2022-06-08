import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

public class SensorGraphMain {
    static SerialPort chosenPort; //object that represents the serial port we are going to connect to. available from jSerialComm library
    static int x = 0;

    public static void main(String[] args) {
        // create and configure the window
        JFrame window = new JFrame();
        window.setTitle("Sensor Graph GUI");
        window.setSize(600,400);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //close the window and the program at the same time

        // create a drop-down box and connect button, then place them at the top of the window
        JComboBox<String> portList = new JComboBox<String>(); // JComboBox is java's drop-down box
        JButton connectButton = new JButton("Connect");
        JPanel topPanel = new JPanel();  //to place JPanel on JFrame later
        topPanel.add(portList);  // place button and combobox on panel
        topPanel.add(connectButton);
        window.add(topPanel, BorderLayout.NORTH);  // place JPanel on JFrame

        // populate the drop-down box
        SerialPort[] portNames= SerialPort.getCommPorts();  // a method in the jSerialComm library gives an array of serial port
        for (int i = 0; i < portNames.length; i++) {  // add serial ports on drop-down box
            portList.addItem(portNames[i].getSystemPortName());  // CHECK after Arduino coding and its connection
        }

        //create the line graph
        XYSeries series = new XYSeries("light Sensor Readings");
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        // multiple series can be added. see youtube
        JFreeChart chart = ChartFactory
                .createXYLineChart("Light Sensor Readings", "Time in seconds", "ADC Reading", dataset);
        window.add(new ChartPanel(chart), BorderLayout.CENTER);  //put chart on JFrame with ChartPanel

        // configure the connect button and use another thread to listen for data
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(connectButton.getText().equals("Connect")) {  // difficult :/
                    // attempt to connect to the serial port
                    chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
                    chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0,0);

                    if(chosenPort.openPort()) {
                        connectButton.setText("Disconnect");
                        portList.setEnabled(false);
                    }

                    // create a new thread that listens for incoming text and populates the graph
                    Thread thread = new Thread(){
                        @Override
                        public void run(){
                            Scanner scanner = new Scanner(chosenPort.getInputStream()); //scanner to read texts
                            while(scanner.hasNextLine()){
                                try {
                                    String line = scanner.nextLine();
                                    int number = Integer.parseInt(line);
                                    series.add(x++,number);  //maybe I dont need what is done on youtube?
                                    window.repaint();
                                } catch(Exception e) {
                                    //do nothing
                                }

                            }
                            scanner.close();
                        }
                    };
                    thread.start();
                } else {
                    // disconnect from the serial port
                    chosenPort.closePort();
                    portList.setEnabled(true);
                    connectButton.setText("Connect");
                    series.clear();
                    x = 0;
                }
            }
        });

        //show the window
        window.setVisible(true);
    }
}

//need revisit from 48:00
