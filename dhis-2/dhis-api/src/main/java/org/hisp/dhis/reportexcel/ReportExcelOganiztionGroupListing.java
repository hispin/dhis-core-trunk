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

package org.hisp.dhis.reportexcel;

import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Chau Thu Tran
 */

public class ReportExcelOganiztionGroupListing
    extends ReportExcel
{
    public static final String ORGANISATION_UNIT_GROUP_TAG = "organisationunit-groups";

    public static final String ID = "id";

    private List<OrganisationUnitGroup> organisationUnitGroups;
    
    private Map<OrganisationUnitGroup, OrganisationUnitLevel> organisationUnitLevels;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ReportExcelOganiztionGroupListing()
    {
        super();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------
    
    

    public List<OrganisationUnitGroup> getOrganisationUnitGroups()
    {
        return organisationUnitGroups;
    }

    public Map<OrganisationUnitGroup, OrganisationUnitLevel> getOrganisationUnitLevels() {
		return organisationUnitLevels;
	}

	public void setOrganisationUnitLevels(
			Map<OrganisationUnitGroup, OrganisationUnitLevel> organisationUnitLevels) {
		this.organisationUnitLevels = organisationUnitLevels;
	}

	public void setOrganisationUnitGroups( List<OrganisationUnitGroup> organisationUnitGroups )
    {
        this.organisationUnitGroups = organisationUnitGroups;
    }

    @Override
    public String getReportType()
    {
        return ReportExcel.TYPE.ORGANIZATION_GROUP_LISTING;
    }

    @Override
    public Document createDocument()
        throws ParserConfigurationException
    {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.newDocument();

        Element root = document.createElement( ReportExcel.XML_TAG.REPORT_EXCEL );
        Element name = document.createElement( ReportExcel.XML_TAG.NAME );
        name.appendChild( document.createTextNode( super.getName() ) );

        Element periodRow = document.createElement( ReportExcel.XML_TAG.PERIOD_ROW );
        periodRow.appendChild( document.createTextNode( String.valueOf( super.getPeriodRow() ) ) );

        Element periodColumn = document.createElement( ReportExcel.XML_TAG.PERIOD_COLUMN );
        periodColumn.appendChild( document.createTextNode( String.valueOf( super.getPeriodColumn() ) ) );

        Element organisationRow = document.createElement( ReportExcel.XML_TAG.ORGANISATIONUNIT_ROW );
        organisationRow.appendChild( document.createTextNode( String.valueOf( super.getOrganisationRow() ) ) );

        Element organisationColumn = document.createElement( ReportExcel.XML_TAG.ORGANISATIONUNIT_COLUMN );
        organisationColumn.appendChild( document.createTextNode( String.valueOf( super.getOrganisationColumn() ) ) );

        Element group = document.createElement( ReportExcel.XML_TAG.GROUP );
        group.appendChild( document.createTextNode( String.valueOf( super.getGroup() ) ) );

        Element excelTemplateFile = document.createElement( ReportExcel.XML_TAG.EXCEL_FILE );
        excelTemplateFile.appendChild( document.createTextNode( String.valueOf( super.getExcelTemplateFile() ) ) );

        Element reportExcelType = document.createElement( ReportExcel.XML_TAG.EXCEL_REPORT_TYPE );
        reportExcelType.appendChild( document.createTextNode( String.valueOf( this.getReportType() ) ) );

        root.appendChild( name );
        root.appendChild( periodRow );
        root.appendChild( periodColumn );
        root.appendChild( organisationRow );
        root.appendChild( organisationColumn );
        root.appendChild( group );
        root.appendChild( excelTemplateFile );
        root.appendChild( reportExcelType );

        Element reportItems = document.createElement( ReportExcelItem.XML_TAG.REPORT_ITEMS );
        root.appendChild( reportItems );

        // Create Elements of Items
        for ( ReportExcelItem item : super.getReportExcelItems() )
        {
            Element reportItem = document.createElement( ReportExcelItem.XML_TAG.REPORT_ITEM );

            Element iName = document.createElement( ReportExcelItem.XML_TAG.NAME );
            iName.appendChild( document.createTextNode( String.valueOf( item.getName() ) ) );

            Element iColumn = document.createElement( ReportExcelItem.XML_TAG.COLUMN );
            iColumn.appendChild( document.createTextNode( String.valueOf( item.getColumn() ) ) );

            Element iRow = document.createElement( ReportExcelItem.XML_TAG.ROW );
            iRow.appendChild( document.createTextNode( String.valueOf( item.getRow() ) ) );

            Element iSheetNo = document.createElement( ReportExcelItem.XML_TAG.SHEET_NO );
            iSheetNo.appendChild( document.createTextNode( String.valueOf( item.getSheetNo() ) ) );

            Element iExpression = document.createElement( ReportExcelItem.XML_TAG.EXPRESSION );
            iExpression.appendChild( document.createTextNode( String.valueOf( item.getExpression() ) ) );

            Element iItemType = document.createElement( ReportExcelItem.XML_TAG.TYPE );
            iItemType.appendChild( document.createTextNode( String.valueOf( item.getItemType() ) ) );

            Element iPeriodType = document.createElement( ReportExcelItem.XML_TAG.PERIOD_TYPE );
            iPeriodType.appendChild( document.createTextNode( String.valueOf( item.getPeriodType() ) ) );

            reportItem.appendChild( iName );
            reportItem.appendChild( iColumn );
            reportItem.appendChild( iRow );
            reportItem.appendChild( iSheetNo );
            reportItem.appendChild( iExpression );
            reportItem.appendChild( iItemType );
            reportItem.appendChild( iPeriodType );

            reportItems.appendChild( reportItem );
        }

        Element organisationUnitGroups = document.createElement( ORGANISATION_UNIT_GROUP_TAG );
        root.appendChild( organisationUnitGroups );

        for ( OrganisationUnitGroup o : this.organisationUnitGroups )
        {
            Element id = document.createElement( ID );
            id.appendChild( document.createTextNode( String.valueOf( o.getId() ) ) );
            organisationUnitGroups.appendChild( id );
        }

        document.appendChild( root );

        return document;
    }

    @Override
    public boolean isCategory()
    {
        return false;
    }

    @Override
    public boolean isNormal()
    {
        return false;
    }

    @Override
    public boolean isOrganisationUnitGroupListing()
    {
        return true;
    }

    @Override
    public boolean isPeriodColumnListing()
    {
        return false;
    }
}
