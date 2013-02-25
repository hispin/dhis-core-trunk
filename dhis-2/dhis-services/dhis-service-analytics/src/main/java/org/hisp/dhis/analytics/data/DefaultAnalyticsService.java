package org.hisp.dhis.analytics.data;

/*
 * Copyright (c) 2004-2012, University of Oslo
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

import static org.hisp.dhis.analytics.AnalyticsTableManager.ANALYTICS_TABLE_NAME;
import static org.hisp.dhis.analytics.AnalyticsTableManager.COMPLETENESS_TABLE_NAME;
import static org.hisp.dhis.analytics.AnalyticsTableManager.COMPLETENESS_TARGET_TABLE_NAME;
import static org.hisp.dhis.analytics.DataQueryParams.CATEGORYOPTIONCOMBO_DIM_ID;
import static org.hisp.dhis.analytics.DataQueryParams.DATAELEMENT_DIM_ID;
import static org.hisp.dhis.analytics.DataQueryParams.DATASET_DIM_ID;
import static org.hisp.dhis.analytics.DataQueryParams.DATA_X_DIM_ID;
import static org.hisp.dhis.analytics.DataQueryParams.DIMENSION_SEP;
import static org.hisp.dhis.analytics.DataQueryParams.FIXED_DIMS;
import static org.hisp.dhis.analytics.DataQueryParams.INDICATOR_DIM_ID;
import static org.hisp.dhis.analytics.DataQueryParams.ORGUNIT_DIM_ID;
import static org.hisp.dhis.analytics.DataQueryParams.PERIOD_DIM_ID;
import static org.hisp.dhis.analytics.DataQueryParams.getDimensionFromParam;
import static org.hisp.dhis.analytics.DataQueryParams.getDimensionOptionsFromParam;
import static org.hisp.dhis.common.IdentifiableObjectUtils.asList;
import static org.hisp.dhis.common.IdentifiableObjectUtils.asTypedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.analytics.AggregationType;
import org.hisp.dhis.analytics.AnalyticsManager;
import org.hisp.dhis.analytics.AnalyticsService;
import org.hisp.dhis.analytics.DataQueryParams;
import org.hisp.dhis.analytics.Dimension;
import org.hisp.dhis.analytics.DimensionOption;
import org.hisp.dhis.analytics.DimensionType;
import org.hisp.dhis.analytics.IllegalQueryException;
import org.hisp.dhis.analytics.QueryPlanner;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroupSet;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.RelativePeriodEnum;
import org.hisp.dhis.period.RelativePeriods;
import org.hisp.dhis.period.comparator.PeriodComparator;
import org.hisp.dhis.system.grid.ListGrid;
import org.hisp.dhis.system.util.ConversionUtils;
import org.hisp.dhis.system.util.ListUtils;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.system.util.SystemUtils;
import org.hisp.dhis.system.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultAnalyticsService
    implements AnalyticsService
{
    private static final Log log = LogFactory.getLog( DefaultAnalyticsService.class );
    
    private static final String VALUE_HEADER_NAME = "Value";
    private static final int PERCENT = 100;
    private static final int MAX_QUERIES = 8; //TODO increase?
    
    //TODO completeness on time
    //TODO make sure data x dims are successive
    
    @Autowired
    private AnalyticsManager analyticsManager;
    
    @Autowired
    private QueryPlanner queryPlanner;
    
    @Autowired
    private IndicatorService indicatorService;
    
    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private DataElementCategoryService categoryService;
    
    @Autowired
    private DataSetService dataSetService;
    
    @Autowired
    private OrganisationUnitService organisationUnitService;
    
    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;
    
    @Autowired
    private ExpressionService expressionService;
    
    @Autowired
    private ConstantService constantService;

    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    public Grid getAggregatedDataValues( DataQueryParams params )
        throws IllegalQueryException, Exception
    {
        log.info( "Query: " + params );
        
        queryPlanner.validate( params );
        
        params.conform();
                
        // ---------------------------------------------------------------------
        // Headers and meta-data
        // ---------------------------------------------------------------------

        Grid grid = new ListGrid();

        grid.setMetaData( getUidNameMap( params ) );
        
        for ( Dimension col : params.getHeaderDimensions() )
        {
            grid.addHeader( new GridHeader( col.getDimensionName(), col.getDimension(), String.class.getName(), false, true ) );
        }
        
        grid.addHeader( new GridHeader( DataQueryParams.VALUE_ID, VALUE_HEADER_NAME, Double.class.getName(), false, false ) );

        // ---------------------------------------------------------------------
        // Indicators
        // ---------------------------------------------------------------------

        if ( params.getIndicators() != null )
        {   
            int indicatorIndex = params.getIndicatorDimensionIndex();
            List<Indicator> indicators = asTypedList( params.getIndicators() );
                  
            DataQueryParams dataSourceParams = new DataQueryParams( params );
            dataSourceParams.removeDimension( DATAELEMENT_DIM_ID );
            dataSourceParams.removeDimension( DATASET_DIM_ID );
            
            dataSourceParams = replaceIndicatorsWithDataElements( dataSourceParams, indicatorIndex );

            Map<String, Double> aggregatedDataMap = getAggregatedDataValueMap( dataSourceParams );

            Map<String, Map<DataElementOperand, Double>> permutationOperandValueMap = dataSourceParams.getPermutationOperandValueMap( aggregatedDataMap );
            
            List<List<DimensionOption>> dimensionOptionPermutations = dataSourceParams.getDimensionOptionPermutations();

            Map<String, Double> constantMap = constantService.getConstantMap();

            for ( Indicator indicator : indicators )
            {
                for ( List<DimensionOption> options : dimensionOptionPermutations )
                {
                    String permKey = DimensionOption.asOptionKey( options );

                    Map<DataElementOperand, Double> valueMap = permutationOperandValueMap.get( permKey );
                    
                    if ( valueMap != null )
                    {
                        Period period = (Period) DimensionOption.getPeriodOption( options );
                        
                        Double value = expressionService.getIndicatorValue( indicator, period, valueMap, constantMap, null );
                        
                        if ( value != null )
                        {
                            List<DimensionOption> row = new ArrayList<DimensionOption>( options );
                            
                            row.add( indicatorIndex, new DimensionOption( INDICATOR_DIM_ID, indicator ) );
                                                        
                            grid.addRow();
                            grid.addValues( DimensionOption.getOptionIdentifiers( row ) );
                            grid.addValue( MathUtils.getRounded( value, 1 ) );
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // Data elements
        // ---------------------------------------------------------------------

        if ( params.getDataElements() != null )
        {
            DataQueryParams dataSourceParams = new DataQueryParams( params );
            dataSourceParams.removeDimension( INDICATOR_DIM_ID );
            dataSourceParams.removeDimension( DATASET_DIM_ID );
            
            Map<String, Double> aggregatedDataMap = getAggregatedDataValueMap( dataSourceParams );
            
            for ( Map.Entry<String, Double> entry : aggregatedDataMap.entrySet() )
            {
                grid.addRow();
                grid.addValues( entry.getKey().split( DIMENSION_SEP ) );
                grid.addValue( entry.getValue() );
            }
        }

        // ---------------------------------------------------------------------
        // Data sets / completeness
        // ---------------------------------------------------------------------

        if ( params.getDataSets() != null )
        {            
            DataQueryParams dataSourceParams = new DataQueryParams( params );
            dataSourceParams.removeDimension( INDICATOR_DIM_ID );
            dataSourceParams.removeDimension( DATAELEMENT_DIM_ID );
            dataSourceParams.setAggregationType( AggregationType.COUNT );

            Map<String, Double> aggregatedDataMap = getAggregatedCompletenessValueMap( dataSourceParams );

            DataQueryParams dataTargetParams = new DataQueryParams( params );
            dataTargetParams.setDimensions( ListUtils.getAll( dataTargetParams.getDimensions(), dataTargetParams.getCompletenessDimensionIndexes() ) );
            dataTargetParams.setFilters( ListUtils.getAll( dataTargetParams.getFilters(), dataTargetParams.getCompletenessFilterIndexes() ) );
            dataTargetParams.setAggregationType( AggregationType.COUNT );
            dataTargetParams.setSkipPartitioning( true );

            Map<String, Double> targetMap = getAggregatedCompletenessTargetMap( dataTargetParams );
            
            Map<String, PeriodType> dsPtMap = dataSourceParams.getDataSetPeriodTypeMap();
            
            Integer periodIndex = dataSourceParams.getPeriodDimensionIndex();
            Integer dataSetIndex = dataSourceParams.getDataSetDimensionIndex();
            
            List<Integer> completenessDimIndexes = dataTargetParams.getCompletenessDimensionIndexes();
            
            for ( Map.Entry<String, Double> entry : aggregatedDataMap.entrySet() )
            {
                List<String> row = new ArrayList<String>( Arrays.asList( entry.getKey().split( DIMENSION_SEP ) ) );
                
                List<String> targetRow = ListUtils.getAll( row, completenessDimIndexes );
                String targetKey = StringUtils.join( targetRow, DIMENSION_SEP );
                Double target = targetMap.get( targetKey );
                             
                if ( target != null && entry.getValue() != null )
                {
                    PeriodType queryPt = PeriodType.getPeriodTypeFromIsoString( row.get( periodIndex ) );
                    PeriodType dataPt = dsPtMap.get( row.get( dataSetIndex ) );
                    
                    target = target * queryPt.getPeriodSpan( dataPt );
                    
                    double value = entry.getValue() * PERCENT / target;
                    
                    grid.addRow();
                    grid.addValues( row.toArray() );
                    grid.addValue( MathUtils.getRounded( value, 1 ) );
                }
            }
        }

        // ---------------------------------------------------------------------
        // Other dimensions
        // ---------------------------------------------------------------------

        if ( params.getIndicators() == null && params.getDataElements() == null && params.getDataSets() == null )
        {
            Map<String, Double> aggregatedDataMap = getAggregatedDataValueMap( new DataQueryParams( params ) );
            
            for ( Map.Entry<String, Double> entry : aggregatedDataMap.entrySet() )
            {
                grid.addRow();
                grid.addValues( entry.getKey().split( DIMENSION_SEP ) );
                grid.addValue( entry.getValue() );
            }
        }
        
        // ---------------------------------------------------------------------
        // Category option combo meta-data
        // ---------------------------------------------------------------------
        
        Integer cocIndex = params.getCocIndex();
        
        if ( cocIndex != null )
        {
            addCocMetaData( grid, cocIndex );
        }
        
        return grid;
    }
    
    public Map<String, Double> getAggregatedDataValueMap( DataQueryParams params )
        throws IllegalQueryException, Exception
    {
        return getAggregatedValueMap( params, ANALYTICS_TABLE_NAME );
    }
    
    public Map<String, Double> getAggregatedCompletenessValueMap( DataQueryParams params )
        throws IllegalQueryException, Exception
    {
        return getAggregatedValueMap( params, COMPLETENESS_TABLE_NAME );
    }

    private Map<String, Double> getAggregatedCompletenessTargetMap( DataQueryParams params )
        throws IllegalQueryException, Exception
    {
        return getAggregatedValueMap( params, COMPLETENESS_TARGET_TABLE_NAME );
    }
    
    /**
     * Generates a mapping between a dimension key and the aggregated value. The
     * dimension key is a concatenation of the identifiers in for the dimensions
     * separated by "-".
     */
    private Map<String, Double> getAggregatedValueMap( DataQueryParams params, String tableName )
        throws IllegalQueryException, Exception
    {
        queryPlanner.validate( params );
        
        Timer t = new Timer().start();

        int optimalQueries = MathUtils.getWithin( SystemUtils.getCpuCores(), 1, MAX_QUERIES );
        
        List<DataQueryParams> queries = queryPlanner.planQuery( params, optimalQueries, tableName );
        
        t.getTime( "Planned query for optimal: " + optimalQueries + ", got: " + queries.size() );
        
        List<Future<Map<String, Double>>> futures = new ArrayList<Future<Map<String, Double>>>();
        
        Map<String, Double> map = new HashMap<String, Double>();
        
        for ( DataQueryParams query : queries )
        {
            futures.add( analyticsManager.getAggregatedDataValues( query ) );
        }
        
        for ( Future<Map<String, Double>> future : futures )
        {
            Map<String, Double> taskValues = future.get();
            
            if ( taskValues != null )
            {
                map.putAll( taskValues );
            }
        }
        
        t.getTime( "Got aggregated values" );
        
        return map;
    }
    
    public DataQueryParams getFromUrl( Set<String> dimensionParams, Set<String> filterParams, 
        AggregationType aggregationType, String measureCriteria, I18nFormat format )
    {
        DataQueryParams params = new DataQueryParams();

        params.setAggregationType( aggregationType );
        
        if ( dimensionParams != null && !dimensionParams.isEmpty() )
        {
            for ( String param : dimensionParams )
            {
                String dimension = getDimensionFromParam( param );
                List<String> options = getDimensionOptionsFromParam( param );
                
                if ( dimension != null && options != null )
                {
                    params.getDimensions().addAll( getDimension( dimension, options, format ) );
                }
            }
        }

        if ( filterParams != null && !filterParams.isEmpty() )
        {
            for ( String param : filterParams )
            {
                String dimension = DataQueryParams.getDimensionFromParam( param );
                List<String> options = DataQueryParams.getDimensionOptionsFromParam( param );
                
                if ( dimension != null && options != null )
                {
                    params.getFilters().addAll( getDimension( dimension, options, format ) );
                }
            }
        }
        
        if ( measureCriteria != null && !measureCriteria.isEmpty() )
        {
            params.setMeasureCriteria( DataQueryParams.getMeasureCriteriaFromParam( measureCriteria ) );
        }

        return params;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------
    
    /**
     * Returns a list of dimensions generated from the given dimension identifier
     * and list of dimension options.
     */
    private List<Dimension> getDimension( String dimension, List<String> options, I18nFormat format )
    {
        if ( DATA_X_DIM_ID.equals( dimension ) )
        {
            List<Dimension> dataDimensions = new ArrayList<Dimension>();
            
            List<IdentifiableObject> indicators = new ArrayList<IdentifiableObject>();
            List<IdentifiableObject> dataElements = new ArrayList<IdentifiableObject>();
            List<IdentifiableObject> dataSets = new ArrayList<IdentifiableObject>();
            
            options : for ( String uid : options )
            {
                Indicator in = indicatorService.getIndicator( uid );
                
                if ( in != null )
                {
                    indicators.add( in );
                    continue options;
                }
                
                DataElement de = dataElementService.getDataElement( uid );
                
                if ( de != null )
                {       
                    dataElements.add( de );
                    continue options;
                }
                
                DataSet ds = dataSetService.getDataSet( uid );
                
                if ( ds != null )
                {
                    dataSets.add( ds );
                    continue options;
                }
                
                throw new IllegalQueryException( "Data dimension option identifier does not reference any option: " + uid );                
            }
            
            if ( !indicators.isEmpty() )
            {
                dataDimensions.add( new Dimension( INDICATOR_DIM_ID, DimensionType.INDICATOR, indicators ) );
            }
            
            if ( !dataElements.isEmpty() )
            {
                dataDimensions.add( new Dimension( DATAELEMENT_DIM_ID, DimensionType.DATAELEMENT, dataElements ) );
            }
            
            if ( !dataSets.isEmpty() )
            {
                dataDimensions.add( new Dimension( DATASET_DIM_ID, DimensionType.DATASET, dataSets ) );
            }
            
            if ( indicators.isEmpty() && dataElements.isEmpty() && dataSets.isEmpty() )
            {
                throw new IllegalQueryException( "Dimension dx is present in query without any valid dimension options" );
            }
            
            return dataDimensions;
        }
        
        if ( CATEGORYOPTIONCOMBO_DIM_ID.equals( dimension ) )
        {
            return Arrays.asList( new Dimension( dimension, DimensionType.CATEGORY_OPTION_COMBO, new ArrayList<IdentifiableObject>() ) );
        }
        
        if ( ORGUNIT_DIM_ID.equals( dimension ) )
        {
            List<IdentifiableObject> ous = asList( organisationUnitService.getOrganisationUnitsByUid( options ) );
            
            if ( ous == null || ous.isEmpty() )
            {
                throw new IllegalQueryException( "Dimension ou is present in query without any valid dimension options" );
            }
            
            return Arrays.asList( new Dimension( dimension, DimensionType.ORGANISATIONUNIT, ous ) );
        }
        
        if ( PERIOD_DIM_ID.equals( dimension ) )
        {
            List<Period> periods = new ArrayList<Period>();
            
            periods : for ( String isoPeriod : options )
            {
                Period period = PeriodType.getPeriodFromIsoString( isoPeriod );
                
                if ( period != null )
                {
                    period.setName( format != null ? format.formatPeriod( period ) : null );
                    periods.add( period );
                    continue periods;
                }
                
                if ( RelativePeriodEnum.contains( isoPeriod ) )
                {
                    RelativePeriodEnum relativePeriod = RelativePeriodEnum.valueOf( isoPeriod );
                    periods.addAll( RelativePeriods.getRelativePeriodsFromEnum( relativePeriod, format, true ) );
                    continue periods;
                }
            }
            
            if ( periods.isEmpty() )
            {
                throw new IllegalQueryException( "Dimension pe is present in query without any valid dimension options" );
            }

            List<Period> periodList = new ArrayList<Period>( periods );
            Collections.sort( periodList, PeriodComparator.INSTANCE );
            
            return Arrays.asList( new Dimension( dimension, DimensionType.PERIOD, asList( periodList ) ) );
        }
        
        OrganisationUnitGroupSet orgUnitGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( dimension );
            
        if ( orgUnitGroupSet != null )
        {
            List<IdentifiableObject> ous = asList( organisationUnitGroupService.getOrganisationUnitGroupsByUid( options ) );
            
            return Arrays.asList( new Dimension( dimension, DimensionType.ORGANISATIONUNIT_GROUPSET, ous ) );
        }
        
        DataElementGroupSet dataElementGroupSet = dataElementService.getDataElementGroupSet( dimension );
        
        if ( dataElementGroupSet != null )
        {
            List<IdentifiableObject> des = asList( dataElementService.getDataElementGroupsByUid( options ) );
            
            return Arrays.asList( new Dimension( dimension, DimensionType.DATAELEMENT_GROUPSET, des ) );
        }
        
        throw new IllegalQueryException( "Dimension identifier does not reference any dimension: " + dimension );
    }
    
    private DataQueryParams replaceIndicatorsWithDataElements( DataQueryParams params, int indicatorIndex )
    {
        List<Indicator> indicators = asTypedList( params.getIndicators() );        
        List<IdentifiableObject> dataElements = asList( expressionService.getDataElementsInIndicators( indicators ) );
        
        params.getDimensions().set( indicatorIndex, new Dimension( DATAELEMENT_DIM_ID, DimensionType.DATAELEMENT, dataElements ) );
        params.enableCategoryOptionCombos();
        
        return params;
    }
    
    private Map<String, String> getUidNameMap( DataQueryParams params )
    {
        Map<String, String> map = new HashMap<String, String>();
        map.putAll( getUidNameMap( params.getDimensions() ) );
        map.putAll( getUidNameMap( params.getFilters() ) );        
        return map;
    }
    
    private Map<String, String> getUidNameMap( List<Dimension> dimensions )
    {
        Map<String, String> map = new HashMap<String, String>();
        
        for ( Dimension dimension : dimensions )
        {
            List<IdentifiableObject> options = new ArrayList<IdentifiableObject>( dimension.getOptions() );

            // -----------------------------------------------------------------
            // If dimension is not fixed and has no options, insert all options
            // -----------------------------------------------------------------
            
            if ( !FIXED_DIMS.contains( dimension.getDimension() ) && ( options == null || options.isEmpty() ) )
            {
                if ( DimensionType.ORGANISATIONUNIT_GROUPSET.equals( dimension.getType() ) )
                {
                    options = asList( organisationUnitGroupService.getOrganisationUnitGroupSet( dimension.getDimension() ).getOrganisationUnitGroups() );
                }
                else if ( DimensionType.DATAELEMENT_GROUPSET.equals( dimension.getType() ) )
                {
                    options = asList( dataElementService.getDataElementGroupSet( dimension.getDimension() ).getMembers() );
                }
            }

            // -----------------------------------------------------------------
            // Insert UID and name into map
            // -----------------------------------------------------------------
            
            for ( IdentifiableObject idObject : options )
            {
                map.put( idObject.getUid(), idObject.getDisplayName() );
            }
        }
        
        return map;
    }
    
    private void addCocMetaData( Grid grid, Integer cocIndex )
    {
        Set<String> uids = new HashSet<String>( ConversionUtils.<String>cast( grid.getColumn( cocIndex ) ) );
        
        Collection<DataElementCategoryOptionCombo> cocs = categoryService.getDataElementCategoryOptionCombosByUid( uids );
        
        for ( DataElementCategoryOptionCombo coc : cocs )
        {
            grid.addMetaData( coc.getUid(), coc.getName() );
        }
    }
}
