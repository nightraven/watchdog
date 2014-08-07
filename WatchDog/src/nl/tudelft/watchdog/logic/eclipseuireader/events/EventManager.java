package nl.tudelft.watchdog.logic.eclipseuireader.events;

import nl.tudelft.watchdog.logic.interval.IntervalManager;
import nl.tudelft.watchdog.logic.interval.intervaltypes.ReadingInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.SessionInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.TypingInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.UserActivityIntervalBase;
import nl.tudelft.watchdog.util.WatchDogGlobals;

/**
 * Manager for {@link EditorEvent}s. Links such events to actions in the
 * IntervalManager, i.e. manages the creation and deletion of intervals based on
 * the incoming events. Is a state machine.
 */
public class EventManager {
	/** The {@link IntervalManager} this observer is working with. */
	private IntervalManager intervalManager;

	/** Constructor. */
	public EventManager(IntervalManager intervalManager) {
		this.intervalManager = intervalManager;
	}

	/** Introduces the supplied editorEvent */
	public void update(WatchDogEvent event) {
		switch (event.getType()) {
		case START_ECLIPSE:
			addNewSessionInterval();
			break;
		case END_ECLIPSE:
			intervalManager.closeAllCurrentIntervals();
			break;
		default:
			break;
		}

		System.out.println("Event " + event.getType());
		boolean previousIntervalHasSameEditor = false;

		UserActivityIntervalBase userActivityInterval = intervalManager
				.getUserActivityIntervalIfAny();

		if (event instanceof EditorEvent) {
			EditorEvent editorEvent = (EditorEvent) event;
			if (userActivityInterval != null) {
				if (userActivityInterval.getEditor().equals(
						editorEvent.getTextEditor())) {
					previousIntervalHasSameEditor = true;
				}
			}
		}
	}

	/** Starts and registers a new session interval. */
	public void addNewSessionInterval() {
		SessionInterval activeSessionInterval = new SessionInterval(
				intervalManager.getSessionSeed());
		intervalManager.addInterval(activeSessionInterval);
	}

	/** Creates a new active typing interval from the supplied event. */
	private void createNewActiveTypingInterval(EditorEvent event) {
		intervalManager.addAndSetNewActiveInterval(
				new TypingInterval(event.getPart(), intervalManager
						.getSessionSeed()), WatchDogGlobals.TYPING_TIMEOUT);
	}

	/** Creates a new active reading interval from the supplied event. */
	private void createNewActiveReadingInterval(EditorEvent event) {
		intervalManager.addAndSetNewActiveInterval(
				new ReadingInterval(event.getPart(), intervalManager
						.getSessionSeed()), WatchDogGlobals.READING_TIMEOUT);
	}
}