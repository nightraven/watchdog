package nl.tudelft.watchdog.logic.interval;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.watchdog.logic.interval.active.IntervalBase;
import nl.tudelft.watchdog.logic.logging.WatchDogLogger;
import nl.tudelft.watchdog.logic.network.JsonTransferer;

/**
 * This manager takes care of the repeated transferal of all closed intervals to
 * the server. Furthermore, it allows the immediate execution of this regularly
 * scheduled task, e.g. when it is needed on exiting.
 */
public class IntervalTransferManager {

	private static int UPDATE_RATE = 5 * 60 * 1000;

	private Timer timer;

	private IntervalsTransferTimerTask task;

	/** Constructor. */
	public IntervalTransferManager(final IntervalPersister intervalPersister) {
		task = new IntervalsTransferTimerTask(intervalPersister);
		timer = new Timer(true);
		timer.scheduleAtFixedRate(task, 0, UPDATE_RATE);
	}

	/**
	 * Immediately synchronizes the intervals with the server.
	 */
	public void sendIntervalsImmediately() {
		task.run();
	}

	private class IntervalsTransferTimerTask extends TimerTask {
		private final IntervalPersister intervalPersister;
		private long lastTransferedIntervalKey;

		private IntervalsTransferTimerTask(IntervalPersister intervalPersister) {
			this.intervalPersister = intervalPersister;
		}

		/**
		 * Transfers all intervals from the persistence storage that are not yet
		 * on the server, to the server.
		 */
		@Override
		public void run() {
			List<IntervalBase> intervalsToTransfer = intervalPersister
					.readIntevals(lastTransferedIntervalKey);

			if (intervalsToTransfer.isEmpty()) {
				return;
			}

			JsonTransferer intervalTransferer = new JsonTransferer();
			if (intervalTransferer.sendIntervals(intervalsToTransfer)) {
				lastTransferedIntervalKey = intervalPersister.getHighestKey();
			} else {
				WatchDogLogger.getInstance().logSevere(
						"Could not transfer intervals to server!");
			}
		}
	}
}