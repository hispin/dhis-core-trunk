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

package org.hisp.dhis.patient.action.relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientAttributeService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patient.state.SelectedStateManager;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class ShowAddRelationshipFormAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private RelationshipTypeService relationshipTypeService;

    public void setRelationshipTypeService( RelationshipTypeService relationshipTypeService )
    {
        this.relationshipTypeService = relationshipTypeService;
    }

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
    // Input/Output
    // -------------------------------------------------------------------------

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private Patient patient;

    public Patient getPatient()
    {
        return patient;
    }

    public void setPatient( Patient patient )
    {
        this.patient = patient;
    }

    private Collection<Patient> patients = new ArrayList<Patient>();

    public Collection<Patient> getPatients()
    {
        return patients;
    }

    Collection<PatientAttribute> patientAttributes;

    public Collection<PatientAttribute> getPatientAttributes()
    {
        return patientAttributes;
    }

    private String relationshipTypeId;

    public String getRelationshipTypeId()
    {
        return relationshipTypeId;
    }

    public void setRelationshipTypeId( String relationshipTypeId )
    {
        this.relationshipTypeId = relationshipTypeId;
    }

    private List<RelationshipType> relationshipTypes;

    public List<RelationshipType> getRelationshipTypes()
    {
        return relationshipTypes;
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

    private Integer searchingAttributeId;

    public Integer getSearchingAttributeId()
    {
        return searchingAttributeId;
    }

    public void setSearchingAttributeId( Integer searchingAttributeId )
    {
        this.searchingAttributeId = searchingAttributeId;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {

        patient = selectedStateManager.getSelectedPatient();

        relationshipTypes = new ArrayList<RelationshipType>( relationshipTypeService.getAllRelationshipTypes() );

        organisationUnit = selectionManager.getSelectedOrganisationUnit();

        patientAttributes = patientAttributeService.getAllPatientAttributes();

        if ( searchingAttributeId != null && searchText != null )
        {
            searchText = searchText.trim();

            if ( searchText.length() > 0 )
            {
                PatientAttribute patientAttribute = patientAttributeService.getPatientAttribute( searchingAttributeId );

                Collection<PatientAttributeValue> matching = patientAttributeValueService.searchPatientAttributeValue(
                    patientAttribute, searchText );

                for ( PatientAttributeValue patientAttributeValue : matching )
                {
                    patients.add( patientAttributeValue.getPatient() );
                }

                patients.remove( patient );

                return SUCCESS;
            }

        }

        if ( searchText != null )
        {
            searchText = searchText.trim();

            if ( searchText.length() > 0 )
            {
                patients = patientService.getPatients( searchText );

                patients.remove( patient );

                return SUCCESS;
            }
        }

        return SUCCESS;

    }
}
