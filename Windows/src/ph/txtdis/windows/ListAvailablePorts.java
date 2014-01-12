package ph.txtdis.windows;

import gnu.io.CommPortIdentifier;  

import java.util.Enumeration;  

public class ListAvailablePorts { 
	private String portName;

	public String getPortName() {
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();  

		while(ports.hasMoreElements())
			setPortName(((CommPortIdentifier)ports.nextElement()).getName()); 
		
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}  
}  
