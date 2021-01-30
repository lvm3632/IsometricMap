//Jorge Luis Manzo Zúniga	A01633991
//Luis Alberto Bodart Valdez	A01635000
//Michel Lujano Velázquez	A01636172

import java.awt.image.BufferedImage;

public class Car {

	private BufferedImage[] frames;
	private int index;

	public Car(BufferedImage[] frames, int i) {
		this.frames = frames;
		this.index = i;
	}

	public BufferedImage getCurrentFrame() {
		return this.frames[this.index];
	}
}
