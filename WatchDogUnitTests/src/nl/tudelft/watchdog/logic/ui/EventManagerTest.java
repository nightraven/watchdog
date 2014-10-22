package nl.tudelft.watchdog.logic.ui;

import java.util.Date;

import nl.tudelft.watchdog.logic.interval.IntervalManager;
import nl.tudelft.watchdog.logic.interval.IntervalPersister;
import nl.tudelft.watchdog.logic.interval.intervaltypes.IntervalBase;
import nl.tudelft.watchdog.logic.interval.intervaltypes.IntervalType;
import nl.tudelft.watchdog.logic.interval.intervaltypes.ReadingInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.TypingInterval;
import nl.tudelft.watchdog.logic.ui.events.WatchDogEvent;
import nl.tudelft.watchdog.logic.ui.events.WatchDogEvent.EventType;

import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests the {@link EventManager}. Because this creates the intervals that are
 * eventually transfered to the server, this is one of the most crucial parts of
 * WatchDog. Tests could flicker because they deal with timers (and Java gives
 * no guarantee as to when these timers will be executed).
 */
public class EventManagerTest {

	private static final int USER_ACTIVITY_TIMEOUT = 300;
	private static final int TIMEOUT_GRACE_PERIOD = (int) (USER_ACTIVITY_TIMEOUT * 1.1);
	private EventManager eventManager;
	private IntervalManager intervalManager;
	ITextEditor mockedTextEditor;

	@Before
	public void setup() {
		IntervalManager intervalManagerReal = new IntervalManager(
				Mockito.mock(IntervalPersister.class),
				Mockito.mock(IntervalPersister.class));
		intervalManager = Mockito.spy(intervalManagerReal);
		mockedTextEditor = Mockito.mock(ITextEditor.class);
		eventManager = new EventManager(intervalManager, USER_ACTIVITY_TIMEOUT);
	}

	@Test
	public void testCreateReadInterval() {
		eventManager.update(createMockEvent(EventType.ACTIVE_FOCUS));
		Mockito.verify(intervalManager).addInterval(
				Mockito.isA(ReadingInterval.class),  Mockito.any(Date.class));
	}

	@Test
	public void testCreateReadIntervalOnlyOnce() {
		eventManager.update(createMockEvent(EventType.ACTIVE_FOCUS));
		Mockito.verify(intervalManager).addInterval(
				Mockito.isA(ReadingInterval.class),  Mockito.any(Date.class));
		eventManager.update(createMockEvent(EventType.CARET_MOVED));
		eventManager.update(createMockEvent(EventType.CARET_MOVED));
		eventManager.update(createMockEvent(EventType.PAINT));
		Mockito.verify(intervalManager).addInterval(
				Mockito.isA(ReadingInterval.class),  Mockito.any(Date.class));
	}

	@Test
	public void testReadIntervalIsClosed() {
		eventManager.update(createMockEvent(EventType.ACTIVE_FOCUS));
		Mockito.verify(intervalManager).addInterval(
				Mockito.isA(ReadingInterval.class),  Mockito.any(Date.class));
		eventManager.update(createMockEvent(EventType.INACTIVE_FOCUS));
		Mockito.verify(intervalManager, Mockito.atLeastOnce()).closeInterval(
				Mockito.isA(ReadingInterval.class));
		Assert.assertEquals(null, intervalManager.getEditorInterval());
	}

	@Test
	public void testCreateWriteInterval() {
		eventManager.update(createMockEvent(EventType.EDIT));
		Mockito.verify(intervalManager).addInterval(
				Mockito.isA(TypingInterval.class),  Mockito.any(Date.class));
	}

	@Test
	public void testCreateWriteIntervalAndNotAReadInterval() {
		eventManager.update(createMockEvent(EventType.START_EDIT));
		eventManager.update(createMockEvent(EventType.EDIT));
		Mockito.verify(intervalManager, Mockito.atLeast(1)).addInterval(
				Mockito.isA(TypingInterval.class),  Mockito.any(Date.class));
		Mockito.verify(intervalManager, Mockito.never()).addInterval(
				Mockito.isA(ReadingInterval.class),  Mockito.any(Date.class));
		eventManager.update(createMockEvent(EventType.CARET_MOVED));
		eventManager.update(createMockEvent(EventType.EDIT));
		eventManager.update(createMockEvent(EventType.PAINT));
		Mockito.verify(intervalManager, Mockito.atLeast(1)).addInterval(
				Mockito.isA(TypingInterval.class), Mockito.any(Date.class));
	}

