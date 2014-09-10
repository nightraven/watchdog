package nl.tudelft.watchdog.ui.handlers;

import nl.tudelft.watchdog.logic.IntervalInitializationManager;
import nl.tudelft.watchdog.logic.logging.WatchDogLogger;
import nl.tudelft.watchdog.ui.UIUtils;
import nl.tudelft.watchdog.ui.preferences.Preferences;
import nl.tudelft.watchdog.util.WatchDogGlobals;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

/**
 * Handler called when an Eclipse instance with the WatchDog plugin installed
 * gets opened and activated.
 */
public class StartUpHandler implements IStartup {

	/** {@inheritDoc} Starts the WatchDog plugin. */
	@Override
	public void earlyStartup() {
		StartupUIThread watchDogUiThread = new StartupUIThread(this,
				Preferences.getInstance());
		Display.getDefault().asyncExec(watchDogUiThread);
	}

	/** Starts WatchDog. */
	/* package */void startWatchDog() {
		try {
			WatchDogLogger.getInstance().logInfo("Starting WatchDog ...");

			// initializes the interval manager, and thereby, WatchDog interval
			// recording.
			IntervalInitializationManager.getInstance();
			WatchDogGlobals.isActive = true;
			// Update WatchDog icon
			UIUtils.refreshCommand("nl.tudelft.watchdog.commands.showWatchDogInfo");
		} catch (Exception exception) {
			WatchDogLogger.getInstance().logSevere(
					"Caught sever exception on top-level: ");
			WatchDogLogger.getInstance().logSevere(exception);
		}
	}
}
