package nl.tudelft.watchdog.logic.ui;

import nl.tudelft.watchdog.logic.document.DocumentFactory;
import nl.tudelft.watchdog.logic.interval.IntervalInitializationManager;
import nl.tudelft.watchdog.logic.interval.IntervalManager;
import nl.tudelft.watchdog.logic.interval.intervaltypes.EditorIntervalBase;
import nl.tudelft.watchdog.logic.interval.intervaltypes.IntervalBase;
import nl.tudelft.watchdog.logic.interval.intervaltypes.IntervalType;
import nl.tudelft.watchdog.logic.interval.intervaltypes.JUnitInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.PerspectiveInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.PerspectiveInterval.Perspective;
import nl.tudelft.watchdog.logic.interval.intervaltypes.ReadingInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.TypingInterval;
import nl.tudelft.watchdog.logic.logging.WatchDogLogger;
import nl.tudelft.watchdog.logic.ui.WatchDogEvent.EventType;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Manager for {@link EditorEvent}s. Links such events to actions in the
 * IntervalManager, i.e. manages the creation and deletion of intervals based on
 * the incoming events. This class therefore contains the logic of when and how
 * new intervals are created, and how WatchDog reacts to incoming events
 * generated by its listeners.
 */
public class EventManager {

	/** The {@link IntervalInitializationManager} this observer is working with. */
	private IntervalManager intervalManager;

	private InactivityNotifier userInactivityNotifier;

	private InactivityNotifier editorInactivityNotifier;

	private InactivityNotifier writingInactivityNotifier;

	private DocumentFactory documentFactory;

	/** Constructor. */
	public EventManager(IntervalManager intervalManager,
			DocumentFactory documentFactory, int userActivityTimeout) {
		this.intervalManager = intervalManager;
		this.documentFactory = documentFactory;

		userInactivityNotifier = new InactivityNotifier(this,
				userActivityTimeout, EventType.USER_INACTIVITY);
		editorInactivityNotifier = new InactivityNotifier(this,
				userActivityTimeout, EventType.EDITOR_INACTIVITY);
		writingInactivityNotifier = new InactivityNotifier(this,
				userActivityTimeout, EventType.WRITING_INACTIVITY);
	}

	/** Introduces the supplied editorEvent */
	public void update(WatchDogEvent event) {

		IntervalBase interval;
		switch (event.getType()) {
		case START_ECLIPSE:
			intervalManager.addInterval(new IntervalBase(
					IntervalType.ECLIPSE_OPEN));
			userInactivityNotifier.trigger();
			break;

		case END_ECLIPSE:
			userInactivityNotifier.cancelTimer();
			editorInactivityNotifier.cancelTimer();
			writingInactivityNotifier.cancelTimer();
			intervalManager.closeAllIntervals();
			break;

		case ACTIVE_WINDOW:
			intervalManager.addInterval(new IntervalBase(
					IntervalType.ECLIPSE_ACTIVE));
			userInactivityNotifier.trigger();
			break;

		case END_WINDOW:
			interval = intervalManager
					.getIntervalOfType(IntervalType.ECLIPSE_ACTIVE);
			intervalManager.closeInterval(interval);
			userInactivityNotifier.cancelTimer();
			editorInactivityNotifier.cancelTimer();
			break;

		case START_JAVA_PERSPECTIVE:
			createNewPerspectiveInterval(Perspective.JAVA);
			userInactivityNotifier.trigger();
			break;

		case START_DEBUG_PERSPECTIVE:
			createNewPerspectiveInterval(Perspective.DEBUG);
			userInactivityNotifier.trigger();
			break;

		case START_UNKNOWN_PERSPECTIVE:
			createNewPerspectiveInterval(Perspective.OTHER);
			userInactivityNotifier.trigger();
			break;

		case JUNIT:
			JUnitInterval junitInterval = (JUnitInterval) event.getSource();
			intervalManager.addInterval(junitInterval);
			break;

		case USER_ACTIVITY:
			userInactivityNotifier.trigger();
			interval = intervalManager
					.getIntervalOfType(IntervalType.USER_ACTIVE);
			if (interval == null) {
				intervalManager.addInterval(new IntervalBase(
						IntervalType.USER_ACTIVE));
			}
			break;

		case USER_INACTIVITY:
			interval = intervalManager
					.getIntervalOfType(IntervalType.USER_ACTIVE);
			intervalManager.closeInterval(interval);
			break;

		case EDIT:
			ITextEditor editor = (ITextEditor) event.getSource();

			EditorIntervalBase editorInterval = intervalManager
					.getEditorInterval();
			if (editorInterval == null
					|| editorInterval.getType() != IntervalType.TYPING) {
				// create new typing interval
				editorInactivityNotifier.cancelTimer();
				intervalManager.closeInterval(editorInterval);
				intervalManager.addAndSetEditorInterval(new TypingInterval(
						editor));
			} else if (editorInterval.getType() == IntervalType.TYPING) {
				// refresh document content for calculation of edit distance
				TypingInterval typingInterval = (TypingInterval) editorInterval;
				try {
					typingInterval.setEndingDocument(documentFactory
							.createDocument(editor));
				} catch (IllegalArgumentException exception) {
					WatchDogLogger.getInstance().logSevere(exception);
				}
			}
			userInactivityNotifier.trigger();
			writingInactivityNotifier.trigger();
			break;

		case PAINT:
		case CARET_MOVED:
		case ACTIVE_FOCUS:
			editor = (ITextEditor) event.getSource();
			editorInterval = intervalManager.getEditorInterval();
			if (editorInterval == null) {
				intervalManager.addAndSetEditorInterval(new ReadingInterval(
						editor));
			}
			editorInactivityNotifier.trigger();
			userInactivityNotifier.trigger();
			break;

		case END_FOCUS:
			editorInactivityNotifier.cancelTimer();
			writingInactivityNotifier.cancelTimer();
			intervalManager.closeInterval(intervalManager.getEditorInterval());
			break;

		case EDITOR_INACTIVITY:
			intervalManager.closeInterval(intervalManager.getEditorInterval());
			break;

		case WRITING_INACTIVITY:
			editorInterval = intervalManager.getEditorInterval();
			if (editorInterval != null
					&& editorInterval.getType() == IntervalType.TYPING) {
				intervalManager.closeInterval(editorInterval);
			}
			break;

		default:
			break;
		}
	}

	/** Creates a new perspective Interval of the given type. */
	private void createNewPerspectiveInterval(
			PerspectiveInterval.Perspective perspecitveType) {
		PerspectiveInterval perspectiveInterval = (PerspectiveInterval) intervalManager
				.getIntervalOfType(IntervalType.PERSPECTIVE);
		if (perspectiveInterval != null
				&& perspectiveInterval.getPerspectiveType() == perspecitveType) {
			// abort if such an interval is already open.
			return;
		}
		intervalManager.closeInterval(perspectiveInterval);
		intervalManager.addInterval(new PerspectiveInterval(perspecitveType));
	}

}