package org.hisp.dhis.chart.impl;

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

import static org.hisp.dhis.chart.Chart.DIMENSION_DATAELEMENT_PERIOD;
import static org.hisp.dhis.chart.Chart.DIMENSION_INDICATOR_PERIOD;
import static org.hisp.dhis.chart.Chart.DIMENSION_ORGANISATIONUNIT_DATAELEMENT;
import static org.hisp.dhis.chart.Chart.DIMENSION_ORGANISATIONUNIT_INDICATOR;
import static org.hisp.dhis.chart.Chart.DIMENSION_PERIOD_DATAELEMENT;
import static org.hisp.dhis.chart.Chart.DIMENSION_PERIOD_INDICATOR;
import static org.hisp.dhis.chart.Chart.SIZE_NORMAL;
import static org.hisp.dhis.chart.Chart.TYPE_BAR;
import static org.hisp.dhis.chart.Chart.TYPE_BAR3D;
import static org.hisp.dhis.chart.Chart.TYPE_LINE;
import static org.hisp.dhis.chart.Chart.TYPE_LINE3D;
import static org.hisp.dhis.chart.Chart.TYPE_PIE;
import static org.hisp.dhis.chart.Chart.TYPE_PIE3D;
import static org.hisp.dhis.chart.Chart.TYPE_STACKED_BAR;
import static org.hisp.dhis.chart.Chart.TYPE_STACKED_BAR3D;
import static org.hisp.dhis.options.SystemSettingManager.AGGREGATION_STRATEGY_REAL_TIME;
import static org.hisp.dhis.options.SystemSettingManager.DEFAULT_AGGREGATION_STRATEGY;
import static org.hisp.dhis.options.SystemSettingManager.KEY_AGGREGATION_STRATEGY;
import static org.hisp.dhis.system.util.ConversionUtils.getArray;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.SplineInterpolator;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealInterpolator;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.hisp.dhis.aggregation.AggregatedDataValueService;
import org.hisp.dhis.aggregation.AggregationService;
import org.hisp.dhis.chart.Chart;
import org.hisp.dhis.chart.ChartGroup;
import org.hisp.dhis.chart.ChartService;
import org.hisp.dhis.chart.ChartStore;
import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.common.NameableObject;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.minmax.MinMaxDataElement;
import org.hisp.dhis.minmax.MinMaxDataElementService;
import org.hisp.dhis.options.SystemSettingManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.RelativePeriods;
import org.hisp.dhis.system.util.Filter;
import org.hisp.dhis.system.util.FilterUtils;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.LineRenderer3D;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.TableOrder;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
@Transactional
public class DefaultChartService
    implements ChartService
{
    private static final Font titleFont = new Font( "Tahoma", Font.BOLD, 14 );

    private static final Font subTitleFont = new Font( "Tahoma", Font.PLAIN, 12 );

    private static final String TREND_PREFIX = "Trend - ";

    private static final String TITLE_SEPARATOR = " - ";

    private static final String DEFAULT_TITLE_PIVOT_CHART = "Pivot Chart";

    private static final Color[] colors = { Color.decode( "#d54a4a" ), Color.decode( "#2e4e83" ),
        Color.decode( "#75e077" ), Color.decode( "#e3e274" ), Color.decode( "#e58c6d" ), Color.decode( "#df6ff3" ),
        Color.decode( "#88878e" ), Color.decode( "#6ff3e8" ), Color.decode( "#6fc3f3" ), Color.decode( "#aaf36f" ),
        Color.decode( "#9d6ff3" ), Color.decode( "#474747" ) };

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ChartStore chartStore;

    public void setChartStore( ChartStore chartStore )
    {
        this.chartStore = chartStore;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private MinMaxDataElementService minMaxDataElementService;

    public void setMinMaxDataElementService( MinMaxDataElementService minMaxDataElementService )
    {
        this.minMaxDataElementService = minMaxDataElementService;
    }

    private AggregationService aggregationService;

    public void setAggregationService( AggregationService aggregationService )
    {
        this.aggregationService = aggregationService;
    }

    private AggregatedDataValueService aggregatedDataValueService;

    public void setAggregatedDataValueService( AggregatedDataValueService aggregatedDataValueService )
    {
        this.aggregatedDataValueService = aggregatedDataValueService;
    }

    private SystemSettingManager systemSettingManager;

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private GenericIdentifiableObjectStore<ChartGroup> chartGroupStore;

    public void setChartGroupStore( GenericIdentifiableObjectStore<ChartGroup> chartGroupStore )
    {
        this.chartGroupStore = chartGroupStore;
    }

    // -------------------------------------------------------------------------
    // ChartService implementation
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public JFreeChart getJFreeChart( int id, I18nFormat format )
    {
        Chart chart = getChart( id );

        if ( chart.getRelatives() != null )
        {
            chart.setRelativePeriods( periodService.reloadPeriods( chart.getRelatives().getRelativePeriods( 1, null,
                false ) ) );
        }

        User user = currentUserService.getCurrentUser();

        if ( chart.isUserOrganisationUnit() && user != null && user.getOrganisationUnit() != null )
        {
            chart.setOrganisationUnit( user.getOrganisationUnit() );
        }

        chart.setFormat( format );
        chart.init();

        return getJFreeChart( chart, !chart.getHideSubtitle() );
    }

    public JFreeChart getJFreeChart( Indicator indicator, OrganisationUnit unit, I18nFormat format )
    {
        RelativePeriods relatives = new RelativePeriods();
        relatives.setMonthsThisYear( true );
        List<Period> periods = periodService.reloadPeriods( relatives.getRelativePeriods( 1, format, true ) );

        Chart chart = new Chart();

        chart.setTitle( indicator.getName() );
        chart.setType( TYPE_BAR );
        chart.setSize( SIZE_NORMAL );
        chart.setDimension( DIMENSION_PERIOD_INDICATOR );
        chart.setHideLegend( true );
        chart.setVerticalLabels( true );
        chart.getIndicators().add( indicator );
        chart.setPeriods( periods );
        chart.setOrganisationUnit( unit );
        chart.setFormat( format );

        chart.init();

        return getJFreeChart( chart, true );
    }

    public JFreeChart getJFreeChart( List<Indicator> indicators, List<DataElement> dataElements, List<Period> periods,
        List<OrganisationUnit> organisationUnits, String dimension, boolean regression, I18nFormat format )
    {
        Chart chart = new Chart();

        if ( indicators != null && indicators.size() > 0 )
        {
            chart.setTitle( getTitle( indicators.get( 0 ), periods, organisationUnits, format ) );
        }
        else if ( dataElements != null && dataElements.size() > 0 )
        {
            chart.setTitle( getTitle( dataElements.get( 0 ), periods, organisationUnits, format ) );
        }

        chart.setType( TYPE_BAR );
        chart.setSize( SIZE_NORMAL );
        chart.setDimension( dimension );
        chart.setHideLegend( false );
        chart.setVerticalLabels( true );
        chart.setHorizontalPlotOrientation( false );
        chart.setRegression( regression );
        chart.setIndicators( indicators );
        chart.setDataElements( dataElements );
        chart.setPeriods( periods );
        chart.setOrganisationUnits( organisationUnits );
        chart.setFormat( format );

        chart.init();

        return getJFreeChart( chart, false );
    }

    public JFreeChart getJFreeChart( String title, PlotOrientation orientation, CategoryLabelPositions labelPositions,
        Map<String, Double> categoryValues )
    {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        for ( Entry<String, Double> entry : categoryValues.entrySet() )
        {
            dataSet.addValue( entry.getValue(), title, entry.getKey() );
        }

        CategoryPlot plot = getCategoryPlot( dataSet, getBarRenderer(), orientation, labelPositions );

        JFreeChart jFreeChart = getBasicJFreeChart( plot );
        jFreeChart.setTitle( title );

        return jFreeChart;
    }

    public JFreeChart getJFreeChartHistory( DataElement dataElement,
        DataElementCategoryOptionCombo categoryOptionCombo, Period lastPeriod, OrganisationUnit organisationUnit,
        int historyLength, I18nFormat format )
    {
        lastPeriod = periodService.reloadPeriod( lastPeriod );

        List<Period> periods = periodService.getPeriods( lastPeriod, historyLength );

        MinMaxDataElement minMax = minMaxDataElementService.getMinMaxDataElement( organisationUnit, dataElement,
            categoryOptionCombo );

        UnivariateRealInterpolator interpolator = new SplineInterpolator();

        Integer periodCount = 0;
        List<Double> x = new ArrayList<Double>();
        List<Double> y = new ArrayList<Double>();

        // ---------------------------------------------------------------------
        // DataValue, MinValue and MaxValue DataSets
        // ---------------------------------------------------------------------

        DefaultCategoryDataset dataValueDataSet = new DefaultCategoryDataset();
        DefaultCategoryDataset metaDataSet = new DefaultCategoryDataset();

        for ( Period period : periods )
        {
            ++periodCount;

            period.setName( format.formatPeriod( period ) );

            DataValue dataValue = dataValueService.getDataValue( organisationUnit, dataElement, period,
                categoryOptionCombo );

            double value = 0;

            if ( dataValue != null && dataValue.getValue() != null && MathUtils.isNumeric( dataValue.getValue() ) )
            {
                value = Double.parseDouble( dataValue.getValue() );

                x.add( periodCount.doubleValue() );
                y.add( value );
            }

            dataValueDataSet.addValue( value, dataElement.getShortName(), period.getName() );

            if ( minMax != null )
            {
                metaDataSet.addValue( minMax.getMin(), "Min value", period.getName() );
                metaDataSet.addValue( minMax.getMax(), "Max value", period.getName() );
            }
        }

        // ---------------------------------------------------------------------
        // Interpolation DataSet
        // ---------------------------------------------------------------------

        if ( x.size() >= 3 ) // minimum 3 points required for interpolation
        {
            periodCount = 0;

            double[] xa = getArray( x );

            int min = MathUtils.getMin( xa ).intValue();
            int max = MathUtils.getMax( xa ).intValue();

            try
            {
                UnivariateRealFunction function = interpolator.interpolate( xa, getArray( y ) );

                for ( Period period : periods )
                {
                    if ( ++periodCount >= min && periodCount <= max )
                    {
                        metaDataSet.addValue( function.value( periodCount ), "Regression value", period.getName() );
                    }
                }
            }
            catch ( MathException ex )
            {
                throw new RuntimeException( "Failed to interpolate", ex );
            }
        }

        // ---------------------------------------------------------------------
        // Plots
        // ---------------------------------------------------------------------

        CategoryPlot plot = getCategoryPlot( dataValueDataSet, getBarRenderer(), PlotOrientation.VERTICAL,
            CategoryLabelPositions.UP_45 );

        plot.setDataset( 1, metaDataSet );
        plot.setRenderer( 1, getLineRenderer() );

        JFreeChart jFreeChart = getBasicJFreeChart( plot );

        return jFreeChart;
    }

    // -------------------------------------------------------------------------
    // ChartGroup
    // -------------------------------------------------------------------------

    public int addChartGroup( ChartGroup chartGroup )
    {
        return chartGroupStore.save( chartGroup );
    }

    public void updateChartGroup( ChartGroup chartGroup )
    {
        chartGroupStore.update( chartGroup );
    }

    public void deleteChartGroup( ChartGroup chartGroup )
    {
        chartGroupStore.delete( chartGroup );
    }

    public ChartGroup getChartGroup( int id )
    {
        return chartGroupStore.get( id );
    }

    public ChartGroup getChartGroupByName( String name )
    {
        return chartGroupStore.getByName( name );
    }

    public Collection<ChartGroup> getAllChartGroups()
    {
        return chartGroupStore.getAll();
    }

    public Collection<ChartGroup> getChartGroups( final Collection<Integer> identifiers )
    {
        Collection<ChartGroup> groups = getAllChartGroups();

        return identifiers == null ? groups : FilterUtils.filter( groups, new Filter<ChartGroup>()
        {
            public boolean retain( ChartGroup object )
            {
                return identifiers.contains( object.getId() );
            }
        } );
    }

    public Collection<ChartGroup> getGroupsContainingChart( Chart chart )
    {
        Collection<ChartGroup> groups = getAllChartGroups();

        Iterator<ChartGroup> iterator = groups.iterator();

        while ( iterator.hasNext() )
        {
            ChartGroup group = iterator.next();

            if ( !group.getMembers().contains( chart ) )
            {
                iterator.remove();
            }
        }

        return groups;
    }

    public int getChartGroupCount()
    {
        return chartGroupStore.getCount();
    }

    public int getChartGroupCountByName( String name )
    {
        return chartGroupStore.getCountByName( name );
    }

    public Collection<ChartGroup> getChartGroupsBetween( int first, int max )
    {
        return chartGroupStore.getBetween( first, max );
    }

    public Collection<ChartGroup> getChartGroupsBetweenByName( String name, int first, int max )
    {
        return chartGroupStore.getBetweenByName( name, first, max );
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Returns a basic JFreeChart.
     */
    private JFreeChart getBasicJFreeChart( CategoryPlot plot )
    {
        JFreeChart jFreeChart = new JFreeChart( null, titleFont, plot, false );

        jFreeChart.setBackgroundPaint( Color.WHITE );
        jFreeChart.setAntiAlias( true );

        return jFreeChart;
    }

    /**
     * Returns a CategoryPlot.
     */
    private CategoryPlot getCategoryPlot( CategoryDataset dataSet, CategoryItemRenderer renderer,
        PlotOrientation orientation, CategoryLabelPositions labelPositions )
    {
        CategoryPlot plot = new CategoryPlot( dataSet, new CategoryAxis(), new NumberAxis(), renderer );

        plot.setDatasetRenderingOrder( DatasetRenderingOrder.FORWARD );
        plot.setOrientation( orientation );

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions( labelPositions );

        return plot;
    }

    /**
     * Returns a bar renderer.
     */
    private BarRenderer getBarRenderer()
    {
        BarRenderer renderer = new BarRenderer();

        renderer.setMaximumBarWidth( 0.07 );

        for ( int i = 0; i < colors.length; i++ )
        {
            renderer.setSeriesPaint( i, colors[i] );
            renderer.setShadowVisible( false );
        }

        return renderer;
    }

    /**
     * Returns a bar3d renderer.
     */
    private BarRenderer getBar3DRenderer()
    {
        BarRenderer3D renderer = new BarRenderer3D();

        renderer.setMaximumBarWidth( 0.07 );

        for ( int i = 0; i < colors.length; i++ )
        {
            renderer.setSeriesPaint( i, colors[i] );
            renderer.setShadowVisible( false );
        }

        return renderer;
    }

    /**
     * Returns a line and shape renderer.
     */
    private LineAndShapeRenderer getLineRenderer()
    {
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();

        for ( int i = 0; i < colors.length; i++ )
        {
            renderer.setSeriesPaint( i, colors[i] );
        }

        return renderer;
    }

    /**
     * Returns a line3d renderer.
     */
    private LineRenderer3D getLineRenderer3D()
    {
        LineRenderer3D renderer = new LineRenderer3D();

        for ( int i = 0; i < colors.length; i++ )
        {
            renderer.setSeriesPaint( i, colors[i] );
        }

        return renderer;
    }

    /**
     * Returns a JFreeChart of type defined in the chart argument.
     */
    private JFreeChart getJFreeChart( Chart chart, boolean subTitle )
    {
        final BarRenderer barRenderer = getBarRenderer();
        final BarRenderer bar3dRenderer = getBar3DRenderer();
        final LineAndShapeRenderer lineRenderer = getLineRenderer();
        final LineRenderer3D line3dRenderer = getLineRenderer3D();

        // ---------------------------------------------------------------------
        // Plot
        // ---------------------------------------------------------------------

        CategoryPlot plot = null;

        CategoryDataset[] dataSets = getCategoryDataSet( chart );
        
        if ( chart.isType( TYPE_LINE ) )
        {
            plot = new CategoryPlot( dataSets[0], new CategoryAxis(), new NumberAxis(), lineRenderer );
        }
        if ( chart.isType( TYPE_LINE3D ) )
        {
            plot = new CategoryPlot( dataSets[0], new CategoryAxis(), new NumberAxis(), line3dRenderer );
        }
        else if ( chart.isType( TYPE_BAR3D ) )
        {
            plot = new CategoryPlot( dataSets[0], new CategoryAxis(), new NumberAxis(), bar3dRenderer );
        }
        else if ( chart.isType( TYPE_BAR ) )
        {
            plot = new CategoryPlot( dataSets[0], new CategoryAxis(), new NumberAxis(), barRenderer );
        }
        else if ( chart.isType( TYPE_PIE ) || chart.isType( TYPE_PIE3D ) )
        {
            return getMultiplePieChart( chart, dataSets );
        }
        else if ( chart.isType( TYPE_STACKED_BAR ) || chart.isType( TYPE_STACKED_BAR3D ) )
        {
            return getStackedBarChart( chart, dataSets[0] );
        }

        if ( chart.isRegression() )
        {
            plot.setDataset( 1, dataSets[1] );
            plot.setRenderer( 1, lineRenderer );
        }

        JFreeChart jFreeChart = new JFreeChart( chart.getTitle(), titleFont, plot, !chart.isHideLegend() );

        if ( chart.isTargetLine() )
        {
            Marker marker = new ValueMarker( chart.getTargetLineValue() );
            marker.setPaint( Color.BLACK );
            marker.setStroke( new BasicStroke( 1.1f ) );
            marker.setLabel( chart.getTargetLineLabel() );
            marker.setLabelOffset( new RectangleInsets( -10, 40, 0, 0 ) );
            marker.setLabelFont( subTitleFont );

            plot.addRangeMarker( marker );
        }

        if ( subTitle )
        {
            jFreeChart.addSubtitle( getSubTitle( chart, chart.getFormat() ) );
        }

        // ---------------------------------------------------------------------
        // Plot orientation
        // ---------------------------------------------------------------------

        plot.setOrientation( chart.isHorizontalPlotOrientation() ? PlotOrientation.HORIZONTAL
            : PlotOrientation.VERTICAL );
        plot.setDatasetRenderingOrder( DatasetRenderingOrder.FORWARD );

        // ---------------------------------------------------------------------
        // Category label positions
        // ---------------------------------------------------------------------

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions( chart.isVerticalLabels() ? CategoryLabelPositions.UP_45
            : CategoryLabelPositions.STANDARD );
        xAxis.setLabel( chart.getDomainAxixLabel() );

        ValueAxis yAxis = plot.getRangeAxis();
        yAxis.setLabel( chart.getRangeAxisLabel() );

        // ---------------------------------------------------------------------
        // Color & antialias
        // ---------------------------------------------------------------------

        jFreeChart.setBackgroundPaint( Color.WHITE );
        jFreeChart.setAntiAlias( true );

        return jFreeChart;
    }

    private JFreeChart getStackedBarChart( Chart chart, CategoryDataset dataSet )
    {
        PlotOrientation orientation = chart.isHorizontalPlotOrientation() ? PlotOrientation.HORIZONTAL
            : PlotOrientation.VERTICAL;

        JFreeChart stackedBarChart = null;

        if ( chart.isType( TYPE_STACKED_BAR ) )
        {
            stackedBarChart = ChartFactory.createStackedBarChart( chart.getTitle(), chart.getDomainAxixLabel(),
                chart.getRangeAxisLabel(), dataSet, orientation, true, false, false );
        }
        else
        {
            stackedBarChart = ChartFactory.createStackedBarChart3D( chart.getTitle(), chart.getDomainAxixLabel(),
                chart.getRangeAxisLabel(), dataSet, orientation, true, false, false );
        }

        CategoryPlot plot = (CategoryPlot) stackedBarChart.getPlot();
        plot.setBackgroundPaint( Color.WHITE );
        plot.setOutlinePaint( Color.WHITE );

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setCategoryLabelPositions( chart.isVerticalLabels() ? CategoryLabelPositions.UP_45
            : CategoryLabelPositions.STANDARD );

        stackedBarChart.setAntiAlias( true );

        return stackedBarChart;
    }

    private JFreeChart getMultiplePieChart( Chart chart, CategoryDataset[] dataSets )
    {
        JFreeChart multiplePieChart = null;

        if ( chart.isType( TYPE_PIE ) )
        {
            multiplePieChart = ChartFactory.createMultiplePieChart( chart.getTitle(), dataSets[0], TableOrder.BY_ROW,
                !chart.getHideLegend(), false, false );
        }
        else
        {
            multiplePieChart = ChartFactory.createMultiplePieChart3D( chart.getTitle(), dataSets[0], TableOrder.BY_ROW,
                !chart.getHideLegend(), false, false );
        }

        multiplePieChart.setBackgroundPaint( Color.WHITE );
        multiplePieChart.setAntiAlias( true );

        TextTitle title = multiplePieChart.getTitle();
        title.setFont( titleFont );

        LegendTitle legend = multiplePieChart.getLegend();
        legend.setItemFont( subTitleFont );

        MultiplePiePlot multiplePiePlot = (MultiplePiePlot) multiplePieChart.getPlot();
        JFreeChart pieChart = multiplePiePlot.getPieChart();
        pieChart.getTitle().setFont( subTitleFont );

        PiePlot piePlot = (PiePlot) pieChart.getPlot();
        piePlot.setBackgroundPaint( Color.WHITE );
        piePlot.setShadowXOffset( 0 );
        piePlot.setShadowYOffset( 0 );
        piePlot.setLabelFont( new Font( "Tahoma", Font.PLAIN, 10 ) );
        piePlot.setLabelGenerator( new StandardPieSectionLabelGenerator( "{2}" ) );
        piePlot.setSimpleLabels( true );
        piePlot.setIgnoreZeroValues( true );
        piePlot.setIgnoreNullValues( true );

        for ( int i = 0; i < dataSets[0].getColumnCount(); i++ )
        {
            piePlot.setSectionPaint( dataSets[0].getColumnKey( i ), colors[(i % colors.length)] );
        }

        return multiplePieChart;
    }

    /**
     * Returns a DefaultCategoryDataSet based on aggregated data for the chart.
     */
    private CategoryDataset[] getCategoryDataSet( Chart chart )
    {
        String aggregationStrategy = (String) systemSettingManager.getSystemSetting( KEY_AGGREGATION_STRATEGY,
            DEFAULT_AGGREGATION_STRATEGY );

        final DefaultCategoryDataset regularDataSet = new DefaultCategoryDataset();
        final DefaultCategoryDataset regressionDataSet = new DefaultCategoryDataset();

        if ( chart != null )
        {
            Period selectedPeriod = chart.getAllPeriods().get( 0 );
            OrganisationUnit selectedOrganisationUnit = chart.getAllOrganisationUnits().get( 0 );

            List<Indicator> indicators = chart.getIndicators();
            List<DataElement> dataElements = chart.getDataElements();

            boolean isIndicatorChart = chart.isDimension( DIMENSION_INDICATOR_PERIOD )
                || chart.isDimension( DIMENSION_ORGANISATIONUNIT_INDICATOR )
                || chart.isDimension( DIMENSION_PERIOD_INDICATOR );

            boolean isDataElementChart = !isIndicatorChart;

            int loopSize = isIndicatorChart ? indicators.size() : dataElements.size();

            for ( int i = 0; i < loopSize; i++ )
            {
                final SimpleRegression regression = new SimpleRegression();

                int columnIndex = 0;

                String shortName = null;

                if ( isIndicatorChart )
                {
                    shortName = indicators.get( i ).getShortName();
                }
                else if ( isDataElementChart )
                {
                    shortName = dataElements.get( i ).getShortName();
                }

                if ( chart.isDimension( DIMENSION_PERIOD_INDICATOR ) || chart.isDimension( DIMENSION_INDICATOR_PERIOD )
                    || chart.isDimension( DIMENSION_PERIOD_DATAELEMENT )
                    || chart.isDimension( DIMENSION_DATAELEMENT_PERIOD ) )
                {
                    // ---------------------------------------------------------
                    // Regular dataset
                    // ---------------------------------------------------------

                    for ( Period period : chart.getAllPeriods() )
                    {
                        Double value = null;
                        
                        if ( isIndicatorChart )
                        {
                            value = aggregationStrategy.equals( AGGREGATION_STRATEGY_REAL_TIME ) ? aggregationService
                                .getAggregatedIndicatorValue( indicators.get( i ), period.getStartDate(),
                                    period.getEndDate(), selectedOrganisationUnit ) : aggregatedDataValueService
                                .getAggregatedValue( indicators.get( i ), period, selectedOrganisationUnit );
                        }
                        else if ( isDataElementChart )
                        {
                            value = aggregationStrategy.equals( AGGREGATION_STRATEGY_REAL_TIME ) ? aggregationService
                                .getAggregatedDataValue( dataElements.get( i ), null, period.getStartDate(),
                                    period.getEndDate(), selectedOrganisationUnit ) : aggregatedDataValueService
                                .getAggregatedValue( dataElements.get( i ), period, selectedOrganisationUnit );
                        }

                        if ( chart.isDimension( DIMENSION_PERIOD_INDICATOR )
                            || chart.isDimension( DIMENSION_PERIOD_DATAELEMENT ) )
                        {
                            regularDataSet.addValue( value != null ? value : 0, shortName, chart.getFormat()
                                .formatPeriod( period ) );
                        }
                        else
                        {
                            regularDataSet.addValue( value != null ? value : 0,
                                chart.getFormat().formatPeriod( period ), shortName );
                        }

                        columnIndex++;

                        // Omit missing values and 0 from regression

                        if ( value != null && value != 0.0 )
                        {
                            regression.addData( columnIndex, value );
                        }
                    }

                    // ---------------------------------------------------------
                    // Regression dataset
                    // ---------------------------------------------------------

                    columnIndex = 0;

                    if ( chart.isRegression() )
                    {
                        for ( Period period : chart.getAllPeriods() )
                        {
                            final double value = regression.predict( columnIndex++ );

                            // Enough values must exist for regression

                            if ( !Double.isNaN( value ) )
                            {
                                regressionDataSet.addValue( value, TREND_PREFIX + shortName, chart.getFormat()
                                    .formatPeriod( period ) );

                            }
                        }
                    }
                }
                else if ( chart.isDimension( DIMENSION_ORGANISATIONUNIT_INDICATOR )
                    || chart.isDimension( DIMENSION_ORGANISATIONUNIT_DATAELEMENT ) )
                {
                    // ---------------------------------------------------------
                    // Regular dataset
                    // ---------------------------------------------------------

                    for ( OrganisationUnit unit : chart.getAllOrganisationUnits() )
                    {
                        Double value = null;

                        if ( isIndicatorChart )
                        {
                            value = aggregationStrategy.equals( AGGREGATION_STRATEGY_REAL_TIME ) ? aggregationService
                                .getAggregatedIndicatorValue( indicators.get( i ), selectedPeriod.getStartDate(),
                                    selectedPeriod.getEndDate(), unit ) : aggregatedDataValueService
                                .getAggregatedValue( indicators.get( i ), selectedPeriod, unit );
                        }
                        else if ( isDataElementChart )
                        {
                            value = aggregationStrategy.equals( AGGREGATION_STRATEGY_REAL_TIME ) ? aggregationService
                                .getAggregatedDataValue( dataElements.get( i ), null, selectedPeriod.getStartDate(),
                                    selectedPeriod.getEndDate(), unit ) : aggregatedDataValueService
                                .getAggregatedValue( dataElements.get( i ), selectedPeriod, unit );
                        }

                        regularDataSet.addValue( value != null ? value : 0, shortName, unit.getShortName() );

                        columnIndex++;
                    }

                    // Regression not relevant for organisation unit category
                }

            }
        }

        return new CategoryDataset[] { regularDataSet, regressionDataSet };
    }

    /**
     * Returns a title based on the chart meta data.
     */
    private String getTitle( NameableObject nameableObject, List<Period> periods,
        List<OrganisationUnit> organisationUnits, I18nFormat format )
    {
        String title = "";

        if ( nameableObject != null )
        {
            title += nameableObject.getShortName() + TITLE_SEPARATOR;
        }

        if ( periods != null && periods.size() > 0 )
        {
            title += format.formatPeriod( periods.get( 0 ) ) + TITLE_SEPARATOR;
        }

        if ( organisationUnits != null && organisationUnits.size() > 0 )
        {
            title += organisationUnits.get( 0 ).getShortName() + TITLE_SEPARATOR;
        }

        if ( title.length() == 0 )
        {
            title = DEFAULT_TITLE_PIVOT_CHART;
        }
        else
        {
            title = title.substring( 0, (title.length() - TITLE_SEPARATOR.length()) );
        }

        return title;
    }

    /**
     * Returns a subtitle based on the chart dimension.
     */
    private TextTitle getSubTitle( Chart chart, I18nFormat format )
    {
        TextTitle subTitle = new TextTitle();

        subTitle.setFont( subTitleFont );

        if ( chart.isDimension( DIMENSION_PERIOD_INDICATOR ) && chart.getAllOrganisationUnits().size() > 0 )
        {
            subTitle.setText( chart.getAllOrganisationUnits().get( 0 ).getName() );
        }
        else if ( chart.isDimension( DIMENSION_ORGANISATIONUNIT_INDICATOR ) && chart.getAllPeriods().size() > 0 )
        {
            subTitle.setText( format.formatPeriod( chart.getAllPeriods().get( 0 ) ) );
        }
        else if ( chart.isDimension( DIMENSION_INDICATOR_PERIOD ) && chart.getIndicators().size() > 0 )
        {
            subTitle.setText( chart.getAllOrganisationUnits().get( 0 ).getName() );
        }
        else if ( chart.isDimension( DIMENSION_PERIOD_DATAELEMENT ) && chart.getAllOrganisationUnits().size() > 0 )
        {
            subTitle.setText( chart.getAllOrganisationUnits().get( 0 ).getName() );
        }
        else if ( chart.isDimension( DIMENSION_ORGANISATIONUNIT_DATAELEMENT ) && chart.getAllPeriods().size() > 0 )
        {
            subTitle.setText( format.formatPeriod( chart.getAllPeriods().get( 0 ) ) );
        }
        else if ( chart.isDimension( DIMENSION_DATAELEMENT_PERIOD ) && chart.getDataElements().size() > 0 )
        {
            subTitle.setText( chart.getAllOrganisationUnits().get( 0 ).getName() );
        }
        
        return subTitle;
    }

    // -------------------------------------------------------------------------
    // CRUD operations
    // -------------------------------------------------------------------------

    public int saveChart( Chart chart )
    {
        return chartStore.save( chart );
    }

    public void saveOrUpdate( Chart chart )
    {
        chartStore.saveOrUpdate( chart );
    }

    public Chart getChart( int id )
    {
        return chartStore.get( id );
    }

    public void deleteChart( Chart chart )
    {
        chartStore.delete( chart );
    }

    public Collection<Chart> getAllCharts()
    {
        return chartStore.getAll();
    }

    public Chart getChartByTitle( String name )
    {
        return chartStore.getByTitle( name );
    }

    public Collection<Chart> getCharts( final Collection<Integer> identifiers )
    {
        Collection<Chart> charts = getAllCharts();

        return identifiers == null ? charts : FilterUtils.filter( charts, new Filter<Chart>()
        {
            public boolean retain( Chart object )
            {
                return identifiers.contains( object.getId() );
            }
        } );
    }

    public int getChartCount()
    {
        return chartStore.getChartCount();
    }

    public int getChartCountByName( String name )
    {
        return chartStore.getChartCountByName( name );
    }

    public Collection<Chart> getChartsBetween( int first, int max )
    {
        return chartStore.getChartsBetween( first, max );
    }

    public Collection<Chart> getChartsBetweenByName( String name, int first, int max )
    {
        return chartStore.getChartsBetweenByName( name, first, max );
    }
}
