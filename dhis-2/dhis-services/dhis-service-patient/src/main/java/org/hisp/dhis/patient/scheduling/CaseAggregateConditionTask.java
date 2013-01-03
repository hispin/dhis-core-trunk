/*
 * Copyright (c) 2004-2009, University of Oslo
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

package org.hisp.dhis.patient.scheduling;

import java.util.Collection;
import java.util.Date;

import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.CalendarPeriodType;
import org.hisp.dhis.period.Period;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Chau Thu Tran
 * 
 * @version RunCaseAggregateConditionTask.java 9:52:10 AM Oct 10, 2012 $
 */
public class CaseAggregateConditionTask
    implements Runnable
{
    public static final String STORED_BY_DHIS_SYSTEM = "DHIS-System";

    private OrganisationUnitService organisationUnitService;

    private CaseAggregationConditionService aggregationConditionService;

    private DataValueService dataValueService;

    private JdbcTemplate jdbcTemplate;

    private DataElementService dataElementService;

    private DataElementCategoryService categoryService;

    private DataSetService dataSetService;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CaseAggregateConditionTask( OrganisationUnitService organisationUnitService,
        CaseAggregationConditionService aggregationConditionService, DataValueService dataValueService,
        JdbcTemplate jdbcTemplate, DataElementService dataElementService, DataElementCategoryService categoryService,
        DataSetService dataSetService )
    {
        this.organisationUnitService = organisationUnitService;
        this.aggregationConditionService = aggregationConditionService;
        this.dataValueService = dataValueService;
        this.jdbcTemplate = jdbcTemplate;
        this.dataElementService = dataElementService;
        this.categoryService = categoryService;
        this.dataSetService = dataSetService;
    }

    // -------------------------------------------------------------------------
    // Runnable implementation
    // -------------------------------------------------------------------------

    @Override
    public void run()
    {
        Collection<OrganisationUnit> orgunits = organisationUnitService.getAllOrganisationUnits();

        Collection<DataSet> dataSets = dataSetService.getAllDataSets();

        for ( DataSet dataSet : dataSets )
        {
            Period period = getPeriod( dataSet.getPeriodType().getName() );

            if ( period != null )
            {
                String sql = "select caseaggregationconditionid, aggregationdataelementid, optioncomboid "
                    + "from caseaggregationcondition cagg inner join datasetmembers dm "
                    + "on cagg.aggregationdataelementid=dm.dataelementid " + "inner join dataset ds "
                    + "on ds.datasetid = dm.datasetid " + "inner join periodtype pt "
                    + "on pt.periodtypeid=ds.periodtypeid " + "where ds.datasetid = " + dataSet.getId();

                SqlRowSet rs = jdbcTemplate.queryForRowSet( sql );

                while ( rs.next() )
                {
                    // -------------------------------------------------------------
                    // Get formula, agg-dataelement and option-combo
                    // -------------------------------------------------------------

                    int dataelementId = rs.getInt( "aggregationdataelementid" );
                    int optionComboId = rs.getInt( "optioncomboid" );

                    DataElement dElement = dataElementService.getDataElement( dataelementId );
                    DataElementCategoryOptionCombo optionCombo = categoryService
                        .getDataElementCategoryOptionCombo( optionComboId );

                    CaseAggregationCondition aggCondition = aggregationConditionService.getCaseAggregationCondition( rs
                        .getInt( "caseaggregationconditionid" ) );

                    // ---------------------------------------------------------------------
                    // Aggregation
                    // ---------------------------------------------------------------------

                    for ( OrganisationUnit orgUnit : orgunits )
                    {
                        DataValue dataValue = dataValueService.getDataValue( orgUnit, dElement, period, optionCombo );

                        Integer resultValue = aggregationConditionService.parseConditition( aggCondition, orgUnit,
                            period );

                        if ( resultValue != null && resultValue != 0 )
                        {
                            // -----------------------------------------------------
                            // Add dataValue
                            // -----------------------------------------------------
                            if ( dataValue == null )
                            {
                                dataValue = new DataValue( dElement, period, orgUnit, "" + resultValue, "", new Date(),
                                    null, optionCombo );
                                dataValueService.addDataValue( dataValue );
                            }

                            // -----------------------------------------------------
                            // Update dataValue
                            // -----------------------------------------------------
                            else if ( (double) resultValue != Double.parseDouble( dataValue.getValue() ) )
                            {
                                dataValue.setValue( "" + resultValue );
                                dataValue.setTimestamp( new Date() );
                                sql = "UPDATE datavalue" + " SET value='" + resultValue + "',lastupdated='"
                                    + new Date() + "' where dataelementId=" + dataelementId + " and periodid="
                                    + period.getId() + " and sourceid=" + orgUnit.getId()
                                    + " and categoryoptioncomboid=" + optionComboId + " and storedby='"
                                    + STORED_BY_DHIS_SYSTEM + "'";
                                jdbcTemplate.execute( sql );
                            }
                        }

                        // ---------------------------------------------------------
                        // Delete dataValue
                        // ---------------------------------------------------------
                        else if ( dataValue != null )
                        {
                            dataValueService.deleteDataValue( dataValue );
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private Period getPeriod( String periodTypeName )
    {
        Date date = new Date();

        CalendarPeriodType periodType = (CalendarPeriodType) CalendarPeriodType.getPeriodTypeByName( periodTypeName );

        Period period = periodType.createPeriod( date );

        return (period.getEndDate().equals( date ) || period.getEndDate().before( date )) ? period : null;
    }
}
