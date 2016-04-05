package org.hisp.dhis.webapi.controller.datastatistics;

/*
 * Copyright (c) 2004-2016, University of Oslo
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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;

import org.hisp.dhis.datastatistics.DataStatisticsEvent;
import org.hisp.dhis.datastatistics.AggregatedStatistics;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.datastatistics.EventType;
import org.hisp.dhis.datastatistics.DataStatisticsService;
import org.hisp.dhis.datastatistics.EventInterval;

import java.util.List;
import java.util.Date;

/**
 * @author Yrjan A. F. Fraschetti
 * @author Julie Hill Roa
 */
@Controller
public class DataStatisticsController
{
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private DataStatisticsService dataStatisticsService;

    @RequestMapping( value = "/dataStatistics", method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.CREATED )
    public void saveEvent( @RequestParam EventType eventType )
    {
        Date timestamp = new Date();
        User user = currentUserService.getCurrentUser();

        DataStatisticsEvent event = new DataStatisticsEvent( eventType, timestamp, user.getUsername() );
        dataStatisticsService.addEvent( event );
    }

    @RequestMapping( value = "/dataStatistics", method = RequestMethod.GET )
    public @ResponseBody List<AggregatedStatistics> report( @RequestParam @DateTimeFormat( pattern = "yyyy-mm-dd" ) Date startDate,
        @RequestParam @DateTimeFormat( pattern = "yyyy-mm-dd" ) Date endDate, 
        @RequestParam EventInterval interval, HttpServletResponse response )
    {
        if ( startDate.after( endDate ) )
        {
            response.setStatus( HttpServletResponse.SC_CONFLICT );
            return null;
        }

        return dataStatisticsService.getReports( startDate, endDate, interval );
    }
}
