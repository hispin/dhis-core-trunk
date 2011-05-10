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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientAttributeOption;
import org.hisp.dhis.patient.PatientAttributeOptionService;
import org.hisp.dhis.patient.PatientAttributeService;
import org.hisp.dhis.patient.PatientIdentifier;
import org.hisp.dhis.patient.PatientIdentifierService;
import org.hisp.dhis.patient.PatientIdentifierType;
import org.hisp.dhis.patient.PatientIdentifierTypeService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class UpdatePatientAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private I18nFormat format;

    private PatientService patientService;

    private PatientAttributeService patientAttributeService;

    private PatientAttributeValueService patientAttributeValueService;

    private PatientIdentifierService patientIdentifierService;

    private PatientIdentifierTypeService patientIdentifierTypeService;

    private OrganisationUnitSelectionManager selectionManager;

    private PatientAttributeOptionService patientAttributeOptionService;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer id;

    private String fullName;

    private String birthDate;

    private boolean isDead;

    private String deathDate;

    private Integer age;

    private char ageType;

    private Character dobType;

    private String gender;

    private String bloodGroup;
	
    private boolean underAge;

    private Integer representativeId;

    private Integer relationshipTypeId;

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private Patient patient;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        OrganisationUnit organisationUnit = selectionManager.getSelectedOrganisationUnit();

        patient = patientService.getPatient( id );

        // ---------------------------------------------------------------------
        // Set FirstName, MiddleName, LastName by FullName
        // ---------------------------------------------------------------------

        int startIndex = fullName.indexOf( ' ' );
        int endIndex = fullName.lastIndexOf( ' ' );

        String firstName = fullName.toString();
        String middleName = "";
        String lastName = "";

        if ( fullName.indexOf( ' ' ) != -1 )
        {
            firstName = fullName.substring( 0, startIndex );
            if ( startIndex == endIndex )
            {
                middleName = "";
                lastName = fullName.substring( startIndex + 1, fullName.length() );
            }
            else
            {
                middleName = fullName.substring( startIndex + 1, endIndex );
                lastName = fullName.substring( endIndex + 1, fullName.length() );
            }
        }

        patient.setFirstName( firstName );
        patient.setMiddleName( middleName );
        patient.setLastName( lastName );

        // ---------------------------------------------------------------------
        // Set Other information for patient
        // ---------------------------------------------------------------------

        patient.setGender( gender );
        patient.setIsDead( isDead );
        patient.setBloodGroup( bloodGroup );
		
        if ( deathDate != null )
        {
            deathDate = deathDate.trim();
            patient.setDeathDate( format.parseDate( deathDate ) );
        }
		
        patient.setUnderAge( underAge );
        patient.setOrganisationUnit( organisationUnit );

        if ( birthDate != null && !birthDate.isEmpty() )
        {
            birthDate = birthDate.trim();
            patient.setBirthDate( format.parseDate( birthDate ) );
        }
        else
        {
            patient.setBirthDateFromAge( age.intValue(), ageType );
        }

        patient.setDobType( dobType );

        // -------------------------------------------------------------------------------------
        // Save PatientIdentifier
        // -------------------------------------------------------------------------------------

        HttpServletRequest request = ServletActionContext.getRequest();

        String value = null;

        Collection<PatientIdentifierType> identifierTypes = patientIdentifierTypeService.getAllPatientIdentifierTypes();

        PatientIdentifier identifier = null;

        if ( identifierTypes != null && identifierTypes.size() > 0 )
        {
            for ( PatientIdentifierType identifierType : identifierTypes )
            {
                if ( identifierType.getFormat() != null && identifierType.getFormat().equals( "State Format" ) )
                {
                    value = request.getParameter( "progcode" ) + request.getParameter( "yearcode" )
                        + request.getParameter( "benicode" );
                }
                else
                {
                    value = request.getParameter( AddPatientAction.PREFIX_IDENTIFIER + identifierType.getId() );
                }

                identifier = patientIdentifierService.getPatientIdentifier( identifierType, patient );

                if ( StringUtils.isNotBlank( value ) )
                {
                    value = value.trim();

                    if ( identifier == null )
                    {
                        identifier = new PatientIdentifier();
                        identifier.setIdentifierType( identifierType );
                        identifier.setPatient( patient );
                        identifier.setIdentifier( value );
                        patient.getIdentifiers().add( identifier );
                    }
                    else
                    {
                        identifier.setIdentifier( value );
                        patient.getIdentifiers().add( identifier );
                    }
                }
                else if ( identifier != null )
                {
                    patient.getIdentifiers().remove( identifier );
                    patientIdentifierService.deletePatientIdentifier( identifier );
                }
            }
        }

        // --------------------------------------------------------------------------------------------------------
        // Save Patient Attributes
        // -----------------------------------------------------------------------------------------------------

        Collection<PatientAttribute> attributes = patientAttributeService.getAllPatientAttributes();

        List<PatientAttributeValue> valuesForSave = new ArrayList<PatientAttributeValue>();
        List<PatientAttributeValue> valuesForUpdate = new ArrayList<PatientAttributeValue>();
        Collection<PatientAttributeValue> valuesForDelete = null;

        PatientAttributeValue attributeValue = null;

        if ( attributes != null && attributes.size() > 0 )
        {
            patient.getAttributes().clear();
            valuesForDelete = patientAttributeValueService.getPatientAttributeValues( patient );

            for ( PatientAttribute attribute : attributes )
            {
                value = request.getParameter( AddPatientAction.PREFIX_ATTRIBUTE + attribute.getId() );

                if ( StringUtils.isNotBlank( value ) )
                {
                    attributeValue = patientAttributeValueService.getPatientAttributeValue( patient, attribute );

                    if ( !patient.getAttributes().contains( attribute ) )
                    {
                        patient.getAttributes().add( attribute );
                    }

                    if ( attributeValue == null )
                    {
                        attributeValue = new PatientAttributeValue();
                        attributeValue.setPatient( patient );
                        attributeValue.setPatientAttribute( attribute );
                        if ( PatientAttribute.TYPE_COMBO.equalsIgnoreCase( attribute.getValueType() ) )
                        {
                            PatientAttributeOption option = patientAttributeOptionService.get( NumberUtils.toInt(
                                value, 0 ) );
                            if ( option != null )
                            {
                                attributeValue.setPatientAttributeOption( option );
                                attributeValue.setValue( option.getName() );
                            }
                            else
                            {
                                // This option was deleted ???
                            }
                        }
                        else
                        {
                            attributeValue.setValue( value.trim() );
                        }
                        valuesForSave.add( attributeValue );
                    }
                    else
                    {
                        if ( PatientAttribute.TYPE_COMBO.equalsIgnoreCase( attribute.getValueType() ) )
                        {
                            PatientAttributeOption option = patientAttributeOptionService.get( NumberUtils.toInt(
                                value, 0 ) );
                            if ( option != null )
                            {
                                attributeValue.setPatientAttributeOption( option );
                                attributeValue.setValue( option.getName() );
                            }
                            else
                            {
                                // This option was deleted ???
                            }
                        }
                        else
                        {
                            attributeValue.setValue( value.trim() );
                        }
                        valuesForUpdate.add( attributeValue );
                        valuesForDelete.remove( attributeValue );
                    }
                }
            }
        }

        patientService.updatePatient( patient, representativeId, relationshipTypeId, valuesForSave, valuesForUpdate,
            valuesForDelete );

        return SUCCESS;
    }

    // -----------------------------------------------------------------------------
    // Getter/Setter
    // -----------------------------------------------------------------------------

    public void setPatientIdentifierTypeService( PatientIdentifierTypeService patientIdentifierTypeService )
    {
        this.patientIdentifierTypeService = patientIdentifierTypeService;
    }

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    public void setPatientAttributeService( PatientAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
    }

    public void setPatientAttributeValueService( PatientAttributeValueService patientAttributeValueService )
    {
        this.patientAttributeValueService = patientAttributeValueService;
    }

    public void setPatientIdentifierService( PatientIdentifierService patientIdentifierService )
    {
        this.patientIdentifierService = patientIdentifierService;
    }

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public void setId( Integer id )
    {
        this.id = id;
    }

    public void setDead( boolean isDead )
    {
        this.isDead = isDead;
    }

    public void setDeathDate( String deathDate )
    {
        this.deathDate = deathDate;
    }

    public void setFullName( String fullName )
    {
        this.fullName = fullName;
    }

    public void setBirthDate( String birthDate )
    {
        this.birthDate = birthDate;
    }

    public void setGender( String gender )
    {
        this.gender = gender;
    }

    public Patient getPatient()
    {
        return patient;
    }

    public void setAge( Integer age )
    {
        this.age = age;
    }

    public void setPatientAttributeOptionService( PatientAttributeOptionService patientAttributeOptionService )
    {
        this.patientAttributeOptionService = patientAttributeOptionService;
    }

    public void setUnderAge( boolean underAge )
    {
        this.underAge = underAge;
    }

    public void setRepresentativeId( Integer representativeId )
    {
        this.representativeId = representativeId;
    }

    public void setRelationshipTypeId( Integer relationshipTypeId )
    {
        this.relationshipTypeId = relationshipTypeId;
    }

    public void setDobType( Character dobType )
    {
        this.dobType = dobType;
    }

    public void setAgeType( char ageType )
    {
        this.ageType = ageType;
    }
}
