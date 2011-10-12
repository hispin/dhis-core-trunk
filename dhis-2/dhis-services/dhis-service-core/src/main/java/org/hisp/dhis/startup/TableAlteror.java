package org.hisp.dhis.startup;

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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.amplecode.quick.StatementHolder;
import org.amplecode.quick.StatementManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.system.startup.AbstractStartupRoutine;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class TableAlteror
    extends AbstractStartupRoutine
{
    private static final Log log = LogFactory.getLog( TableAlteror.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private StatementManager statementManager;

    public void setStatementManager( StatementManager statementManager )
    {
        this.statementManager = statementManager;
    }

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    @Transactional
    public void execute()
    {
        // ---------------------------------------------------------------------
        // Drop outdated tables
        // ---------------------------------------------------------------------

        executeSql( "DROP TABLE categoryoptioncomboname" );
        executeSql( "DROP TABLE orgunitgroupsetstructure" );
        executeSql( "DROP TABLE orgunitstructure" );
        executeSql( "DROP TABLE orgunithierarchystructure" );
        executeSql( "DROP TABLE orgunithierarchy" );
        executeSql( "DROP TABLE datavalueaudit" );
        executeSql( "DROP TABLE columnorder" );
        executeSql( "DROP TABLE roworder" );
        executeSql( "DROP TABLE sectionmembers" );
        executeSql( "DROP TABLE reporttable_categoryoptioncombos" );
        executeSql( "DROP TABLE dashboardcontent_datamartexports" );
        executeSql( "DROP TABLE customvalue" );
        executeSql( "DROP TABLE reporttable_displaycolumns" );
        executeSql( "DROP TABLE reportreporttables" );
        executeSql( "DROP TABLE frequencyoverrideassociation" );
        executeSql( "DROP TABLE dataelement_dataelementgroupsetmembers" );
        executeSql( "DROP TABLE dashboardcontent_olapurls" );
        executeSql( "DROP TABLE olapurl" );
        executeSql( "DROP TABLE calculateddataelement" );
        executeSql( "ALTER TABLE dataelementcategoryoption drop column categoryid" );
        executeSql( "ALTER TABLE reporttable DROP column dimension_type" );
        executeSql( "ALTER TABLE reporttable DROP column dimensiontype" );
        executeSql( "ALTER TABLE reporttable DROP column tablename" );
        executeSql( "ALTER TABLE reporttable DROP column existingtablename" );
        executeSql( "ALTER TABLE reporttable DROP column docategoryoptioncombos" );
        executeSql( "ALTER TABLE reporttable DROP column mode" );
        executeSql( "ALTER TABLE categoryoptioncombo DROP COLUMN displayorder" );
        executeSql( "ALTER TABLE dataelementcategoryoption DROP COLUMN shortname" );
        executeSql( "ALTER TABLE section DROP COLUMN label" );
        executeSql( "ALTER TABLE section DROP COLUMN title" );
        executeSql( "ALTER TABLE organisationunit DROP COLUMN polygoncoordinates" );
        executeSql( "ALTER TABLE indicator DROP COLUMN extendeddataelementid" );
        executeSql( "ALTER TABLE indicator DROP COLUMN numeratoraggregationtype" );
        executeSql( "ALTER TABLE indicator DROP COLUMN denominatoraggregationtype" );
        
        // remove relative period type
        executeSql( "DELETE FROM period WHERE periodtypeid=(select periodtypeid from periodtype where name in ( 'Survey', 'OnChange', 'Relative' ))" );
        executeSql( "DELETE FROM periodtype WHERE name in ( 'Survey', 'OnChange', 'Relative' )" );

        // mapping
        executeSql( "DROP TABLE maporganisationunitrelation" );
        executeSql( "ALTER TABLE mapview DROP COLUMN mapid" );
        executeSql( "DROP TABLE map" );
        executeSql( "DELETE FROM systemsetting WHERE name = 'longitude'" );
        executeSql( "DELETE FROM systemsetting WHERE name = 'latitude'" );
        
        executeSql( "ALTER TABLE map DROP CONSTRAINT fk_map_organisationunitid" );
        executeSql( "ALTER TABLE map DROP COLUMN organisationunitid" );
        executeSql( "ALTER TABLE map DROP COLUMN longitude" );
        executeSql( "ALTER TABLE map DROP COLUMN latitude" );
        executeSql( "ALTER TABLE map DROP COLUMN zoom" );
        executeSql( "ALTER TABLE maplayer DROP CONSTRAINT maplayer_mapsource_key" );
        executeSql( "ALTER TABLE maplayer DROP COLUMN mapsource" );
        executeSql( "ALTER TABLE maplayer DROP COLUMN mapsourcetype" );
        executeSql( "ALTER TABLE maplayer DROP COLUMN layer" );

        // extended data element
        executeSql( "ALTER TABLE dataelement DROP CONSTRAINT fk_dataelement_extendeddataelementid" );
        executeSql( "ALTER TABLE dataelement DROP COLUMN extendeddataelementid" );
        executeSql( "ALTER TABLE indicator DROP CONSTRAINT fk_indicator_extendeddataelementid" );
        executeSql( "ALTER TABLE indicator DROP COLUMN extendeddataelementid" );
        executeSql( "DROP TABLE extendeddataelement" );
        
        // ---------------------------------------------------------------------
        // Update tables for dimensional model
        // ---------------------------------------------------------------------

        // categories_categoryoptions
        // set to 0 temporarily
        int c1 = executeSql( "UPDATE categories_categoryoptions SET sort_order=0 WHERE sort_order is NULL OR sort_order=0" );
        if ( c1 > 0 )
        {
            updateSortOrder( "categories_categoryoptions", "categoryid", "categoryoptionid" );
        }
        executeSql( "ALTER TABLE categories_categoryoptions DROP CONSTRAINT categories_categoryoptions_pkey" );
        executeSql( "ALTER TABLE categories_categoryoptions ADD CONSTRAINT categories_categoryoptions_pkey PRIMARY KEY (categoryid, sort_order)" );

        // categorycombos_categories
        // set to 0 temporarily
        int c2 = executeSql( "update categorycombos_categories SET sort_order=0 where sort_order is NULL OR sort_order=0" );
        if ( c2 > 0 )
        {
            updateSortOrder( "categorycombos_categories", "categorycomboid", "categoryid" );
        }
        executeSql( "ALTER TABLE categorycombos_categories DROP CONSTRAINT categorycombos_categories_pkey" );
        executeSql( "ALTER TABLE categorycombos_categories ADD CONSTRAINT categorycombos_categories_pkey PRIMARY KEY (categorycomboid, sort_order)" );

        // categorycombos_optioncombos
        executeSql( "ALTER TABLE categorycombos_optioncombos DROP CONSTRAINT categorycombos_optioncombos_pkey" );
        executeSql( "ALTER TABLE categorycombos_optioncombos ADD CONSTRAINT categorycombos_optioncombos_pkey PRIMARY KEY (categoryoptioncomboid)" );
        executeSql( "ALTER TABLE categorycombos_optioncombos DROP CONSTRAINT fk4bae70f697e49675" );

        // categoryoptioncombos_categoryoptions
        // set to 0 temporarily
        int c3 = executeSql( "update categoryoptioncombos_categoryoptions SET sort_order=0 where sort_order is NULL OR sort_order=0" );
        if ( c3 > 0 )
        {
            updateSortOrder( "categoryoptioncombos_categoryoptions", "categoryoptioncomboid", "categoryoptionid" );
        }
        executeSql( "ALTER TABLE categoryoptioncombos_categoryoptions DROP CONSTRAINT categoryoptioncombos_categoryoptions_pkey" );
        executeSql( "ALTER TABLE categoryoptioncombos_categoryoptions ADD CONSTRAINT categoryoptioncombos_categoryoptions_pkey PRIMARY KEY (categoryoptioncomboid, sort_order)" );

        // dataelementcategoryoption
        executeSql( "ALTER TABLE dataelementcategoryoption DROP CONSTRAINT fk_dataelement_categoryid" );
        // executeSql(
        // "ALTER TABLE dataelementcategoryoption DROP CONSTRAINT
        // dataelementcategoryoption_name_key"
        // ); will be maintained in transition period
        executeSql( "ALTER TABLE dataelementcategoryoption DROP CONSTRAINT dataelementcategoryoption_shortname_key" );

        // minmaxdataelement query index
        executeSql( "CREATE INDEX index_minmaxdataelement ON minmaxdataelement( sourceid, dataelementid, categoryoptioncomboid )" );

        // add mandatory boolean field to patientattribute
        executeSql( "ALTER TABLE patientattribute ADD mandatory bool" );
        
        if ( executeSql( "ALTER TABLE patientattribute ADD groupby bool" ) >= 0){
            executeSql( "UPDATE patientattribute SET groupby=false" );
        }
        
        // update periodType field to ValidationRule
        executeSql( "UPDATE validationrule SET periodtypeid = (SELECT periodtypeid FROM periodtype WHERE name='Monthly') WHERE periodtypeid is null" );

        // update dataelement.domainTypes of which values is null
        executeSql( "UPDATE dataelement SET domaintype='aggregate' WHERE domaintype is null" );
        
        // set varchar to text
        executeSql( "ALTER TABLE dataelement ALTER description TYPE text" );
        executeSql( "ALTER TABLE indicator ALTER description TYPE text" );
        executeSql( "ALTER TABLE datadictionary ALTER description TYPE text" );
        executeSql( "ALTER TABLE validationrule ALTER description TYPE text" );
        executeSql( "ALTER TABLE expression ALTER expression TYPE text" );
        executeSql( "ALTER TABLE translation ALTER value TYPE text" );
        executeSql( "ALTER TABLE organisationunit ALTER comment TYPE text" );

        // orgunit shortname uniqueness
        executeSql( "ALTER TABLE organisationunit DROP CONSTRAINT organisationunit_shortname_key" );

        // update dataset-dataentryform association and programstage -
        // dataentryform association
        if ( updateDataSetAssociation() && updateProgramStageAssociation() )
        {
            // delete table dataentryformassociation
            executeSql( "DROP TABLE dataentryformassociation" );
        }

        executeSql( "ALTER TABLE section DROP CONSTRAINT section_name_key" );
        executeSql( "UPDATE patientattribute set inheritable=false where inheritable is null" );
        executeSql( "UPDATE dataelement set numbertype='number' where numbertype is null and valuetype='int'" );

       // revert prepare aggregateXXXValue tables for offline diffs

        executeSql( "ALTER TABLE aggregateddatavalue DROP COLUMN modified");
        executeSql( "ALTER TABLE aggregatedindicatorvalue DROP COLUMN modified ");
        executeSql( "UPDATE indicatortype SET indicatornumber=false WHERE indicatornumber is null" );

        // remove outdated relative periods
        
        executeSql( "ALTER TABLE reporttable DROP COLUMN last3months" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN last6months" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN last9months" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN last12months" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN sofarthisyear" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN sofarthisfinancialyear" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN last3to6months" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN last6to9months" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN last9to12months" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN last12individualmonths" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN individualmonthsthisyear" );
        executeSql( "ALTER TABLE reporttable DROP COLUMN individualquartersthisyear" );

        executeSql( "ALTER TABLE chart DROP COLUMN last3months" );
        executeSql( "ALTER TABLE chart DROP COLUMN last6months" );
        executeSql( "ALTER TABLE chart DROP COLUMN last9months" );
        executeSql( "ALTER TABLE chart DROP COLUMN last12months" );
        executeSql( "ALTER TABLE chart DROP COLUMN sofarthisyear" );
        executeSql( "ALTER TABLE chart DROP COLUMN sofarthisfinancialyear" );
        executeSql( "ALTER TABLE chart DROP COLUMN last3to6months" );
        executeSql( "ALTER TABLE chart DROP COLUMN last6to9months" );
        executeSql( "ALTER TABLE chart DROP COLUMN last9to12months" );
        executeSql( "ALTER TABLE chart DROP COLUMN last12individualmonths" );
        executeSql( "ALTER TABLE chart DROP COLUMN individualmonthsthisyear" );
        executeSql( "ALTER TABLE chart DROP COLUMN individualquartersthisyear" );

        executeSql( "ALTER TABLE datamartexport DROP COLUMN last3months" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN last6months" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN last9months" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN last12months" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN sofarthisyear" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN sofarthisfinancialyear" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN last3to6months" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN last6to9months" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN last9to12months" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN last12individualmonths" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN individualmonthsthisyear" );
        executeSql( "ALTER TABLE datamartexport DROP COLUMN individualquartersthisyear" );

        // remove source
        
        executeSql( "ALTER TABLE datasetsource DROP CONSTRAINT fk766ae2938fd8026a" );
        executeSql( "ALTER TABLE datasetlocksource DROP CONSTRAINT fk582fdf7e8fd8026a" );
        executeSql( "ALTER TABLE completedatasetregistration DROP CONSTRAINT fk_datasetcompleteregistration_sourceid" );
        executeSql( "ALTER TABLE minmaxdataelement DROP CONSTRAINT fk_minmaxdataelement_sourceid" );
        executeSql( "ALTER TABLE datavalue DROP CONSTRAINT fk_datavalue_sourceid" );
        executeSql( "ALTER TABLE datavaluearchive DROP CONSTRAINT fk_datavaluearchive_sourceid" );
        executeSql( "ALTER TABLE organisationunit DROP CONSTRAINT fke509dd5ef1c932ed" );
        executeSql( "DROP TABLE source CASCADE" );        

        // message

        executeSql( "ALTER TABLE message DROP COLUMN messagesubject" );
        executeSql( "ALTER TABLE usermessage DROP COLUMN messagedate" );
        executeSql( "DROP TABLE message_usermessages" );

        // create code unique constraints
        
        executeSql( "ALTER TABLE dataelement ADD CONSTRAINT dataelement_code_key UNIQUE(code)" );
        executeSql( "ALTER TABLE indicator ADD CONSTRAINT indicator_code_key UNIQUE(code)" );
        executeSql( "ALTER TABLE organisationunit ADD CONSTRAINT organisationunit_code_key UNIQUE(code)" );
        
        log.info( "Tables updated" );
    }

    private List<Integer> getDistinctIdList( String table, String col1 )
    {
        StatementHolder holder = statementManager.getHolder();

        List<Integer> distinctIds = new ArrayList<Integer>();

        try
        {
            Statement statement = holder.getStatement();

            ResultSet resultSet = statement.executeQuery( "SELECT DISTINCT " + col1 + " FROM " + table );

            while ( resultSet.next() )
            {
                distinctIds.add( resultSet.getInt( 1 ) );
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
        finally
        {
            holder.close();
        }

        return distinctIds;
    }

    private Map<Integer, List<Integer>> getIdMap( String table, String col1, String col2, List<Integer> distinctIds )
    {
        StatementHolder holder = statementManager.getHolder();

        Map<Integer, List<Integer>> idMap = new HashMap<Integer, List<Integer>>();

        try
        {
            Statement statement = holder.getStatement();

            for ( Integer distinctId : distinctIds )
            {
                List<Integer> foreignIds = new ArrayList<Integer>();

                ResultSet resultSet = statement.executeQuery( "SELECT " + col2 + " FROM " + table + " WHERE " + col1
                    + "=" + distinctId );

                while ( resultSet.next() )
                {
                    foreignIds.add( resultSet.getInt( 1 ) );
                }

                idMap.put( distinctId, foreignIds );
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
        finally
        {
            holder.close();
        }

        return idMap;
    }

    private void updateSortOrder( String table, String col1, String col2 )
    {
        List<Integer> distinctIds = getDistinctIdList( table, col1 );

        log.info( "Got distinct ids: " + distinctIds.size() );

        Map<Integer, List<Integer>> idMap = getIdMap( table, col1, col2, distinctIds );

        log.info( "Got id map: " + idMap.size() );

        for ( Integer distinctId : idMap.keySet() )
        {
            int sortOrder = 1;

            for ( Integer foreignId : idMap.get( distinctId ) )
            {
                String sql = "UPDATE " + table + " SET sort_order=" + sortOrder++ + " WHERE " + col1 + "=" + distinctId
                    + " AND " + col2 + "=" + foreignId;

                int count = executeSql( sql );

                log.info( "Executed: " + count + " - " + sql );
            }
        }
    }

    private int executeSql( String sql )
    {
        try
        {
            return statementManager.getHolder().executeUpdate( sql );
        }
        catch ( Exception ex )
        {
            log.debug( ex );

            return -1;
        }
    }

    private boolean updateDataSetAssociation()
    {
        StatementHolder holder = statementManager.getHolder();

        try
        {
            Statement statement = holder.getStatement();

            ResultSet isUpdated = statement
                .executeQuery( "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'dataentryformassociation'" );

            if ( isUpdated.next() )
            {

                ResultSet resultSet = statement
                    .executeQuery( "SELECT associationid, dataentryformid FROM dataentryformassociation WHERE associationtablename = 'dataset'" );

                while ( resultSet.next() )
                {
                    executeSql( "UPDATE dataset SET dataentryform=" + resultSet.getInt( 2 ) + " WHERE datasetid="
                        + resultSet.getInt( 1 ) );
                }
                return true;
            }

            return false;

        }
        catch ( Exception ex )
        {
            log.debug( ex );
            return false;
        }
        finally
        {
            holder.close();
        }

    }

    private boolean updateProgramStageAssociation()
    {
        StatementHolder holder = statementManager.getHolder();

        try
        {
            Statement statement = holder.getStatement();

            ResultSet isUpdated = statement
                .executeQuery( "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'dataentryformassociation'" );

            if ( isUpdated.next() )
            {

                ResultSet resultSet = statement
                    .executeQuery( "SELECT associationid, dataentryformid FROM dataentryformassociation WHERE associationtablename = 'programstage'" );

                while ( resultSet.next() )
                {
                    executeSql( "UPDATE programstage SET dataentryform=" + resultSet.getInt( 2 )
                        + " WHERE programstageid=" + resultSet.getInt( 1 ) );
                }
            }
            return true;
        }
        catch ( Exception ex )
        {
            log.debug( ex );
            return false;
        }
        finally
        {
            holder.close();
        }

    }

}
