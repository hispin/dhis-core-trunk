package org.hisp.dhis.analytics.table;

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

import static org.hisp.dhis.system.util.TextUtils.getQuotedCommaDelimitedString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.hisp.dhis.analytics.DataQueryParams;
import org.hisp.dhis.dataelement.DataElementGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.system.util.DateUtils;
import org.springframework.scheduling.annotation.Async;

/**
 * This class manages the analytics table. The analytics table is a denormalized
 * table designed for analysis which contains raw data values. It has columns for
 * each organisation unit group set and organisation unit level. Also, columns
 * for dataelementid, periodid, organisationunitid, categoryoptioncomboid, value.
 * 
 * The analytics table is horizontally partitioned. The partition key is the start 
 * date of the  period of the data record. The table is partitioned according to 
 * time span with one partition per calendar quarter.
 * 
 * The data records in this table are not aggregated. Typically, queries will
 * aggregate in organisation unit hierarchy dimension, in the period/time dimension,
 * and the category dimensions, as well as organisation unit group set dimensions.
 * 
 * @author Lars Helge Overland
 */
public class JdbcAnalyticsTableManager
    extends AbstractJdbcTableManager
{
    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------
    
    public String getTableName()
    {
        return "analytics";
    }
        
    public void createTable( String tableName )
    {
        final String sqlDrop = "drop table " + tableName;
        
        executeSilently( sqlDrop );
        
        String sqlCreate = "create table " + tableName + " (";
        
        for ( String[] col : getDimensionColumns() )
        {
            sqlCreate += col[0] + " " + col[1] + ",";
        }
        
        sqlCreate += "daysxvalue double precision, value double precision)";
        
        log.info( "Create SQL: " + sqlCreate );
        
        executeSilently( sqlCreate );
    }
    
    @Async
    public Future<?> populateTableAsync( String tableName, Period period )
    {
        Date startDate = period.getStartDate();
        Date endDate = period.getEndDate();
        
        populateTable( tableName, startDate, endDate, "cast(dv.value as double precision)", "int" );
        
        populateTable( tableName, startDate, endDate, "1" , "bool" );
        
        return null;
    }
    
    private void populateTable( String tableName, Date startDate, Date endDate, String valueExpression, String valueType )
    {
        final String start = DateUtils.getMediumDateString( startDate );
        final String end = DateUtils.getMediumDateString( endDate );
        
        String insert = "insert into " + tableName + " (";
        
        for ( String[] col : getDimensionColumns() )
        {
            insert += col[0] + ",";
        }
        
        insert += "daysxvalue, value) ";
        
        String select = "select ";
        
        for ( String[] col : getDimensionColumns() )
        {
            select += col[2] + ",";
        }
        
        select = select.replace( "organisationunitid", "sourceid" ); // Legacy fix TODO remove
        
        select += 
            valueExpression + " * ps.daysno as value, " +
            valueExpression + " as value " +
            "from datavalue dv " +
            "left join _dataelementgroupsetstructure degs on dv.dataelementid=degs.dataelementid " +
            "left join _organisationunitgroupsetstructure ougs on dv.sourceid=ougs.organisationunitid " +
            "left join _orgunitstructure ous on dv.sourceid=ous.organisationunitid " +
            "left join _periodstructure ps on dv.periodid=ps.periodid " +
            "left join dataelement de on dv.dataelementid=de.dataelementid " +
            "left join categoryoptioncombo coc on dv.categoryoptioncomboid=coc.categoryoptioncomboid " +
            "left join period pe on dv.periodid=pe.periodid " +
            "where de.valuetype='" + valueType + "' " +
            "and pe.startdate >= '" + start + "' " +
            "and pe.startdate <= '" + end + "'" +
            "and dv.value != ''" +
            "and dv.value is not null";

        final String sql = insert + select;
        
        log.info( "Populate SQL: "+ sql );
        
        jdbcTemplate.execute( sql );
    }

    public List<String[]> getDimensionColumns()
    {
        List<String[]> columns = new ArrayList<String[]>();

        Collection<DataElementGroupSet> dataElementGroupSets =
            dataElementService.getCompulsoryDataElementGroupSets();
        
        Collection<OrganisationUnitGroupSet> orgUnitGroupSets = 
            organisationUnitGroupService.getCompulsoryOrganisationUnitGroupSets();
        
        Collection<OrganisationUnitLevel> levels =
            organisationUnitService.getOrganisationUnitLevels();

        for ( DataElementGroupSet groupSet : dataElementGroupSets )
        {
            String[] col = { groupSet.getUid(), "character(11)", "degs." + groupSet.getUid() };
            columns.add( col );
        }
        
        for ( OrganisationUnitGroupSet groupSet : orgUnitGroupSets )
        {
            String[] col = { groupSet.getUid(), "character(11)", "ougs." + groupSet.getUid() };
            columns.add( col );
        }
        
        for ( OrganisationUnitLevel level : levels )
        {
            String column = PREFIX_ORGUNITLEVEL + level.getLevel();
            String[] col = { column, "character(11)", "ous." + column };
            columns.add( col );
        }
        
        for ( PeriodType periodType : PeriodType.getAvailablePeriodTypes().subList( 0, 7 ) )
        {
            String column = periodType.getName().toLowerCase();
            String[] col = { column, "character varying(10)", "ps." + column };
            columns.add( col );
        }
        
        String[] de = { "de", "character(11) not null", "de.uid" };
        String[] co = { "coc", "character(11) not null", "coc.uid" };
        String[] level = { "level", "integer", "ous.level" };
        
        columns.addAll( Arrays.asList( de, co, level ) );
        
        return columns;
    }
    
    public Date getEarliestData()
    {
        final String sql = "select min(pe.startdate) from datavalue dv " +
            "join period pe on dv.periodid=pe.periodid";
        
        return jdbcTemplate.queryForObject( sql, Date.class );
    }

    public Date getLatestData()
    {
        final String sql = "select max(pe.enddate) from datavalue dv " +
            "join period pe on dv.periodid=pe.periodid";
        
        return jdbcTemplate.queryForObject( sql, Date.class );
    }
    
    public void applyAggregationLevels( String tableName, Collection<String> dataElements, int aggregationLevel )
    {
        StringBuilder sql = new StringBuilder( "update " + tableName + " set " );
        
        for ( int i = 0; i < aggregationLevel; i++ )
        {
            int level = i + 1;
            
            String column = DataQueryParams.LEVEL_PREFIX + level;
            
            sql.append( column + " = null," );
        }
        
        sql.deleteCharAt( sql.length() - ",".length() );
        
        sql.append( " where level > " + aggregationLevel );
        sql.append( " and de in (" + getQuotedCommaDelimitedString( dataElements ) + ")" );
        
        log.info( "Aggregation level SQL: " + sql.toString() );
        
        jdbcTemplate.execute( sql.toString() );
    }
}
