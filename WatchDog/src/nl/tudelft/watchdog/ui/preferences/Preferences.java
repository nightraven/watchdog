package nl.tudelft.watchdog.ui.preferences;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.watchdog.Activator;
import nl.tudelft.watchdog.ui.UIUtils;
import nl.tudelft.watchdog.util.WatchDogGlobals;

import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Utilities for accessing WatchDog's Eclipse preferences.
 */
public class Preferences {

	/** The user's id on the WatchDog server. */
	public final static String USERID_KEY = "USERID";

	/** The URL of the WatchDog server. */
	public final static String SERVER_KEY = "SERVERURL";

	/** Flag denoting whether WatchDog plugin should do logging or not. */
	public final static String LOGGING_ENABLED_KEY = "ENABLE_LOGGING";

	/** Flag denoting whether WatchDog plugin should do authentication or not. */
	public final static String AUTHENTICATION_ENABLED_KEY = "ENABLE_AUTH";

	/** A serialized List of {@link WorkspacePreferenceSetting}s. */
	public final static String WORKSPACES_KEY = "WORKSPACE_SETTINGS";

	/** The type of a list of {@link WorkspacePreferenceSetting}s for Gson. */
	private final static Type TYPE_WORKSPACE_SETTINGS = new TypeToken<List<WorkspacePreferenceSetting>>() {
	}.getType();

	/** The Gson object. */
	private final static Gson gson = new Gson();

	/** The preference store. */
	private IPreferenceStore store;

	/** The map of registered workspaces. */
	private List<WorkspacePreferenceSetting> workspaceSettings = new ArrayList<WorkspacePreferenceSetting>();

	/** The WatchDog preference instance. */
	private static Preferences singletonInstance;

	/**
	 * Constructor internally implements a singleton, not visible to class
	 * users. The preferences are stored on a per eclipse installation basis.
	 */
	private Preferences() {
		store = Activator.getDefault().getPreferenceStore();
		store.setDefault(LOGGING_ENABLED_KEY, false);
		store.setDefault(AUTHENTICATION_ENABLED_KEY, true);
		store.setDefault(USERID_KEY, "");
		store.setDefault(SERVER_KEY, WatchDogGlobals.DEFAULT_SERVER_URI);
		store.setDefault(WORKSPACES_KEY, "");

		workspaceSettings = readSerializedWorkspaceSettings(WORKSPACES_KEY);
	}

	/**
	 * Reads and constructs a HashMap object from a serialized String preference
	 * key.
	 */
	private List<WorkspacePreferenceSetting> readSerializedWorkspaceSettings(
			String KEY) {
		String serializedWorksapceSettings = store.getString(KEY);
		if (UIUtils.isEmpty(serializedWorksapceSettings)) {
			return new ArrayList<WorkspacePreferenceSetting>();
		}

		return gson.fromJson(serializedWorksapceSettings,
				TYPE_WORKSPACE_SETTINGS);
	}

	/** Returns the singleton instance from WatchdogPreferences. */
	public static Preferences getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new Preferences();
		}

		return singletonInstance;
	}

	/**
	 * Returns whether logging is enabled (<code>true</code>) or not (
	 * <code>false</code>).
	 */
	public boolean isLoggingEnabled() {
		return store.getBoolean(LOGGING_ENABLED_KEY);
	}

	/**
	 * Returns whether authentication on the url is enabled (<code>true</code>)
	 * or not ( <code>false</code>).
	 */
	public boolean isAuthenticationEnabled() {
		return store.getBoolean(AUTHENTICATION_ENABLED_KEY);
	}

	/** @return The userid. */
	public String getUserid() {
		return store.getString(USERID_KEY);
	}

	/** Sets the userid for the store. */
	public void setUserid(String userid) {
		store.setValue(USERID_KEY, userid);
	}

	/** @return The serverURL. */
	public String getServerURI() {
		return store.getString(SERVER_KEY);
	}

	/**
	 * @return <code>true</code> if this workspace has already been registered
	 *         with WatchDog, <code>false</code> otherwise. Note: This does not
	 *         say whether WatchDog should be activated, which is returned by
	 *         {@link #shouldWatchDogBeActive(String)}.
	 */
	public boolean isWorkspaceRegistered(String workspace) {
		return (getWorkspaceSetting(workspace) == null) ? false : true;
	}

	/**
	 * @return The matching {@link WorkspacePreferenceSetting}, or
	 *         <code>null</code> in case there was no match.
	 */
	public WorkspacePreferenceSetting getWorkspaceSetting(String workspace) {
		for (WorkspacePreferenceSetting setting : workspaceSettings) {
			if (setting.workspace.equals(workspace)) {
				return setting;
			}
		}
		return null;
	}

	/**
	 * @return The matching {@link WorkspacePreferenceSetting}, or a completely
	 *         new one in case there was no match.
	 */
	private WorkspacePreferenceSetting getOrCreateWorkspaceSetting(
			String workspace) {
		WorkspacePreferenceSetting setting = getWorkspaceSetting(workspace);
		if (setting == null) {
			setting = new WorkspacePreferenceSetting();
			setting.workspace = workspace;
			workspaceSettings.add(setting);
		}
		return setting;
	}

	/**
	 * Registers the given workspace with WatchDog. If use is <code>true</code>,
	 * WatchDog will be used.
	 */
	public void registerWorkspaceUse(String workspace, boolean use) {
		WorkspacePreferenceSetting setting = getOrCreateWorkspaceSetting(workspace);
		setting.enableWatchdog = use;
		storeWorkspaceSettings();
	}

	/** Registers the given projectId with the given workspace. */
	public void registerWorkspaceProject(String workspace, String projectId) {
		WorkspacePreferenceSetting setting = getOrCreateWorkspaceSetting(workspace);
		setting.projectId = projectId;
		storeWorkspaceSettings();
	}

	/** Registers the given projectId with the given workspace. */
	public void registerLastTransferedInterval(String workspace,
			long intervalKey) {
		WorkspacePreferenceSetting setting = getOrCreateWorkspaceSetting(workspace);
		setting.lastTransferedInterval = intervalKey;
		// TODO (MMB) this does not get stored when called from preshutdown hook
		// (the whole save workspace settings concept seems a bit flawed)
		// TODO (MMB) investigate what happens when this is called to the list
		// workspaceSettings -- is it actually updated?
		storeWorkspaceSettings();
	}

	/** Updates the serialized workspace settings in the preference store. */
	private void storeWorkspaceSettings() {
		store.setValue(WORKSPACES_KEY,
				gson.toJson(workspaceSettings, TYPE_WORKSPACE_SETTINGS));
	}

	/** @return The {@link IPreferenceStore} for WatchDog. */
	public IPreferenceStore getStore() {
		return store;
	}

	/** @return a list of workspace settings. */
	public List<WorkspacePreferenceSetting> getWorkspaceSettings() {
		return workspaceSettings;
	}

}
