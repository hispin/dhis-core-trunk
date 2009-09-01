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
package org.hisp.dhis.vn.reportitem.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataelement.comparator.DataElementGroupNameComparator;
import org.hisp.dhis.indicator.IndicatorGroup;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.indicator.comparator.IndicatorGroupNameComparator;
import org.hisp.dhis.vn.report.ReportExcelInterface;
import org.hisp.dhis.vn.report.ReportExcelService;
import org.hisp.dhis.vn.report.ReportItem;
import org.hisp.dhis.vn.report.comparator.ReportItemNameComparator;

import com.opensymphony.xwork2.Action;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */
public class ListReportItemAction
    implements Action
{
    // -------------------------------------------
    // Dependency
    // -------------------------------------------

    private ReportExcelService reportService;

    private DataElementService dataElementService;

    private IndicatorService indicatorService;

    // -------------------------------------------
    // Input & Output
    // -------------------------------------------

    private Integer reportId;

    private List<ReportItem> reportItems;

    private ReportExcelInterface reportExcel;

    private List<DataElementGroup> dataElementGroups;

    private List<IndicatorGroup> indicatorGroups;     
    
    private List<Integer> sheets;
    
    private Integer sheetNo;

    // -------------------------------------------
    // Getter & Setter
    // -------------------------------------------
    
    

    public List<ReportItem> getReportItems()
    {
        return reportItems;
    }

    public List<Integer> getSheets()
    {
        return sheets;
    }

    public Integer getSheetNo()
    {
        return sheetNo;
    }

    public void setSheetNo( Integer sheetNo )
    {
        this.sheetNo = sheetNo;
    }

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    public List<DataElementGroup> getDataElementGroups()
    {
        return dataElementGroups;
    }

    public List<IndicatorGroup> getIndicatorGroups()
    {
        return indicatorGroups;
    }

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    public ReportExcelInterface getReportExcel()
    {
        return reportExcel;
    }

    public void setReportId( Integer reportId )
    {
        this.reportId = reportId;
    }

    public void setReportService( ReportExcelService reportService )
    {
        this.reportService = reportService;
    }

    public String execute()
        throws Exception
    {
        reportExcel = reportService.getReport( reportId.intValue() );
        
        if ( sheetNo == null )
        {

            reportItems = new ArrayList<ReportItem>( reportExcel.getReportItems());

        }
        else
        {

            reportItems = new ArrayList<ReportItem>( reportService.getReportItem( sheetNo, reportId ) );

        }
        sheets = new ArrayList<Integer>(reportService.getSheets( reportId ));

        Collections.sort( reportItems, new ReportItemNameComparator() );

        dataElementGroups = new ArrayList<DataElementGroup>( dataElementService.getAllDataElementGroups() );

        Collections.sort( dataElementGroups, new DataElementGroupNameComparator() );
        
        indicatorGroups = new ArrayList<IndicatorGroup>(indicatorService.getAllIndicatorGroups());
        
        Collections.sort( indicatorGroups, new IndicatorGroupNameComparator() );

        return SUCCESS;
    }

}
