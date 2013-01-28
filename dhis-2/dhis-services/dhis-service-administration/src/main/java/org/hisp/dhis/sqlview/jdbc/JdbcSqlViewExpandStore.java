package org.hisp.dhis.sqlview.jdbc;

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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.common.Grid;
import org.hisp.dhis.sqlview.SqlView;
import org.hisp.dhis.sqlview.SqlViewExpandStore;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Dang Duy Hieu
 * @version $Id JdbcSqlViewExpandStore.java July 06, 2010$
 */
public class JdbcSqlViewExpandStore
    implements SqlViewExpandStore
{
    private static final String PREFIX_CREATEVIEW_QUERY = "CREATE VIEW ";

    private static final String PREFIX_DROPVIEW_QUERY = "DROP VIEW IF EXISTS ";

    private static final String PREFIX_SELECT_QUERY = "SELECT * FROM ";

    private static final String[] types = { "VIEW" };

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------------------------
    // Implementing methods
    // -------------------------------------------------------------------------

    @Override
    public Collection<String> getAllSqlViewNames()
    {
        DatabaseMetaData mtdt;
        Set<String> viewersName = new HashSet<String>();

        try
        {
            mtdt = jdbcTemplate.getDataSource().getConnection().getMetaData();

            ResultSet rs = mtdt.getTables( null, null, SqlView.PREFIX_VIEWNAME + "%", types );

            while ( rs.next() )
            {
                viewersName.add( rs.getString( "TABLE_NAME" ) );
            }
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }

        return viewersName;

    }

    @Override
    public boolean isViewTableExists( String viewTableName )
    {
        DatabaseMetaData mtdt;

        try
        {
            mtdt = jdbcTemplate.getDataSource().getConnection().getMetaData();
            ResultSet rs = mtdt.getTables( null, null, viewTableName.toLowerCase(), types );

            return rs.next();
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public String createView( SqlView sqlViewInstance )
    {
        String viewName = sqlViewInstance.getViewName();

        try
        {
            this.dropViewTable( viewName );

            jdbcTemplate.execute( PREFIX_CREATEVIEW_QUERY + viewName + " AS " + sqlViewInstance.getSqlQuery() );
        }
        catch ( BadSqlGrammarException bge )
        {
            return bge.getCause().getMessage();
        }

        return null;
    }

    @Override
    public void setUpDataSqlViewTable( Grid grid, String viewTableName )
    {
        ResultSet rs;

        try
        {
            rs = this.getResultSet( PREFIX_SELECT_QUERY + viewTableName, jdbcTemplate );
        }
        catch ( SQLException e )
        {
            throw new RuntimeException( "Failed to get data from view " + viewTableName, e );
        }

        grid.addHeaders( rs );
        grid.addRow( rs );
    }

    @Override
    public String testSqlGrammar( String sql )
    {
        String viewNameCheck = SqlView.PREFIX_VIEWNAME + System.currentTimeMillis();

        try
        {
            jdbcTemplate.execute( PREFIX_CREATEVIEW_QUERY + viewNameCheck + " AS " + sql );

            dropViewTable( viewNameCheck );
        }
        catch ( Exception ex )
        {
            return ex.getCause().getMessage();
        }

        return "";
    }

    @Override
    public void dropViewTable( String viewName )
    {
        try
        {
            jdbcTemplate.update( PREFIX_DROPVIEW_QUERY + viewName );
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( "Failed to drop view: " + viewName, ex );
        }
    }

    // -------------------------------------------------------------------------
    // Supporting methods
    // -------------------------------------------------------------------------

    /**
     * Uses StatementManager to obtain a scrollable, read-only ResultSet based
     * on the query string.
     * 
     * @param sql the query
     * @param holder the StatementHolder object
     * @return null or the ResultSet
     */
    private ResultSet getResultSet( String sql, JdbcTemplate jdbcTemplate )
        throws SQLException
    {
        Connection con = jdbcTemplate.getDataSource().getConnection();
        Statement stm = con.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
        stm.execute( sql );
        return stm.getResultSet();
    }
}