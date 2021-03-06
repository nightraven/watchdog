package nl.tudelft.watchdog.eclipse.logic.ui.listeners;

import nl.tudelft.watchdog.core.logic.ui.events.WatchDogEventType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A listener that determines whether there was general activity in the Eclipse
 * window.
 */
public class GeneralActivityListener {

	/** Constructor. */
	public GeneralActivityListener(Display display) {
		display.syncExec(() -> {
			display.addFilter(SWT.KeyDown | SWT.KeyUp | SWT.MouseDown | SWT.MouseUp
					| SWT.MouseMove, new Listener() {

				@Override
				public void handleEvent(Event event) {
					WatchDogEventType.USER_ACTIVITY.process(event);
				}
			});
		});
	}
}
