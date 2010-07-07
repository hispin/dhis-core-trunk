package org.hisp.dhis.datamart.aggregation.dataelement;

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

import static org.hisp.dhis.dataelement.DataElement.AGGREGATION_OPERATOR_SUM;
import static org.hisp.dhis.dataelement.DataElement.VALUE_TYPE_INT;
import static org.hisp.dhis.system.util.DateUtils.getDaysInclusive;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.datamart.CrossTabDataValue;
import org.hisp.dhis.datamart.aggregation.cache.AggregationCache;
import org.hisp.dhis.datamart.crosstab.CrossTabService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitHierarchy;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;

/**
 * @author Lars Helge Overland
 * @version $Id: SumIntAggregator.java 6049 2008-10-28 09:36:17Z larshelg $
 */
public class SumIntAggregator
    implements DataElementAggregator
{
    private static final Log log = LogFactory.getLog( SumIntAggregator.class );
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CrossTabService crossTabService;

    public void setCrossTabService( CrossTabService crossTabService )
    {
        this.crossTabService = crossTabService;
    }

    protected AggregationCache aggregationCache;
        
    public void setAggregationCache( AggregationCache aggregationCache )
    {
        this.aggregationCache = aggregationCache;
    }

    // -------------------------------------------------------------------------
    // DataElementAggregator implementation
    // -------------------------------------------------------------------------

    public Map<DataElementOperand, Double> getAggregatedValues( final Map<DataElementOperand, Integer> operandIndexMap, 
        final Period period, final OrganisationUnit unit, int unitLevel, OrganisationUnitHierarchy hierarchy, String key )
    {
        if ( operandIndexMap == null || operandIndexMap.size() == 0 )
        {
            return new HashMap<DataElementOperand, Double>();
        }
        
        final Collection<CrossTabDataValue> crossTabValues = crossTabService.getCrossTabDataValues( operandIndexMap, 
            aggregationCache.getIntersectingPeriods( period.getStartDate(), period.getEndDate() ), hierarchy.getChildren( unit.getId() ), key );
        
        final Map<DataElementOperand, double[]> entries = getAggregate( crossTabValues, period.getStartDate(), 
            period.getEndDate(), period.getStartDate(), period.getEndDate(), unitLevel ); // <Operand, [total value, total relevant days]>
        
        final Map<DataElementOperand, Double> values = new HashMap<DataElementOperand, Double>( entries.size() ); // <Operand, total value>
        
        for ( final Entry<DataElementOperand, double[]> entry : entries.entrySet() )
        {
            if ( entry.getValue() != null && entry.getValue()[ 1 ] > 0 )
            {
                values.put( entry.getKey(), entry.getValue()[ 0 ] );
            }
        }
        
        return values;
    }
    
    public Map<DataElementOperand, double[]> getAggregate( final Collection<CrossTabDataValue> crossTabValues, 
        final Date startDate, final Date endDate, final Date aggregationStartDate, final Date aggregationEndDate, int unitLevel )
    {
        final Map<DataElementOperand, double[]> totalSums = new HashMap<DataElementOperand, double[]>(); // <Operand, [total value, total relevant days]>

        Period period = null;
        Date currentStartDate = null;
        Date currentEndDate = null;
        
        double value = 0.0;
        double relevantDays = 0.0;
        double factor = 0.0;
        double existingValue = 0.0;
        double existingRelevantDays = 0.0;
        double duration = 0.0;
        
        int dataValueLevel = 0;
        
        for ( final CrossTabDataValue crossTabValue : crossTabValues )
        {
            period = aggregationCache.getPeriod( crossTabValue.getPeriodId() );
            
            currentStartDate = period.getStartDate();
            currentEndDate = period.getEndDate();
            
            duration = getDaysInclusive( currentStartDate, currentEndDate );
            
            dataValueLevel = aggregationCache.getLevelOfOrganisationUnit( crossTabValue.getSourceId() );
            
            if ( duration > 0 )
            {
                for ( final Entry<DataElementOperand, String> entry : crossTabValue.getValueMap().entrySet() ) // <Operand, value>
                {
                    if ( entry.getValue() != null && entry.getKey().aggregationLevelIsValid( unitLevel, dataValueLevel ) )
                    {
                        value = 0.0;
                        relevantDays = 0.0;
                        factor = 0.0;                        
                        
                        try
                        {
                            value = Double.parseDouble( entry.getValue() );
                        }
                        catch ( NumberFormatException ex )
                        {
                            log.warn( "Value skipped, not numeric: '" + entry.getValue() + 
                                "', for data element with id: '" + entry.getKey() +
                                "', for period with id: '" + crossTabValue.getPeriodId() +
                                "', for source with id: '" + crossTabValue.getSourceId() + "'" );
                        }
                        
                        if ( currentStartDate.compareTo( startDate ) >= 0 && currentEndDate.compareTo( endDate ) <= 0 ) // Value is within period
                        {
                            relevantDays = getDaysInclusive( startDate, endDate );
                            factor = 1;
                        }
                        else if ( currentStartDate.compareTo( startDate ) <= 0 && currentEndDate.compareTo( endDate ) >= 0 ) // Value spans whole period
                        {
                            relevantDays = getDaysInclusive( startDate, endDate );
                            factor = relevantDays / duration;
                        }
                        else if ( currentStartDate.compareTo( startDate ) <= 0 && currentEndDate.compareTo( startDate ) >= 0
                            && currentEndDate.compareTo( endDate ) <= 0 ) // Value spans period start
                        {
                            relevantDays = getDaysInclusive( startDate, currentEndDate );
                            factor = relevantDays / duration;
                        }
                        else if ( currentStartDate.compareTo( startDate ) >= 0 && currentStartDate.compareTo( endDate ) <= 0
                            && currentEndDate.compareTo( endDate ) >= 0 ) // Value spans period end
                        {
                            relevantDays = getDaysInclusive( currentStartDate, endDate );
                            factor = relevantDays / duration;
                        }
                        
                        value = value * factor;
                        
                        existingValue = totalSums.containsKey( entry.getKey() ) ? totalSums.get( entry.getKey() )[ 0 ] : 0;
                        existingRelevantDays = totalSums.containsKey( entry.getKey() ) ? totalSums.get( entry.getKey() )[ 1 ] : 0;
                        
                        final double[] values = { ( value + existingValue ), ( relevantDays + existingRelevantDays ) };
                        
                        totalSums.put( entry.getKey(), values );
                    }
                }
            }
        }
        
        return totalSums;
    }

    public Map<DataElementOperand, Integer> getOperandIndexMap( Collection<DataElementOperand> operands, PeriodType periodType, Map<DataElementOperand, Integer> operandIndexMap )
    {
        Map<DataElementOperand, Integer> sumOperandIndexMap = new HashMap<DataElementOperand, Integer>();
        
        for ( final DataElementOperand operand : operands )
        {
            if ( operand.getValueType().equals( VALUE_TYPE_INT ) && operand.getAggregationOperator().equals( AGGREGATION_OPERATOR_SUM ) )
            {
                sumOperandIndexMap.put( operand, operandIndexMap.get( operand ) );
            }
        }
        
        return sumOperandIndexMap;
    }
}
