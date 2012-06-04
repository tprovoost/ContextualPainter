package plugins.tprovoost.contextualpainter.popups;

import icy.canvas.IcyCanvas;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.roi.ROI2D;
import icy.roi.ROI2DArea;
import icy.roi.ROI2DEllipse;
import icy.roi.ROI2DPolygon;
import icy.roi.ROI2DRectangle;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.type.DataType;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class PopupROI extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1447332106769927581L;
	static ROI2D roiCopied = null;

	/**
	 * This class creates a JPopupMenu for the ROIs, when user right clich on
	 * their edge or center.
	 * 
	 * @param roi
	 * @param e
	 * @param imagePoint
	 * @param canvas
	 */
	public PopupROI(final ROI2D roi, MouseEvent e, final Point2D imagePoint, final IcyCanvas canvas) {
		super(roi.getName());
		Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0, getForeground());
		TitledBorder labelBorder = BorderFactory.createTitledBorder(titleUnderline, getLabel(), TitledBorder.CENTER, TitledBorder.ABOVE_TOP, super.getFont().deriveFont(Font.BOLD),
				getForeground());
		setBorder(BorderFactory.createCompoundBorder(getBorder(), labelBorder));

		// ---------
		// ROI MENU
		// ---------
		JMenuItem itemCrop = new JMenuItem("Crop");
		itemCrop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ThreadUtil.bgRun(new Runnable() {

					@Override
					public void run() {
						crop(canvas.getSequence(), roi, false);
					}
				});
			}
		});
		add(itemCrop);
		itemCrop.setEnabled(roi instanceof ROI2DRectangle || roi instanceof ROI2DPolygon || roi instanceof ROI2DEllipse || roi instanceof ROI2DArea);

		JMenuItem itemCropBounds = new JMenuItem("Crop Bounds");
		itemCropBounds.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ThreadUtil.bgRun(new Runnable() {

					@Override
					public void run() {
						crop(canvas.getSequence(), roi, true);
					}
				});
			}
		});
		if (!(roi instanceof ROI2DRectangle))
			add(itemCropBounds);

		// ---------
		// COPY MENU
		// ---------
		JMenuItem itemCopy = new JMenuItem("Copy");
		itemCopy.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				roiCopied = (ROI2D) roi;
			}
		});
		add(itemCopy);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g2);
	}

	/**
	 * Crop the roi in the canvas only if the ROI is a Rectangle, a Polygon, an
	 * Ellipse or an Area. The <code>cropBound</code> boolean defines if the
	 * data outside the shape should be set to the smallest value on the image.
	 * 
	 * @param sequence
	 *            : Sequence on which the crop should happen.
	 * @param roi
	 *            : ROI used for cropping.
	 * @param cropBounds
	 *            : crop with a rectangle bound of the shape or not.
	 */
	public static void crop(Sequence sequence, ROI2D roi, boolean cropBounds) {
		crop(sequence, roi, cropBounds, true);
	}

	/**
	 * Crop the roi in the canvas only if the ROI is a Rectangle, a Polygon, an
	 * Ellipse or an Area. The <code>cropBound</code> boolean defines if the
	 * data outside the shape should be set to the smallest value on the image.
	 * 
	 * @param sequence
	 *            : Sequence on which the crop should happen.
	 * @param roi
	 *            : ROI used for cropping.
	 * @param cropBounds
	 *            : crop with a rectangle bound of the shape or not.
	 * @param replaceWithMinimum
	 *            : used only if cropBound is true: will set pixels outside the
	 *            shape to zero or the minimum value of the cropped image (for
	 *            visual purposes, because of automatic bounds of the LUT).
	 */
	public static void crop(Sequence sequence, ROI2D roi, boolean cropBounds, boolean replaceWithMinimum) {
		int sizeZ = sequence.getSizeZ();
		int sizeT = sequence.getSizeT();
		int sizeC = sequence.getSizeC();

		Rectangle r = roi.getBounds();
		Sequence cropped = sequence.getSubSequence(r.x, r.y, 0, 0, r.width, r.height, sizeZ, sizeT);
		cropped.setName(sequence.getName() + " - Cropped");
		if (cropBounds || roi instanceof ROI2DRectangle) {
			Icy.addSequence(cropped);
		} else if (roi instanceof ROI2DPolygon || roi instanceof ROI2DEllipse || roi instanceof ROI2DArea) {
			int w = cropped.getWidth();
			int h = cropped.getHeight();
			DataType type = cropped.getDataType_();

			for (int t = 0; t < sizeT; ++t) {

				for (int z = 0; z < sizeZ; ++z) {

					IcyBufferedImage img = new IcyBufferedImage(w, h, sizeC, DataType.DOUBLE);

					for (int c = 0; c < sizeC; ++c) {

						double[] values = cropped.getImage(t, z).convertToType(DataType.DOUBLE, false).getDataXYAsDouble(c);
						double min = cropped.getChannelMin(c);

						for (int y = 0; y < h; ++y) {

							for (int x = 0; x < w; ++x) {

								if (!roi.contains(x + r.x, y + r.y)) {
									if (replaceWithMinimum)
										values[x + y * w] = min;
									else
										values[x + y * w] = 0;
								}
							}
						}
						img.setDataXYAsDouble(c, values);
					}

					cropped.setImage(t, z, img.convertToType(type, false));
				}

			}
			Icy.addSequence(cropped);
		}
	}

}
