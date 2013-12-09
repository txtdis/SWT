
package ph.txtdis.windows;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public abstract class Printer {
	private int yLoop, width;
	private int[][] value;
	protected boolean printed;
	protected SerialPort port;
	protected OutputStream os;
	protected InputStream is;
	protected PrintStream ps;
	protected Report report;

	// Decimal ASCII values for ESC/P commands 
	protected static final char ESC 	= 27; 			//escape
	protected static final char AT 		= 64; 			//@
	protected static final char EXCLAMATION = 33;		//!
	protected static final char WIDE 	= 0b0000_0000;	//5x7
	protected static final char HUGE 	= 0b0011_0001;	//double width & height
	protected static final char N 		= 78; 			//N
	protected static final char CHAR_PER_LINE = 4;		//character per line
	protected static final char NARROW	= 1; 			//42 cpl
	protected static final char DLE		= 16; 			//DLE
	protected static final char EOT		= 4; 			//EOT
	protected static final char STATUS	= 1; 			//printer status
	private static final char ASTERISK 	= 42;			//*
	private static final char J 		= 74; 			//J

	protected static final char COLUMN_WIDTH = 42;

	public Printer() {
	}

	public Printer(Report report) {
		this.report = report;
		setPrinter();
	}

	protected void setPrinter() {
		printed = false;
		// Get Printer Port
		String wantedPortName = "COM14";			 
		Enumeration<?> portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId = null;
		String portName = null;
		while (portIdentifiers.hasMoreElements()) {
			portId = (CommPortIdentifier) portIdentifiers.nextElement();
			System.out.println("portName: "  + portId.getName());
			if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL &&
					portId.getName().equals(wantedPortName)) {
				portName = portId.getName();
				break;
			}
		}
		try {
			if(portName == null) {
				new ErrorDialog(
						wantedPortName + " cannot be found;\n" +
								"ensure printer is on and\n" +
								"plugged in to said port,\n" +
						"restart server then start again.");
			} else {
				// Open Serial Port
				port = (SerialPort) portId.open(
						portName, 	// Name of the application asking for the port 
						100  	// Wait max. 10 sec. to acquire port
						);
				// Set Serial Port Parameters
				port.setSerialPortParams(  
						9600, 
						SerialPort.DATABITS_8, 
						SerialPort.STOPBITS_1, 
						SerialPort.PARITY_NONE);
				// Read from Serial Port
				is = port.getInputStream();
				os = port.getOutputStream();
				ps = new PrintStream(os, true);
				// get logo and convert to a BitArray
				setLogo();
				printed = print();
			}
		} catch(PortInUseException e) {
			e.printStackTrace();
			new ErrorDialog("" +
					"Port Already in Use.\n" +
					"Close Other Apps.\n"); 
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
			new ErrorDialog("UnsupportedCommOperation:\n" + e);
		} catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("" +
					"No Signal from Printer.\n" +
					"Restart It and Try Again.");
		} finally {
			// close port
			try {
				if (is != null) is.close();
				if (os != null) os.close();
				if (ps != null) ps.close();
				if (port != null) port.close();
			} catch (IOException e) {
				e.printStackTrace();
				new ErrorDialog("Close Port:\n" + e);
			}
		}
	}

	private void setLogo() {
		String string;
		Image image = new Image(
				UI.DISPLAY, 
				this.getClass().getResourceAsStream("images/Magnum.bmp"));
		ImageData data = image.getImageData(); 
		int height = data.height;
		int yOffset = height % 8 / 2;
		int pixel;
		yLoop = height / 8;
		width = data.width;
		value = new int[yLoop][width];
		for (int i = 0; i < yLoop; i++) {
			for (int x = 0; x < width; x++) {
				string = "";
				for (int y = 0; y < 8; y++) {
					pixel = data.getPixel(x, yOffset + y + i * 8);
					string += pixel == 1 ? "0" : "1";
				}
				value[i][x] = Integer.parseInt(string, 2);				
			}
		}		
	}

	protected void printLogo() {
		try {
			for (int i = 0; i < yLoop; i++) {
				os.write(ESC);
				os.write(ASTERISK);
				os.write((byte) 0); 	// m
				os.write((byte) width); // n1
				os.write((byte) 0); 	// n2
				for (int j = 0; j < width; j++) 
					os.write((byte) value[i][j]);
				os.write(ESC);
				os.write(J);
				os.write((byte) 14);	// n/144" feed
				os.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			new ErrorDialog("Print Dialog:\n " + e);
		}
	}

	protected void printNormal() throws IOException {
		os.write(ESC);
		os.write(N);
		os.write(CHAR_PER_LINE);
		os.write(NARROW);
		os.write(ESC);
		os.write(AT);
		ps.println();
	}

	protected void printHuge() throws IOException {
		os.write(ESC);
		os.write(EXCLAMATION);
		os.write(HUGE);
	}


	protected void printDash() {
		ps.println(StringUtils.leftPad("", COLUMN_WIDTH, "-"));
	}

	protected void printPageEnd() {
		ps.println("________________________________________");
		ps.println();
		ps.println();
		ps.println();
		ps.println();
	}

	protected void waitForPrintingToEnd() throws IOException {
		for (int i = 0; i < 10; i++) {
			int buffer = port.getOutputBufferSize();
			if(buffer == 0) {
				os.write(DLE);
				os.write(EOT);
				os.write(STATUS);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected boolean print() throws IOException {
		return false;
	}

	public boolean isPrinted() {
		return printed;
	}
}

