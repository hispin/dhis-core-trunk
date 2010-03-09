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

import java.util.Map;

import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.reporttable.ReportTableData;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public interface ReportTableManager
{
    String ID = ReportTableManager.class.getName();
    
    /**
     * Creates a report table in the database.
     * 
     * @param reportTable the report table to create.
     */
    void createReportTable( ReportTable reportTable );
    
    /**
     * Deletes a ReportTable.
     * 
     * @param reportTable the ReportTable to delete.
     */
    void removeReportTable( ReportTable reportTable );
    
    /**
     * Tests whether the report table has been generated in the database.
     * 
     * @param reportTable the ReportTable.
     * @return true if the report table has been generated, false it not.
     */
    boolean reportTableIsGenerated( ReportTable reportTable );
    
    /**
     * Returns an AggregatedValueMap.
     * 
     * @param reportTable the ReportTable.
     * @param indicator the Indicator.
     * @param categoryOptionCombo the DataElementCategoryOptionCombo.
     * @param period the Period.
     * @param unit the OrganisationUnit.
     * @return a Map with String as keys and Double as values.
     */
    Map<String, Double> getAggregatedValueMap( ReportTable reportTable, IdentifiableObject indicator, 
        DataElementCategoryOptionCombo categoryOptionCombo, Period period, OrganisationUnit unit );
    
    /**
     * Returns a ReportTableData object based on the registered ReportTableColumns
     * for the given ReportTable.
     *  
     * @param reportTable the ReportTable.
     * @return a ReportTableData object.
     */
    ReportTableData getDisplayReportTableData( ReportTable reportTable );
    
    /**
     * Returns a ReportTableData object for the given ReportTable.
     * 
     * @param reportTable the ReportTable.
     * @return a ReportTableData object.
     */
    ReportTableData getReportTableData( ReportTable reportTable );
}
