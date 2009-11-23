package org.hisp.dhis.reporttable.jdbc;

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

import static org.hisp.dhis.reporttable.ReportTable.SEPARATOR;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.amplecode.quick.StatementHolder;
import org.amplecode.quick.StatementManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.jdbc.StatementBuilder;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.reporttable.ReportTableColumn;
import org.hisp.dhis.reporttable.ReportTableData;
import org.hisp.dhis.reporttable.statement.CreateReportTableStatement;
import org.hisp.dhis.reporttable.statement.DisplayReportTableStatement;
import org.hisp.dhis.reporttable.statement.GetReportTableDataStatement;
import org.hisp.dhis.reporttable.statement.GetReportTableStatement;
import org.hisp.dhis.reporttable.statement.RemoveReportTableStatement;
import org.hisp.dhis.reporttable.statement.ReportTableStatement;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class JDBCReportTableManager
    implements ReportTableManager
{
    private static final Log log = LogFactory.getLog( JDBCReportTableManager.class );
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private StatementManager statementManager;

    public void setStatementManager( StatementManager statementManager )
    {
        this.statementManager = statementManager;
    }

    private StatementBuilder statementBuilder;

    public void setStatementBuilder( StatementBuilder statementBuilder )
    {
        this.statementBuilder = statementBuilder;
    }

    // -------------------------------------------------------------------------
    // ReportTableManager implementation
    // -------------------------------------------------------------------------

    public void createReportTable( ReportTable reportTable )
    {
        removeReportTable( reportTable );
        
        StatementHolder holder = statementManager.getHolder();
        
        ReportTableStatement statement = new CreateReportTableStatement( reportTable, statementBuilder );
        
        final String sql = statement.getStatement();
        
        log.debug( "Creating report table with SQL statement: '" + sql + "'" );
        
        try
        {
            holder.getStatement().executeUpdate( sql );
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( "Failed to create table: " + reportTable.getTableName(), ex );
        }
        finally
        {
            holder.close();
        }
    }
    
    public void removeReportTable( ReportTable reportTable )
    {
        StatementHolder holder = statementManager.getHolder();
        
        ReportTableStatement statement = new RemoveReportTableStatement( reportTable );

        final String sql = statement.getStatement();
        
        try
        {
            holder.getStatement().executeUpdate( sql );
        }
        catch ( Exception ex )
        {
            log.info( "Table does not exist: " + reportTable.getTableName() );
        }
        finally
        {
            holder.close();
        }
    }
    
    public Map<String, Double> getAggregatedValueMap( ReportTable reportTable,
        IdentifiableObject metaObject, DataElementCategoryOptionCombo categoryOptionCombo, Period period, OrganisationUnit unit )
    {
        StatementHolder holder = statementManager.getHolder();
        
        ReportTableStatement statement = new GetReportTableDataStatement( reportTable );
        
        statement.setInt( ReportTable.DATAELEMENT_ID, metaObject != null ? metaObject.getId() : -1 );
        statement.setInt( ReportTable.INDICATOR_ID, metaObject != null ? metaObject.getId() : -1 );
        statement.setInt( ReportTable.DATASET_ID, metaObject != null ? metaObject.getId() : -1 );
        statement.setInt( ReportTable.CATEGORYCOMBO_ID, categoryOptionCombo != null ? categoryOptionCombo.getId() : -1 );
        statement.setInt( ReportTable.PERIOD_ID, period != null ? period.getId() : -1 );
        statement.setInt( ReportTable.ORGANISATIONUNIT_ID, unit != null ? unit.getId() : -1 );
        
        try
        {
            ResultSet resultSet = holder.getStatement().executeQuery( statement.getStatement() );
            
            log.debug( "Get values statement: " + statement.getStatement() );
            
            int columnCount = resultSet.getMetaData().getColumnCount(); //TODO improve performance?
            
            Map<String, Double> map = new HashMap<String, Double>();

            // -----------------------------------------------------------------
            // Inserts into a map the aggregated value as value and a unique 
            // identifier constructed from the index columns in the report table 
            // as key.
            // -----------------------------------------------------------------

            while ( resultSet.next() )
            {
                final double value = resultSet.getDouble( 1 );
                
                final StringBuffer identifier = new StringBuffer();
                
                for ( int i = 0; i < columnCount - 1; i++ )
                {
                    int columnIndex = i + 2;
                    
                    identifier.append( resultSet.getInt( columnIndex ) + SEPARATOR );                    
                }
                
                final String key = identifier.substring( 0, identifier.lastIndexOf( SEPARATOR ) );
                
                map.put( key, value );
            }
            
            return map;
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( "Failed to get aggregated value map", ex );
        }
        finally
        {
            holder.close();
        }
    }
    
    public ReportTableData getDisplayReportTableData( ReportTable reportTable )
    {
        ReportTableData data = new ReportTableData();
        
        // ---------------------------------------------------------------------
        // Set name
        // ---------------------------------------------------------------------

        data.setName( reportTable.getName() );

        ReportTableStatement statement = new DisplayReportTableStatement( reportTable );
        
        StatementHolder holder = statementManager.getHolder();

        try
        {
            log.debug( "Get display report table data statement: " + statement.getStatement() );
            
            ResultSet resultSet = holder.getStatement().executeQuery( statement.getStatement() );
            
            // -----------------------------------------------------------------
            // Set columns
            // -----------------------------------------------------------------

            int index = 1;
            
            for ( ReportTableColumn column : reportTable.getFilledDisplayColumns() )
            {
                if ( !column.isHidden() )
                {
                    data.getColumns().put( index++, column.getName() );
                
                    data.getPrettyPrintColumns().add( column.getHeader() );
                }
            }

            // -----------------------------------------------------------------
            // Set data
            // -----------------------------------------------------------------

            while ( resultSet.next() )
            {
                SortedMap<Integer, String> row = new TreeMap<Integer, String>();
                
                index = 1;
                
                for ( ReportTableColumn column : reportTable.getFilledDisplayColumns() )
                {
                    if ( !column.isHidden() )
                    {
                        row.put( index++, String.valueOf( resultSet.getObject( column.getName() ) ) );
                    }
                }
                
                data.getRows().add( row );
            }
            
            log.debug( "Number of rows: " + data.getRows().size() );
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( "Failed to get display report table data", ex );
        }
        finally
        {
            holder.close();
        }
        
        return data;
    }
    
    public ReportTableData getReportTableData( ReportTable reportTable )
    {
        ReportTableData data = new ReportTableData();
        
        // ---------------------------------------------------------------------
        // Set name
        // ---------------------------------------------------------------------

        data.setName( reportTable.getName() );

        ReportTableStatement statement = new GetReportTableStatement( reportTable );
        
        StatementHolder holder = statementManager.getHolder();

        try
        {
            log.debug( "Get report table data statement: " + statement.getStatement() );
            
            ResultSet resultSet = holder.getStatement().executeQuery( statement.getStatement() );
            
            ResultSetMetaData metaData = resultSet.getMetaData();
            
            int columnCount = metaData.getColumnCount();

            // -----------------------------------------------------------------
            // Set columns
            // -----------------------------------------------------------------

            for ( int i = 0; i < columnCount; i++ )
            {
                final int index = i + 1;
                
                data.getColumns().put( index, metaData.getColumnName( index ) );
                
                data.getPrettyPrintColumns().add( reportTable.prettyPrintColumn( metaData.getColumnName( index ) ) );                        
            }

            // -----------------------------------------------------------------
            // Set data
            // -----------------------------------------------------------------

            while ( resultSet.next() )
            {
                SortedMap<Integer, String> row = new TreeMap<Integer, String>();
                
                for ( int i = 0; i < columnCount; i++ )
                {
                    final int index = i + 1;
                                
                    row.put( index, String.valueOf( resultSet.getObject( index ) ) );
                }
                
                data.getRows().add( row );
            }
            
            log.debug( "Number of rows: " + data.getRows().size() );
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( "Failed to get report table data", ex );
        }
        finally
        {
            holder.close();
        }
        
        return data;
    }
}
