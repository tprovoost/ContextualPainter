package plugins.tprovoost.contextualpainter.plugpainters;

import icy.canvas.IcyCanvas;
import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.painter.AbstractPainter;
import icy.painter.PainterEvent;
import icy.painter.PainterListener;
import icy.resource.ResourceUtil;
import icy.sequence.Sequence;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenu;


public class ImagePainter extends AbstractPainter implements PainterListener, IPopupOnPainter {

	enum ActionOrigin {
		MOVE, UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT, UNKNOWN
	};

	private IcyBufferedImage img;
	private IcyBufferedImage imgToRender;
	private int x;
	private int y;
	private int width;
	private int height;
	private boolean isEditable = true;
	private ActionOrigin origin = ActionOrigin.UNKNOWN;
	private Point2D moveOrigin = null;

	// arrow images are loaded once
	private static Image upperLeft = ResourceUtil.getIconAsImage("alpha/arrow_top_left.png");
	private static Image lowerLeft = ResourceUtil.getIconAsImage("alpha/arrow_bottom_left.png");;
	private static Image upperRight = ResourceUtil.getIconAsImage("alpha/arrow_top_right.png");;
	private static Image lowerRight = ResourceUtil.getIconAsImage("alpha/arrow_bottom_rigth.png");;

	// Resized version of arrow images
	private Image upperLeftImage;
	private Image lowerLeftImage;
	private Image upperRightImage;
	private Image lowerRightImage;

	public ImagePainter(IcyBufferedImage img) {
		this(img, 0, 0);
	}

	public ImagePainter(IcyBufferedImage img, int x, int y) {
		this.img = img;
		this.x = x;
		this.y = y;
		width = img.getWidth();
		height = img.getHeight();
		imgToRender = img.getCopy();
		updateIcons();

	}

	void updateImageToRender() {
		imgToRender = img.getScaledCopy(width, height);
	}

	void updateIcons() {
		if (upperLeft != null) {
			IcyBufferedImage upperLeftResized = IcyBufferedImage.createFrom(ImageUtil.convertImage(upperLeft, null)).getScaledCopy(10, 10);
			upperLeftImage = ImageUtil.paintColorImageFromAlphaImage(upperLeftResized, null, Color.YELLOW);
		}
		if (upperRight != null) {
			IcyBufferedImage upperRightResized = IcyBufferedImage.createFrom(ImageUtil.convertImage(upperRight, null)).getScaledCopy(10, 10);
			upperRightImage = ImageUtil.paintColorImageFromAlphaImage(upperRightResized, null, Color.YELLOW);
		}
		if (lowerLeft != null) {
			IcyBufferedImage lowerLeftResized = IcyBufferedImage.createFrom(ImageUtil.convertImage(lowerLeft, null)).getScaledCopy(10, 10);
			lowerLeftImage = ImageUtil.paintColorImageFromAlphaImage(lowerLeftResized, null, Color.YELLOW);
		}
		if (lowerRight != null) {
			IcyBufferedImage lowerRightResized = IcyBufferedImage.createFrom(ImageUtil.convertImage(lowerRight, null)).getScaledCopy(10, 10);
			lowerRightImage = ImageUtil.paintColorImageFromAlphaImage(lowerRightResized, null, Color.YELLOW);
		}
	}

	@Override
	public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
		super.paint(g, sequence, canvas);
		Graphics2D g2 = (Graphics2D) g.create();
		
		if (isEditable) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.drawImage(imgToRender, null, x, y);

			// draw arrows
			g2.drawImage(upperLeftImage, x, y, null);
			g2.drawImage(upperRightImage, x + width - 10, y, null);
			g2.drawImage(lowerLeftImage, x, y + height - 10, null);
			g2.drawImage(lowerRightImage, x + width - 10, y + height - 10, null);
		} else {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawImage(imgToRender, null, x, y);
		}

		g2.dispose();
	}

	@Override
	public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		if (isEditable && e.getButton() == MouseEvent.BUTTON1) {
			double px = imagePoint.getX();
			double py = imagePoint.getY();

			if (px >= x && px <= x + width && py >= y && py <= y + height) {
				// coordinates in the image

				if (px >= x && px <= x + 10 && py >= y && py <= y + 10) {
					origin = ActionOrigin.UPPER_LEFT;
				} else if (px >= x + width - 10 && px <= x + width && py >= y && py <= y + 10) {
					origin = ActionOrigin.UPPER_RIGHT;
				} else if (px >= x && px <= x + 10 && py >= y + height - 10 && py <= y + height) {
					origin = ActionOrigin.LOWER_LEFT;
				} else if (px >= x + width - 10 && px <= x + width && py >= y + height - 10 && py <= y + height) {
					origin = ActionOrigin.LOWER_RIGHT;
				} else {
					origin = ActionOrigin.MOVE;
					moveOrigin = imagePoint;
				}
			}
			e.consume();
		}
	}

	@Override
	public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		if (isEditable && e.getButton() == MouseEvent.BUTTON1) {
			double px = imagePoint.getX();
			double py = imagePoint.getY();
			switch (origin) {
			case MOVE: {
				int moveX = (int) (imagePoint.getX() - moveOrigin.getX());
				int moveY = (int) (imagePoint.getY() - moveOrigin.getY());
				x += moveX;
				y += moveY;
				moveOrigin = imagePoint;
				break;
			}
			case UPPER_LEFT:
				width -= (int) px - x;
				height -= (int) py - y;
				x = (int) px;
				y = (int) py;
				height = height > 1 ? height : 1;
				width = width > 1 ? width : 1;
				break;
			case UPPER_RIGHT:
				height -= (int) py - y;
				width = (int) px - x > 1 ? (int) px - x : 1;
				y = (int) py;
				height = height > 1 ? height : 1;
				break;
			case LOWER_LEFT:
				width -= (int) px - x;
				height = (int) py - y > 1 ? (int) py - y : 1;
				x = (int) px;
				width = width > 1 ? width : 1;
				break;
			case LOWER_RIGHT:
				width = (int) px - x > 1 ? (int) px - x : 1;
				height = (int) py - y > 1 ? (int) py - y : 1;
				break;
			}
			updateImageToRender();
			changed();
			e.consume();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		super.mouseReleased(e, imagePoint, canvas);
		if (e.getButton() == MouseEvent.BUTTON1 && origin != ActionOrigin.UNKNOWN)
			origin = ActionOrigin.UNKNOWN;
	}

	@Override
	public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas) {
		if (!e.isConsumed()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				setEditable(false);
				updateImageToRender();
				changed();
				break;
			case KeyEvent.VK_ESCAPE:
				width = img.getWidth();
				height = img.getHeight();
				setEditable(false);
				updateImageToRender();
				changed();
				break;
			}
		}

	}

	@Override
	public void painterChanged(PainterEvent event) {
		System.out.println("changed");
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public boolean isEditable() {
		return isEditable;
	}

	@Override
	public boolean isPopupWanted(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		return true;
	}

	@Override
	public JMenu createMenu(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		return new MenuImagePainter(this, e, imagePoint, canvas);
	}

}
