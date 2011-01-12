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

package org.hisp.dhis.caseentry.action.caseaggregation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.caseentry.state.PeriodGenericManager;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.comparator.OrganisationUnitNameComparator;
import org.hisp.dhis.organisationunit.comparator.OrganisationUnitShortNameComparator;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.CalendarPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.user.CurrentUserService;

import com.opensymphony.xwork2.Action;

public class CaseAggregationResultAction
    implements Action
{

    Log log = LogFactory.getLog( getClass() );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private CaseAggregationConditionService aggregationConditionService;

    public void setAggregationConditionService( CaseAggregationConditionService aggregationConditionService )
    {
        this.aggregationConditionService = aggregationConditionService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private PeriodGenericManager periodGenericManager;

    public void setPeriodGenericManager( PeriodGenericManager periodGenericManager )
    {
        this.periodGenericManager = periodGenericManager;
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
    // Input & Output Parameters
    // -------------------------------------------------------------------------

    private int sDateLB;

    public void setSDateLB( int dateLB )
    {
        sDateLB = dateLB;
    }

    private int eDateLB;

    public void setEDateLB( int dateLB )
    {
        eDateLB = dateLB;
    }

    private String facilityLB;

    public void setFacilityLB( String facilityLB )
    {
        this.facilityLB = facilityLB;
    }

    private Integer dataSetId;

    public void setDataSetId( Integer dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    private Map<DataValue, String> mapDataValues;

    public Map<DataValue, String> getMapDataValues()
    {
        return mapDataValues;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        mapDataValues = new HashMap<DataValue, String>();

        String storedBy = currentUserService.getCurrentUsername() + "_CAE";

        // ---------------------------------------------------------------------
        // Get selected orgunits
        // ---------------------------------------------------------------------

        OrganisationUnit selectedOrgunit = selectionTreeManager.getReloadedSelectedOrganisationUnit();

        if ( selectedOrgunit == null )
        {
            return SUCCESS;
        }

        List<OrganisationUnit> orgUnitList = new ArrayList<OrganisationUnit>();
        if ( facilityLB.equals( "children" ) )
        {
            orgUnitList = getChildOrgUnitTree( selectedOrgunit );
        }
        else if ( facilityLB.equals( "immChildren" ) )
        {
            orgUnitList.add( selectedOrgunit );
            List<OrganisationUnit> organisationUnits = new ArrayList<OrganisationUnit>( selectedOrgunit.getChildren() );
            Collections.sort( organisationUnits, new OrganisationUnitShortNameComparator() );
            orgUnitList.addAll( organisationUnits );
        }
        else
        {
            orgUnitList.add( selectedOrgunit );
        }

        // ---------------------------------------------------------------------
        // Get DataElement list of selected dataset
        // ---------------------------------------------------------------------

        DataSet selectedDataSet = dataSetService.getDataSet( dataSetId );

        List<DataElement> dataElementList = new ArrayList<DataElement>( selectedDataSet.getDataElements() );

        // ---------------------------------------------------------------------
        // Get selected periods list
        // ---------------------------------------------------------------------

        List<Period> periodList = new ArrayList<Period>();

        periodGenericManager.setSelectedPeriodIndex( PeriodGenericManager.SESSION_KEY_SELECTED_PERIOD_INDEX_START,
            sDateLB );
        Period startPeriod = periodGenericManager.getSelectedPeriod(
            PeriodGenericManager.SESSION_KEY_SELECTED_PERIOD_INDEX_START,
            PeriodGenericManager.SESSION_KEY_BASE_PERIOD_START );

        periodGenericManager.setSelectedPeriodIndex( PeriodGenericManager.SESSION_KEY_SELECTED_PERIOD_INDEX_END,
            eDateLB );
        Period endPeriod = periodGenericManager.getSelectedPeriod(
            PeriodGenericManager.SESSION_KEY_SELECTED_PERIOD_INDEX_END,
            PeriodGenericManager.SESSION_KEY_BASE_PERIOD_END );
     
        if ( sDateLB != -1 && eDateLB != -1 )
        {
            periodList = getPeriodList( (CalendarPeriodType)selectedDataSet.getPeriodType(), startPeriod, endPeriod );
        }

        // ---------------------------------------------------------------------
        // Aggregation
        // ---------------------------------------------------------------------

        for ( OrganisationUnit orgUnit : orgUnitList )
        {
            for ( DataElement dElement : dataElementList )
            {
                List<DataElementCategoryOptionCombo> deCOCList = new ArrayList<DataElementCategoryOptionCombo>(
                    dElement.getCategoryCombo().getOptionCombos() );

                for ( DataElementCategoryOptionCombo optionCombo : deCOCList )
                {
                    CaseAggregationCondition condition = aggregationConditionService.getCaseAggregationCondition(
                        dElement, optionCombo );

                    if ( condition == null )
                        break;

                    for ( Period period : periodList )
                    {
                        String message = i18n.getString( "in" ) + " " + format.formatPeriod( period );

                        double resultValue = aggregationConditionService.parseConditition( condition, orgUnit, period );

                        if ( resultValue != 0 )
                        {
                            DataValue dataValue = dataValueService
                                .getDataValue( orgUnit, dElement, period, optionCombo );

                            if ( dataValue == null )
                            {
                                dataValue = new DataValue( dElement, period, orgUnit, "" + resultValue, storedBy,
                                    new Date(), null, optionCombo );

                                dataValueService.addDataValue( dataValue );
                                mapDataValues.put( dataValue, i18n.getString( "added" ) + " " + message );
                            }
                            else
                            {
                                dataValue.setValue( "" + resultValue );
                                dataValue.setTimestamp( new Date() );
                                dataValue.setStoredBy( storedBy );

                                dataValueService.updateDataValue( dataValue );

                                mapDataValues.put( dataValue, i18n.getString( "updated" ) + " " + message );
                            }

                        }

                    }// PeriodList end

                }// OptionComboList end
            }// DataElementList end

        }// Orgunit for loop end

        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Support methods
    // -------------------------------------------------------------------------

    // Returns the OrgUnitTree for which Root is the orgUnit
    private List<OrganisationUnit> getChildOrgUnitTree( OrganisationUnit orgUnit )
    {
        List<OrganisationUnit> orgUnitTree = new ArrayList<OrganisationUnit>();
        orgUnitTree.add( orgUnit );

        List<OrganisationUnit> children = new ArrayList<OrganisationUnit>( orgUnit.getChildren() );
        Collections.sort( children, new OrganisationUnitNameComparator() );

        for ( OrganisationUnit child : children )
        {
            orgUnitTree.addAll( getChildOrgUnitTree( child ) );
        }
        return orgUnitTree;
    }

    private List<Period> getPeriodList( CalendarPeriodType periodType, Period startPeriod, Period endPeriod )
    {
        Period period = periodType.createPeriod( startPeriod.getStartDate());

        List<Period> periods = new ArrayList<Period>();
        
        periods.add( period );
        
        while ( period.getEndDate().before( endPeriod.getEndDate() ))
        {
            period = periodType.getNextPeriod( period ) ;
            periods.add( period );
        }

        period = periodType.createPeriod( endPeriod.getStartDate() ) ;
        periods.add( period );
        
        return periods;
    }

}
