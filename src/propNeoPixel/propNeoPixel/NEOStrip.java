package propNeoPixel.propNeoPixel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * This class talks to the Propeller NeoPixelStrip driver using
 * serial commands. You call the methods here to fill up the
 * driver's buffer. Then call "draw" to render the buffer.
 */
public class NEOStrip {
	
	// Default color palette (4 shades of these)
	// BLACK*0 ... WHITE*3
	
	public static final int BLACK =  0;
	public static final int BLUE =   1;
	public static final int RED =    2;
	public static final int PURPLE = 3;
	public static final int GREEN =  4;
	public static final int CYAN =   5;
	public static final int YELLOW = 6;
	public static final int WHITE =  7;
	
	private OutputStream os;
	InputStream is;
	
	// Wait for the serial response
	private void waitThrottle(int value) throws IOException {		
		int w;
		while(true) {
			w = is.read();
			if(w>=0) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * This constructs a NEOStrip object.
	 * @param portName the name of the port to use, like "COM7"
	 * @throws NoSuchPortException communication failure
	 * @throws PortInUseException communication failure
	 * @throws UnsupportedCommOperationException communication failure
	 * @throws IOException communication failure
	 */
	public NEOStrip(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		SerialPort serialPort = (SerialPort) portIdentifier.open("NEOStrip",2000);					
		serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);		
		serialPort.setInputBufferSize(256);		
		
		os = serialPort.getOutputStream();
		is = serialPort.getInputStream();
		
		// I don't want to talk about it!
		// Some kind of weird timing thingie going on. Need this at the beginning.
		setPattern(0, 0,0,0,0,0);
	}
	
	/**
	 * Clear the strip's buffer.
	 */
	public void clear() {
		try {
			os.write('C');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Draw the strip's buffer to the LEDs.
	 */
	public void draw() {
		try {
			os.write('D');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Set an individual LED in the buffer.
	 * @param number the pixel number (0 to 144)
	 * @param color the color of the pixel (0 to 255)
	 */
	public void set(int number, int color) {
		try {
			os.write('S');
			os.write(number);
			os.write(color%32);
			waitThrottle('S');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
	
	/**
	 * Fill a line of pixels.
	 * @param start beginning of the line (inclusive)
	 * @param end of the line (inclusive)
	 * @param color pixel color
	 */
	public void fill(int start, int end, int color) {
		try {
			os.write('F');
			os.write(start);
			os.write(end);
			os.write(color);
			waitThrottle('F');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}
	
	/**
	 * Set an entry in the color palette
	 * @param index the color slot
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 */
	public void setPalletColor(int index, int r, int g, int b) {
		try {
			os.write('P');
			os.write(index);
			os.write(r);
			os.write(g);
			os.write(b);
			waitThrottle('P');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Set the contents of a pattern buffer
	 * @param index the pattern number 0 .. 15
	 * @param pix array of pixels
	 */
	public void setPattern(int index, int ... pix) {
		try {
			os.write('I');
			os.write(index);
			os.write(pix.length);
			for(int p : pix) {
				os.write(p);
			}
			waitThrottle('I');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
	
	/**
	 * Stamp the given pattern into the buffer
	 * @param patternIndex pattern number 0 .. 15
	 * @param pos start pixel position in the buffer
	 */
	public void stampPattern(int patternIndex, int pos) {
		try {
			os.write('M');
			os.write(patternIndex);
			os.write(pos);
			waitThrottle('M');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
