package org.hisp.dhis.resourcetable.hibernate;

/*
 * Copyright (c) 2004-2007, University of Oslo
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
import java.util.List;

import org.amplecode.quick.Statement;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hisp.dhis.dataelement.DataElementCategory;
import org.hisp.dhis.dataelement.DataElementGroupSet;
import org.hisp.dhis.indicator.IndicatorGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.resourcetable.DataElementCategoryOptionComboName;
import org.hisp.dhis.resourcetable.GroupSetStructure;
import org.hisp.dhis.resourcetable.OrganisationUnitStructure;
import org.hisp.dhis.resourcetable.ResourceTableStore;
import org.hisp.dhis.resourcetable.statement.CreateCategoryTableStatement;
import org.hisp.dhis.resourcetable.statement.CreateDataElementGroupSetTableStatement;
import org.hisp.dhis.resourcetable.statement.CreateIndicatorGroupSetTableStatement;
import org.hisp.dhis.resourcetable.statement.CreateOrganisationUnitGroupSetTableStatement;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class HibernateResourceTableStore
    implements ResourceTableStore
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SessionFactory sessionFactory;

    public void setSessionFactory( SessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }
    
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }    

    // -------------------------------------------------------------------------
    // OrganisationUnitStructure
    // -------------------------------------------------------------------------

    public int addOrganisationUnitStructure( OrganisationUnitStructure structure )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( structure );
    }

    @SuppressWarnings( "unchecked" )
    public Collection<OrganisationUnitStructure> getOrganisationUnitStructures()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( OrganisationUnitStructure.class );

        return criteria.list();
    }

    public int deleteOrganisationUnitStructures()
    {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( "DELETE FROM OrganisationUnitStructure" );

        return query.executeUpdate();
    }

    // -------------------------------------------------------------------------
    // GroupSetStructure
    // -------------------------------------------------------------------------

    public int addGroupSetStructure( GroupSetStructure structure )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( structure );
    }

    @SuppressWarnings( "unchecked" )
    public Collection<GroupSetStructure> getGroupSetStructures()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( GroupSetStructure.class );

        return criteria.list();
    }

    public int deleteGroupSetStructures()
    {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( "DELETE FROM GroupSetStructure" );

        return query.executeUpdate();
    }

    // -------------------------------------------------------------------------
    // DataElementCategoryOptionComboName
    // -------------------------------------------------------------------------

    public int addDataElementCategoryOptionComboName( DataElementCategoryOptionComboName name )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( name );
    }

    @SuppressWarnings( "unchecked" )
    public Collection<DataElementCategoryOptionComboName> getDataElementCategoryOptionComboNames()
    {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria( DataElementCategoryOptionComboName.class );

        return criteria.list();
    }

    public int deleteDataElementCategoryOptionComboNames()
    {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( "DELETE FROM DataElementCategoryOptionComboName" );

        return query.executeUpdate();
    }

    // -------------------------------------------------------------------------
    // DataElementGroupSetTable
    // -------------------------------------------------------------------------

    public void createDataElementGroupSetStructure( List<DataElementGroupSet> groupSets )
    {
        try
        {
            jdbcTemplate.update( "DROP TABLE " + CreateDataElementGroupSetTableStatement.TABLE_NAME );
        }
        catch ( BadSqlGrammarException ex )
        {
            // Do nothing, table does not exist
        }
        
        Statement statement = new CreateDataElementGroupSetTableStatement( groupSets );
        
        jdbcTemplate.update( statement.getStatement() );
    }

    // -------------------------------------------------------------------------
    // DataElementGroupSetTable
    // -------------------------------------------------------------------------

    public void createIndicatorGroupSetStructure( List<IndicatorGroupSet> groupSets )
    {
        try
        {
            jdbcTemplate.update( "DROP TABLE " + CreateIndicatorGroupSetTableStatement.TABLE_NAME );
        }
        catch ( BadSqlGrammarException ex )
        {
            // Do nothing, table does not exist
        }
        
        Statement statement = new CreateIndicatorGroupSetTableStatement( groupSets );
        
        jdbcTemplate.update( statement.getStatement() );
    }
    
    // -------------------------------------------------------------------------
    // OrganisationUnitGroupSetTable
    // -------------------------------------------------------------------------

    public void createOrganisationUnitGroupSetStructure( List<OrganisationUnitGroupSet> groupSets )
    {
        try
        {
            jdbcTemplate.update( "DROP TABLE " + CreateOrganisationUnitGroupSetTableStatement.TABLE_NAME );
        }
        catch ( BadSqlGrammarException ex )
        {
            // Do nothing, table does not exist
        }
        
        Statement statement = new CreateOrganisationUnitGroupSetTableStatement( groupSets );
        
        jdbcTemplate.update( statement.getStatement() );
    }
    
    // -------------------------------------------------------------------------
    // CategoryTable
    // -------------------------------------------------------------------------

    public void createCategoryStructure( List<DataElementCategory> categories )
    {
        try
        {
            jdbcTemplate.update( "DROP TABLE " + CreateCategoryTableStatement.TABLE_NAME );
        }
        catch ( BadSqlGrammarException ex )
        {
            // Do nothing, table does not exist
        }
        
        Statement statement = new CreateCategoryTableStatement( categories );
        
        jdbcTemplate.update( statement.getStatement() );
    }
}
