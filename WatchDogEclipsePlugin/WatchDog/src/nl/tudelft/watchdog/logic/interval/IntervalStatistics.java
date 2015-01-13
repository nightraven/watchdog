package nl.tudelft.watchdog.logic.interval;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.tudelft.watchdog.logic.document.DocumentType;
import nl.tudelft.watchdog.logic.interval.intervaltypes.EclipseOpenInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.EditorIntervalBase;
import nl.tudelft.watchdog.logic.interval.intervaltypes.IntervalBase;
import nl.tudelft.watchdog.logic.interval.intervaltypes.JUnitInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.PerspectiveInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.PerspectiveInterval.Perspective;
import nl.tudelft.watchdog.logic.interval.intervaltypes.ReadingInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.TypingInterval;
import nl.tudelft.watchdog.logic.interval.intervaltypes.UserActiveInterval;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/** Gathers and calculates statistics on interval length. */
@SuppressWarnings("javadoc")
public class IntervalStatistics extends IntervalManagerBase {
	private static final int FILTERED_INTERVALS_IN_MINUTES = 60;

	private final IntervalPersister intervalPersister;

	public Duration eclipseOpen;
	public Duration userActive;
	public Duration userReading;
	public Duration userTyping;
	public Duration userProduction;
	public Duration userTest;
	public Duration perspectiveDebug;
	public Duration perspectiveJava;
	public Duration perspectiveOther;
	public double averageTestDuration;

	public Date mostRecentDate;
	public Date oldestDate;

	public int junitRunsCount;

	/** Constructor. */
	public IntervalStatistics(IntervalManager intervalManager) {
		intervalPersister = intervalManager.getIntervalsStatisticsPersister();
		addIntervals(intervalManager);
		filterIntervals();
		calculateStatistics();
	}

	private void addIntervals(IntervalManager intervalManager) {
		for (IntervalBase interval : intervalPersister.readIntervals()) {
			interval.setClosed();
			intervals.add(interval);
		}
		intervals.addAll(intervalManager.getOpenIntervals());
	}

	/** Filters out and removes intervals which are older than one hour. */
	private void filterIntervals() {
		ArrayList<IntervalBase> filteredIntervals = new ArrayList<IntervalBase>();
		ArrayList<IntervalBase> intervalsToRemove = new ArrayList<IntervalBase>();

		if (intervals.size() == 0) {
			return;
		}

		mostRecentDate = intervals.get(intervals.size() - 1).getEnd();
		DateTime thresholdDate = new DateTime(mostRecentDate);
		thresholdDate = thresholdDate
				.minusMinutes(FILTERED_INTERVALS_IN_MINUTES);

		for (IntervalBase interval : intervals) {
			if (interval.getEnd().after(thresholdDate.toDate())) {
				IntervalBase clonedInterval = (IntervalBase) interval.clone();
				if (interval.getStart().before(thresholdDate.toDate())) {
					clonedInterval.setStartTime(thresholdDate.toDate());
				}
				if (!clonedInterval.isClosed()) {
					clonedInterval.setEndTime(mostRecentDate);
				}
				filteredIntervals.add(clonedInterval);
			} else {
				intervalsToRemove.add(interval);
			}
		}

		oldestDate = filteredIntervals.get(0).getStart();
		intervalPersister.removeIntervals(intervalsToRemove);
		intervals = filteredIntervals;
	}

	private void calculateStatistics() {
		eclipseOpen = aggregateDurations(getIntervals(EclipseOpenInterval.class));
		userActive = aggregateDurations(getIntervals(UserActiveInterval.class));
		userReading = aggregateDurations(getIntervals(ReadingInterval.class));
		userTyping = aggregateDurations(getIntervals(TypingInterval.class));
		userTest = aggregateDurations(getEditorIntervals(DocumentType.TEST))
				.plus(aggregateDurations(getEditorIntervals(DocumentType.TEST_FRAMEWORK)))
				.plus(aggregateDurations(getEditorIntervals(DocumentType.FILENAME_TEST)))
				.plus(aggregateDurations(getEditorIntervals(DocumentType.PATHNAMME_TEST)));
		userProduction = aggregateDurations(getEditorIntervals(DocumentType.PRODUCTION));
		performDataSanitation();

		perspectiveDebug = aggregateDurations(getPerspectiveIntervals(Perspective.DEBUG));
		perspectiveJava = aggregateDurations(getPerspectiveIntervals(Perspective.JAVA));
		perspectiveOther = aggregateDurations(getPerspectiveIntervals(Perspective.OTHER));
		averageTestDuration = getPreciseTime(aggregateDurations(getIntervals(JUnitInterval.class)))
				/ getIntervals(JUnitInterval.class).size();

		junitRunsCount = getIntervals(JUnitInterval.class).size();
	}

	/**
	 * @return An {@link ArrayList} of intervals of the specified document type.
	 */
	protected List<EditorIntervalBase> getEditorIntervals(DocumentType type) {
		List<EditorIntervalBase> collectedIntervals = new ArrayList<EditorIntervalBase>();
		for (EditorIntervalBase interval : getIntervals(EditorIntervalBase.class)) {
			if (interval.getDocument().getDocumentType() == type) {
				collectedIntervals.add(interval);
			}
		}

		return collectedIntervals;
	}

	/**
	 * @return An {@link ArrayList} of intervals of the specified Perspective
	 *         type.
	 */
	protected List<PerspectiveInterval> getPerspectiveIntervals(Perspective type) {
		List<PerspectiveInterval> collectedIntervals = new ArrayList<PerspectiveInterval>();
		for (PerspectiveInterval interval : getIntervals(PerspectiveInterval.class)) {
			if (interval.getPerspectiveType() == type) {
				collectedIntervals.add(interval);
			}
		}
		return collectedIntervals;
	}

	private void performDataSanitation() {
		Duration summarizedUserActivity = userReading.plus(userTyping);
		if (userActive.isShorterThan(summarizedUserActivity)) {
			userActive = summarizedUserActivity;
		}
		if (eclipseOpen.isShorterThan(userActive)) {
			eclipseOpen = userActive;
		}
	}

	private Duration aggregateDurations(List<? extends IntervalBase> intervals) {
		Duration aggregatedDuration = new Duration(0);
		for (IntervalBase interval : intervals) {
			aggregatedDuration = aggregatedDuration
					.plus(interval.getDuration());
		}
		return aggregatedDuration;
	}

	/** @return the number of intervals. */
	public int getNumberOfIntervals() {
		return intervals.size();
	}

	public double getPreciseTime(Duration duration) {
		return ((double) duration.getStandardSeconds() / 60);
	}
}
