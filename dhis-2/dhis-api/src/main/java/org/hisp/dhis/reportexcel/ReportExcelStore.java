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
package org.hisp.dhis.reportexcel;

import java.util.Collection;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.reportexcel.status.DataEntryStatus;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */
public interface ReportExcelStore
{
    String ID = ReportExcelStore.class.getName();

    // --------------------------------------
    // Service of Report
    // --------------------------------------

    public int addReportExcel( ReportExcel report );

    public void updateReportExcel( ReportExcel report );

    public void deleteReportExcel( int id );

    public ReportExcel getReportExcel( int id );

    public ReportExcel getReportExcel( String name );

    public Collection<ReportExcel> getReportExcelsByOrganisationUnit( OrganisationUnit organisationUnit );

    public Collection<ReportExcel> getALLReportExcel();

    public Collection<String> getReportExcelGroups();

    public Collection<ReportExcel> getReportsByGroup( String group );

    // --------------------------------------
    // Service of Report Item
    // --------------------------------------

    public void addReportExcelItem( ReportExcelItem reportItem );

    public void updateReportExcelItem( ReportExcelItem reportItem );

    public void deleteReportExcelItem( int id );

    public ReportExcelItem getReportExcelItem( int id );  

    public Collection<ReportExcelItem> getALLReportExcelItem();

    public Collection<ReportExcelItem> getReportExcelItem( int sheetNo, Integer reportId );

    public Collection<Integer> getSheets( Integer reportId );

    // --------------------------------------
    // Report DataElement Order
    // --------------------------------------

    public DataElementGroupOrder getDataElementGroupOrder( Integer id );

    public void updateDataElementGroupOrder( DataElementGroupOrder dataElementGroupOrder );
    
    public void deleteDataElementGroupOrder( Integer id );

    // --------------------------------------
    // Data Entry Status
    // --------------------------------------

    public int saveDataEntryStatus( DataEntryStatus dataStatus );

    public DataEntryStatus getDataEntryStatus( int id );

    public void deleteDataEntryStatus( int id );   

    public Collection<DataEntryStatus> getALLDataEntryStatus();

    public Collection<DataEntryStatus> getDataEntryStatusDefault();    

    Collection<DataEntryStatus> getDataEntryStatusDefaultByDataSets( Collection<DataSet> dataSets );

    public int countDataValueOfDataSet( DataSet dataSet, OrganisationUnit organisationUnit, Period period );
}
