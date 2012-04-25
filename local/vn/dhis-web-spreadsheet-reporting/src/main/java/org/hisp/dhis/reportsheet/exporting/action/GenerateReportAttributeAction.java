package org.hisp.dhis.reportsheet.exporting.action;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hisp.dhis.expression.Expression.SEPARATOR;

import org.apache.poi.ss.usermodel.Sheet;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementFormNameComparator;
import org.hisp.dhis.dataelement.LocalDataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.reportsheet.AttributeValueGroupOrder;
import org.hisp.dhis.reportsheet.ExportItem;
import org.hisp.dhis.reportsheet.ExportReport;
import org.hisp.dhis.reportsheet.ExportReportAttribute;
import org.hisp.dhis.reportsheet.exporting.AbstractGenerateExcelReportSupport;
import org.hisp.dhis.reportsheet.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Dang Duy Hieu
 * @version $Id$
 */
public class GenerateReportAttributeAction
    extends AbstractGenerateExcelReportSupport
{
    @Autowired
    private LocalDataElementService localDataElementService;

    @Override
    protected void executeGenerateOutputFile( ExportReport exportReport, Period period )
        throws Exception
    {
        OrganisationUnit organisationUnit = organisationUnitSelectionManager.getSelectedOrganisationUnit();

        ExportReportAttribute exportReportInstance = (ExportReportAttribute) exportReport;

        this.installReadTemplateFile( exportReportInstance, period, organisationUnit );

        DataElementCategoryOptionCombo defaultOptionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        for ( Integer sheetNo : exportReportService.getSheets( selectionManager.getSelectedReportId() ) )
        {
            Sheet sheet = this.templateWorkbook.getSheetAt( sheetNo - 1 );

            Collection<ExportItem> exportReportItems = exportReportInstance.getExportItemBySheet( sheetNo );

            this.generateOutPutFile( defaultOptionCombo, exportReportInstance, exportReportItems, organisationUnit,
                sheet );
        }
    }

    // -------------------------------------------------------------------------
    // Supportive method
    // -------------------------------------------------------------------------

    private void generateOutPutFile( DataElementCategoryOptionCombo optionCombo, ExportReportAttribute exportReport,
        Collection<ExportItem> exportReportItems, OrganisationUnit organisationUnit, Sheet sheet )
    {
        boolean flag = false;
        int rowBegin = 0;

        for ( AttributeValueGroupOrder avgOrder : exportReport.getAttributeValueOrders() )
        {
            int serial = 1;
            DataElement de = null;
            List<DataElement> dataElements = null;

            flag = true;

            for ( String avalue : avgOrder.getAttributeValues() )
            {
                dataElements = new ArrayList<DataElement>( localDataElementService.getDataElementsByAttribute( avgOrder
                    .getAttribute(), avalue ) );

                Collections.sort( dataElements, new DataElementFormNameComparator() );

                for ( ExportItem exportItem : exportReportItems )
                {
                    rowBegin = (rowBegin == 0 ? exportItem.getRow() : exportItem.getRow() + rowBegin - 1);
                    // int beginChapter = rowBegin;

                    if ( flag )
                    {
                        if ( exportItem.getItemType().equalsIgnoreCase( ExportItem.TYPE.DATAELEMENT_NAME ) )
                        {
                            ExcelUtils.writeValueByPOI( rowBegin, exportItem.getColumn(), avgOrder.getName(),
                                ExcelUtils.TEXT, sheet, this.csText12BoldCenter );
                        }

                        rowBegin++;
                    }

                    if ( exportItem.getItemType().equalsIgnoreCase( ExportItem.TYPE.DATAELEMENT_NAME ) )
                    {
                        ExcelUtils.writeValueByPOI( rowBegin, exportItem.getColumn(), avalue, ExcelUtils.TEXT, sheet,
                            this.csText10Bold );
                    }
                    else if ( exportItem.getItemType().equalsIgnoreCase( ExportItem.TYPE.SERIAL ) )
                    {
                        ExcelUtils.writeValueByPOI( rowBegin, exportItem.getColumn(), serial + "", ExcelUtils.NUMBER,
                            sheet, this.csTextSerial );
                    }
                    else
                    {
                        ExportItem newExportItem = new ExportItem();

                        de = dataElements.get( Integer.parseInt( exportItem.getExtraExpression() ) - 1 );

                        if ( de != null )
                        {
                            newExportItem.setExpression( de.getId() + SEPARATOR + optionCombo.getId() );

                            double value = this.getDataValue( newExportItem, organisationUnit );

                            ExcelUtils.writeValueByPOI( rowBegin, exportItem.getColumn(), value + "",
                                ExcelUtils.NUMBER, sheet, this.csNumber );
                        }
                    }
                }

                flag = false;
                rowBegin++;
                serial++;

                // if ( exportItem.getItemType().equalsIgnoreCase(
                // ExportItem.TYPE.DATAELEMENT ) )
                // {
                // String columnName = ExcelUtils.convertColumnNumberToName(
                // exportItem.getColumn() );
                // String formula = "SUM(" + columnName + (beginChapter + 1) +
                // ":" + columnName + (rowBegin - 1) + ")";
                //
                // ExcelUtils.writeFormulaByPOI( beginChapter,
                // exportItem.getColumn(), formula, sheet, this.csFormula );
                // }
            }
        }
    }
}
