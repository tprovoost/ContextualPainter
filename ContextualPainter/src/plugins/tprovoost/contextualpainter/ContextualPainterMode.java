package plugins.tprovoost.contextualpainter;

import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.gui.main.MainAdapter;
import icy.gui.main.MainEvent;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.painter.Painter;
import icy.plugin.abstract_.PluginActionable;
import icy.plugin.interface_.PluginDaemon;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

public class ContextualPainterMode extends PluginActionable implements PluginDaemon {

	public static boolean instanced = false;
	MainAdapter mainadapter = new MainAdapter() {
		@Override
		public void sequenceOpened(final MainEvent event) {
			ThreadUtil.bgRun(new Runnable() {

				@Override
				public void run() {
					Sequence s = ((Sequence) event.getSource());
					ContextualPainter ap = new ContextualPainter();
					s.addPainter(ap);
					for (Viewer v : s.getViewers()) {
						ThreadUtil.sleep(2000);
						IcyCanvas canvas = v.getCanvas();
						if (canvas != null) {
							Layer l = canvas.getLayer(ap);
							if (l != null)
								l.setName("Contextual");
						}
					}
				}
			});
		}

	};

	@Override
	public void run() {
		if (instanced) {
			return;
		}
		instanced = true;
		for (Sequence s : Icy.getMainInterface().getSequences()) {
			ContextualPainter ap = new ContextualPainter();
			s.addPainter(ap);
			for (Viewer v : s.getViewers()) {
				v.getCanvas().getLayer(ap).setName("Contextual");
			}
		}
		Icy.getMainInterface().addListener(mainadapter);
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
}
