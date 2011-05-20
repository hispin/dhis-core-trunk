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

package org.hisp.dhis.caseentry.action.caseentry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hisp.dhis.caseentry.screen.DataEntryScreenManager;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patientdatavalue.PatientDataValue;
import org.hisp.dhis.patientdatavalue.PatientDataValueService;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * @version $ LoadCustomDataEntryAction.java May 7, 2011 2:37:44 PM $
 * 
 */
public class LoadDataEntryAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramStageService programStageService;

    private ProgramInstanceService programInstanceService;

    private DataEntryScreenManager dataEntryScreenManager;

    private PatientDataValueService patientDataValueService;

    private ProgramStageInstanceService programStageInstanceService;

    private PatientService patientService;

    private OrganisationUnitSelectionManager selectionManager;

    // -------------------------------------------------------------------------
    // Input && Output
    // -------------------------------------------------------------------------

    private Integer programStageId;

    private Integer patientId;

    private Boolean useDefaultForm;

    private ProgramStageInstance programStageInstance;

    private String customDataEntryFormCode;

    private I18n i18n;

    private Collection<ProgramStageDataElement> programStageDataElements = new ArrayList<ProgramStageDataElement>();

    private Map<Integer, PatientDataValue> patientDataValueMap;

    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public void setProgramStageInstanceService( ProgramStageInstanceService programStageInstanceService )
    {
        this.programStageInstanceService = programStageInstanceService;
    }

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    //
    // public void setMinMaxDataElementService( MinMaxDataElementService
    // minMaxDataElementService )
    // {
    // this.minMaxDataElementService = minMaxDataElementService;
    // }

    public void setProgramStageService( ProgramStageService programStageService )
    {
        this.programStageService = programStageService;
    }

    public void setProgramInstanceService( ProgramInstanceService programInstanceService )
    {
        this.programInstanceService = programInstanceService;
    }

    public void setDataEntryScreenManager( DataEntryScreenManager dataEntryScreenManager )
    {
        this.dataEntryScreenManager = dataEntryScreenManager;
    }

    public void setPatientDataValueService( PatientDataValueService patientDataValueService )
    {
        this.patientDataValueService = patientDataValueService;
    }

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public ProgramStageInstance getProgramStageInstance()
    {
        return programStageInstance;
    }

    public void setPatientId( Integer patientId )
    {
        this.patientId = patientId;
    }

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    public void setProgramStageId( Integer programStageId )
    {
        this.programStageId = programStageId;
    }

    public void setUseDefaultForm( Boolean useDefaultForm )
    {
        this.useDefaultForm = useDefaultForm;
    }

    public String getCustomDataEntryFormCode()
    {
        return customDataEntryFormCode;
    }

    public Collection<ProgramStageDataElement> getProgramStageDataElements()
    {
        return programStageDataElements;
    }

    public Map<Integer, PatientDataValue> getPatientDataValueMap()
    {
        return patientDataValueMap;
    }

    // -------------------------------------------------------------------------
    // Implementation Action
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        // ---------------------------------------------------------------------
        // Get program-stage-instance
        // ---------------------------------------------------------------------

        Patient patient = patientService.getPatient( patientId );

        ProgramStage programStage = programStageService.getProgramStage( programStageId );

        ProgramInstance programInstance = programInstanceService.getProgramInstances( patient,
            programStage.getProgram(), false ).iterator().next();

        OrganisationUnit organisationUnit = selectionManager.getSelectedOrganisationUnit();

        programStageDataElements = programStage.getProgramStageDataElements();

        programStageInstance = programStageInstanceService.getProgramStageInstance( programInstance, programStage );

        if ( programStageInstance != null )
        {
            // ---------------------------------------------------------------------
            // Get data values
            // ---------------------------------------------------------------------

            Collection<PatientDataValue> patientDataValues = patientDataValueService
                .getPatientDataValues( programStageInstance );

            patientDataValueMap = new HashMap<Integer, PatientDataValue>( patientDataValues.size() );

            for ( PatientDataValue patientDataValue : patientDataValues )
            {
                patientDataValueMap.put( patientDataValue.getDataElement().getId(), patientDataValue );
            }

            // ---------------------------------------------------------------------
            // Get data-entry-form
            // ---------------------------------------------------------------------

            DataEntryForm dataEntryForm = programStage.getDataEntryForm();

            if ( !useDefaultForm && dataEntryForm != null)
            {
              customDataEntryFormCode = dataEntryScreenManager.populateCustomDataEntryScreenForMultiDimensional(
                    dataEntryForm.getHtmlCode(), patientDataValues, "", i18n, programStage, programStageInstance,
                    organisationUnit );
            }

            return SUCCESS;
        }

        return SUCCESS;
    }

}
