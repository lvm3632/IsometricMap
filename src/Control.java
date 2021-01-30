//Jorge Luis Manzo Zúniga	A01633991
//Luis Alberto Bodart Valdez	A01635000
//Michel Lujano Velázquez	A01636172

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Control extends MouseAdapter {

	protected static boolean leftClick;
	protected static int mouse_X, mouse_Y;

	public Control() {
		leftClick = false;
		mouse_X = mouse_Y = 0;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			leftClick = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			leftClick = false;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouse_X = e.getX();
		mouse_Y = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouse_X = e.getX();
		mouse_Y = e.getY();
	}
}
