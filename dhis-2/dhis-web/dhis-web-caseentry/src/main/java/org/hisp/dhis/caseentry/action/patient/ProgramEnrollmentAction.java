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
package org.hisp.dhis.caseentry.action.patient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.caseentry.state.SelectedStateManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientAttributeGroup;
import org.hisp.dhis.patient.PatientAttributeGroupService;
import org.hisp.dhis.patient.PatientAttributeService;
import org.hisp.dhis.patient.PatientIdentifier;
import org.hisp.dhis.patient.PatientIdentifierService;
import org.hisp.dhis.patient.PatientIdentifierType;
import org.hisp.dhis.patient.PatientIdentifierTypeService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patient.comparator.PatientAttributeGroupSortOrderComparator;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramAttribute;
import org.hisp.dhis.program.ProgramAttributeService;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStageInstance;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class ProgramEnrollmentAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PatientService patientService;

    private ProgramService programService;

    private ProgramInstanceService programInstanceService;

    private PatientIdentifierTypeService identifierTypeService;

    private PatientIdentifierService patientIdentifierService;

    private PatientAttributeService patientAttributeService;

    private PatientAttributeGroupService patientAttributeGroupService;

    private PatientAttributeValueService patientAttributeValueService;

    private ProgramAttributeService programAttributeService;

    private SelectedStateManager selectedStateManager;

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private Integer patientId;

    private Integer programId;

    private Map<Integer, String> identiferMap;

    private Patient patient;

    private Program program;

    private ProgramInstance programInstance;

    private Collection<ProgramStageInstance> programStageInstances = new ArrayList<ProgramStageInstance>();

    private Collection<PatientIdentifierType> identifierTypes;

    private Collection<PatientAttribute> noGroupAttributes;

    private List<PatientAttributeGroup> attributeGroups;

    private Map<Integer, String> patientAttributeValueMap = new HashMap<Integer, String>();

    private Collection<ProgramAttribute> programAttributes;

    private Boolean hasDataEntry;

    // -------------------------------------------------------------------------
    // Getters/Setters
    // -------------------------------------------------------------------------

    public void setSelectedStateManager( SelectedStateManager selectedStateManager )
    {
        this.selectedStateManager = selectedStateManager;
    }

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    public Collection<ProgramAttribute> getProgramAttributes()
    {
        return programAttributes;
    }

    public void setProgramAttributeService( ProgramAttributeService programAttributeService )
    {
        this.programAttributeService = programAttributeService;
    }

    public Collection<PatientAttribute> getNoGroupAttributes()
    {
        return noGroupAttributes;
    }

    public List<PatientAttributeGroup> getAttributeGroups()
    {
        return attributeGroups;
    }

    public Map<Integer, String> getPatientAttributeValueMap()
    {
        return patientAttributeValueMap;
    }

    public void setPatientAttributeService( PatientAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
    }

    public void setPatientAttributeGroupService( PatientAttributeGroupService patientAttributeGroupService )
    {
        this.patientAttributeGroupService = patientAttributeGroupService;
    }

    public void setPatientAttributeValueService( PatientAttributeValueService patientAttributeValueService )
    {
        this.patientAttributeValueService = patientAttributeValueService;
    }

    public Map<Integer, String> getIdentiferMap()
    {
        return identiferMap;
    }

    public void setPatientIdentifierService( PatientIdentifierService patientIdentifierService )
    {
        this.patientIdentifierService = patientIdentifierService;
    }

    public void setIdentifierTypeService( PatientIdentifierTypeService identifierTypeService )
    {
        this.identifierTypeService = identifierTypeService;
    }

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    public void setProgramInstanceService( ProgramInstanceService programInstanceService )
    {
        this.programInstanceService = programInstanceService;
    }

    public Collection<PatientIdentifierType> getIdentifierTypes()
    {
        return identifierTypes;
    }

    public void setPatientId( Integer patientId )
    {
        this.patientId = patientId;
    }

    public ProgramInstance getProgramInstance()
    {
        return programInstance;
    }

    public Patient getPatient()
    {
        return patient;
    }

    public Program getProgram()
    {
        return program;
    }

    public void setProgramId( Integer programId )
    {
        this.programId = programId;
    }

    public Collection<ProgramStageInstance> getProgramStageInstances()
    {
        return programStageInstances;
    }

    public Boolean getHasDataEntry()
    {
        return hasDataEntry;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        OrganisationUnit orgunit = selectedStateManager.getSelectedOrganisationUnit();

        patient = patientService.getPatient( patientId );

        program = programService.getProgram( programId );

        // ---------------------------------------------------------------------
        // Load active ProgramInstance, completed = false
        // ---------------------------------------------------------------------

        Collection<ProgramInstance> programInstances = programInstanceService.getProgramInstances( patient, program,
            false );

        if ( programInstances.iterator().hasNext() )
        {
            programInstance = programInstances.iterator().next();

            programStageInstances = programInstance.getProgramStageInstances();

            loadIdentifierTypes();

            loadPatientAttributes();

            // -----------------------------------------------------------------
            // Load patient-attributes of the selected program
            // -----------------------------------------------------------------

            programAttributes = programAttributeService.getAllProgramAttributes();
        }

        hasDataEntry = showDataEntry( orgunit, program, programInstance );

        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void loadIdentifierTypes()
    {
        // ---------------------------------------------------------------------
        // Load identifier types of the selected program
        // ---------------------------------------------------------------------

        identifierTypes = identifierTypeService.getPatientIdentifierTypes( program );
        identiferMap = new HashMap<Integer, String>();

        if ( identifierTypes != null && identifierTypes.size() > 0 )
        {
            Collection<PatientIdentifier> patientIdentifiers = patientIdentifierService.getPatientIdentifiers(
                identifierTypes, patient );

            for ( PatientIdentifier identifier : patientIdentifiers )
            {
                identiferMap.put( identifier.getIdentifierType().getId(), identifier.getIdentifier() );
            }
        }
    }

    private void loadPatientAttributes()
    {
        // ---------------------------------------------------------------------
        // Load patient-attributes of the selected program
        // ---------------------------------------------------------------------

        attributeGroups = new ArrayList<PatientAttributeGroup>( patientAttributeGroupService
            .getPatientAttributeGroups( program ) );
        Collections.sort( attributeGroups, new PatientAttributeGroupSortOrderComparator() );

        noGroupAttributes = patientAttributeService.getPatientAttributes( program, null );

        Collection<PatientAttributeValue> patientAttributeValues = patientAttributeValueService
            .getPatientAttributeValues( patient );

        for ( PatientAttributeValue patientAttributeValue : patientAttributeValues )
        {
            if ( PatientAttribute.TYPE_COMBO.equalsIgnoreCase( patientAttributeValue.getPatientAttribute()
                .getValueType() ) )
            {
                patientAttributeValueMap.put( patientAttributeValue.getPatientAttribute().getId(),
                    patientAttributeValue.getPatientAttributeOption().getName() );
            }
            else
            {
                patientAttributeValueMap.put( patientAttributeValue.getPatientAttribute().getId(),
                    patientAttributeValue.getValue() );
            }
        }
    }

    private boolean showDataEntry( OrganisationUnit orgunit, Program program, ProgramInstance programInstance )
    {
        if ( !program.getOrganisationUnits().contains( orgunit ) )
        {
            return false;
        }
        else if ( !program.getSingleEvent() && programInstance == null )
        {
            return false;
        }

        return true;
    }

}
