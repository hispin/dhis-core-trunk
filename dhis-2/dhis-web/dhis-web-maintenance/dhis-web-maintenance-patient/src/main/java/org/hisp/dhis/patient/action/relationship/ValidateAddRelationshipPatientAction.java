package org.hisp.dhis.patient.action.relationship;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientIdentifier;
import org.hisp.dhis.patient.PatientIdentifierService;
import org.hisp.dhis.patient.PatientIdentifierType;
import org.hisp.dhis.patient.PatientIdentifierTypeService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patient.action.patient.AddPatientAction;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;

import com.opensymphony.xwork2.Action;


public class ValidateAddRelationshipPatientAction
implements Action
{
    public static final String PATIENT_DUPLICATE = "duplicate";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitSelectionManager selectionManager;

    private I18nFormat format;

    private PatientService patientService;

    private PatientAttributeValueService patientAttributeValueService;

    private PatientIdentifierService patientIdentifierService;

    private PatientIdentifierTypeService identifierTypeService;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String firstName;

    private String middleName;

    private String lastName;

    private String birthDate;

    private Integer age;

    private String gender;

    private Integer id;

    private boolean checkedDuplicate;
    
    private boolean underAge;
    
    private Integer relationshipId;
    
    private Integer relationshipTypeId;

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private String message;

    private I18n i18n;

    private Patient patient;

    private Map<String, String> patientAttributeValueMap = new HashMap<String, String>();

    private PatientIdentifier patientIdentifier;

    private Collection<Patient> patients;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {

        Date dateOfBirth;
        System.out.println("selectionManager: "+selectionManager);

        if ( selectionManager.getSelectedOrganisationUnit() == null )
        {
            message = i18n.getString( "please_select_a_registering_unit" );

            return INPUT;
        }

        if ( firstName == null && middleName == null && lastName == null )
        {
            message = i18n.getString( "specfiy_name_s" );

            return INPUT;
        }

        if ( firstName != null )
        {
            firstName = firstName.trim();
        }

        if ( middleName != null )
        {
            middleName = middleName.trim();
        }

        if ( lastName != null )
        {
            lastName = lastName.trim();
        }
        if ( firstName.length() == 0 && middleName.length() == 0 && lastName.length() == 0 )
        {
            message = i18n.getString( "specfiy_name_s" );

            return INPUT;
        }

        if ( age == null && birthDate == null )
        {
            message = i18n.getString( "specfiy_birth_date_or_age" );

            return INPUT;
        }

        if ( birthDate != null )
        {
            birthDate = birthDate.trim();

            if ( birthDate.length() != 0 )
            {
                dateOfBirth = format.parseDate( birthDate );

                if ( dateOfBirth == null || dateOfBirth.after( new Date() ) )
                {
                    message = i18n.getString( "please_enter_a_valid_birth_date" );

                    return INPUT;
                }
            }
            else
            {
                if ( age == null )
                {
                    message = i18n.getString( "specfiy_birth_date_or_age" );

                    return INPUT;
                }
            }
        }
        
        if ( !checkedDuplicate )
        {
            // Check duplication name, birthdate, gender
            patients = patientService.getPatient( firstName, middleName, lastName, format.parseDate( birthDate ), gender );

            if ( patients != null && patients.size() > 0 )
            {
                message = i18n.getString( "patient_duplicate" );
                
                boolean flagDuplicate = false;
                for ( Patient p : patients )
                {
                    if ( id == null ||  ( id != null &&  p.getId().intValue() != id.intValue() ) )
                    {
                        flagDuplicate = true;
                        Collection<PatientAttributeValue> patientAttributeValues = patientAttributeValueService
                            .getPatientAttributeValues( p );
                        
                        for ( PatientAttributeValue patientAttributeValue : patientAttributeValues )
                        {
                            patientAttributeValueMap
                                .put( p.getId() + "_" + patientAttributeValue.getPatientAttribute().getId(),
                                    patientAttributeValue.getValue() );
                        }
                    }
                }
                if( flagDuplicate )
                    return PATIENT_DUPLICATE;
            }
        }
        
        
        // Check Identifiers duplicate

        Patient p = new Patient();
        if ( birthDate != null )
        {
            birthDate = birthDate.trim();

            if ( birthDate.length() != 0 )
            {
                p.setBirthDate( format.parseDate( birthDate ) );
            }
            else
            {
                if ( age != null )
                {
                    p.setBirthDateFromAge( age.intValue() );
                }
            }
        }
        else
        {
            if ( age != null )
            {
                p.setBirthDateFromAge( age.intValue() );
            }
        }

        HttpServletRequest request = ServletActionContext.getRequest();

        Collection<PatientIdentifierType> identifiers = identifierTypeService.getAllPatientIdentifierTypes();

        if ( identifiers != null && identifiers.size() > 0 )
        {
            String value = null;
            String idDuplicate = "";
            for ( PatientIdentifierType idType : identifiers )
            {
                // If underAge is TRUE : Only check duplicate on PatientIdentifierType which related is FALSE
                if(  !underAge ||  ( underAge && !idType.isRelated() )   )
                {
                    value = request.getParameter( AddPatientAction.PREFIX_IDENTIFIER + idType.getId() );
                    if ( StringUtils.isNotBlank( value ) )
                    {
                        PatientIdentifier identifier = patientIdentifierService.get( idType, value );
                        if ( identifier != null && (id == null || identifier.getPatient().getId().intValue() != id.intValue()) )
                        {
                            idDuplicate += idType.getName() + ", ";
                        }
                    }
                }
            }

            if ( StringUtils.isNotBlank( idDuplicate ) )
            {
                idDuplicate = StringUtils.substringBeforeLast( idDuplicate, "," );
                message = i18n.getString( "identifier_duplicate" ) + ": " + idDuplicate;
                return INPUT;
            }
        }

        // ---------------------------------------------------------------------
        // Validation success
        // ---------------------------------------------------------------------

        message = i18n.getString( "everything_is_ok" );

        return SUCCESS;
    }

    // ---------------------------------------------------------------------
    // Getter/Setter
    // ---------------------------------------------------------------------

    public Collection<Patient> getPatients()
    {
        return patients;
    }

    public void setIdentifierTypeService( PatientIdentifierTypeService identifierTypeService )
    {
        this.identifierTypeService = identifierTypeService;
    }

    public void setPatientIdentifierService( PatientIdentifierService patientIdentifierService )
    {
        this.patientIdentifierService = patientIdentifierService;
    }

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    public void setPatientAttributeValueService( PatientAttributeValueService patientAttributeValueService )
    {
        this.patientAttributeValueService = patientAttributeValueService;
    }

    public void setFirstName( String firstName )
    {
        this.firstName = firstName;
    }

    public void setMiddleName( String middleName )
    {
        this.middleName = middleName;
    }

    public void setLastName( String lastName )
    {
        this.lastName = lastName;
    }

    public void setBirthDate( String birthDate )
    {
        this.birthDate = birthDate;
    }

    public void setAge( Integer age )
    {
        this.age = age;
    }

    public String getMessage()
    {
        return message;
    }

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    public Patient getPatient()
    {
        return patient;
    }

    public Map<String, String> getPatientAttributeValueMap()
    {
        return patientAttributeValueMap;
    }

    public PatientIdentifier getPatientIdentifier()
    {
        return patientIdentifier;
    }

    public void setId( Integer id )
    {
        this.id = id;
    }

    public void setCheckedDuplicate( boolean checkedDuplicate )
    {
        this.checkedDuplicate = checkedDuplicate;
    }

    public void setGender( String gender )
    {
        this.gender = gender;
    }

    public void setUnderAge( boolean underAge )
    {
        this.underAge = underAge;
    }

    public void setRelationshipTypeId( Integer relationshipTypeId )
    {
        this.relationshipTypeId = relationshipTypeId;
    }

    public void setRelationshipId( Integer relationshipId )
    {
        this.relationshipId = relationshipId;
    }
}
