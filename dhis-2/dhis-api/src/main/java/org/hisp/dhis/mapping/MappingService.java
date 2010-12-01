package org.hisp.dhis.mapping;

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

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.hisp.dhis.aggregation.AggregatedMapValue;

/**
 * @author Jan Henrik Overland
 * @version $Id$
 */
public interface MappingService
{
    final String ID = MappingService.class.getName();

    final String GEOJSON_DIR = "geojson";

    final String MAP_VALUE_TYPE_INDICATOR = "indicator";

    final String MAP_VALUE_TYPE_DATAELEMENT = "dataelement";

    final String MAPLEGENDSET_TYPE_AUTOMATIC = "automatic";

    final String MAPLEGENDSET_TYPE_PREDEFINED = "predefined";

    final String KEY_MAP_DATE_TYPE = "dateType";

    final String MAP_DATE_TYPE_FIXED = "fixed";

    final String MAP_DATE_TYPE_START_END = "start-end";

    final String ORGANISATION_UNIT_SELECTION_TYPE_PARENT = "parent";

    final String ORGANISATION_UNIT_SELECTION_TYPE_LEVEL = "level";

    // -------------------------------------------------------------------------
    // IndicatorMapValue
    // -------------------------------------------------------------------------

    Collection<AggregatedMapValue> getIndicatorMapValues( int indicatorId, int periodId, int parentOrganisationUnitId );

    Collection<AggregatedMapValue> getIndicatorMapValues( int indicatorId, Date startDate, Date endDate,
        int parentOrganisationUnitId );

    Collection<AggregatedMapValue> getIndicatorMapValues( int indicatorId, int periodId, int parentOrganisationUnitId,
        int level );

    Collection<AggregatedMapValue> getIndicatorMapValues( int indicatorId, Date startDate, Date endDate,
        int parentOrganisationUnitId, int level );

    Collection<AggregatedMapValue> getIndicatorMapValuesByLevel( int dataElementId, int periodId, int level );

    Collection<AggregatedMapValue> getIndicatorMapValuesByLevel( int dataElementId, Date startDate, Date endDate,
        int level );

    // -------------------------------------------------------------------------
    // DataMapValue
    // -------------------------------------------------------------------------

    Collection<AggregatedMapValue> getDataElementMapValues( int dataElementId, int periodId,
        int parentOrganisationUnitId );

    Collection<AggregatedMapValue> getDataElementMapValues( int dataElementId, Date startDate, Date endDate,
        int parentOrganisationUnitId );

    Collection<AggregatedMapValue> getDataElementMapValues( int dataElementId, int periodId,
        int parentOrganisationUnitId, int level );

    Collection<AggregatedMapValue> getDataElementMapValues( int dataElementId, Date startDate, Date endDate,
        int parentOrganisationUnitId, int level );

    Collection<AggregatedMapValue> getDataElementMapValuesByLevel( int dataElementId, int periodId, int level );

    Collection<AggregatedMapValue> getDataElementMapValuesByLevel( int dataElementId, Date startDate, Date endDate,
        int level );

    // -------------------------------------------------------------------------
    // MapLegend
    // -------------------------------------------------------------------------

    void addOrUpdateMapLegend( String name, Double startValue, Double endValue, String color );

    void deleteMapLegend( MapLegend legend );

    MapLegend getMapLegend( int id );

    MapLegend getMapLegendByName( String name );

    Collection<MapLegend> getAllMapLegends();

    // -------------------------------------------------------------------------
    // MapLegendSet
    // -------------------------------------------------------------------------

    int addMapLegendSet( MapLegendSet legendSet );

    void updateMapLegendSet( MapLegendSet legendSet );

    void addOrUpdateMapLegendSet( String name, String type, int method, int classes, String colorLow, String colorHigh,
        Set<MapLegend> mapLegends );

    void deleteMapLegendSet( MapLegendSet legendSet );

    MapLegendSet getMapLegendSet( int id );

    MapLegendSet getMapLegendSetByName( String name );

    Collection<MapLegendSet> getMapLegendSetsByType( String type );

    MapLegendSet getMapLegendSetByIndicator( int indicatorId );

    MapLegendSet getMapLegendSetByDataElement( int dataElementId );

    Collection<MapLegendSet> getAllMapLegendSets();

    boolean indicatorHasMapLegendSet( int indicatorId );

    // -------------------------------------------------------------------------
    // MapView
    // -------------------------------------------------------------------------

    int addMapView( MapView mapView );

    void updateMapView( MapView mapView );

    void addOrUpdateMapView( String name, String featureType, String mapValueType, Integer indicatorGroupId,
        Integer indicatorId, Integer dataElementGroupId, Integer dataElementId, String periodTypeName,
        Integer periodId, String startDate, String endDate, Integer parentOrganisationUnitId,
        Integer organisationUnitLevelId, String mapLegendType, Integer method, Integer classes, String bounds,
        String colorLow, String colorHigh, Integer mapLegendSetId, Integer radiusLow, Integer radiusHigh,
        String longitude, String latitude, int zoom );

    void deleteMapView( MapView view );

    MapView getMapView( int id );

    MapView getMapViewByName( String name );

    Collection<MapView> getAllMapViews();

    Collection<MapView> getMapViewsByFeatureType( String featureType );

    // -------------------------------------------------------------------------
    // MapLayer
    // -------------------------------------------------------------------------

    int addMapLayer( MapLayer mapLayer );

    void updateMapLayer( MapLayer mapLayer );

    void addOrUpdateMapLayer( String name, String type, String mapSource, String layer, String fillColor,
        double fillOpacity, String strokeColor, int strokeWidth );

    void deleteMapLayer( MapLayer mapLayer );

    MapLayer getMapLayer( int id );

    MapLayer getMapLayerByName( String name );

    Collection<MapLayer> getMapLayersByType( String type );

    MapLayer getMapLayerByMapSource( String mapSource );

    Collection<MapLayer> getAllMapLayers();
}