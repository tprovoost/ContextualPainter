package plugins.tprovoost.contextualpainter.plugpainters;

import icy.canvas.IcyCanvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * This class is an example of the use of {@link IPopupOnPainter} with
 * the ImagePainter class.
 * 
 * @see ImagePainter
 * 
 * @author thomasprovoost
 * 
 */
public class MenuImagePainter extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8520188896414153144L;

	public MenuImagePainter(final ImagePainter painter, MouseEvent e, final Point2D imagePoint, final IcyCanvas canvas) {
		super(canvas.getLayer(painter).getName() + " (ImagePainter)");

		// ---------
		// ROI MENU
		// ---------
		JMenuItem itemEdit = new JMenuItem("Edit");
		itemEdit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				painter.setEditable(true);
			}
		});
		add(itemEdit);
	}

}
