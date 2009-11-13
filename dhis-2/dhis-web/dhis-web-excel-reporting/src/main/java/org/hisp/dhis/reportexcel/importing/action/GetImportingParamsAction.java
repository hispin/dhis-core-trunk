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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.reportexcel.action.ActionSupport;
import org.hisp.dhis.reportexcel.excelitem.ExcelItemGroup;
import org.hisp.dhis.reportexcel.excelitem.ExcelItemService;
import org.hisp.dhis.reportexcel.state.SelectionManager;

/**
 * @author Chau Thu Tran
 * @version $Id$
 */
public class GetImportingParamsAction
    extends ActionSupport
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitSelectionManager organisationUnitSelectionManager;

    private ExcelItemService excelItemService;

    private SelectionManager selectionManager;

    // -------------------------------------------------------------------------
    // Inputs && Outputs
    // -------------------------------------------------------------------------

    private OrganisationUnit organisationUnit;

    private List<ExcelItemGroup> excelItemGroups;

    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public void setSelectionManager( SelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public void setExcelItemService( ExcelItemService excelItemService )
    {

        this.excelItemService = excelItemService;

    }

    public void setOrganisationUnitSelectionManager( OrganisationUnitSelectionManager organisationUnitSelectionManager )
    {
        this.organisationUnitSelectionManager = organisationUnitSelectionManager;

    }

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    public Collection<ExcelItemGroup> getExcelItemGroups()
    {
        return excelItemGroups;
    }

    private File fileExcel;

    public File getFileExcel()
    {
        return fileExcel;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {

        // ---------------------------------------------------------
        // Get File Excel
        // ---------------------------------------------------------

        String path = selectionManager.getUploadFilePath();

        if ( path != null && path != "" )
        {

            fileExcel = new File( path );

        }

        // ---------------------------------------------------------------------
        // Validate selected OrganisationUnit
        // ---------------------------------------------------------------------

        organisationUnit = organisationUnitSelectionManager.getSelectedOrganisationUnit();

        if ( organisationUnit == null )
        {

            return SUCCESS;
        }

        // ---------------------------------------------------------------------
        // Load and sort ExcelItemGroups
        // ---------------------------------------------------------------------

        excelItemGroups = new ArrayList<ExcelItemGroup>( excelItemService
            .getExcelItemGroupsByOrganisationUnit( organisationUnit ) );

        Collections.sort( excelItemGroups, new Comparator<ExcelItemGroup>()
        {

            @Override
            public int compare( ExcelItemGroup arg0, ExcelItemGroup arg1 )
            {
                return arg0.getName().compareTo( arg1.getName() );
            }
        } );

        return SUCCESS;
    }
}
