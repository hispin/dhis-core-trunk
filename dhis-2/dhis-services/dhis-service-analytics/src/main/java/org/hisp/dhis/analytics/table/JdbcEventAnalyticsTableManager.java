package org.hisp.dhis.analytics.table;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

import static org.hisp.dhis.system.util.TextUtils.removeLast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import org.hisp.dhis.analytics.AnalyticsTable;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.ListUtils;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 */
public class JdbcEventAnalyticsTableManager
    extends AbstractJdbcTableManager
{
    @Autowired
    private ProgramService programService;

    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public List<AnalyticsTable> getTables( Date earliest )
    {
        log.info( "Get tables using earliest: " + earliest );

        return getTables( getDataYears( earliest ) );
    }

    @Override
    @Transactional
    public List<AnalyticsTable> getAllTables()
    {
        return getTables( ListUtils.getClosedOpenList( 1500, 2100 ) );
    }
    
    private List<AnalyticsTable> getTables( List<Integer> dataYears )
    {
        List<AnalyticsTable> tables = new ArrayList<>();

        Collections.sort( dataYears );
        
        String baseName = getTableName();

        for ( Integer year : dataYears )
        {
            Period period = PartitionUtils.getPeriod( year );
            
            List<Integer> programs = getDataPrograms( period );
            
            for ( Integer id : programs )
            {
                Program program = programService.getProgram( id );
                
                AnalyticsTable table = new AnalyticsTable( baseName, null, period, program );
                List<String[]> dimensionColumns = getDimensionColumns( table );
                table.setDimensionColumns( dimensionColumns );
                tables.add( table );
            }
        }

        return tables;
    }
    
    @Override
    public String validState()
    {
        boolean hasData = jdbcTemplate.queryForRowSet( "select dataelementid from trackedentitydatavalue limit 1" ).next();
        
        if ( !hasData )
        {
            return "No events exist, not updating event analytics tables";
        }
        
        return null;
    }

    @Override
    public String getTableName()
    {
        return "analytics_event";
    }

    @Override
    public void createTable( AnalyticsTable table )
    {
        final String tableName = table.getTempTableName();

        final String sqlDrop = "drop table " + tableName;

        executeSilently( sqlDrop );

        String sqlCreate = "create table " + tableName + " (";

        List<String[]> columns = getDimensionColumns( table );
        
        validateDimensionColumns( columns );
        
        for ( String[] col : columns )
        {
            sqlCreate += col[0] + " " + col[1] + ",";
        }

        sqlCreate = removeLast( sqlCreate, 1 ) + ") ";

        sqlCreate += statementBuilder.getTableOptions( false );

        log.info( "Creating table: " + tableName );
        
        log.debug( "Create SQL: " + sqlCreate );
        
        executeSilently( sqlCreate );
    }

    @Async
    @Override
    public Future<?> populateTableAsync( ConcurrentLinkedQueue<AnalyticsTable> tables )
    {
        taskLoop: while ( true )
        {
            AnalyticsTable table = tables.poll();

            if ( table == null )
            {
                break taskLoop;
            }

            final String start = DateUtils.getMediumDateString( table.getPeriod().getStartDate() );
            final String end = DateUtils.getMediumDateString( table.getPeriod().getEndDate() );
            final String tableName = table.getTempTableName();

            String sql = "insert into " + table.getTempTableName() + " (";

            for ( String[] col : getDimensionColumns( table ) )
            {
                sql += col[0] + ",";
            }

            sql = removeLast( sql, 1 ) + ") select ";

            for ( String[] col : getDimensionColumns( table ) )
            {
                sql += col[2] + ",";
            }

            sql = removeLast( sql, 1 ) + " ";

            sql += "from programstageinstance psi " +
                "left join _organisationunitgroupsetstructure ougs on psi.organisationunitid=ougs.organisationunitid " +
                "left join programinstance pi on psi.programinstanceid=pi.programinstanceid " +
                "left join programstage ps on psi.programstageid=ps.programstageid " +
                "left join program pr on pi.programid=pr.programid " +
                "left join trackedentityinstance tei on pi.trackedentityinstanceid=tei.trackedentityinstanceid " +
                "left join organisationunit ou on psi.organisationunitid=ou.organisationunitid " +
                "left join _orgunitstructure ous on psi.organisationunitid=ous.organisationunitid " +
                "left join _dateperiodstructure dps on psi.executiondate=dps.dateperiod " +
                "where psi.executiondate >= '" + start + "' " + 
                "and psi.executiondate <= '" + end + "' " +
                "and pr.programid=" + table.getProgram().getId() + " " + 
                "and psi.organisationunitid is not null " +
                "and psi.executiondate is not null";

            populateAndLog( sql, tableName );
        }

        return null;
    }

    @Override
    public List<String[]> getDimensionColumns( AnalyticsTable table )
    {
        final String dbl = statementBuilder.getDoubleColumnType();
        final String text = "character varying(255)";
        final String numericClause = " and value " + statementBuilder.getRegexpMatch() + " '"
            + MathUtils.NUMERIC_LENIENT_REGEXP + "'";
        final String doubleSelect = "cast(value as " + dbl + ")";

        List<String[]> columns = new ArrayList<>();

        Collection<OrganisationUnitGroupSet> orgUnitGroupSets = 
            organisationUnitGroupService.getDataDimensionOrganisationUnitGroupSets();

        Collection<OrganisationUnitLevel> levels = 
            organisationUnitService.getOrganisationUnitLevels();

        List<PeriodType> periodTypes = PeriodType.getAvailablePeriodTypes();

        for ( OrganisationUnitGroupSet groupSet : orgUnitGroupSets )
        {
            String[] col = { quote( groupSet.getUid() ), "character(11)", "ougs." + quote( groupSet.getUid() ) };
            columns.add( col );
        }
        
        for ( OrganisationUnitLevel level : levels )
        {
            String column = quote( PREFIX_ORGUNITLEVEL + level.getLevel() );
            String[] col = { column, "character(11)", "ous." + column };
            columns.add( col );
        }

        for ( PeriodType periodType : periodTypes )
        {
            String column = quote( periodType.getName().toLowerCase() );
            String[] col = { column, "character varying(15)", "dps." + column };
            columns.add( col );
        }

        for ( DataElement dataElement : table.getProgram().getAllDataElements() )
        {
            String dataType = dataElement.isNumericType() ? dbl : text;
            String dataClause = dataElement.isNumericType() ? numericClause : "";
            String select = dataElement.isNumericType() ? doubleSelect : "value";

            String sql = "(select " + select + " from trackedentitydatavalue where programstageinstanceid="
                + "psi.programstageinstanceid and dataelementid=" + dataElement.getId() + dataClause + ") as "
                + quote( dataElement.getUid() );

            String[] col = { quote( dataElement.getUid() ), dataType, sql };
            columns.add( col );
        }

        for ( TrackedEntityAttribute attribute : table.getProgram().getTrackedEntityAttributes() )
        {
            String dataType = attribute.isNumericType() ? dbl : text;
            String dataClause = attribute.isNumericType() ? numericClause : "";
            String select = attribute.isNumericType() ? doubleSelect : "value";

            String sql = "(select " + select + " from trackedentityattributevalue where trackedentityinstanceid=pi.trackedentityinstanceid and "
                + "trackedentityattributeid=" + attribute.getId() + dataClause + ") as " + quote( attribute.getUid() );

            String[] col = { quote( attribute.getUid() ), dataType, sql };
            columns.add( col );
        }

        String[] psi = { quote( "psi" ), "character(11) not null", "psi.uid" };
        String[] pi = { quote( "pi" ), "character(11) not null", "pi.uid" };
        String[] ps = { quote( "ps" ), "character(11) not null", "ps.uid" };
        String[] ed = { quote( "executiondate" ), "timestamp", "psi.executiondate" };
        String[] longitude = { quote( "longitude" ), dbl, "psi.longitude" };
        String[] latitude = { quote( "latitude" ), dbl, "psi.latitude" };
        String[] ou = { quote( "ou" ), "character(11) not null", "ou.uid" };
        String[] oun = { quote( "ouname" ), "character varying(230) not null", "ou.name" };
        String[] ouc = { quote( "oucode" ), "character varying(50)", "ou.code" };

        columns.addAll( Arrays.asList( psi, pi, ps, ed, longitude, latitude, ou, oun, ouc ) );

        if ( table.hasProgram() && table.getProgram().isRegistration() )
        {
            String[] tei = { quote( "tei" ), "character(11)", "tei.uid" };
            columns.add( tei );
        }
        
        return columns;
    }

    @Override
    public List<Integer> getDataYears( Date earliest )
    {
        String sql = 
            "select distinct(extract(year from psi.executiondate)) " +
            "from programstageinstance psi " +
            "where psi.executiondate is not null";

        if ( earliest != null )
        {
            sql += "and psi.executiondate >= '" + DateUtils.getMediumDateString( earliest ) + "'";
        }
        
        return jdbcTemplate.queryForList( sql, Integer.class );
    }
    
    private List<Integer> getDataPrograms( Period period )
    {
        final String start = DateUtils.getMediumDateString( period.getStartDate() );
        final String end = DateUtils.getMediumDateString( period.getEndDate() );
        
        final String sql = 
            "select distinct pi.programid " +
            "from programstageinstance psi " +
            "inner join programinstance pi on psi.programinstanceid = pi.programinstanceid " +
            "where psi.executiondate >= '" + start + "' " + 
            "and psi.executiondate <= '" + end + "' " +
            "and psi.organisationunitid is not null " +
            "and psi.executiondate is not null";
        
        return jdbcTemplate.queryForList( sql, Integer.class );
    }
    
    @Override
    @Async
    public Future<?> applyAggregationLevels( ConcurrentLinkedQueue<AnalyticsTable> tables,
        Collection<String> dataElements, int aggregationLevel )
    {
        return null; // Not relevant
    }

    @Override
    @Async
    public Future<?> vacuumTablesAsync( ConcurrentLinkedQueue<AnalyticsTable> tables )
    {
        return null; // Not needed
    }
}
