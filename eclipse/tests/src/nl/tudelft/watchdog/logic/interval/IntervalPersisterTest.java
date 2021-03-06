package nl.tudelft.watchdog.logic.interval;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.tudelft.watchdog.core.logic.interval.intervaltypes.IDEOpenInterval;
import nl.tudelft.watchdog.core.logic.interval.intervaltypes.IntervalBase;
import nl.tudelft.watchdog.core.logic.storage.WatchDogItem;

public class IntervalPersisterTest extends IntervalPersisterTestBase {

	@BeforeClass
	public static void setup_before_class() {
		databaseName = "BaseTest";
		setUpSuperClass();
	}

	@Test
	public void hundred_interactions() {
		testInteraction(100);
		assertEquals(100, persister.getSize());

		Iterator<WatchDogItem> readIntervals = persister.readItems()
				.iterator();
		ArrayList<WatchDogItem> firstInterval = new ArrayList<WatchDogItem>(
				Arrays.asList(readIntervals.next()));
		persister.removeItems(firstInterval);
		assertEquals(99, persister.getSize());

		persister.clearAndResetMap();
		assertEquals(0, persister.getSize());
	}

	private void testInteraction(int items) {
		List<IntervalBase> generatedIntervals = generateIntervalList(items);

		// Shuffle the generated intervals to test for
		// correct ordering of returned values
		Collections.shuffle(generatedIntervals);
		Collections.sort(generatedIntervals);

		for (IntervalBase intervalBase : generatedIntervals) {
			persister.save(intervalBase);
		}

		List<WatchDogItem> readIntervals = new ArrayList<WatchDogItem>(
				persister.readItems());
		assertEquals(readIntervals.size(), items);

		// Test order of returned results
		assertEquals(readIntervals, generatedIntervals);
	}

	private List<IntervalBase> generateIntervalList(int n) {

		List<IntervalBase> intervals = new ArrayList<IntervalBase>();
		for (int i = 0; i < n; i++) {
			intervals.add(createRandomInterval());
		}
		return intervals;
	}

	static IntervalBase createRandomInterval() {
		IntervalBase interval = new IDEOpenInterval(new Date());
		interval.setSessionSeed("444");
		interval.setStartTime(new Date(interval.getStart().getTime()
				+ (new Random()).nextInt(100000)));
		interval.setEndTime(new Date(interval.getStart().getTime()
				+ (new Random()).nextInt(100000)));
		return interval;
	}
}
