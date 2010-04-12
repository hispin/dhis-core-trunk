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
package org.hisp.dhis.reportexcel.export.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.organisationunit.comparator.OrganisationUnitNameComparator;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.reportexcel.ReportExcelItem;
import org.hisp.dhis.reportexcel.ReportExcelOganiztionGroupListing;
import org.hisp.dhis.reportexcel.utils.ExcelUtils;

/**
 * @author Tran Thanh Tri
 * @author Dang Duy Hieu
 * @version $Id$
 * @since 2009-09-18
 */
public class GenerateReportOrganizationGroupListingAction
    extends GenerateReportSupport
{

    // -------------------------------------------
    // Dependency
    // -------------------------------------------

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    @Override
    public String execute()
        throws Exception
    {
        statementManager.initialise();

        OrganisationUnit organisationUnit = organisationUnitSelectionManager.getSelectedOrganisationUnit();
        Period period = periodDatabaseService.getSelectedPeriod();
        this.installPeriod( period );

        ReportExcelOganiztionGroupListing reportExcel = (ReportExcelOganiztionGroupListing) reportService
            .getReportExcel( selectionManager.getSelectedReportId() );

        Map<OrganisationUnitGroup, OrganisationUnitLevel> orgUniGroupAtLevels = new HashMap<OrganisationUnitGroup, OrganisationUnitLevel>(
            reportExcel.getOrganisationUnitLevels() );

        this.installReadTemplateFile( reportExcel, period, organisationUnit );

        for ( Integer sheetNo : reportService.getSheets( selectionManager.getSelectedReportId() ) )
        {
            Sheet sheet = this.templateWorkbook.getSheetAt( sheetNo - 1 );

            Collection<ReportExcelItem> reportExcelItems = reportExcel.getReportItemBySheet( sheetNo );

            this.generateOutPutFile( reportExcel, orgUniGroupAtLevels, reportExcelItems, organisationUnit, sheet );

        }

        for ( Integer sheetNo : reportService.getSheets( selectionManager.getSelectedReportId() ) )
        {
            Sheet sheet = this.templateWorkbook.getSheetAt( sheetNo - 1 );

            this.recalculatingFormula( sheet );
        }

        this.complete();

        statementManager.destroy();

        return SUCCESS;
    }

    private void generateOutPutFile( ReportExcelOganiztionGroupListing reportExcel,
        Map<OrganisationUnitGroup, OrganisationUnitLevel> orgUniGroupAtLevels,
        Collection<ReportExcelItem> reportExcelItems, OrganisationUnit organisationUnit, Sheet sheet )
    {

        for ( ReportExcelItem reportItem : reportExcelItems )
        {
            int iRow = 0;
            int iCol = 0;
            int chapperNo = 0;
            int rowBegin = reportItem.getRow();

            for ( OrganisationUnitGroup organisationUnitGroup : reportExcel.getOrganisationUnitGroups() )
            {

                OrganisationUnitLevel organisationUnitLevel = orgUniGroupAtLevels.get( organisationUnitGroup );

                List<OrganisationUnit> organisationUnitsAtLevel = new ArrayList<OrganisationUnit>();

                List<OrganisationUnit> childrenOrganisationUnits = new ArrayList<OrganisationUnit>( organisationUnit
                    .getChildren() );

                List<OrganisationUnit> organisationUnits = new ArrayList<OrganisationUnit>( organisationUnitGroup
                    .getMembers() );

                if ( organisationUnitLevel != null )
                {
                    organisationUnitsAtLevel = new ArrayList<OrganisationUnit>( organisationUnitService
                        .getOrganisationUnitsAtLevel( organisationUnitLevel.getLevel(), organisationUnit ) );

                    organisationUnits.retainAll( organisationUnitsAtLevel );

                }
                else
                {

                    organisationUnits.retainAll( childrenOrganisationUnits );
                }

                Collections.sort( organisationUnits, new OrganisationUnitNameComparator() );

                int beginChapter = rowBegin;

                if ( reportItem.getItemType().equalsIgnoreCase( ReportExcelItem.TYPE.ORGANISATION )
                    && (!organisationUnits.isEmpty()) )
                {
                    ExcelUtils.writeValueByPOI( rowBegin, reportItem.getColumn(), String.valueOf( organisationUnitGroup
                        .getName() ), ExcelUtils.TEXT, sheet, this.csText12BoldCenter );
                }
                else if ( reportItem.getItemType().equalsIgnoreCase( ReportExcelItem.TYPE.SERIAL )
                    && (!organisationUnits.isEmpty()) )
                {
                    ExcelUtils.writeValueByPOI( rowBegin, reportItem.getColumn(), chappter[chapperNo], ExcelUtils.TEXT,
                        sheet, this.csText12BoldCenter );
                }
                chapperNo++;
                rowBegin++;
                int serial = 1;

                for ( OrganisationUnit o : organisationUnits )
                {
                    if ( reportItem.getItemType().equalsIgnoreCase( ReportExcelItem.TYPE.ORGANISATION ) )
                    {
                        ExcelUtils.writeValueByPOI( rowBegin, reportItem.getColumn(), o.getName(), ExcelUtils.TEXT,
                            sheet, this.csText10Bold );
                    }
                    else if ( reportItem.getItemType().equalsIgnoreCase( ReportExcelItem.TYPE.SERIAL ) )
                    {
                        ExcelUtils.writeValueByPOI( rowBegin, reportItem.getColumn(), String.valueOf( serial ),
                            ExcelUtils.NUMBER, sheet, this.csTextSerial );
                    }
                    else if ( reportItem.getItemType().equalsIgnoreCase( ReportExcelItem.TYPE.DATAELEMENT ) )
                    {
                        double value = this.getDataValue( reportItem, o );

                        ExcelUtils.writeValueByPOI( rowBegin, reportItem.getColumn(), String.valueOf( value ),
                            ExcelUtils.NUMBER, sheet, this.csNumber );
                    }
                    else if ( reportItem.getItemType().equalsIgnoreCase( ReportExcelItem.TYPE.INDICATOR ) )
                    {
                        double value = this.getIndicatorValue( reportItem, o );

                        ExcelUtils.writeValueByPOI( rowBegin, reportItem.getColumn(), String.valueOf( value ),
                            ExcelUtils.NUMBER, sheet, this.csNumber );
                    }
                    else if ( reportItem.getItemType().equalsIgnoreCase( ReportExcelItem.TYPE.FORMULA_EXCEL ) )
                    {
                        ExcelUtils.writeFormulaByPOI( rowBegin, reportItem.getColumn(), ExcelUtils
                            .checkingExcelFormula( reportItem.getExpression(), iRow, iCol ), sheet, this.csFormula );
                    }

                    rowBegin++;
                    serial++;
                    iRow++;
                }

                if ( reportItem.getItemType().equalsIgnoreCase( ReportExcelItem.TYPE.DATAELEMENT )
                    && (!organisationUnits.isEmpty()) )
                {
                    String columnName = ExcelUtils.convertColNumberToColName( reportItem.getColumn() );
                    String formula = "SUM(" + columnName + (beginChapter + 1) + ":" + columnName + (rowBegin - 1) + ")";
                    ExcelUtils.writeFormulaByPOI( beginChapter, reportItem.getColumn(), formula, sheet, this.csFormula );
                }

            }

        }
    }
}
