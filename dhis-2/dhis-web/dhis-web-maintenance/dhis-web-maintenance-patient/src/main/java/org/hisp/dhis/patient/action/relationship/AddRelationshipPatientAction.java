package org.hisp.dhis.patient.action.relationship;

import java.util.Collection;
import java.util.Date;

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
import org.hisp.dhis.patient.idgen.PatientIdentifierGenerator;
import org.hisp.dhis.patient.state.SelectedStateManager;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.relationship.RelationshipService;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;

import com.opensymphony.xwork2.Action;


public class AddRelationshipPatientAction implements Action
{
    public static final String PREFIX_ATTRIBUTE = "attr";

    public static final String PREFIX_IDENTIFIER = "iden";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private I18nFormat format;

    private PatientService patientService;

    private PatientIdentifierService patientIdentifierService;

    private PatientIdentifierTypeService patientIdentifierTypeService;

    private OrganisationUnitSelectionManager selectionManager;

    private SelectedStateManager selectedStateManager;

    private PatientAttributeService patientAttributeService;

    private PatientAttributeValueService patientAttributeValueService;

    private PatientAttributeOptionService patientAttributeOptionService;
    
    private RelationshipService relationshipService;
    
    private RelationshipTypeService relationshipTypeService;

    // -------------------------------------------------------------------------
    // Input - name
    // -------------------------------------------------------------------------
    private String firstName;

    private String middleName;

    private String lastName;

    // -------------------------------------------------------------------------
    // Input - demographics
    // -------------------------------------------------------------------------

    private String birthDate;

    private Integer age;

    private boolean birthDateEstimated;

    private String gender;

    private String bloodGroup;
    

    // -------------------------------------------------------------------------
    // Input - others
    // -------------------------------------------------------------------------

    private boolean underAge;
    
    private Integer relationshipId;
    
    private Integer relationshipTypeId;

    // -------------------------------------------------------------------------
    // Output - making the patient available so that its attributes can be
    // edited
    // -------------------------------------------------------------------------

    private Patient patient;

