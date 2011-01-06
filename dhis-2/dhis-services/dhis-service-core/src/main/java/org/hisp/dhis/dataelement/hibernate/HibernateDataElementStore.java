package org.hisp.dhis.dataelement.hibernate;

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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.amplecode.quick.StatementManager;
import org.amplecode.quick.mapper.ObjectMapper;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.dataelement.CalculatedDataElement;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.dataelement.DataElementStore;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.hisp.dhis.hierarchy.HierarchyViolationException;
import org.hisp.dhis.system.objectmapper.DataElementOperandMapper;
import org.hisp.dhis.system.util.ConversionUtils;
import org.hisp.dhis.system.util.TextUtils;

/**
 * @author Torgeir Lorange Ostby
 * @version $Id: HibernateDataElementStore.java 5243 2008-05-25 10:18:58Z
 *          larshelg $
 */
public class HibernateDataElementStore
    extends HibernateGenericStore<DataElement> implements DataElementStore
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private StatementManager statementManager;

    public void setStatementManager( StatementManager statementManager )
    {
        this.statementManager = statementManager;
    }

    // -------------------------------------------------------------------------
    // DataElement
    // -------------------------------------------------------------------------

    public int addDataElement( DataElement dataElement )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( dataElement );
    }

    public void updateDataElement( DataElement dataElement )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( dataElement );
    }

    public void deleteDataElement( DataElement dataElement )
        throws HierarchyViolationException
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( dataElement );
    }

    public DataElement getDataElement( int id )
    {
        Session session = sessionFactory.getCurrentSession();

        return (DataElement) session.get( DataElement.class, id );
    }

    public DataElement getDataElement( String uuid )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "uuid", uuid ) );

        return (DataElement) criteria.uniqueResult();
    }

    public DataElement getDataElementByName( String name )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "name", name ) );

        return (DataElement) criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> searchDataElementByName( String key )
    {

        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.ilike( "name", "%" + key + "%" ) );

        return criteria.list();
    }

    public DataElement getDataElementByAlternativeName( String alternativeName )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "alternativeName", alternativeName ) );

        return (DataElement) criteria.uniqueResult();
    }

    public DataElement getDataElementByShortName( String shortName )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "shortName", shortName ) );

        return (DataElement) criteria.uniqueResult();
    }

    public DataElement getDataElementByCode( String code )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "code", code ) );

        return (DataElement) criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getAllDataElements()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.setCacheable( true );
        criteria.addOrder( Order.asc( "name" ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getAggregateableDataElements()
    {
        Session session = sessionFactory.getCurrentSession();

        Set<String> types = new HashSet<String>();

        types.add( DataElement.VALUE_TYPE_INT );
        types.add( DataElement.VALUE_TYPE_BOOL );

        Criteria criteria = session.createCriteria( DataElement.class );

        criteria.add( Restrictions.in( "type", types ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getAllActiveDataElements()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "active", true ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsByAggregationOperator( String aggregationOperator )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "aggregationOperator", aggregationOperator ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsByType( String type )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "type", type ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsByDomainType( String domainType )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "domainType", domainType ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementByCategoryCombo( DataElementCategoryCombo categoryCombo )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "categoryCombo", categoryCombo ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsWithGroupSets()
    {
        final String sql = "from DataElement d where d.groupSets.size > 0";

        Query query = sessionFactory.getCurrentSession().createQuery( sql );

        return query.list();
    }

    public void setZeroIsSignificantForDataElements( Collection<Integer> dataElementIds )
    {
        Session session = sessionFactory.getCurrentSession();

        String sql = "update DataElement set zeroIsSignificant = false";

        Query query = session.createQuery( sql );

        query.executeUpdate();

        if ( !dataElementIds.isEmpty() )
        {
            sql = "update DataElement set zeroIsSignificant=true where id in (:dataElementIds)";

            query = session.createQuery( sql );
            query.setParameterList( "dataElementIds", dataElementIds );

            query.executeUpdate();
        }
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsByZeroIsSignificant( boolean zeroIsSignificant )
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "zeroIsSignificant", zeroIsSignificant ) );

        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsWithoutGroups()
    {
        String hql = "from DataElement d where d.groups.size = 0";

        return sessionFactory.getCurrentSession().createQuery( hql ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsWithoutDataSets()
    {
        String hql = "from DataElement d where d.dataSets.size = 0";

        return sessionFactory.getCurrentSession().createQuery( hql ).list();
    }

    public boolean dataElementExists( int id )
    {
        final String sql = "select count(*) from dataelement where dataelementid=" + id;

        return statementManager.getHolder().queryForInteger( sql ) > 0;
    }

    public boolean dataElementCategoryOptionComboExists( int id )
    {
        final String sql = "select count(*) from categoryoptioncombo where categoryoptioncomboid=" + id;

        return statementManager.getHolder().queryForInteger( sql ) > 0;
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElement> getDataElementsByDataSets( Collection<DataSet> dataSets )
    {
        String hql = "select distinct de from DataElement de join de.dataSets ds where ds.id in (:ids)";

        return sessionFactory.getCurrentSession().createQuery( hql ).setParameterList( "ids",
            ConversionUtils.getIdentifiers( DataSet.class, dataSets ) ).list();
    }

    // -------------------------------------------------------------------------
    // CalculatedDataElement
    // -------------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public Collection<CalculatedDataElement> getAllCalculatedDataElements()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( CalculatedDataElement.class );

        return criteria.list();
    }

    public CalculatedDataElement getCalculatedDataElementByDataElement( DataElement dataElement )
    {
        Session session = sessionFactory.getCurrentSession();

        Set<Integer> dataElementIds = new HashSet<Integer>();
        dataElementIds.add( dataElement.getId() );

        Criteria criteria = session.createCriteria( CalculatedDataElement.class ).createCriteria( "expression" )
            .createCriteria( "dataElementsInExpression" ).add( Restrictions.in( "id", dataElementIds ) );

        return (CalculatedDataElement) criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<CalculatedDataElement> getCalculatedDataElementsByDataElements(
        Collection<DataElement> dataElements )
    {
        Session session = sessionFactory.getCurrentSession();

        Set<Integer> dataElementIds = new HashSet<Integer>();

        for ( DataElement dataElement : dataElements )
        {
            dataElementIds.add( dataElement.getId() );
        }

        Criteria criteria = session.createCriteria( CalculatedDataElement.class ).createCriteria( "expression" )
            .createCriteria( "dataElementsInExpression" ).add( Restrictions.in( "id", dataElementIds ) );

        return new HashSet<CalculatedDataElement>( criteria.list() );

    }

    public Collection<DataElement> getDataElementsLikeName( String name )
    {
        return getLikeName( name );
    }
    
    public Collection<DataElement> getDataElementsBetween( int first, int max )
    {
        return getBetween( first, max );
    }

    public Collection<DataElement> getDataElementsBetweenByName( String name, int first, int max )
    {
        return getBetweenByName( name, first, max );
    }

    public int getDataElementCount()
    {
        return getCount();
    }

    public int getDataElementCountByName( String name )
    {
        return getCountByName( name );
    }
    
    // -------------------------------------------------------------------------
    // DataElementOperand
    // -------------------------------------------------------------------------

    public Collection<DataElementOperand> getAllGeneratedOperands()
    {
        final ObjectMapper<DataElementOperand> mapper = new ObjectMapper<DataElementOperand>();

        final String sql = "SELECT de.dataelementid, de.name, cocn.categoryoptioncomboid, cocn.categoryoptioncomboname "
            + "FROM dataelement as de "
            + "JOIN categorycombo as cc on de.categorycomboid=cc.categorycomboid "
            + "JOIN categorycombos_optioncombos as ccoc on cc.categorycomboid=ccoc.categorycomboid "
            + "LEFT JOIN _categoryoptioncomboname as cocn on ccoc.categoryoptioncomboid=cocn.categoryoptioncomboid;";

        try
        {
            ResultSet resultSet = statementManager.getHolder().getStatement().executeQuery( sql );

            return mapper.getCollection( resultSet, new DataElementOperandMapper() );
        }
        catch ( SQLException ex )
        {
            throw new RuntimeException( "Failed to get all operands", ex );
        }
    }

    public Collection<DataElementOperand> getAllGeneratedOperands( Collection<DataElement> dataElements )
    {
        final String dataElementString = TextUtils.getCommaDelimitedString( ConversionUtils.getIdentifiers(
            DataElement.class, dataElements ) );

        final ObjectMapper<DataElementOperand> mapper = new ObjectMapper<DataElementOperand>();

        final String sql = "SELECT de.dataelementid, de.name, cocn.categoryoptioncomboid, cocn.categoryoptioncomboname "
            + "FROM dataelement as de "
            + "JOIN categorycombo as cc on de.categorycomboid=cc.categorycomboid "
            + "JOIN categorycombos_optioncombos as ccoc on cc.categorycomboid=ccoc.categorycomboid "
            + "LEFT JOIN _categoryoptioncomboname as cocn on ccoc.categoryoptioncomboid=cocn.categoryoptioncomboid "
            + "WHERE de.dataelementid IN (" + dataElementString + ");";

        try
        {
            ResultSet resultSet = statementManager.getHolder().getStatement().executeQuery( sql );

            return mapper.getCollection( resultSet, new DataElementOperandMapper() );
        }
        catch ( SQLException ex )
        {
            throw new RuntimeException( "Failed to get all operands", ex );
        }
    }
}
