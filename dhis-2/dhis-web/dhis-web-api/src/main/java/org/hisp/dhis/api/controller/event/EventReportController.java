package org.hisp.dhis.api.controller.event;

/*
 * Copyright (c) 2004-2014, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static org.hisp.dhis.common.DimensionalObjectUtils.getUniqueDimensions;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hisp.dhis.api.controller.AbstractCrudController;
import org.hisp.dhis.api.utils.ContextUtils;
import org.hisp.dhis.common.DimensionService;
import org.hisp.dhis.dxf2.utils.JacksonUtils;
import org.hisp.dhis.eventreport.EventReport;
import org.hisp.dhis.eventreport.EventReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Lars Helge Overland
 */
@Controller
@RequestMapping( value = EventReportController.RESOURCE_PATH )
public class EventReportController
    extends AbstractCrudController<EventReport>
{
    public static final String RESOURCE_PATH = "/eventReports";

    @Autowired
    private EventReportService eventReportService;

    @Autowired
    private DimensionService dimensionService;
    
    //--------------------------------------------------------------------------
    // CRUD
    //--------------------------------------------------------------------------

    @Override
    @RequestMapping( method = RequestMethod.POST, consumes = "application/json" )
    public void postJsonObject( HttpServletResponse response, HttpServletRequest request, InputStream input ) throws Exception
    {
        EventReport report = JacksonUtils.fromJson( input, EventReport.class );
        
        mergeEventReport( report );
        
        eventReportService.saveEventReport( report );
        
        ContextUtils.createdResponse( response, "Event report created", RESOURCE_PATH + "/" + report.getUid() );
    }
    
    //--------------------------------------------------------------------------
    // Supportive methods
    //--------------------------------------------------------------------------

    private void mergeEventReport( EventReport report )
    {
        dimensionService.mergeAnalyticalObject( report );
        
        report.getColumnDimensions().clear();
        report.getRowDimensions().clear();
        report.getFilterDimensions().clear();
        
        report.getColumnDimensions().addAll( getUniqueDimensions( report.getColumns() ) );
        report.getRowDimensions().addAll( getUniqueDimensions( report.getRows() ) );
        report.getFilterDimensions().addAll( getUniqueDimensions( report.getFilters() ) );
    }
}
