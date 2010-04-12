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

import static org.hisp.dhis.system.util.MathUtils.isNumeric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.aggregation.AggregatedMapValue;
import org.hisp.dhis.datamart.DataMartStore;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorGroup;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.user.UserSettingService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jan Henrik Overland
 * @version $Id$
 */
@Transactional
public class DefaultMappingService
    implements MappingService
{
    private static final Log log = LogFactory.getLog( DefaultMappingService.class );

    private static final String RELATION_SEPARATOR = ";;";

    private static final String PAIR_SEPARATOR = "::";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private MappingStore mappingStore;

    public void setMappingStore( MappingStore mappingStore )
    {
        this.mappingStore = mappingStore;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
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

    private UserSettingService userSettingService;

    public void setUserSettingService( UserSettingService userSettingService )
    {
        this.userSettingService = userSettingService;
    }

    private DataMartStore dataMartStore;

    public void setDataMartStore( DataMartStore dataMartStore )
    {
        this.dataMartStore = dataMartStore;
    }

    // -------------------------------------------------------------------------
    // MappingService implementation
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // MapValues
    // -------------------------------------------------------------------------

    public Collection<AggregatedMapValue> getAggregatedMapValues( int indicatorId, int periodId, String mapLayerPath )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        int level = map.getOrganisationUnitLevel().getLevel();

        Collection<AggregatedMapValue> mapValues = dataMartStore.getAggregatedMapValues( indicatorId, periodId, level );

        java.util.Map<Integer, String> relations = getOrganisationUnitFeatureMap( getMapOrganisationUnitRelationsByMap( map ) );

        for ( AggregatedMapValue value : mapValues )
        {
            value.setFeatureId( relations.get( value.getOrganisationUnitId() ) );
        }

        return mapValues;
    }

    public Collection<AggregatedMapValue> getAggregatedMapValues( int indicatorId, int periodId, int level )
    {
        return dataMartStore.getAggregatedMapValues( indicatorId, periodId, level );
    }

    /**
     * Returns a map for the given MapOrganisationUnitRelations where the key is
     * the OrganisationUnit identifier and the value is the feature identifier.
     */
    private java.util.Map<Integer, String> getOrganisationUnitFeatureMap(
        Collection<MapOrganisationUnitRelation> relations )
    {
        java.util.Map<Integer, String> map = new HashMap<Integer, String>();

        for ( MapOrganisationUnitRelation relation : relations )
        {
            map.put( relation.getOrganisationUnit().getId(), relation.getFeatureId() );
        }

        return map;
    }

    // -------------------------------------------------------------------------
    // Map
    // -------------------------------------------------------------------------

    public int addMap( Map map )
    {
        return mappingStore.addMap( map );
    }

    public int addMap( String name, String mapLayerPath, String type, String sourceType, int organisationUnitId,
        int organisationUnitLevelId, String nameColumn, String longitude, String latitude, int zoom )
    {
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        OrganisationUnitLevel organisationUnitLevel = organisationUnitService
            .getOrganisationUnitLevel( organisationUnitLevelId );

        Map map = new Map( name, mapLayerPath, type, sourceType, organisationUnit, organisationUnitLevel, nameColumn,
            longitude, latitude, zoom, null );

        return addMap( map );
    }

    public void addOrUpdateMap( String name, String mapLayerPath, String type, String sourceType,
        int organisationUnitId, int organisationUnitLevelId, String nameColumn, String longitude, String latitude,
        int zoom )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        if ( map != null )
        {
            map.setName( name );
            map.setNameColumn( nameColumn );
            map.setLongitude( longitude );
            map.setLatitude( latitude );
            map.setZoom( zoom );

            updateMap( map );
        }
        else
        {
            OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

            OrganisationUnitLevel organisationUnitLevel = organisationUnitService
                .getOrganisationUnitLevel( organisationUnitLevelId );

            map = new Map( name, mapLayerPath, type, sourceType, organisationUnit, organisationUnitLevel, nameColumn,
                longitude, latitude, zoom, null );

            addMap( map );
        }
    }

    public void updateMap( Map map )
    {
        mappingStore.updateMap( map );
    }

    public void deleteMap( Map map )
    {
        mappingStore.deleteMap( map );
    }

    public void deleteMapByMapLayerPath( String mapLayerPath )
    {
        deleteMap( getMapByMapLayerPath( mapLayerPath ) );
    }

    public Map getMap( int id )
    {
        return mappingStore.getMap( id );
    }

    public Map getMapByMapLayerPath( String mapLayerPath )
    {
        return mappingStore.getMapByMapLayerPath( mapLayerPath );
    }

    public Collection<Map> getMapsByType( String type )
    {
        return mappingStore.getMapsByType( type );
    }

    public Collection<Map> getMapsBySourceType()
    {
        String sourceType = (String) userSettingService.getUserSetting( KEY_MAP_SOURCE_TYPE, MAP_SOURCE_TYPE_GEOJSON );

        return mappingStore.getMapsBySourceType( sourceType );
    }

    public Collection<Map> getMapsAtLevel( OrganisationUnitLevel organisationUnitLevel )
    {
        return mappingStore.getMapsAtLevel( organisationUnitLevel );
    }

    public Collection<Map> getAllMaps()
    {
        return mappingStore.getAllMaps();
    }

    public Collection<Map> getAllGeneratedMaps()
    {
        List<OrganisationUnitLevel> organisationUnitLevels = organisationUnitService.getOrganisationUnitLevels();

        List<Map> maps = new ArrayList<Map>();

        for ( OrganisationUnitLevel organisationUnitLevel : organisationUnitLevels )
        {
            Map map = new Map();
            map.setName( organisationUnitLevel.getName() );
            map.setMapLayerPath( String.valueOf( organisationUnitLevel.getLevel() ) );
            map.setOrganisationUnitLevel( organisationUnitLevel );
            maps.add( map );
        }

        return maps;
    }

    public Collection<Map> getAllUserMaps()
    {
        String type = (String) userSettingService.getUserSetting( KEY_MAP_SOURCE_TYPE, MAP_SOURCE_TYPE_GEOJSON );

        return type != null && type.equals( MAP_SOURCE_TYPE_DATABASE ) ? getAllGeneratedMaps() : getMapsBySourceType();
    }

    // -------------------------------------------------------------------------
    // MapOrganisationUnitRelation
    // -------------------------------------------------------------------------

    public int addMapOrganisationUnitRelation( MapOrganisationUnitRelation mapOrganisationUnitRelation )
    {
        return mappingStore.addMapOrganisationUnitRelation( mapOrganisationUnitRelation );
    }

    public int addMapOrganisationUnitRelation( String mapLayerPath, int organisationUnitId, String featureId )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        MapOrganisationUnitRelation mapOrganisationUnitRelation = new MapOrganisationUnitRelation( map,
            organisationUnit, featureId );

        return addMapOrganisationUnitRelation( mapOrganisationUnitRelation );
    }

    public void addOrUpdateMapOrganisationUnitRelations( String mapLayerPath, String relations )
    {
        String[] rels = relations.split( RELATION_SEPARATOR );

        Map map = getMapByMapLayerPath( mapLayerPath );

        java.util.Map<Integer, MapOrganisationUnitRelation> relationMap = getRelationshipMap( getMapOrganisationUnitRelationsByMap( map ) );

        relationsLoop: for ( int i = 0; i < rels.length; i++ )
        {
            final String[] rel = rels[i].split( PAIR_SEPARATOR );

            if ( rel.length != 2 )
            {
                log.warn( "Pair '" + toString( rel ) + "' is invalid for input '" + rels[i] + "'" );

                continue relationsLoop;
            }

            if ( !isNumeric( rel[0] ) )
            {
                log.warn( "Organisation unit id '" + rel[0] + "' belonging to feature id '" + rel[1]
                    + "' is not numeric" );

                continue relationsLoop;
            }

            final int organisationUnitId = Integer.parseInt( rel[0] );
            final String featureId = rel[1];

            MapOrganisationUnitRelation mapOrganisationUnitRelation = relationMap.get( organisationUnitId );

            if ( mapOrganisationUnitRelation != null )
            {
                mapOrganisationUnitRelation.setFeatureId( featureId );

                updateMapOrganisationUnitRelation( mapOrganisationUnitRelation );
            }
            else
            {
                final OrganisationUnit organisationUnit = organisationUnitService
                    .getOrganisationUnit( organisationUnitId );

                mapOrganisationUnitRelation = new MapOrganisationUnitRelation( map, organisationUnit, featureId );

                addMapOrganisationUnitRelation( mapOrganisationUnitRelation );
            }
        }
    }

    /**
     * Provides a textual representation of the contents of a String array.
     */
    private String toString( String[] array )
    {
        final StringBuffer buffer = new StringBuffer( "{" );

        for ( int i = 0; i < array.length; i++ )
        {
            buffer.append( "[" + array[i] + "]," );
        }

        return buffer.append( "}" ).toString();
    }

    public void addOrUpdateMapOrganisationUnitRelation( String mapLayerPath, int organisationUnitId, String featureId )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        MapOrganisationUnitRelation mapOrganisationUnitRelation = getMapOrganisationUnitRelation( map, organisationUnit );

        if ( mapOrganisationUnitRelation != null )
        {
            mapOrganisationUnitRelation.setFeatureId( featureId );

            updateMapOrganisationUnitRelation( mapOrganisationUnitRelation );
        }
        else
        {
            mapOrganisationUnitRelation = new MapOrganisationUnitRelation( map, organisationUnit, featureId );

            addMapOrganisationUnitRelation( mapOrganisationUnitRelation );
        }
    }

    public void updateMapOrganisationUnitRelation( MapOrganisationUnitRelation mapOrganisationUnitRelation )
    {
        mappingStore.updateMapOrganisationUnitRelation( mapOrganisationUnitRelation );
    }

    public void deleteMapOrganisationUnitRelation( MapOrganisationUnitRelation mapOrganisationUnitRelation )
    {
        mappingStore.deleteMapOrganisationUnitRelation( mapOrganisationUnitRelation );
    }

    public MapOrganisationUnitRelation getMapOrganisationUnitRelation( int id )
    {
        return mappingStore.getMapOrganisationUnitRelation( id );
    }

    public MapOrganisationUnitRelation getMapOrganisationUnitRelation( Map map, OrganisationUnit organisationUnit )
    {
        return mappingStore.getMapOrganisationUnitRelation( map, organisationUnit );
    }

    public MapOrganisationUnitRelation getMapOrganisationUnitRelationByFeatureId( String featureId, String mapLayerPath )
    {
        Map map = mappingStore.getMapByMapLayerPath( mapLayerPath );

        Collection<MapOrganisationUnitRelation> relations = mappingStore.getMapOrganisationUnitRelationsByMap( map );

        for ( MapOrganisationUnitRelation relation : relations )
        {
            if ( relation.getFeatureId().equals( featureId ) )
            {
                return relation;
            }
        }

        return null;
    }

    public Collection<MapOrganisationUnitRelation> getAllMapOrganisationUnitRelations()
    {
        return mappingStore.getAllMapOrganisationUnitRelations();
    }

    public Collection<MapOrganisationUnitRelation> getMapOrganisationUnitRelationsByMap( Map map )
    {
        return mappingStore.getMapOrganisationUnitRelationsByMap( map );
    }

    public Collection<MapOrganisationUnitRelation> getAvailableMapOrganisationUnitRelations( Map map )
    {
        Collection<OrganisationUnit> organisationUnits = organisationUnitService.getOrganisationUnitsAtLevel( map
            .getOrganisationUnitLevel().getLevel() );

        java.util.Map<Integer, MapOrganisationUnitRelation> relationMap = getRelationshipMap( getMapOrganisationUnitRelationsByMap( map ) );

        Collection<MapOrganisationUnitRelation> availableRelations = new ArrayList<MapOrganisationUnitRelation>();

        for ( OrganisationUnit unit : organisationUnits )
        {
            MapOrganisationUnitRelation relation = relationMap.get( unit.getId() );

            availableRelations.add( relation != null ? relation : new MapOrganisationUnitRelation( map, unit, null ) );
        }

        return availableRelations;
    }

    /**
     * Returns a Map<Integer, MapOrganisationUnitRelation> where the key is the
     * OrganisationUnit identifier and the value the MapOrganisationUnitRelation
     * itself.
     */
    private java.util.Map<Integer, MapOrganisationUnitRelation> getRelationshipMap(
        Collection<MapOrganisationUnitRelation> relations )
    {
        java.util.Map<Integer, MapOrganisationUnitRelation> map = new HashMap<Integer, MapOrganisationUnitRelation>();

        for ( MapOrganisationUnitRelation relation : relations )
        {
            map.put( relation.getOrganisationUnit().getId(), relation );
        }

        return map;
    }

    public Collection<MapOrganisationUnitRelation> getAvailableMapOrganisationUnitRelations( String mapLayerPath )
    {
        Map map = getMapByMapLayerPath( mapLayerPath );

        return getAvailableMapOrganisationUnitRelations( map );
    }

    public int deleteMapOrganisationUnitRelations( OrganisationUnit organisationUnit )
    {
        return mappingStore.deleteMapOrganisationUnitRelations( organisationUnit );
    }

    public int deleteMapOrganisationUnitRelations( Map map )
    {
        return mappingStore.deleteMapOrganisationUnitRelations( map );
    }

    // -------------------------------------------------------------------------
    // MapLegend
    // -------------------------------------------------------------------------

    public void addOrUpdateMapLegend( String name, Double startValue, Double endValue, String color )
    {
        MapLegend mapLegend = getMapLegendByName( name );

        if ( mapLegend != null )
        {
            mapLegend.setName( name );
            mapLegend.setStartValue( startValue );
            mapLegend.setEndValue( endValue );
            mapLegend.setColor( color );

            mappingStore.updateMapLegend( mapLegend );
        }
        else
        {
            mapLegend = new MapLegend( name, startValue, endValue, color );

            mappingStore.addMapLegend( mapLegend );
        }
    }

    public void deleteMapLegend( MapLegend mapLegend )
    {
        mappingStore.deleteMapLegend( mapLegend );
    }

    public MapLegend getMapLegend( int id )
    {
        return mappingStore.getMapLegend( id );
    }

    public MapLegend getMapLegendByName( String name )
    {
        return mappingStore.getMapLegendByName( name );
    }

    public Collection<MapLegend> getAllMapLegends()
    {
        return mappingStore.getAllMapLegends();
    }

    // -------------------------------------------------------------------------
    // MapLegendSet
    // -------------------------------------------------------------------------

    public int addMapLegendSet( MapLegendSet mapLegendSet )
    {
        return mappingStore.addMapLegendSet( mapLegendSet );
    }

    public void updateMapLegendSet( MapLegendSet mapLegendSet )
    {
        mappingStore.updateMapLegendSet( mapLegendSet );
    }

    public void addOrUpdateMapLegendSet( String name, String type, int method, int classes, String colorLow,
        String colorHigh, Set<MapLegend> mapLegends )
    {
        MapLegendSet mapLegendSet = getMapLegendSetByName( name );

        Set<Indicator> indicators = new HashSet<Indicator>();

        if ( mapLegendSet != null )
        {
            mapLegendSet.setType( type );
            mapLegendSet.setMethod( method );
            mapLegendSet.setClasses( classes );
            mapLegendSet.setColorLow( colorLow );
            mapLegendSet.setColorHigh( colorHigh );
            mapLegendSet.setMapLegends( mapLegends );
            mapLegendSet.setIndicators( indicators );

            this.mappingStore.updateMapLegendSet( mapLegendSet );
        }
        else
        {
            mapLegendSet = new MapLegendSet( name, type, method, classes, colorLow, colorHigh, mapLegends, indicators );

            this.mappingStore.addMapLegendSet( mapLegendSet );
        }
    }

    public void deleteMapLegendSet( MapLegendSet mapLegendSet )
    {
        mappingStore.deleteMapLegendSet( mapLegendSet );
    }

    public MapLegendSet getMapLegendSet( int id )
    {
        return mappingStore.getMapLegendSet( id );
    }

    public MapLegendSet getMapLegendSetByName( String name )
    {
        return mappingStore.getMapLegendSetByName( name );
    }

    public Collection<MapLegendSet> getMapLegendSetsByType( String type )
    {
        return this.mappingStore.getMapLegendSetsByType( type );
    }

    public MapLegendSet getMapLegendSetByIndicator( int indicatorId )
    {
        Indicator indicator = indicatorService.getIndicator( indicatorId );

        Collection<MapLegendSet> mapLegendSets = mappingStore.getAllMapLegendSets();

        for ( MapLegendSet mapLegendSet : mapLegendSets )
        {
            if ( mapLegendSet.getIndicators().contains( indicator ) )
            {
                return mapLegendSet;
            }
        }

        return null;
    }

    public Collection<MapLegendSet> getAllMapLegendSets()
    {
        return mappingStore.getAllMapLegendSets();
    }

    public boolean indicatorHasMapLegendSet( int indicatorId )
    {
        Indicator indicator = indicatorService.getIndicator( indicatorId );

        Collection<MapLegendSet> mapLegendSets = mappingStore.getAllMapLegendSets();

        for ( MapLegendSet mapLegendSet : mapLegendSets )
        {
            if ( mapLegendSet.getIndicators().contains( indicator ) )
            {
                return true;
            }
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // MapView
    // -------------------------------------------------------------------------

    public int addMapView( MapView mapView )
    {
        return mappingStore.addMapView( mapView );
    }

    public int addMapView( String name, int indicatorGroupId, int indicatorId, String periodTypeName, int periodId,
        String mapSourceType, String mapSource, String mapLegendType, int method, int classes, String colorLow,
        String colorHigh, int mapLegendSetId, String longitude, String latitude, int zoom )
    {
        MapView mapView = new MapView();

        IndicatorGroup indicatorGroup = indicatorService.getIndicatorGroup( indicatorGroupId );

        Indicator indicator = indicatorService.getIndicator( indicatorId );

        PeriodType periodType = periodService.getPeriodTypeByClass( PeriodType.getPeriodTypeByName( periodTypeName )
            .getClass() );

        Period period = periodService.getPeriod( periodId );

        MapLegendSet mapLegendSet = getMapLegendSet( mapLegendSetId );
        
        mapView.setName( name );
        mapView.setIndicatorGroup( indicatorGroup );
        mapView.setIndicator( indicator );
        mapView.setPeriodType( periodType );
        mapView.setPeriod( period );
        mapView.setMapSourceType( mapSourceType );
        mapView.setMapSource( mapSource );
        mapView.setMapLegendType( mapLegendType );
        mapView.setMethod( method );
        mapView.setClasses( classes );
        mapView.setColorLow( colorLow );
        mapView.setColorHigh( colorHigh );
        mapView.setMapLegendSet( mapLegendSet );
        mapView.setLongitude( longitude );
        mapView.setLatitude( latitude );
        mapView.setZoom( zoom );

        return mappingStore.addMapView( mapView );
    }

    public void updateMapView( MapView mapView )
    {
        mappingStore.updateMapView( mapView );
    }

    public void addOrUpdateMapView( String name, int indicatorGroupId, int indicatorId, String periodTypeName,
        int periodId, String mapSource, String mapLegendType, int method, int classes, String colorLow, String colorHigh,
        int mapLegendSetId, String longitude, String latitude, int zoom )
    {
        IndicatorGroup indicatorGroup = indicatorService.getIndicatorGroup( indicatorGroupId );

        Indicator indicator = indicatorService.getIndicator( indicatorId );

        PeriodType periodType = periodService.getPeriodTypeByClass( PeriodType.getPeriodTypeByName( periodTypeName )
            .getClass() );

        Period period = periodService.getPeriod( periodId );

        MapLegendSet mapLegendSet = getMapLegendSet( mapLegendSetId );
        
        String mapSourceType = (String) userSettingService
            .getUserSetting( KEY_MAP_SOURCE_TYPE, MAP_SOURCE_TYPE_GEOJSON );

        MapView mapView = mappingStore.getMapViewByName( name );

        if ( mapView != null )
        {
            mapView.setName( name );
            mapView.setIndicatorGroup( indicatorGroup );
            mapView.setIndicator( indicator );
            mapView.setPeriodType( periodType );
            mapView.setPeriod( period );
            mapView.setMapSourceType( mapSourceType );
            mapView.setMapSource( mapSource );
            mapView.setMapLegendType( mapLegendType );
            mapView.setMethod( method );
            mapView.setClasses( classes );
            mapView.setColorLow( colorLow );
            mapView.setColorHigh( colorHigh );
            mapView.setMapLegendSet( mapLegendSet );
            mapView.setLongitude( longitude );
            mapView.setLatitude( latitude );
            mapView.setZoom( zoom );

            updateMapView( mapView );
        }
        else
        {
            mapView = new MapView( name, indicatorGroup, indicator, periodType, period, mapSourceType, mapSource,
                mapLegendType, method, classes, colorLow, colorHigh, mapLegendSet, longitude, latitude, zoom );

            addMapView( mapView );
        }
    }

    public void deleteMapView( MapView view )
    {
        mappingStore.deleteMapView( view );
    }

    public MapView getMapView( int id )
    {
        return mappingStore.getMapView( id );
    }

    public MapView getMapViewByName( String name )
    {
        return mappingStore.getMapViewByName( name );
    }

    public Collection<MapView> getMapViewsByMapSourceType()
    {
        String type = (String) userSettingService.getUserSetting( KEY_MAP_SOURCE_TYPE, MAP_SOURCE_TYPE_GEOJSON );

        return mappingStore.getMapViewsByMapSourceType( type );
    }

    public Collection<MapView> getAllMapViews()
    {
        Collection<MapView> selectedMapViews = new ArrayList<MapView>();

        Collection<MapView> mapViews = mappingStore.getAllMapViews();

        String type = (String) userSettingService.getUserSetting( KEY_MAP_SOURCE_TYPE, MAP_SOURCE_TYPE_GEOJSON );

        if ( mapViews != null )
        {
            for ( MapView mapView : mapViews )
            {
                if ( mapView.getMapSourceType() != null && mapView.getMapSourceType().equals( type ) )
                {
                    selectedMapViews.add( mapView );
                }
            }
        }

        return selectedMapViews;
    }

    // -------------------------------------------------------------------------
    // MapLayer
    // -------------------------------------------------------------------------

    public int addMapLayer( MapLayer mapLayer )
    {
        return mappingStore.addMapLayer( mapLayer );
    }

    public void updateMapLayer( MapLayer mapLayer )
    {
        mappingStore.updateMapLayer( mapLayer );
    }

    public void addOrUpdateMapLayer( String name, String type, String mapSource, String fillColor, double fillOpacity,
        String strokeColor, int strokeWidth )
    {
        MapLayer mapLayer = mappingStore.getMapLayerByName( name );

        String mapSourceType = (String) userSettingService
            .getUserSetting( KEY_MAP_SOURCE_TYPE, MAP_SOURCE_TYPE_GEOJSON );

        if ( mapLayer != null )
        {
            mapLayer.setName( name );
            mapLayer.setType( type );
            mapLayer.setMapSourceType( mapSourceType );
            mapLayer.setMapSource( mapSource );
            mapLayer.setFillColor( fillColor );
            mapLayer.setFillOpacity( fillOpacity );
            mapLayer.setStrokeColor( strokeColor );
            mapLayer.setStrokeWidth( strokeWidth );

            updateMapLayer( mapLayer );
        }
        else
        {
            addMapLayer( new MapLayer( name, type, mapSourceType, mapSource, fillColor, fillOpacity, strokeColor,
                strokeWidth ) );
        }
    }

    public void deleteMapLayer( MapLayer mapLayer )
    {
        mappingStore.deleteMapLayer( mapLayer );
    }

    public MapLayer getMapLayer( int id )
    {
        return mappingStore.getMapLayer( id );
    }

    public MapLayer getMapLayerByName( String name )
    {
        return mappingStore.getMapLayerByName( name );
    }

    public Collection<MapLayer> getMapLayersByMapSourceType()
    {
        String type = (String) userSettingService.getUserSetting( KEY_MAP_SOURCE_TYPE, MAP_SOURCE_TYPE_GEOJSON );

        return mappingStore.getMapLayersByMapSourceType( type );
    }

    public MapLayer getMapLayerByMapSource( String mapSource )
    {
        return mappingStore.getMapLayerByMapSource( mapSource );
    }

    public Collection<MapLayer> getAllMapLayers()
    {
        return mappingStore.getAllMapLayers();
    }
}
