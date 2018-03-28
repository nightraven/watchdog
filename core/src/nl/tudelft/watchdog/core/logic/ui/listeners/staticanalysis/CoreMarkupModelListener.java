package nl.tudelft.watchdog.core.logic.ui.listeners.staticanalysis;

import nl.tudelft.watchdog.core.logic.document.Document;
import nl.tudelft.watchdog.core.logic.event.TrackingEventManager;
import nl.tudelft.watchdog.core.logic.event.eventtypes.TrackingEventType;
import nl.tudelft.watchdog.core.logic.event.eventtypes.staticanalysis.StaticAnalysisWarningEvent;
import org.joda.time.DateTime;

import java.util.stream.Stream;

public class CoreMarkupModelListener {

    protected Document document;
    protected final TrackingEventManager trackingEventManager;

    public CoreMarkupModelListener(TrackingEventManager trackingEventManager) {
        this.trackingEventManager = trackingEventManager;
    }

    public CoreMarkupModelListener(Document document, TrackingEventManager trackingEventManager) {
        this.trackingEventManager = trackingEventManager;
        this.document = document;
    }

    protected void addCreatedWarnings(Stream<Warning<String>> createdWarnings) {
        this.addCreatedWarnings(createdWarnings, this.document);
    }

    protected void addCreatedWarnings(Stream<Warning<String>> createdWarnings, Document document) {
        // Prepare the document again to update the line numbers and other statistics
        document.prepareDocument();
        trackingEventManager.addEvents(createdWarnings
                .map(warning -> this.createEventFromWarning(TrackingEventType.SA_WARNING_CREATED, warning, document))
        );
    }

    protected void addRemovedWarnings(Stream<Warning<String>> types) {
        this.addRemovedWarnings(types, this.document);
    }

    protected void addRemovedWarnings(Stream<Warning<String>> removedWarnings, Document document) {
        // Prepare the document again to update the line numbers and other statistics
        document.prepareDocument();
        trackingEventManager.addEvents(removedWarnings.map(warning ->
                this.createEventFromWarning(TrackingEventType.SA_WARNING_REMOVED, warning, document))
        );
    }

    private StaticAnalysisWarningEvent createEventFromWarning(TrackingEventType trackingEventType, Warning<String> warning, Document document) {
        return new StaticAnalysisWarningEvent(
                warning.type,
                document,
                trackingEventType,
                DateTime.now().toDate(),
                warning.warningCreationTime,
                warning.secondsBetween,
                warning.lineNumber
        );
    }

}