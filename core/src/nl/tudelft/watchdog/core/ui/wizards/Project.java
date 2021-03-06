package nl.tudelft.watchdog.core.ui.wizards;

import java.util.Date;

import nl.tudelft.watchdog.core.logic.storage.WatchDogItem;

/**
 * The concept of a project comprises all information entered about the project
 * by the user.
 */
public class Project extends WatchDogItem {

	public static final String PROJECT = "project";
	public static final String PROJECT_REGISTRATION_TITLE = "Project registration";
	public static final String PROJECT_NAME_TEXTFIELD_TOOLTIP = "The name of the project(s) you work on in this workspace.";
	public static final String PROJECT_NAME_LABEL = "Project name: ";
	public static final String PROJECT_WEBSITE_TEXTFIELD_TOOLTIP = "If you have a website, we'd love to see it here.";
	public static final String PROJECT_WEBSITE_LABEL = "Project website: ";
	public static final String CI_USAGE_LABEL_TEXT = "Does your project use Continuous Integration (Travis, Jenkins, ...)?";
	public static final String CODE_STYLE_USAGE_LABEL_TEXT = "  ... enforce a uniform code style (e.g. whitespace)?";
	public static final String BUG_FINDING_USAGE_LABEL_TEXT = "  ... find functional bugs (e.g. NullPointerException)? ";
	public static final String OTHER_AUTOMATION_USAGE_LABEL_TEXT = "  ... perform other automated tasks (e.g. enforcing license headers)? ";
	public static final String PROJECT_CREATION_MESSAGE_SUCCESSFUL = "Your WatchDog Project has successfully been created.";
	public static final String PROJECT_CREATION_MESSAGE_FAILURE = "Problem creating a new WatchDog project.";
	public static final String TOOL_USAGE_LABEL_TEXT = "Which static analysis tools do you use in this project?";
	public static final String TOOL_USAGE_TEXTFIELD_TOOLTIP = "Please provide the names of the tools, for example CheckStyle or PMD.";
	public static final String BEFORE_PROJECT_REGISTRATION = "Now we have to create a new WatchDog project for this workspace.";
	public static final String PROJECT_ID_LABEL = "The WatchDog project ID: ";
	public static final String PROJECT_ID_TOOLTIP = "The WatchDog project ID associated with this workspace.";

	public static final String PROJECT_DATA_REQUEST = "Please fill in the following data to create a WatchDog project for you.";
	public static final String WATCHDOG_PROJECT_PROFILE = "WatchDog project profile";
	public static final String DO_YOU_USE_STATIC_ANALYSIS = "Does your project use static analysis tools to...";
	public static final String CREATE_PROJECT_BUTTON_TEXT = "Create new WatchDog project";
	public static final String YOUR_PROJECT_ID_LABEL = "Your Project ID is: ";

	public static final String SLIDER_QUESTION = "Estimate how you divide your time into the two activities testing and production. Just have a wild guess!";
	public static final String SLIDER_TOOLTIP_PRODUCTION = "To the production activity, every activity that has to do with regular, non-test production code counts.";
	public static final String SLIDER_TOOLTIP_TESTING = "To the testing activity, everything you do with Junit tests counts. Examples: writing, modifying, debugging, and executing Junit tests";
	public static final String SLIDER_TESTING_DEFINITION = "Testing is every activity related to testing (reading, writing, modifying, refactoring and executing JUnit tests).";
	public static final String SLIDER_PRODUCTION_DEFINITION = "Production is every activity related to regular code (reading, writing, modifying, and refactoring Java classes).";
	public static final String SLIDER_WARNING = "To proceed, you have to enter how you divide your time between production and test time, by at least touching the slider.";

	/** Constructor. */
	public Project(String userId) {
		localRegistrationDate = new Date();
		this.userId = userId;
	}

	/** eMail. */
	public String name;

	/**
	 * Does the registered WatchDog project belong to a single software project
	 * (<code>true</code> if it does)
	 */
	public boolean belongToASingleSoftware;

	/** Do you use ContinuousIntegration? */
	public YesNoDontKnowChoice usesContinuousIntegration;

	/** Do you use Junit? */
	public YesNoDontKnowChoice usesJunit;

	/** Do you use other frameworks than Junit? */
	public YesNoDontKnowChoice usesOtherTestingFrameworks;

	/** Do you use other testing strategies than Unit testing? */
	public YesNoDontKnowChoice usesOtherTestingForms;

	public YesNoDontKnowChoice usesCodeStyleSA;

	public YesNoDontKnowChoice usesBugFindingSA;

	public YesNoDontKnowChoice usesOtherAutomationSA;

	public String usesToolsSA;

	/** The percentage of how much production code is done (0% - 100%). */
	public int productionPercentage;

	/** The starting value of productionPercentage, a random value between 0 and 100. */
	public int productionPercentageStart;

	/** Is Junit used only for pure true-to-the-sense unit testing? */
	public YesNoDontKnowChoice useJunitOnlyForUnitTesting;

	/** Do you follow TDD? */
	public YesNoDontKnowChoice followTestDrivenDesign;

	/** The registration date. */
	public Date localRegistrationDate;

	/** The user who registers this project. */
	public String userId;

	/** The project's website. */
	public String website;

}