    public Patient getPatient()
    {
        return patient;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        // ---------------------------------------------------------------------
        // Prepare values
        // ---------------------------------------------------------------------

        OrganisationUnit organisationUnit = selectionManager.getSelectedOrganisationUnit();

        patient = new Patient();

        patient.setFirstName( firstName.trim() );
        patient.setMiddleName( middleName.trim() );
        patient.setLastName( lastName.trim() );
        patient.setGender( gender );
        patient.setBloodGroup( bloodGroup );
        patient.setUnderAge( underAge );

        if ( birthDate != null )
        {
            birthDate = birthDate.trim();

            if ( birthDate.length() != 0 )
            {
                patient.setBirthDate( format.parseDate( birthDate ) );
                patient.setBirthDateEstimated( birthDateEstimated );
            }
            else
            {
                if ( age != null )
                {
                    patient.setBirthDateFromAge( age.intValue() );
                }
            }
        }
        else
        {
            if ( age != null )
            {
                patient.setBirthDateFromAge( age.intValue() );
            }
        }

        patient.setRegistrationDate( new Date() );

        // --------------------------------------------------------------------------------
        // Generate system id with this format :
        // (BirthDate)(Gender)(XXXXXX)(checkdigit)
        // PatientIdentifierType will be null
        // --------------------------------------------------------------------------------

        String identifier = PatientIdentifierGenerator.getNewIdentifier( patient.getBirthDate(), patient.getGender() );

        PatientIdentifier systemGenerateIdentifier = patientIdentifierService.get( null, identifier );
        while ( systemGenerateIdentifier != null )
        {
            identifier = PatientIdentifierGenerator.getNewIdentifier( patient.getBirthDate(), patient.getGender() );
            systemGenerateIdentifier = patientIdentifierService.get( null, identifier );
        }

        systemGenerateIdentifier = new PatientIdentifier();
        systemGenerateIdentifier.setIdentifier( identifier );
        systemGenerateIdentifier.setOrganisationUnit( organisationUnit );
        systemGenerateIdentifier.setPatient( patient );

        patient.getIdentifiers().add( systemGenerateIdentifier );

        selectedStateManager.clearListAll();
        selectedStateManager.clearSearchingAttributeId();
        selectedStateManager.setSearchText( systemGenerateIdentifier.getIdentifier() );

        // -----------------------------------------------------------------------------
        // Save Patient Identifiers
        // -----------------------------------------------------------------------------
        
        HttpServletRequest request = ServletActionContext.getRequest();

        String value = null;
        
        Collection<PatientIdentifierType> identifierTypes = patientIdentifierTypeService.getAllPatientIdentifierTypes();

        PatientIdentifier pIdentifier = null;

        if ( identifierTypes != null && identifierTypes.size() > 0 )
        {
            for ( PatientIdentifierType identifierType : identifierTypes )
            {
                value = request.getParameter( PREFIX_IDENTIFIER + identifierType.getId() );

                if ( StringUtils.isNotBlank( value ) )
                {
                    pIdentifier = new PatientIdentifier();
                    pIdentifier.setIdentifierType( identifierType );
                    pIdentifier.setPatient( patient );
                    pIdentifier.setOrganisationUnit( organisationUnit );
                    pIdentifier.setIdentifier( value.trim() );
                    patient.getIdentifiers().add( pIdentifier );
                }
            }
        }
      
        patientService.savePatient( patient );

        if( relationshipId != null && relationshipTypeId != null )
        {
            Patient relationship = patientService.getPatient( relationshipId );
            if( relationship != null )
            {
                if( underAge )
                    patient.setRepresentative( relationship );
                
                Relationship rel = new Relationship();
                rel.setPatientA( relationship );
                rel.setPatientB(  patient );
                
                if( relationshipTypeId != null )
                {
                    RelationshipType relType = relationshipTypeService.getRelationshipType( relationshipTypeId );
                    if( relType != null )
                    {
                        rel.setRelationshipType( relType );
                        relationshipService.saveRelationship( rel );
                    }
                }
            }
        }
        
        // -----------------------------------------------------------------------------
        // Save Patient Attributes
        // -----------------------------------------------------------------------------
        Collection<PatientAttribute> attributes = patientAttributeService.getAllPatientAttributes();

        PatientAttributeValue attributeValue = null;

        if ( attributes != null && attributes.size() > 0 )
        {
            for ( PatientAttribute attribute : attributes )
            {
                value = request.getParameter( PREFIX_ATTRIBUTE + attribute.getId() );
                if ( StringUtils.isNotBlank( value ) )
                {
                    if ( !patient.getAttributes().contains( attribute ) )
                    {
                        patient.getAttributes().add( attribute );
                    }

                    attributeValue = new PatientAttributeValue();
                    attributeValue.setPatient( patient );
                    attributeValue.setPatientAttribute( attribute );

                    if ( PatientAttribute.TYPE_COMBO.equalsIgnoreCase( attribute.getValueType() ) )
                    {
                        PatientAttributeOption option = patientAttributeOptionService
                            .get( NumberUtils.toInt( value, 0 ) );
                        if ( option != null )
                        {
                            attributeValue.setPatientAttributeOption( option );
                            attributeValue.setValue( option.getName() );
                        }
                        else
                        {
                            // Someone deleted this option ...
                        }
                    }
                    else
                    {
                        attributeValue.setValue( value.trim() );
                    }
                    patientAttributeValueService.savePatientAttributeValue( attributeValue );
                }
            }
        }


        return SUCCESS;
    }

    // -----------------------------------------------------------------------------
    // Getter/Setter
    // -----------------------------------------------------------------------------

    public void setPatientIdentifierTypeService( PatientIdentifierTypeService patientIdentifierTypeService )
    {
        this.patientIdentifierTypeService = patientIdentifierTypeService;
    }

    public void setBirthDate( String birthDate )
    {
        this.birthDate = birthDate;
    }

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    public void setPatientIdentifierService( PatientIdentifierService patientIdentifierService )
    {
        this.patientIdentifierService = patientIdentifierService;
    }

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public void setSelectedStateManager( SelectedStateManager selectedStateManager )
    {
        this.selectedStateManager = selectedStateManager;
    }

    public void setPatientAttributeService( PatientAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
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

    public void setAge( Integer age )
    {
        this.age = age;
    }

    public void setBirthDateEstimated( boolean birthDateEstimated )
    {
        this.birthDateEstimated = birthDateEstimated;
    }

    public void setGender( String gender )
    {
        this.gender = gender;
    }

    public void setBloodGroup( String bloodGroup )
    {
        this.bloodGroup = bloodGroup;
    }

    public void setPatientAttributeOptionService( PatientAttributeOptionService patientAttributeOptionService )
    {
        this.patientAttributeOptionService = patientAttributeOptionService;
    }

    public void setRelationshipTypeId( Integer relationshipTypeId )
    {
        this.relationshipTypeId = relationshipTypeId;
    }

    public void setRelationshipService( RelationshipService relationshipService )
    {
        this.relationshipService = relationshipService;
    }

    public void setRelationshipTypeService( RelationshipTypeService relationshipTypeService )
    {
        this.relationshipTypeService = relationshipTypeService;
    }

    public void setUnderAge( boolean underAge )
    {
        this.underAge = underAge;
    }

    public void setRelationshipId( Integer relationshipId )
    {
        this.relationshipId = relationshipId;
    }
}
