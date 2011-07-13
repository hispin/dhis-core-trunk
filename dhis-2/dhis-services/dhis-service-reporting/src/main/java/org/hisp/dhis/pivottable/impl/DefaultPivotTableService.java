package org.hisp.dhis.pivottable.impl;

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

import static org.hisp.dhis.system.util.DateUtils.getMediumDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hisp.dhis.aggregation.AggregatedDataValueService;
import org.hisp.dhis.common.AggregatedValue;
import org.hisp.dhis.common.AbstractIdentifiableObject;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.organisationunit.comparator.OrganisationUnitNameComparator;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.comparator.AscendingPeriodComparator;
import org.hisp.dhis.pivottable.PivotTable;
import org.hisp.dhis.pivottable.PivotTableService;
import org.hisp.dhis.system.util.ConversionUtils;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class DefaultPivotTableService
    implements PivotTableService
{
    private static final Comparator<AbstractIdentifiableObject> INDICATOR_COMPARATOR = new IdentifiableObjectNameComparator();
    private static final Comparator<OrganisationUnit> ORGUNIT_COMPARATOR = new OrganisationUnitNameComparator();
    private static final Comparator<Period> PERIOD_COMPARATOR = new AscendingPeriodComparator();
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private AggregatedDataValueService aggregatedDataValueService;
    
    public void setAggregatedDataValueService( AggregatedDataValueService aggregatedDataValueService )
    {
        this.aggregatedDataValueService = aggregatedDataValueService;
    }

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }
    
    private DataElementService dataElementService;
    
    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
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
    
    // -------------------------------------------------------------------------
    // PivotTableService implementation
    // -------------------------------------------------------------------------

    public PivotTable getPivotTable( int dataType, int groupId, String periodTypeName, String startDate, String endDate, int organisationUnitId )
    {        
        PeriodType periodType = PeriodType.getPeriodTypeByName( periodTypeName );
        
        List<Period> periods = new ArrayList<Period>( 
            periodService.getPeriodsBetweenDates( periodType, getMediumDate( startDate ), getMediumDate( endDate ) ) );
        
        List<OrganisationUnit> organisationUnits = new ArrayList<OrganisationUnit>( 
            organisationUnitService.getOrganisationUnit( organisationUnitId ).getChildrenThisIfEmpty() );
         
        List<? extends AbstractIdentifiableObject> indicators = null;
        Collection<? extends AggregatedValue> aggregatedValues = null;
        
        if ( dataType == DATA_TYPE_INDICATOR )
        {
            if ( groupId == -1 ) // All
            {
                indicators = new ArrayList<Indicator>( indicatorService.getAllIndicators() );
                
                aggregatedValues = aggregatedDataValueService.getAggregatedIndicatorValues(
                    ConversionUtils.getIdentifiers( Period.class, periods ), 
                    ConversionUtils.getIdentifiers( OrganisationUnit.class, organisationUnits ) );
            }
            else
            {
                indicators = new ArrayList<Indicator>( indicatorService.getIndicatorGroup( groupId ).getMembers() );
                
                aggregatedValues = aggregatedDataValueService.getAggregatedIndicatorValues(
                    ConversionUtils.getIdentifiers( Indicator.class, indicators ),
                    ConversionUtils.getIdentifiers( Period.class, periods ), 
                    ConversionUtils.getIdentifiers( OrganisationUnit.class, organisationUnits ) );            
            }
        }
        else if ( dataType == DATA_TYPE_DATA_ELEMENT )
        {
            if ( groupId == -1 ) // All
            {
                indicators = new ArrayList<DataElement>( dataElementService.getAggregateableDataElements() );
                
                aggregatedValues = aggregatedDataValueService.getAggregatedDataValues( 
                    ConversionUtils.getIdentifiers( Period.class, periods ), 
                    ConversionUtils.getIdentifiers( OrganisationUnit.class, organisationUnits ) );
            }
            else
            {
                indicators = new ArrayList<AbstractIdentifiableObject>( dataElementService.getDataElementGroup( groupId ).getMembers() );
                
                aggregatedValues = aggregatedDataValueService.getAggregatedDataValues(
                    ConversionUtils.getIdentifiers( Indicator.class, indicators ),
                    ConversionUtils.getIdentifiers( Period.class, periods ), 
                    ConversionUtils.getIdentifiers( OrganisationUnit.class, organisationUnits ) );  
            }
        }
        else
        {
            throw new IllegalArgumentException( "Illegal data type: " + dataType );
        }
        
        PivotTable pivotTable = new PivotTable();
        
        pivotTable.setIndicators( indicators );
        pivotTable.setPeriods( periods );
        pivotTable.setOrganisationUnits( organisationUnits );
        pivotTable.setIndicatorValues( aggregatedValues );
        
        Collections.sort( pivotTable.getIndicators(), INDICATOR_COMPARATOR );
        Collections.sort( pivotTable.getOrganisationUnits(), ORGUNIT_COMPARATOR );
        Collections.sort( pivotTable.getPeriods(), PERIOD_COMPARATOR );
        
        return pivotTable;
    }
}
