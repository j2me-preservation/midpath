/*
 *  (c) Copyright 2003 Christian Lorenz  ALL RIGHTS RESERVED.
 * 
 * This file is part of the JavaBluetooth Stack.
 * 
 * The JavaBluetooth Stack is free software; you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * The JavaBluetooth Stack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Created on Jul 31, 2003
 * by Christian Lorenz
 */

package org.javabluetooth.demo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.javabluetooth.demo.Connector;
import org.javabluetooth.stack.BluetoothStack;
import org.javabluetooth.stack.BluetoothStackLocal;
import org.javabluetooth.stack.hci.BlueZTransport;
import org.javabluetooth.stack.hci.HCIDriver;
import org.javabluetooth.stack.hci.HCIException;
import org.javabluetooth.stack.hci.UARTTransport;

/** 
 * The GUI Application used to demonstrate the Bluetooth Stack. It performs an Inquiry
 * and displays the available devices. When a device is clicked it performs a Service Lookup
 * and displays the available services.
 * @author Christian Lorenz
 */
public class BluetoothBrowser implements DiscoveryListener {
	private static ImageIcon devIcon = new ImageIcon("icon.jpg");
	private static ImageIcon serviceIcon = new ImageIcon("icon.jpg");
	private Vector devList = new Vector();
	private Vector services = new Vector(); // the services corresponding to the devices in the previous Vector
	public JFrame mainFrame;
	private JPanel panel;
	public JPanel top;
	public JLabel statusLabel;
	private LocalDevice ld;
	private DiscoveryAgent agent;

