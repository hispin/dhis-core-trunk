package org.hisp.dhis.dxf2.events.report;

import org.hisp.dhis.dxf2.events.event.EventSearchParams;
import org.hisp.dhis.dxf2.events.event.EventService;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstanceService;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractTrackedEntityInstanceRowService
    implements TrackedEntityInstanceRowService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private EventService eventService;

    @Autowired
    private TrackedEntityInstanceService trackedEntityInstanceService;

    @Override
    public EventRows getEventRows( EventSearchParams params )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
