package org.hisp.dhis.mapping.hibernate;

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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.mapping.MapLayer;
import org.hisp.dhis.mapping.MapLegend;
import org.hisp.dhis.mapping.MapLegendSet;
import org.hisp.dhis.mapping.MapView;
import org.hisp.dhis.mapping.MappingStore;

/**
 * @author Jan Henrik Overland
 * @version $Id$
 */
public class HibernateMappingStore
    implements MappingStore
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SessionFactory sessionFactory;

    public void setSessionFactory( SessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }

    // -------------------------------------------------------------------------
    // MapLegend
    // -------------------------------------------------------------------------

    public int addMapLegend( MapLegend legend )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( legend );
    }

    public void updateMapLegend( MapLegend legend )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( legend );
    }

    public void deleteMapLegend( MapLegend legend )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( legend );
    }

    public MapLegend getMapLegend( int id )
    {
        Session session = sessionFactory.getCurrentSession();

        return (MapLegend) session.get( MapLegend.class, id );
    }

    public MapLegend getMapLegendByName( String name )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLegendSet.class );

        criteria.add( Restrictions.eq( "name", name ) );

        return (MapLegend) criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<MapLegend> getAllMapLegends()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLegend.class );

        return criteria.list();
    }

    // -------------------------------------------------------------------------
    // MapLegendSet
    // -------------------------------------------------------------------------

    public int addMapLegendSet( MapLegendSet legendSet )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( legendSet );
    }

    public void updateMapLegendSet( MapLegendSet legendSet )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( legendSet );
    }

    public void deleteMapLegendSet( MapLegendSet legendSet )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( legendSet );
    }

    public MapLegendSet getMapLegendSet( int id )
    {
        Session session = sessionFactory.getCurrentSession();

        return (MapLegendSet) session.get( MapLegendSet.class, id );
    }

    public MapLegendSet getMapLegendSetByName( String name )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLegendSet.class );

        criteria.add( Restrictions.eq( "name", name ) );

        return (MapLegendSet) criteria.uniqueResult();
    }
    
    @SuppressWarnings( "unchecked" )
    public Collection<MapLegendSet> getMapLegendSetsByType( String type )
    {
        Session session = this.sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLegendSet.class );

        criteria.add( Restrictions.eq( "type", type ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<MapLegendSet> getAllMapLegendSets()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLegendSet.class );

        return criteria.list();
    }

    // -------------------------------------------------------------------------
    // MapView
    // -------------------------------------------------------------------------

    public int addMapView( MapView view )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( view );
    }

    public void updateMapView( MapView view )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( view );
    }

    public void deleteMapView( MapView view )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( view );
    }

    public MapView getMapView( int id )
    {
        Session session = sessionFactory.getCurrentSession();

        return (MapView) session.get( MapView.class, id );
    }

    public MapView getMapViewByName( String name )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapView.class );

        criteria.add( Restrictions.eq( "name", name ) );

        return (MapView) criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<MapView> getMapViewsByMapSourceType( String mapSourceType )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapView.class );

        criteria.add( Restrictions.eq( "mapSourceType", mapSourceType ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<MapView> getAllMapViews()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapView.class );

        return criteria.list();
    }

    // -------------------------------------------------------------------------
    // MapLayer
    // -------------------------------------------------------------------------

    public int addMapLayer( MapLayer mapLayer )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( mapLayer );
    }

    public void updateMapLayer( MapLayer mapLayer )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( mapLayer );
    }

    public void deleteMapLayer( MapLayer mapLayer )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( mapLayer );
    }

    public MapLayer getMapLayer( int id )
    {
        Session session = sessionFactory.getCurrentSession();

        return (MapLayer) session.get( MapLayer.class, id );
    }

    public MapLayer getMapLayerByName( String name )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLayer.class );

        criteria.add( Restrictions.eq( "name", name ) );

        return (MapLayer) criteria.uniqueResult();
    }
    
    @SuppressWarnings( "unchecked" )
    public Collection<MapLayer> getMapLayersByType( String type )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLayer.class );

        criteria.add( Restrictions.eq( "type", type ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<MapLayer> getMapLayersByMapSourceType( String mapSourceType )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLayer.class );

        criteria.add( Restrictions.eq( "mapSourceType", mapSourceType ) );

        return criteria.list();
    }

    public MapLayer getMapLayerByMapSource( String mapSource )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLayer.class );

        criteria.add( Restrictions.eq( "mapSource", mapSource ) );

        return (MapLayer) criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<MapLayer> getAllMapLayers()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( MapLayer.class );

        return criteria.list();
    }
}
