package org.hisp.dhis.reporting.tablecreator.action;

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

import static org.hisp.dhis.system.util.ConversionUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dimension.DimensionService;
import org.hisp.dhis.dimension.DimensionSet;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.RelativePeriods;
import org.hisp.dhis.reporttable.ReportParams;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.reporttable.ReportTableService;

import com.opensymphony.xwork2.Action;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class SaveTableAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ReportTableService reportTableService;

    public void setReportTableService( ReportTableService reportTableService )
    {
        this.reportTableService = reportTableService;
    }
    
    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DimensionService dimensionService;
    
    public void setDimensionService( DimensionService dimensionService )
    {
        this.dimensionService = dimensionService;
    }

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }
    
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer tableId;

    public void setTableId( Integer id )
    {
        this.tableId = id;
    }
    
    private String tableName;

    public void setTableName( String tableName )
    {
        this.tableName = tableName;
    }
    
    private String mode;

    public void setMode( String mode )
    {
        this.mode = mode;
    }
    
    private boolean regression;

    public void setRegression( boolean regression )
    {
        this.regression = regression;
    }    
    
    private String dimensionSetId;

    public void setDimensionSetId( String dimensionSetId )
    {
        this.dimensionSetId = dimensionSetId;
    }

    private boolean doIndicators;

    public void setDoIndicators( boolean doIndicators )
    {
        this.doIndicators = doIndicators;
    }
    
    private boolean doPeriods;

    public void setDoPeriods( boolean doPeriods )
    {
        this.doPeriods = doPeriods;
    }

    private boolean doOrganisationUnits;

    public void setDoOrganisationUnits( boolean doUnits )
    {
        this.doOrganisationUnits = doUnits;
    }

    private List<String> selectedDataElements = new ArrayList<String>();

    public void setSelectedDataElements( List<String> selectedDataElements )
    {
        this.selectedDataElements = selectedDataElements;
    }
    
    private List<String> selectedIndicators = new ArrayList<String>();

    public void setSelectedIndicators( List<String> selectedIndicators )
    {
        this.selectedIndicators = selectedIndicators;
    }

    private List<String> selectedDataSets = new ArrayList<String>();

    public void setSelectedDataSets( List<String> selectedDataSets )
    {
        this.selectedDataSets = selectedDataSets;
    }
    
    private List<String> selectedPeriods = new ArrayList<String>();

    public void setSelectedPeriods( List<String> selectedPeriods )
    {
        this.selectedPeriods = selectedPeriods;
    }

    private List<String> selectedOrganisationUnits = new ArrayList<String>();

    public void setSelectedOrganisationUnits( List<String> selectedOrganisationUnits )
    {
        this.selectedOrganisationUnits = selectedOrganisationUnits;
    }

    private boolean reportingMonth;

    public void setReportingMonth( boolean reportingMonth )
    {
        this.reportingMonth = reportingMonth;
    }

    private boolean monthsThisYear;

    public void setMonthsThisYear( boolean monthsThisYear )
    {
        this.monthsThisYear = monthsThisYear;
    }

    private boolean quartersThisYear;

    public void setQuartersThisYear( boolean quartersThisYear )
    {
        this.quartersThisYear = quartersThisYear;
    }

    private boolean thisYear;

    public void setThisYear( boolean thisYear )
    {
        this.thisYear = thisYear;
    }

    private boolean monthsLastYear;

    public void setMonthsLastYear( boolean monthsLastYear )
    {
        this.monthsLastYear = monthsLastYear;
    }

    private boolean quartersLastYear;

    public void setQuartersLastYear( boolean quartersLastYear )
    {
        this.quartersLastYear = quartersLastYear;
    }

    private boolean lastYear;
    
    public void setLastYear( boolean lastYear )
    {
        this.lastYear = lastYear;
    }

    private boolean paramReportingMonth;

    public void setParamReportingMonth( boolean paramReportingMonth )
    {
        this.paramReportingMonth = paramReportingMonth;
    }

    private boolean paramParentOrganisationUnit;

    public void setParamParentOrganisationUnit( boolean paramParentOrganisationUnit )
    {
        this.paramParentOrganisationUnit = paramParentOrganisationUnit;
    }
    
    private boolean paramOrganisationUnit;

    public void setParamOrganisationUnit( boolean paramOrganisationUnit )
    {
        this.paramOrganisationUnit = paramOrganisationUnit;
    }
        
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        ReportTable reportTable = getReportTable();
        
        reportTableService.saveReportTable( reportTable );
        
        return SUCCESS;
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private ReportTable getReportTable()
        throws Exception
    {
        List<DataElement> dataElements = getList( dataElementService.getDataElements( getIntegerCollection( selectedDataElements ) ) );        
        List<Indicator> indicators = getList( indicatorService.getIndicators( getIntegerCollection( selectedIndicators ) ) );        
        List<DataSet> dataSets = getList( dataSetService.getDataSets( getIntegerCollection( selectedDataSets ) ) );        
        List<Period> periods = getList( periodService.getPeriodsByExternalIds( selectedPeriods ) );        
        List<OrganisationUnit> units = getList( organisationUnitService.getOrganisationUnits( getIntegerCollection( selectedOrganisationUnits ) ) );

        DimensionSet dimensionSet = dimensionService.getDimensionSet( dimensionSetId );
        
        RelativePeriods relatives = new RelativePeriods( reportingMonth, monthsThisYear, quartersThisYear, thisYear, monthsLastYear, quartersLastYear, lastYear );
        
        ReportParams reportParams = new ReportParams();
        
        reportParams.setParamReportingMonth( paramReportingMonth );
        reportParams.setParamParentOrganisationUnit( paramParentOrganisationUnit );
        reportParams.setParamOrganisationUnit( paramOrganisationUnit );
        
        ReportTable reportTable = null;
        
        if ( tableId == null )
        {
            reportTable = new ReportTable( tableName, mode, regression,
                dataElements, indicators, dataSets, periods, null, units, null,
                dimensionSet, doIndicators, doPeriods, doOrganisationUnits, relatives, reportParams, 
                null, null );
        }
        else
        {
            reportTable = reportTableService.getReportTable( tableId );
            
            reportTable.setName( tableName );
            reportTable.setRegression( regression );
            reportTable.setDataElements( dataElements );
            reportTable.setIndicators( indicators );
            reportTable.setDataSets( dataSets );
            reportTable.setPeriods( periods );
            reportTable.setUnits( units );
            reportTable.setDoIndicators( doIndicators );
            reportTable.setDoPeriods( doPeriods );
            reportTable.setDoUnits( doOrganisationUnits );
            reportTable.setRelatives( relatives );
            reportTable.setReportParams( reportParams );
            reportTable.setDimensionSet( dimensionSet );
        }
        
        return reportTable;
    }
}