	@Test
	public void testWritingIntervalsGetClosedOnHigherCancel() {
		eventManager.update(createMockEvent(EventType.EDIT));
		eventManager.update(createMockEvent(EventType.END_ECLIPSE));
		Mockito.verify(intervalManager, Mockito.atLeastOnce()).closeInterval(
				Mockito.isA(TypingInterval.class));
	}

	@Test
	public void testTimeoutWorksForRegularIntervals() {
		eventManager.update(createMockEvent(EventType.ACTIVE_WINDOW));
		eventManager.update(createMockEvent(EventType.USER_ACTIVITY));
		Mockito.verify(intervalManager, Mockito.timeout(TIMEOUT_GRACE_PERIOD))
				.closeInterval(Mockito.isA(IntervalBase.class));
		Assert.assertEquals(null, intervalManager.getEditorInterval());
	}

	@Test
	public void testTimeoutWorksForReadingIntervals() {
		eventManager.update(createMockEvent(EventType.ACTIVE_FOCUS));
		Mockito.verify(intervalManager,
				Mockito.timeout(TIMEOUT_GRACE_PERIOD).atLeast(1))
				.closeInterval(Mockito.isA(ReadingInterval.class));
		Assert.assertEquals(null, intervalManager.getEditorInterval());
	}

	@Test
	public void testTimeoutWorksForWritingIntervals() {
		eventManager.update(createMockEvent(EventType.EDIT));
		// first close null interval
		Mockito.verify(intervalManager,
				Mockito.timeout(TIMEOUT_GRACE_PERIOD).atLeast(1))
				.closeInterval(null);

		Mockito.verify(intervalManager,
				Mockito.timeout(TIMEOUT_GRACE_PERIOD).atLeast(1))
				.closeInterval(Mockito.isA(TypingInterval.class));
	}

	@Test
	public void testReadingTimeoutIsProlonged() {
		eventManager.update(createMockEvent(EventType.ACTIVE_FOCUS));
		Mockito.verify(intervalManager,
				Mockito.timeout(USER_ACTIVITY_TIMEOUT / 2).never())
				.closeInterval(Mockito.any(IntervalBase.class));
		eventManager.update(createMockEvent(EventType.CARET_MOVED));
		Mockito.verify(intervalManager,
				Mockito.timeout(TIMEOUT_GRACE_PERIOD).never()).closeInterval(
				Mockito.any(IntervalBase.class));
		Mockito.verify(intervalManager,
				Mockito.timeout(TIMEOUT_GRACE_PERIOD * 2).atLeast(1))
				.closeInterval(Mockito.isA(ReadingInterval.class));
		Assert.assertEquals(null, intervalManager.getEditorInterval());
	}

	@Test
	public void testUserInactiveShouldNotCloseReading() {
		// FIXME: This test flickers!
		eventManager.update(createMockEvent(EventType.USER_ACTIVITY));
		eventManager.update(createMockEvent(EventType.ACTIVE_FOCUS));
		Mockito.verify(intervalManager,
				Mockito.timeout(USER_ACTIVITY_TIMEOUT / 2).never())
				.closeInterval(Mockito.any(IntervalBase.class));
		eventManager.update(createMockEvent(EventType.CARET_MOVED));
		Mockito.verify(intervalManager,
				Mockito.timeout(TIMEOUT_GRACE_PERIOD).never()).closeInterval(
				Mockito.any(IntervalBase.class));
		eventManager.update(createMockEvent(EventType.CARET_MOVED));
		Mockito.verify(intervalManager,
				Mockito.timeout((int) (TIMEOUT_GRACE_PERIOD * 2.4)).never())
				.closeInterval(Mockito.any(IntervalBase.class));
		Mockito.verify(intervalManager,
				Mockito.timeout((int) (TIMEOUT_GRACE_PERIOD * 3.5)))
				.closeInterval(Mockito.isA(ReadingInterval.class));
		Assert.assertEquals(null, intervalManager.getEditorInterval());
	}

	/**
	 * This test verifies that one {@link IntervalBase} intervals is created
	 * when a reading interval is created. This should be of type
	 * {@link IntervalType#USER_ACTIVE}, but this is not possible to test for
	 * due to limitations in Mockito.
	 */
	@Test
	public void verifiesAtLeastOneIntervalIsCreated() {
		eventManager.update(createMockEvent(EventType.EDIT));
		Mockito.verify(intervalManager, Mockito.atLeast(1)).addInterval(
				Mockito.isA(IntervalBase.class), Mockito.any(Date.class));
		Assert.assertNotNull(intervalManager
				.getIntervalOfType(IntervalType.USER_ACTIVE));

	}

	private WatchDogEvent createMockEvent(EventType eventType) {
		return new WatchDogEvent(mockedTextEditor, eventType);
	}
}
