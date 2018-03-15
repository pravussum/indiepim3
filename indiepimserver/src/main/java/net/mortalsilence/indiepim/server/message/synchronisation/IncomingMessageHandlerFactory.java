package net.mortalsilence.indiepim.server.message.synchronisation;

import net.mortalsilence.indiepim.server.message.SyncUpdateMethod;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class IncomingMessageHandlerFactory {

    private final UpdateFlagsAndFoldersHandler updateFlagsAndFoldersHandler;
    private final PersistMessageHandler persistMessageHandler;

    @Inject
    public IncomingMessageHandlerFactory(UpdateFlagsAndFoldersHandler updateFlagsAndFoldersHandler,
                                         PersistMessageHandler persistMessageHandler) {
        this.updateFlagsAndFoldersHandler = updateFlagsAndFoldersHandler;
        this.persistMessageHandler = persistMessageHandler;
    }

    public IncomingMessageHandler getIncomingMessageHandler(SyncUpdateMethod updateMode) {
        final IncomingMessageHandler updateHandler;
        if(updateMode == SyncUpdateMethod.FLAGS)
            updateHandler = updateFlagsAndFoldersHandler;
        else if (updateMode == SyncUpdateMethod.FULL)
            updateHandler = persistMessageHandler;
        else if(updateMode == SyncUpdateMethod.NONE)
            updateHandler = null;
        else throw new IllegalArgumentException();
        return updateHandler;
    }
}
