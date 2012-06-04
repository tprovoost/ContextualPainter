package plugins.tprovoost.contextualpainter;

import icy.canvas.IcyCanvas;
import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.main.Icy;
import icy.painter.AbstractPainter;
import icy.roi.ROI;
import icy.roi.ROI2D;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JPopupMenu;

import plugins.tprovoost.contextualpainter.popups.PopupClassic;
import plugins.tprovoost.contextualpainter.popups.PopupROI;

public class ContextualPainter extends AbstractPainter {

	static ROI2D selectedRoi = null;

	public ContextualPainter() {
		Icy.getMainInterface().addListener(new ROIListener());
	}

	@Override
	public void mouseClick(MouseEvent e, final Point2D imagePoint, final IcyCanvas canvas) {

		if (!e.isConsumed() && e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
			JPopupMenu popup = null;

			ROI roi = canvas.getSequence().getFocusedROI();
			if (roi != null || (selectedRoi != null && selectedRoi.getBounds().contains(imagePoint))) {
				// --------------
				// roi popupmenu
				// --------------
				if (roi != null) {
					roi.setSelected(true, true);
					popup = new PopupROI((ROI2D) roi, e, imagePoint, canvas);
				} else {
					selectedRoi.setSelected(true, true);
					popup = new PopupROI(selectedRoi, e, imagePoint, canvas);
				}
			} else {
				// ------------------
				// Default PopupMenu
				// ------------------
				popup = new PopupClassic(e, imagePoint, canvas);
			}
			popup.setLocation(e.getLocationOnScreen());
			// Display
			popup.show(e.getComponent(), e.getX(), e.getY());
			selectedRoi = null;
		} else {
			ROI roiSelected = canvas.getSequence().getSelectedROI();
			if (roiSelected == null || roiSelected instanceof ROI2D)
				selectedRoi = (ROI2D) roiSelected;
		}
	}

	class ROIListener extends MainAdapter {

		@Override
		public void roiAdded(MainEvent event) {
			ROI r = (ROI) event.getSource();
			if (r == null)
				return;
			if (r instanceof ROI2D)
				selectedRoi = (ROI2D) r;
			super.roiAdded(event);
		}

		@Override
		public void roiRemoved(MainEvent event) {
			ROI r = (ROI) event.getSource();
			if (r == null)
				return;
			if (selectedRoi == r)
				selectedRoi = null;
			super.roiRemoved(event);
		}

	}
}