	/** Draws all GUI components and assigns event handlers */
	public BluetoothBrowser(String name) throws BluetoothStateException {
		ld = LocalDevice.getLocalDevice();
		 System.out.println("Local Bluetooth Name is " + ld.getFriendlyName());
		agent = ld.getDiscoveryAgent();
		//ld.setDiscoverable(DiscoveryAgent.GIAC);
		
		// create first the mail Windows
		mainFrame = new JFrame(name);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) { // Exit the application when the user clicks with the mouse
				System.exit(0);
			}
		});
		mainFrame.setSize(new Dimension(400, 500));
		mainFrame.setResizable(true);

		/*
		 * construct the panel with all the buttons here
		 *
		 */

		//		add a new jpanel for the buttons
		JPanel buttons = new JPanel();
		JButton inquiryButton = new JButton("Initiate Inquiry");
		inquiryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusLabel.setText("Inquiry in Progress...");
				inquire();
			}
		});
		buttons.add(inquiryButton);
		//first create our 2 panels, top and bottom
		top = new JPanel();
		top.setPreferredSize(new Dimension(640, 440));
		top.setLayout(new FlowLayout());
		top.setBackground(Color.white);
		//status label
		statusLabel = new JLabel();
		statusLabel.setText("Waiting...(ready)");
		// now add everything to the frame
		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.getContentPane().add(buttons, BorderLayout.NORTH); // add the panel in the frame
		mainFrame.getContentPane().add(statusLabel, BorderLayout.SOUTH);
		mainFrame.getContentPane().add(top); // add the device & services split pane to the frame
		mainFrame.pack();
		mainFrame.show();
	}

	public void inquire() {
		statusLabel.setText("Inquiry in Progress...");
		devList.removeAllElements();
		try {
			agent.startInquiry(DiscoveryAgent.GIAC, this);
		} catch (BluetoothStateException e) {
			JOptionPane.showMessageDialog(null, e, "Generic Bluetooth Exception", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void connectTo(ServiceRecord servRec) {
		String url = servRec.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
		// open up a new JFrame
		top.removeAll();
		JLabel incoming = new JLabel();
		JTextArea outgoing = new JTextArea();
		top.add(incoming);
		top.add(outgoing);
		top.revalidate();
		// initiate chat now!
		try {
			L2CAPConnection conn = (L2CAPConnection) Connector.open(url);
			byte[] receiveBytes = new byte[1024];
			//while (true) {
			/*String text = outgoing.getText();
			 if((text!=null)&&(text!=""))
			 {	byte[] bytes = text.getBytes();
			 outgoing.setText("");
			 conn.send(bytes);	
			 }*/

			if (conn.ready()) {
				conn.receive(receiveBytes);
				incoming.setText(receiveBytes.toString());
			}
			//}
		} catch (IOException e) {
		}
	}

	public void listServices(RemoteDevice dev) {
		services.removeAllElements();
		int[] attrIDList = { 0, 1, 4, 256 };
		UUID[] uuidList = { new UUID(0x1002) };
		try {
			agent.searchServices(attrIDList, uuidList, dev, this);
		} catch (BluetoothStateException e) {
			JOptionPane.showMessageDialog(null, e, "Generic Bluetooth Exception", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) throws BluetoothStateException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, HCIException {
		//HCIDriver.init(new UARTTransport("serial0"));
		//HCIDriver.init(new UARTTransport("COM40"));
		HCIDriver.init(new BlueZTransport(0));
		BluetoothStack.init(new BluetoothStackLocal());
		//BluetoothStack.init(new BluetoothTCPClient("192.168.10.2", 2600));
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); // get the native OS look and feel
		BluetoothBrowser browser = new BluetoothBrowser("Bluetooth Network Neighborhood"); // create and show the app. (window)
		
		browser.inquire();
		
		
	}

	/** @see javax.bluetooth.DiscoveryListener#deviceDiscovered(javax.bluetooth.RemoteDevice, javax.bluetooth.DeviceClass) */
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		System.out.println("***************** Device Discovered: " + btDevice.bdAddrLong);
		devList.add(btDevice);
	}

	/** @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int, javax.bluetooth.ServiceRecord[]) */
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		for (int i = 0; i < servRecord.length; i++) {
			ServiceRecord record = servRecord[i];
			services.addElement(record);
		}
	}

	/** @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int, int) */
	public void serviceSearchCompleted(int transID, int respCode) {
		top.removeAll();
		for (int i = 0; i < services.size(); i++) {
			final ServiceRecord record = (ServiceRecord) services.get(i);
			final JLabel devLabel = new JLabel(serviceIcon);
			
			devLabel.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					DataElement connectionInfo = record.getAttributeValue(4);
					if (connectionInfo != null) {
						statusLabel.setText(connectionInfo.toString());
					}
				}
				public void mouseExited(MouseEvent e) {
					statusLabel.setText(" ");
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				public void mouseReleased(MouseEvent e) {
					statusLabel.setText("Connecting to "
							+ record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
					connectTo(record);
				}
			});

			//new ServiceRecordMouseListener(devLabel, this, record));
			DataElement nameElement = record.getAttributeValue(256);
			if (nameElement != null) {
				String serviceName = (String) nameElement.getValue();
				devLabel.setText(serviceName);
			}
			top.add(devLabel);
		}
		top.revalidate();
	}

	/** @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int) */
	public void inquiryCompleted(int discType) {
		
		System.out.println("***************** Inquiry Completed");
		top.removeAll();
		for (int i = 0; i < devList.size(); i++) {
			final RemoteDevice remoteDev = (RemoteDevice) devList.get(i);
			final JLabel devLabel = new JLabel(devIcon);

			devLabel.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					try {
						String friendlyName = remoteDev.getFriendlyName(false);
						devLabel.setText(friendlyName);
					} catch (IOException e1) {
					}
				}

				public void mouseExited(MouseEvent e) {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

				public void mouseReleased(MouseEvent e) {
					listServices(remoteDev); // list the services for the specified device
				}
			});

			//new RemoteDeviceMouseListener(devLabel, this, remoteDev)); // add a mouse listener to the bluetooth device
			devLabel.setText(remoteDev.getBluetoothAddress());
			top.add(devLabel);
		}
		top.revalidate();
		statusLabel.setText("Inquiry in Completed.");
	}
}
