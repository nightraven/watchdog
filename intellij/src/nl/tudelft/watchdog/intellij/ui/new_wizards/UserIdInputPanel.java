package nl.tudelft.watchdog.intellij.ui.new_wizards;

import com.intellij.ui.DocumentAdapter;
import nl.tudelft.watchdog.core.logic.network.NetworkUtils;
import nl.tudelft.watchdog.core.logic.network.ServerCommunicationException;
import nl.tudelft.watchdog.core.logic.network.ServerReturnCodeException;
import nl.tudelft.watchdog.intellij.ui.preferences.Preferences;
import nl.tudelft.watchdog.intellij.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.function.Consumer;

import static nl.tudelft.watchdog.intellij.ui.new_wizards.UserRegistrationStep.ID_LENGTH;
import static nl.tudelft.watchdog.intellij.ui.new_wizards.WizardStep.DEFAULT_SPACING;

class UserIdInputPanel extends JPanel {

    private static final String USER_ID_INPUT_TOOLTIP = "The User-ID we sent you upon your first WatchDog registration";
    private static final String USER_ID_LABEL_TEXT = "Your WatchDog User-ID: ";
    private static final String VERIFICATION_BUTTON_TEXT = "Verify";
    private static final String VERIFICATION_SUCCESFUL_MESSAGE = "User ID verification succesful!";
    private static final String VERIFICATION_MESSAGE_FAILURE = "User ID verification failed.";
    private final JTextField textfield;
    private final JPanel statusContainer;

    UserIdInputPanel(Consumer<Boolean> callback) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 3));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel fieldContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        this.add(fieldContainer);

        this.textfield = WizardStep.createLinkedLabelTextField(USER_ID_LABEL_TEXT, USER_ID_INPUT_TOOLTIP, ID_LENGTH, fieldContainer);

        JButton verify = new JButton(VERIFICATION_BUTTON_TEXT);
        verify.setEnabled(false);
        verify.setContentAreaFilled(true);
        verify.setOpaque(true);
        fieldContainer.add(Box.createHorizontalStrut(DEFAULT_SPACING));
        fieldContainer.add(verify);

        textfield.setDocument(new UIUtils.JTextFieldLimit(ID_LENGTH));
        textfield.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                verify.setEnabled(e.getDocument().getLength() == ID_LENGTH);
                callback.accept(false);
            }
        });

        this.statusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        this.add(statusContainer);

        verify.addActionListener(actionEvent -> {
            this.statusContainer.removeAll();
            callback.accept(verifyUserIdRegistration());
        });
    }

    private boolean verifyUserIdRegistration() {
        try {
            NetworkUtils.getURLAndGetResponse(NetworkUtils.buildExistingUserURL(this.textfield.getText()));
        } catch (ServerCommunicationException exception) {
            this.statusContainer.add(new JLabel(VERIFICATION_MESSAGE_FAILURE));
            this.statusContainer.add(WizardStep.createErrorMessageLabel(exception));

            return false;
        }

        Preferences preferences = Preferences.getInstance();
        preferences.setUserId(this.textfield.getText());

        this.statusContainer.add(new JLabel(VERIFICATION_SUCCESFUL_MESSAGE));
        return true;
    }
}
