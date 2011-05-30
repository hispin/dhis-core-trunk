/*
 * Copyright (c) 2004-2010, University of Oslo
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

package org.hisp.dhis.reporting.dataset.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.Grid;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datasetreport.DataSetReportService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

import static org.hisp.dhis.dataset.DataSet.*;

/**
 * @author Chau Thu Tran
 * @author Lars Helge Overland
 */
public class GenerateDataSetReportAction
    implements Action
{
    private static final String PARAM_PAGE = "page";
    
    private static final Map<String, String> VIEW_MAP = new HashMap<String, String>() { {
        put( TYPE_CUSTOM, "/dhis-web-reporting/renderCustomDataSetReportForm.vm" );
        put( TYPE_SECTION, "/dhis-web-reporting/renderSectionDataSetReportForm.vm" );
        put( TYPE_DEFAULT, "/dhis-web-reporting/renderDefaultDataSetReportForm.vm" );
    } };
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }

    private DataSetReportService dataSetReportService;

    public void setDataSetReportService( DataSetReportService dataSetReportService )
    {
        this.dataSetReportService = dataSetReportService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer dataSetId;

    public void setDataSetId( Integer dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    private String periodId;

    public void setPeriodId( String periodId )
    {
        this.periodId = periodId;
    }

    private boolean selectedUnitOnly;

    public void setSelectedUnitOnly( boolean selectedUnitOnly )
    {
        this.selectedUnitOnly = selectedUnitOnly;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private OrganisationUnit selectedOrgunit;

    public OrganisationUnit getSelectedOrgunit()
    {
        return selectedOrgunit;
    }

    private DataSet selectedDataSet;

    public DataSet getSelectedDataSet()
    {
        return selectedDataSet;
    }

    private Period selectedPeriod;

    public Period getSelectedPeriod()
    {
        return selectedPeriod;
    }

    private String customDataEntryFormCode;

    public String getCustomDataEntryFormCode()
    {
        return customDataEntryFormCode;
    }

    private List<Grid> grids = new ArrayList<Grid>();

    public List<Grid> getGrids()
    {
        return grids;
    }

    private Grid grid;

    public Grid getGrid()
    {
        return grid;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        selectedOrgunit = selectionTreeManager.getSelectedOrganisationUnit();

        if ( dataSetId == null || periodId == null || selectedOrgunit == null )
        {
            return ERROR;
        }

        selectedDataSet = dataSetService.getDataSet( dataSetId );

        selectedPeriod = periodService.getPeriodByExternalId( periodId );

        String type = selectedDataSet.getDataSetType();
        
        if ( TYPE_CUSTOM.equals( type ) )
        {
            customDataEntryFormCode = dataSetReportService.getCustomDataSetReport( selectedDataSet, selectedOrgunit, selectedPeriod, selectedUnitOnly, format );
        }
        else if ( TYPE_SECTION.equals( type ) )
        {
            grids = dataSetReportService.getSectionDataSetReport( selectedDataSet, selectedPeriod, selectedOrgunit, selectedUnitOnly, format, i18n );            
        }
        else
        {
            grid = dataSetReportService.getDefaultDataSetReport( selectedDataSet, selectedPeriod, selectedOrgunit, selectedUnitOnly, format, i18n );
        }
        
        ActionContext.getContext().getActionInvocation().getStack().setValue( PARAM_PAGE, VIEW_MAP.get( type ) );
        
        return SUCCESS;
    }
}
