package propNeoPixel.propNeoPixel;

public class Tests {
	
	public static void main2(String [] args) throws Exception {
		
		NEOStrip strip = new NEOStrip("COM7");
		
		for(int x=0;x<144;++x) {
			strip.set(x, x%8);
		}
		
		strip.draw();
		
	}

public static void main(String[] args) throws Exception {
		
		NEOStrip strip = new NEOStrip("COM5");

		// Pattern of 5 pixels with blanks on the end
		strip.setPattern(0, 0,1,2,3,2,1,0);
		
		while(true) {
			// Right to left
			for(int x=0;x<137;++x) {
				strip.stampPattern(0, x);
				strip.draw();
				Thread.sleep(20);
			}			
			// Left to right
			for(int x=137;x>0;--x) {
				strip.stampPattern(0, x);
				strip.draw();
				Thread.sleep(20);
			}
		}					
		
	}

}
