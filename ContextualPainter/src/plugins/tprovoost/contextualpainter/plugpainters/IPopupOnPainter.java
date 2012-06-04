package plugins.tprovoost.contextualpainter.plugpainters;

import icy.canvas.IcyCanvas;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenu;

/**
 * This interface can be used by any Painter. The JPopupMenu of the Contextual Painter
 * will differ according to the created JPopupMenu.
 * @author thomasprovoost
 *
 */
public interface IPopupOnPainter {

	/**
	 * Returns if the painter should display a JPopupMenu or not.
	 * @param e
	 * @param imagePoint
	 * @param canvas
	 * @return
	 */
	public boolean isPopupWanted(MouseEvent e, Point2D imagePoint, IcyCanvas canvas);
	
	/**
	 * Returns the needed JPopupMenu.
	 * @param e
	 * @param imagePoint
	 * @param canvas
	 * @return
	 */
	public JMenu createMenu(MouseEvent e, Point2D imagePoint, IcyCanvas canvas);
	
}
