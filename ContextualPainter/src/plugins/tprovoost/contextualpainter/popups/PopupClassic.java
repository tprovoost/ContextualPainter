package plugins.tprovoost.contextualpainter.popups;

import icy.canvas.IcyCanvas;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.painter.Painter;
import icy.sequence.Sequence;
import icy.type.DataType;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import plugins.tprovoost.contextualpainter.plugpainters.IPopupOnPainter;
import plugins.tprovoost.contextualpainter.plugpainters.ImagePainter;

public class PopupClassic extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9098022483394607257L;
	private static IcyBufferedImage selectedImage = null;

	MouseEvent e;
	Point2D imagePoint;
	IcyCanvas canvas;

	public PopupClassic(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		super("Images");
		this.e = e;
		this.imagePoint = imagePoint;
		this.canvas = canvas;

		Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0, getForeground());
		TitledBorder labelBorder = BorderFactory.createTitledBorder(titleUnderline, getLabel(), TitledBorder.CENTER, TitledBorder.ABOVE_TOP, super.getFont().deriveFont(Font.BOLD),
				getForeground());
		setBorder(BorderFactory.createCompoundBorder(getBorder(), labelBorder));

		final Sequence s = canvas.getSequence();

		// ---------
		// COPY MENU
		// ---------
		JMenu menuCopy = getCopyMenu(s);
		add(menuCopy);

		// ---------
		// PASTE MENU
		// ---------
		JMenu menuPaste = getPasteMenu(s);
		add(menuPaste);
		menuPaste.setEnabled(selectedImage != null || PopupROI.roiCopied != null);

		// ---------------
		// PAINTERS MENUS
		// ---------------
		JMenu menuPainters = new JMenu("Painters");
		menuPainters.setToolTipText("Only specific painters are visible here");
		for (Painter p : canvas.getLayersPainter()) {
			if (p instanceof IPopupOnPainter && ((IPopupOnPainter) p).isPopupWanted(e, imagePoint, canvas)) {
				menuPainters.add(((IPopupOnPainter) p).createMenu(e, imagePoint, canvas));
			}
		}
		if (menuPainters.getMenuComponentCount() == 0) {
			JMenuItem item = new JMenuItem("Painters");
			item.setToolTipText("Only specific painters are visible here");
			item.setEnabled(false);
			add(item);
		} else
			add(menuPainters);

		// -------------
		// CONVERT MENU
		// -------------
		JMenu menuConvert = getConvertMenu(canvas);
		add(menuConvert);

		// -------------
		// EXTRACT MENU
		// -------------
		JMenu menuExtract = getExtractMenu(canvas);
		add(menuExtract);

		// -------------
		// SNAPSHOT MENU
		// -------------
		JMenu menuSnapshot = new JMenu("Snapshot");
		JMenuItem itemSnapshotDefault = new JMenuItem("Default Size");
		itemSnapshotDefault.addActionListener(createListener(canvas, false));
		menuSnapshot.add(itemSnapshotDefault);
		JMenuItem itemSnapshotCurrentSize = new JMenuItem("Current Size");
		itemSnapshotCurrentSize.addActionListener(createListener(canvas, true));
		menuSnapshot.add(itemSnapshotCurrentSize);
		add(menuSnapshot);

		// ---------------
		// DUPLICATE MENU
		// ---------------
		JMenuItem itemDupli = new JMenuItem("Duplicate");
		itemDupli.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Icy.addSequence(s.getCopy());
			}
		});
		add(itemDupli);
	}

	/**
	 * Generates the Paste menu.
	 * 
	 * @param s
	 *            : reference to sequence.
	 * @return
	 */
	private JMenu getPasteMenu(final Sequence s) {
		JMenu toReturn = new JMenu("Paste");

		JMenu itemPasteImage = new JMenu("Image");
		JMenuItem itemAsPainter = new JMenuItem("As Painter");
		itemAsPainter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				s.addPainter(new ImagePainter(selectedImage));
			}
		});
		itemPasteImage.add(itemAsPainter);
		itemPasteImage.setEnabled(selectedImage != null);
		toReturn.add(itemPasteImage);

		// ---------
		// PASTE MENU
		// ---------
		JMenuItem itemPasteROI = new JMenuItem("ROI");
		itemPasteROI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				s.addROI(PopupROI.roiCopied);
			}
		});
		itemPasteROI.setEnabled(PopupROI.roiCopied != null);
		toReturn.add(itemPasteROI);

		return toReturn;
	}

	/**
	 * Generates the copy menu
	 * 
	 * @param s
	 *            : reference to sequence.
	 * @return
	 */
	private JMenu getCopyMenu(Sequence s) {
		JMenu toReturn = new JMenu("Copy");

		// -------------
		// CHANNEL COPY
		// -------------
		JMenu itemCopySingleChannel = new JMenu("Copy Channel");
		for (int i = 0; i < s.getSizeC(); ++i) {
			itemCopySingleChannel.add(createChannelItem(i));
		}
		toReturn.add(itemCopySingleChannel);

		// -------------
		// IMAGE COPY
		// -------------
		JMenuItem itemCopySingleImage = new JMenuItem("Copy Image Only");
		itemCopySingleImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Viewer v = Icy.getMainInterface().getFocusedViewer();
				selectedImage = v.getCurrentImage().getCopy();
			}
		});
		toReturn.add(itemCopySingleImage);

		// -------------
		// SNAPSHOT COPY
		// -------------
		JMenuItem itemCopySnapshot = new JMenuItem("Copy Snapshot");
		itemCopySnapshot.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				IcyCanvas canvas = Icy.getMainInterface().getFocusedViewer().getCanvas();
				Sequence rendered = canvas.getRenderedSequence(false);
				selectedImage = rendered.getFirstImage();
			}
		});
		toReturn.add(itemCopySnapshot);

		// -------------
		// PAINTERS COPY
		// -------------
		JMenu itemCopyPainter = getMenuPainters(s);
		toReturn.add(itemCopyPainter);

		return toReturn;
	}

	private JMenu getMenuPainters(final Sequence s) {
		// variables
		final JMenu toReturn = new JMenu("Copy Painter");
		final Viewer v = Icy.getMainInterface().getFocusedViewer();

		// all painters item
		JMenuItem itemAll = new JMenuItem("All painters");
		itemAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BufferedImage img = new BufferedImage(s.getWidth(), s.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = img.createGraphics();

				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				for (Painter p : s.getPainters()) {
					final float alpha = v.getCanvas().getLayer(p).getAlpha();

					if (alpha != 1f)
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
					else
						g.setComposite(AlphaComposite.SrcOver);
					p.paint(g, s, v.getCanvas());
				}
				selectedImage = IcyBufferedImage.createFrom(img);
				g.dispose();
			}
		});
		toReturn.add(itemAll);

		// creation of others
		for (Painter p : s.getPainters()) {
			toReturn.add(createPainterItem(s, v, p));
		}

		return toReturn;
	}

	private JMenu getConvertMenu(final IcyCanvas canvas) {
		JMenu toReturn = new JMenu("Convert");
		final Sequence s = canvas.getSequence();

		for (int i = 0; i < DataType.values().length - 1; ++i) {
			final DataType type = DataType.values()[i];
			JMenuItem item = new JMenuItem(type.toLongString());
			if (type == s.getDataType_())
				item.setEnabled(false);
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					boolean rescale = ConfirmDialog.confirm("Rescale", "Do you want to rescale the values ?", JOptionPane.YES_NO_OPTION);
					Icy.addSequence(s.convertToType(type, rescale));
				}
			});
			toReturn.add(item);
		}
		return toReturn;
	}

	private JMenu getExtractMenu(final IcyCanvas canvas) {
		final Sequence s = canvas.getSequence();
		final int sizeZ = s.getSizeZ();
		final int sizeT = s.getSizeT();
		JMenu toReturn = new JMenu("Extract");

		JMenu menuExtractChannel = new JMenu("Channel");
		for (int i = 0; i < s.getSizeC(); ++i) {
			JMenuItem item = new JMenuItem("Extract channel " + i);
			final int currentChannel = i;
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Sequence extracted = s.extractChannel(currentChannel);
					extracted.setName(s.getName() + " - Channel " + currentChannel);
					Icy.addSequence(extracted);
				}
			});
			menuExtractChannel.add(item);
		}
		toReturn.add(menuExtractChannel);

		if (sizeT > 1 || sizeZ > 1) {
			JMenuItem itemExtractImg = new JMenuItem("Current Image");
			itemExtractImg.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int posZ = canvas.getPositionZ();
					int posT = canvas.getPositionT();
					Sequence extracted = new Sequence(canvas.getCurrentImage());
					extracted.setName(s.getName() + " - z: " + posZ + " t: " + posT);
					Icy.addSequence(extracted);
				}
			});
			toReturn.add(itemExtractImg);

			if (sizeT > 1 && sizeZ > 1) {
				JMenuItem itemExtractZ = new JMenuItem("Z stack");
				itemExtractZ.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						int posT = canvas.getPositionT();
						Sequence extracted = new Sequence();
						extracted.addVolumetricImage(0, s.getVolumetricImage(posT));
						extracted.setName(s.getName() + " - t: " + posT);
						Icy.addSequence(extracted);
					}
				});
				toReturn.add(itemExtractZ);

				JMenuItem itemExtractT = new JMenuItem("T stack");
				itemExtractT.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						int posZ = canvas.getPositionZ();
						Sequence extracted = s.getSubSequence(0, 0, posZ, 0, s.getSizeX(), s.getSizeY(), 1, sizeT);
						extracted.setName(s.getName() + " - z: " + posZ);
						Icy.addSequence(extracted);
					}
				});
				toReturn.add(itemExtractT);
			}
		}

		return toReturn;
	}

	private ActionListener createListener(final IcyCanvas canvas, final boolean canvasView) {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Sequence rendered = canvas.getRenderedSequence(canvasView);
				rendered.setName("Render of " + canvas.getSequence().getName());
				Icy.addSequence(rendered);
			}
		};
	}

	private JMenuItem createPainterItem(final Sequence s, final Viewer v, final Painter p) {
		JMenuItem toReturn = new JMenuItem("" + p.getClass().getSimpleName());
		toReturn.setToolTipText("" + p.getClass().getName());

		toReturn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BufferedImage img = new BufferedImage(s.getWidth(), s.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = img.createGraphics();

				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				final float alpha = v.getCanvas().getLayer(p).getAlpha();

				if (alpha != 1f)
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				else
					g.setComposite(AlphaComposite.SrcOver);
				p.paint(g, s, v.getCanvas());
				selectedImage = IcyBufferedImage.createFrom(img);
				g.dispose();
			}
		});

		return toReturn;
	}

	private JMenuItem createChannelItem(final int channelID) {
		JMenuItem toReturn = new JMenuItem("Channel " + channelID);
		toReturn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Viewer v = Icy.getMainInterface().getFocusedViewer();
				selectedImage = v.getCurrentImage().extractChannel(channelID);
			}
		});
		return toReturn;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g2);
	}
}
