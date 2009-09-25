package org.hisp.dhis.reportexcel.importing.action;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.reportexcel.ReportExcel;
import org.hisp.dhis.reportexcel.ReportExcelItem;
import org.hisp.dhis.reportexcel.ReportExcelService;
import org.hisp.dhis.reportexcel.ReportLocationManager;
import org.hisp.dhis.reportexcel.importing.ReportExcelItemValue;
import org.hisp.dhis.reportexcel.utils.ExcelUtils;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * @version $Id
 */

public class ViewDataAction
    implements Action
{
    // --------------------------------------------------------------------
    // Dependencies
    // --------------------------------------------------------------------

    private ReportExcelService reportExcelService;

    public void setReportExcelService( ReportExcelService reportExcelService )
    {
        this.reportExcelService = reportExcelService;
    }

    private ReportLocationManager reportLocationManager;

    public void setReportLocationManager( ReportLocationManager reportLocationManager )
    {
        this.reportLocationManager = reportLocationManager;
    }

    // --------------------------------------------------------------------
    // Getters and Setters
    // --------------------------------------------------------------------

    private Integer reportId;

    public void setReportId( Integer reportId )
    {
        this.reportId = reportId;
    }

    private String uploadFileName;

    public void setUploadFileName( String uploadFileName )
    {
        this.uploadFileName = uploadFileName;
    }

    private List<ReportExcelItemValue> reportItemValues;

    public List<ReportExcelItemValue> getReportItemValues()
    {
        return reportItemValues;
    }

    // --------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------

    public String execute()
    {
        try
        {            
            File upload = new File( reportLocationManager.getReportExcelTempDirectory() + File.separator
                + uploadFileName );
            
            Workbook templateWorkbook = Workbook.getWorkbook( upload );
            
            Sheet sheet = templateWorkbook.getSheet( 0 );

            ReportExcel report = reportExcelService.getReportExcel( reportId );
            Collection<ReportExcelItem> reportItems = report.getReportExcelItems();

            reportItemValues = new ArrayList<ReportExcelItemValue>();

            for ( ReportExcelItem reportItem : reportItems )
            {
                if ( reportItem.getItemType().equals( ReportExcelItem.TYPE.DATAELEMENT ) )
                {
                    String value = ExcelUtils.readValue( reportItem.getRow(), reportItem.getColumn(), sheet );

                    ReportExcelItemValue reportItemvalue = new ReportExcelItemValue( reportItem, value );

                    if ( value.length() == 0 )
                    {
                        reportItemvalue.setValue( 0 + "" );
                    }

                    reportItemValues.add( reportItemvalue );
                }

            }

            return SUCCESS;
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        return ERROR;
    }

}
