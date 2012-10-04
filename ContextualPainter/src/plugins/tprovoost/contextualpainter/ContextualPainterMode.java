package plugins.tprovoost.contextualpainter;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginDaemon;
import icy.sequence.Sequence;

public class ContextualPainterMode extends Plugin implements PluginDaemon {

	public static boolean instanced = false;
	MainAdapter mainadapter = new MainAdapter() {
		
		@Override
		public void viewerOpened(MainEvent event) {
			Viewer v = (Viewer)event.getSource();
			Sequence s = v.getSequence();
			for (Painter p : s.getPainters() ){ 
				if (p instanceof ContextualPainter)
					return;
			}
			ContextualPainter cp = new ContextualPainter();
			s.addPainter(cp);
			IcyCanvas canvas = v.getCanvas();
			if (canvas != null) {
				Layer l = canvas.getLayer(cp);
				if (l != null)
					l.setName("Context Menu");
			}
		};

	};

	@Override
	public void run() {
		l1: for (Sequence s : Icy.getMainInterface().getSequences()) {
			for (Painter p : s.getPainters() ){ 
				if (p instanceof ContextualPainter)
					continue l1;
			}
			ContextualPainter ap = new ContextualPainter();
			s.addPainter(ap);
			for (Viewer v : s.getViewers()) {
				v.getCanvas().getLayer(ap).setName("Context Menu");
			}
		}
	}

	@Override
	public void stop() {
		for (Sequence s : Icy.getMainInterface().getSequences()) {
			for (Painter p : s.getPainters()) {
				if (p instanceof ContextualPainter)
					s.removePainter(p);
			}
		}
		Icy.getMainInterface().removeListener(mainadapter);
		instanced = false;
	}

	@Override
	public void init() {
		if (instanced) {
			return;
		}
		instanced = true;
		Icy.getMainInterface().addListener(mainadapter);
	}
}
