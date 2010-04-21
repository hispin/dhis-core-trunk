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

package org.hisp.dhis.patient.action.patient;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientAttributeService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patient.paging.PagingUtil;
import org.hisp.dhis.patient.paging.RequestUtil;
import org.hisp.dhis.patient.state.SelectedStateManager;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class SearchPatientAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitSelectionManager selectionManager;

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    private SelectedStateManager selectedStateManager;

    public void setSelectedStateManager( SelectedStateManager selectedStateManager )
    {
        this.selectedStateManager = selectedStateManager;
    }

    private PatientService patientService;

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    private PatientAttributeService patientAttributeService;

    public void setPatientAttributeService( PatientAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
    }

    private PatientAttributeValueService patientAttributeValueService;

    public void setPatientAttributeValueService( PatientAttributeValueService patientAttributeValueService )
    {
        this.patientAttributeValueService = patientAttributeValueService;
    }

    // -------------------------------------------------------------------------
    // Input/output
    // -------------------------------------------------------------------------

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private String searchText;

    public void setSearchText( String searchText )
    {
        this.searchText = searchText;
    }

    public String getSearchText()
    {
        return searchText;
    }

    private boolean listAll;

    public void setListAll( boolean listAll )
    {
        this.listAll = listAll;
    }

    private Integer searchingAttributeId;

    public Integer getSearchingAttributeId()
    {
        return searchingAttributeId;
    }

    public void setSearchingAttributeId( Integer searchingAttributeId )
    {
        this.searchingAttributeId = searchingAttributeId;
    }

    Collection<PatientAttribute> patientAttributes;

    public Collection<PatientAttribute> getPatientAttributes()
    {
        return patientAttributes;
    }

    private Collection<Patient> patients = new ArrayList<Patient>();

    public Collection<Patient> getPatients()
    {
        return patients;
    }
    
    private Integer currentPage;
    
    public void setCurrentPage( Integer currentPage )
    {
        this.currentPage = currentPage;
    }

    private Integer pageSize;
    
    public void setPageSize( Integer pageSize )
    {
        this.pageSize = pageSize;
    }
    
    private Integer defaultPageSize = 50;
    
    public void setDefaultPageSize( Integer defaultPageSize )
    {
        this.defaultPageSize = defaultPageSize;
    }
    
    private PagingUtil pagingUtil;
    
    public PagingUtil getPagingUtil()
    {
        return pagingUtil;
    }
    
    private Integer total;
    
    public Integer getTotal()
    {
        return total;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        // ---------------------------------------------------------------------
        // Validate selected OrganisationUnit
        // ---------------------------------------------------------------------

        organisationUnit = selectionManager.getSelectedOrganisationUnit();

        patientAttributes = patientAttributeService.getAllPatientAttributes();

        if ( listAll )
        {
            selectedStateManager.setListAll( listAll );

            selectedStateManager.clearSearchingAttributeId();
            selectedStateManager.clearSearchTest();
            
            pagingUtil = new PagingUtil( RequestUtil.getCurrentLink(ServletActionContext.getRequest()) + "?listAll=true", pageSize == null ? defaultPageSize : pageSize );
            
            pagingUtil.setCurrentPage( currentPage == null ? 0 : currentPage );
            
            total = patientService.countGetPatientsByOrgUnit( organisationUnit );
            
            pagingUtil.setTotal( total );
            
            patients = patientService.getPatientsByOrgUnit( organisationUnit , pagingUtil.getStartPos(), pagingUtil.getPageSize() );
            
            searchText = "list_all_patients";

            return SUCCESS;
        }

        if ( searchingAttributeId != null && searchText != null )
        {
            selectedStateManager.clearListAll();

            selectedStateManager.setSearchingAttributeId( searchingAttributeId );
            selectedStateManager.setSearchText( searchText );

            PatientAttribute patientAttribute = patientAttributeService.getPatientAttribute( searchingAttributeId );
            
            pagingUtil = new PagingUtil( RequestUtil.getCurrentLink(ServletActionContext.getRequest()) 
                + "?searchingAttributeId=" + searchingAttributeId + "&searchText=" + searchText
                , pageSize == null ? defaultPageSize : pageSize );
            
            pagingUtil.setCurrentPage( currentPage == null ? 0 : currentPage );
            
            total = patientAttributeValueService.countSearchPatientAttributeValue( patientAttribute, searchText );
            
            pagingUtil.setTotal( total );

            patients = patientAttributeValueService.searchPatientAttributeValue(
                patientAttribute, searchText, pagingUtil.getStartPos(), pagingUtil.getPageSize() );

            return SUCCESS;
        }

        if ( searchingAttributeId == null && searchText != null )
        {
            selectedStateManager.clearListAll();
            selectedStateManager.clearSearchingAttributeId();

            selectedStateManager.setSearchText( searchText );
            
            pagingUtil = new PagingUtil( RequestUtil.getCurrentLink(ServletActionContext.getRequest()) + "?searchText=" + searchText, pageSize == null ? defaultPageSize : pageSize );
            
            pagingUtil.setCurrentPage( currentPage == null ? 0 : currentPage );
            
            total = patientService.countGetPatients( searchText );
            
            pagingUtil.setTotal( total );
            
            patients = patientService.getPatients( searchText, pagingUtil.getStartPos(), pagingUtil.getPageSize() );

            return SUCCESS;
        }

        listAll = selectedStateManager.getListAll();
        
        if ( listAll )
        {
            selectedStateManager.setListAll( listAll );

            selectedStateManager.clearSearchingAttributeId();
            selectedStateManager.clearSearchTest();
            
            pagingUtil = new PagingUtil( RequestUtil.getCurrentLink(ServletActionContext.getRequest()), pageSize == null ? defaultPageSize : pageSize );
            
            pagingUtil.setCurrentPage( currentPage == null ? 0 : currentPage );
            
            total = patientService.countGetPatientsByOrgUnit( organisationUnit );
            
            pagingUtil.setTotal( total );
            
            patients = patientService.getPatientsByOrgUnit( organisationUnit , pagingUtil.getStartPos(), pagingUtil.getPageSize() );
            
            searchText = "list_all_patients";

            return SUCCESS;

        }
        
        searchingAttributeId = selectedStateManager.getSearchingAttributeId();
        searchText = selectedStateManager.getSearchText();
        
        if ( searchingAttributeId != null && searchText != null )
        {
            PatientAttribute patientAttribute = patientAttributeService.getPatientAttribute( searchingAttributeId );
            
            pagingUtil = new PagingUtil( RequestUtil.getCurrentLink(ServletActionContext.getRequest()) 
                + "?searchingAttributeId=" + searchingAttributeId + "&searchText=" + searchText
                , pageSize == null ? defaultPageSize : pageSize );
            
            pagingUtil.setCurrentPage( currentPage == null ? 0 : currentPage );
            
            total = patientAttributeValueService.countSearchPatientAttributeValue( patientAttribute, searchText );
            
            pagingUtil.setTotal( total );

            patients = patientAttributeValueService.searchPatientAttributeValue(
                patientAttribute, searchText, pagingUtil.getStartPos(), pagingUtil.getPageSize() );
        }
        
        
        pagingUtil = new PagingUtil( RequestUtil.getCurrentLink(ServletActionContext.getRequest()) + "?searchText=" + searchText, pageSize == null ? defaultPageSize : pageSize );
        
        pagingUtil.setCurrentPage( currentPage == null ? 0 : currentPage );
        
        total = patientService.countGetPatients( searchText );
        
        pagingUtil.setTotal( total );
        
        patients = patientService.getPatients( searchText, pagingUtil.getStartPos(), pagingUtil.getPageSize() );
        
        
        return SUCCESS;

    }

}
