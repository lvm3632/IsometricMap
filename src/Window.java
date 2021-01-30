//Jorge Luis Manzo Zúniga	A01633991
//Luis Alberto Bodart Valdez	A01635000
//Michel Lujano Velázquez	A01636172

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Window extends JFrame {

	private TileMap tileMap;
	private Control control;

	public Window() {
		super("Isometric Map");
		setSize(1024, 768);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tileMap = new TileMap();
		control = new Control();

		add(tileMap);
		addMouseListener(control);
		addMouseMotionListener(control);

		setVisible(true);
	}

	public static void main(String[] args) {
		// Get UI of PC instead of java
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (InstantiationException ie) {
			ie.printStackTrace();
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		} catch (UnsupportedLookAndFeelException ulfe) {
			ulfe.printStackTrace();
		}
		new Window();
	}
}
