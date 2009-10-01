/*
 * Copyright (c) 2004-2007, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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
package org.hisp.dhis.reportexcel.export.action;

import java.util.Map;

import org.hisp.dhis.period.Period;
import org.hisp.dhis.reportexcel.ReportExcel;

import com.opensymphony.xwork2.ActionContext;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */
public class SelectionManager
{
    private static final String SELECTED_YEAR = "SELECTED_YEAR";

    private static final String SELETED_PERIOD = "SELETED_PERIOD";

    private static final String SELETED_REPORT_EXCEL = "SELETED_REPORT_EXCEL";

    private static final String SELECTED_REPORT_EXCEL_ID = "SELECTED_REPORT_EXCEL_ID";

    private static final String REPORTEXCEL_OUTPUT = "REPORTEXCEL_OUTPUT";

    public Period getSelectedPeriod()
    {
        return (Period) getSession().get( SELETED_PERIOD );
    }

    @SuppressWarnings( "unchecked" )
    public void setSelectedPeriod( Period period )
    {
        getSession().put( SELETED_PERIOD, period );
    }

    public ReportExcel getSelectedReportExcel()
    {
        return (ReportExcel) getSession().get( SELETED_REPORT_EXCEL );
    }

    @SuppressWarnings( "unchecked" )
    public void setSelectedReportExcel( ReportExcel reportExcel )
    {
        getSession().put( SELETED_REPORT_EXCEL, reportExcel );
    }

    @SuppressWarnings( "unchecked" )
    public void setSelectedReportExcelId( Integer id )
    {
        getSession().put( SELECTED_REPORT_EXCEL_ID, id );
    }

    public Integer getSelectedReportExcelId()
    {
        return (Integer) getSession().get( SELECTED_REPORT_EXCEL_ID );
    }

    public int getSelectedYear()
    {
        return (Integer) getSession().get( SELECTED_YEAR );
    }

    @SuppressWarnings( "unchecked" )
    public void setSeletedYear( int year )
    {
        getSession().put( SELECTED_YEAR, year );
    }

    @SuppressWarnings( "unchecked" )
    private static final Map getSession()
    {
        return ActionContext.getContext().getSession();
    }

    @SuppressWarnings( "unchecked" )
    public void setReportExcelOutput( String path )
    {
        getSession().put( REPORTEXCEL_OUTPUT, path );
    }

    public String getReportExcelOutput()
    {
        return (String) getSession().get( REPORTEXCEL_OUTPUT );
    }

}
