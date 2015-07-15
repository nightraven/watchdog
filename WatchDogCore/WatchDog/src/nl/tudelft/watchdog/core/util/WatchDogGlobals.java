package nl.tudelft.watchdog.core.util;

import com.google.gson.annotations.SerializedName;

import nl.tudelft.watchdog.core.ui.preferences.PreferencesBase;

/**
 * Globals for the current WatchDog instance.
 */
public class WatchDogGlobals {

	/** A text used in the UI if WatchDog is running. */
	public final static String ACTIVE_WATCHDOG_TEXT = "WatchDog is active and recording ...";

	/** A text used in the UI if WatchDog is not running. */
	public final static String INACTIVE_WATCHDOG_TEXT = "WatchDog is inactive!";

	/** The default URI of the WatchDogServer. */
	public final static String DEFAULT_SERVER_URI = "http://192.168.56.101:3000/";//http://watchdog.testroots.org/";

	/** Flag determining whether WatchDog is active. */
	public static boolean isActive = false;

	/** Whether the last interval transmission succeeded or failed. */
	public static boolean lastTransactionFailed = false;

	/** The client's version, as set in pom.xml. */
	public final static String CLIENT_VERSION = "1.5.0";

	/** The host ide this plugin is running on. */
	public static IDE hostIDE;

    public static String logDirectory;

    /** Preferences for this instance of IDE */
    public static PreferencesBase preferences;

	/** Describes the different supported IDE plugin hosts. */
	public enum IDE {
		/** Eclipse-IDE */
		@SerializedName("ec")
		ECLIPSE,

		/** IntelliJ-IDE */
		@SerializedName("ij")
		INTELLIJ
	}
}