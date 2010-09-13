package org.hisp.dhis.mobile.util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import org.hisp.dhis.mobile.db.ModelRecordStore;
import org.hisp.dhis.mobile.db.Storage;
import org.hisp.dhis.mobile.ui.DHISMIDlet;

public class ReinitConfirmListener
    extends AlertConfirmListener
{
    public void commandAction( Command c, Displayable d )
    {
        if ( c.getCommandType() == Command.OK )
        {
            Storage.clear( ModelRecordStore.DATAVALUE_DB );
            ((DHISMIDlet) this.midlet).switchDisplayable( null, nextScreen );
        }
        else if ( c.getCommandType() == Command.CANCEL )
        {
            ((DHISMIDlet) this.midlet).switchDisplayable( null, currentScrren );
        }
    }
}
