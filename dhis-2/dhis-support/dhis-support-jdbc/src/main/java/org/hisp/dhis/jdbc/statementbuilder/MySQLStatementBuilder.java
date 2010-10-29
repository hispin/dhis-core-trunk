package org.hisp.dhis.jdbc.statementbuilder;

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

import static org.hisp.dhis.system.util.DateUtils.getSqlDateString;

import org.hisp.dhis.jdbc.StatementBuilder;
import org.hisp.dhis.period.Period;

/**
 * @author Lars Helge Overland
 * @version $Id: MySQLStatementBuilder.java 5715 2008-09-17 14:05:28Z larshelg $
 */
public class MySQLStatementBuilder
    implements StatementBuilder
{
    public String getDoubleColumnType()
    {
        return "DOUBLE";
    }

    public String encode( String value )
    {
        if ( value != null )
        {
            value = value.endsWith( "\\" ) ? value.substring( 0, value.length() - 1 ) : value;
            value = value.replaceAll( QUOTE, "\\\\" + QUOTE );
        }
        
        return QUOTE + value + QUOTE;
    }
    
    public String getPeriodIdentifierStatement( Period period )
    {
        return
            "SELECT periodid FROM period WHERE periodtypeid=" + period.getPeriodType().getId() + " " + 
            "AND startdate='" + getSqlDateString( period.getStartDate() ) + "' " +
            "AND enddate='" + getSqlDateString( period.getEndDate() ) + "'";
    }    

    public String getCreateAggregatedDataValueTable()
    {
        return
            "CREATE TABLE aggregateddatavalue ( " +
            "dataelementid INTEGER, " +
            "categoryoptioncomboid INTEGER, " +
            "periodid INTEGER, " +
            "organisationunitid INTEGER, " +
            "periodtypeid INTEGER, " +
            "level INTEGER, " +
            "value DOUBLE );";
    }
    
    public String getCreateAggregatedIndicatorTable()
    {
        return
            "CREATE TABLE aggregatedindicatorvalue ( " +
            "indicatorid INTEGER, " +
            "periodid INTEGER, " +
            "organisationunitid INTEGER, " +
            "periodtypeid INTEGER, " +
            "level INTEGER, " +
            "annualized VARCHAR( 10 ), " +
            "factor DOUBLE, " +
            "value DOUBLE, " +
            "numeratorvalue DOUBLE, " +
            "denominatorvalue DOUBLE );";
    }

    public String getCreateDataSetCompletenessTable()
    {
        return
            "CREATE TABLE aggregateddatasetcompleteness ( " +
            "datasetid INTEGER, " +
            "periodid INTEGER, " +
            "periodname VARCHAR( 30 ), " +
            "organisationunitid INTEGER, " +
            "reporttableid INTEGER, " +
            "sources INTEGER, " +
            "registrations INTEGER, " +
            "registrationsOnTime INTEGER, " +
            "value DOUBLE, " +
            "valueOnTime DOUBLE );";
    }

    public String getCreateDataValueIndex()
    {
        return
            "CREATE INDEX crosstab " +
            "ON datavalue ( periodid, sourceid );";
    }

    public String getDeleteZeroDataValues()
    {
        return
            "DELETE FROM datavalue " +
            "USING datavalue, dataelement " +
            "WHERE datavalue.dataelementid = dataelement.dataelementid " +
            "AND dataelement.aggregationtype = 'sum' " +
            "AND datavalue.value = '0'";
    }
    
    public int getMaximumNumberOfColumns()
    {
        return 720;
    }

    public String getDropDatasetForeignKeyForDataEntryFormTable()
    {
        return  "ALTER TABLE dataentryform DROP FOREIGN KEY fk_dataentryform_datasetid;" ;
    }

    @Override
    public String getMoveDataValueToDestination( int sourceId, int destinationId )
    {
        return "UPDATE datavalue AS d1 SET sourceid=" + destinationId + " " + "WHERE sourceid=" + sourceId + " "
        + "AND NOT EXISTS ( " + "SELECT * from ( SELECT * FROM datavalue ) AS d2 " + "WHERE d2.sourceid=" + destinationId + " "
        + "AND d1.dataelementid=d2.dataelementid " + "AND d1.periodid=d2.periodid "
        + "AND d1.categoryoptioncomboid=d2.categoryoptioncomboid );";
    }

    @Override
    public String getSummarizeDestinationAndSourceWhereMatching( int sourceId, int destId )
    {
        return "UPDATE datavalue AS d1 SET value=( " + "SELECT SUM( value ) " + "FROM (SELECT * FROM datavalue) as d2 "
            + "WHERE d1.dataelementid=d2.dataelementid " + "AND d1.periodid=d2.periodid "
            + "AND d1.categoryoptioncomboid=d2.categoryoptioncomboid " + "AND d2.sourceid IN ( " + destId + ", "
            + sourceId + " ) ) " + "WHERE d1.sourceid=" + destId + " "
            + "AND d1.dataelementid in ( SELECT dataelementid FROM dataelement WHERE valuetype='int' );";
    }

    @Override
    public String getUpdateDestination( int destDataElementId, int destCategoryOptionComboId,
        int sourceDataElementId, int sourceCategoryOptionComboId )
    {
        
        return "UPDATE datavalue d1 LEFT JOIN datavalue d2 ON d2.dataelementid = " + destDataElementId
            + " AND d2.categoryoptioncomboid = " + destCategoryOptionComboId
            + " AND d1.periodid = d2.periodid AND d1.sourceid = d2.sourceid SET d1.dataelementid = "
            + destDataElementId + ", d1.categoryoptioncomboid = " + destCategoryOptionComboId
            + " WHERE d1.dataelementid = " + sourceDataElementId + " AND d1.categoryoptioncomboid = "
            + sourceCategoryOptionComboId + " AND d2.dataelementid IS NULL";

    }

    @Override
    public String getMoveFromSourceToDestination( int destDataElementId, int destCategoryOptionComboId,
        int sourceDataElementId, int sourceCategoryOptionComboId )
    {
        return "UPDATE datavalue d1, datavalue d2 SET d1.value=d2.value,d1.storedby=d2.storedby,d1.lastupdated=d2.lastupdated,d1.comment=d2.comment,d1.followup=d2.followup "
            + "WHERE d1.periodid=d2.periodid "
            + "AND d1.sourceid=d2.sourceid "
            + "AND d1.lastupdated<d2.lastupdated "
            + "AND d1.dataelementid="
            + destDataElementId
            + " AND d1.categoryoptioncomboid="
            + destCategoryOptionComboId
            + " "
            + "AND d2.dataelementid="
            + sourceDataElementId
            + " AND d2.categoryoptioncomboid=" + sourceCategoryOptionComboId + ";";
    }
    
    public String getStandardDeviation( int dataElementId, int categoryOptionComboId, int organisationUnitId ){
    	
    	return "SELECT STDDEV( value ) FROM datavalue " +
            "WHERE dataelementid='" + dataElementId + "' " +
            "AND categoryoptioncomboid='" + categoryOptionComboId + "' " +
            "AND sourceid='" + organisationUnitId + "'";
        
    }
    
    public String getAverage( int dataElementId, int categoryOptionComboId, int organisationUnitId ){
    	 return  "SELECT AVG( value ) FROM datavalue " +
            "WHERE dataelementid='" + dataElementId + "' " +
            "AND categoryoptioncomboid='" + categoryOptionComboId + "' " +
            "AND sourceid='" + organisationUnitId + "'";
    }
    
    public String getDeflatedDataValues( int dataElementId, String dataElementName, int categoryOptionComboId,
    		String periodIds, int organisationUnitId, String organisationUnitName, int lowerBound, int upperBound ){
    	
    	return  "SELECT dv.dataelementid, dv.periodid, dv.sourceid, dv.categoryoptioncomboid, dv.value, dv.storedby, dv.lastupdated, " +
            "dv.comment, dv.followup, '" + lowerBound + "' AS minvalue, '" + upperBound + "' AS maxvalue, " +
            encode( dataElementName ) + " AS dataelementname, pt.name AS periodtypename, pe.startdate, pe.enddate, " + 
            encode( organisationUnitName ) + " AS sourcename, cc.categoryoptioncomboname " +
            "FROM datavalue AS dv " +
            "JOIN period AS pe USING (periodid) " +
            "JOIN periodtype AS pt USING (periodtypeid) " +
            "LEFT JOIN _categoryoptioncomboname AS cc USING (categoryoptioncomboid) " +
            "WHERE dv.dataelementid='" + dataElementId + "' " +
            "AND dv.categoryoptioncomboid='" + categoryOptionComboId + "' " +
            "AND dv.periodid IN (" + periodIds + ") " +
            "AND dv.sourceid='" + organisationUnitId + "' " +
            "AND ( dv.value < '" + lowerBound + "' " +
            "OR  dv.value > '" + upperBound + "' )";
   }
   
   public String archivePatientData ( String startDate, String endDate )
   {
       return "DELETE pdv FROM patientdatavalue AS pdv "
                + "INNER JOIN programstageinstance AS psi "
                +    "ON pdv.programstageinstanceid = psi.programstageinstanceid "
                + "INNER JOIN programinstance AS pi "
                +    "ON pi.programinstanceid = psi.programinstanceid "
                + "WHERE pi.enddate >= '" + startDate + "' "
                +    "AND pi.enddate <= '" +  endDate + "';";
   }
   
   public String unArchivePatientData ( String startDate, String endDate )
   {
       return "DELETE pdv FROM patientdatavaluearchive AS pdv "
               + "INNER JOIN programstageinstance AS psi "
               +    "ON pdv.programstageinstanceid = psi.programstageinstanceid "
               + "INNER JOIN programinstance AS pi "
               +    "ON pi.programinstanceid = psi.programinstanceid "
               + "WHERE pi.enddate >= '" + startDate + "' "
               +    "AND pi.enddate <= '" +  endDate + "';";
   }
   
   public String deleteRegularOverlappingPatientData(){
       return "DELETE d FROM patientdatavalue AS d " +
               "INNER JOIN patientdatavaluearchive AS a " +
               "WHERE d.programstageinstanceid=a.programstageinstanceid " +
               "AND d.dataelementid=a.dataelementid " +
               "AND d.organisationunitid=a.organisationunitid " +
               "AND d.categoryoptioncomboid=a.categoryoptioncomboid;";
  }
   
  public String deleteArchivedOverlappingPatientData()
  {
      return "DELETE a FROM patientdatavaluearchive AS a " +
              "INNER JOIN patientdatavalue AS d " +
              "WHERE d.programstageinstanceid=a.programstageinstanceid " +
              "AND d.dataelementid=a.dataelementid " +
              "AND d.organisationunitid=a.organisationunitid " +
              "AND d.categoryoptioncomboid=a.categoryoptioncomboid ";
  }
   
  public String deleteOldestOverlappingPatientDataValue(){
       return "DELETE d FROM patientdatavalue AS d " +
               "INNER JOIN patientdatavaluearchive AS a " +
               "WHERE d.programstageinstanceid=a.programstageinstanceid " +
               "AND d.dataelementid=a.dataelementid " +
               "AND d.organisationunitid=a.organisationunitid " +
               "AND d.categoryoptioncomboid=a.categoryoptioncomboid " +
               "AND d.timestamp<a.timestamp;";
   }
   
   public String deleteOldestOverlappingPatientArchiveData(){
       return "DELETE a FROM patientdatavaluearchive AS a " +
               "INNER JOIN patientdatavalue AS d " +
               "WHERE d.programstageinstanceid=a.programstageinstanceid " +
               "AND d.dataelementid=a.dataelementid " +
               "AND d.organisationunitid=a.organisationunitid " +
               "AND d.categoryoptioncomboid=a.categoryoptioncomboid " +
               "AND a.timestamp<=d.timestamp;";
   }
}
