package org.hisp.dhis.web.api.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.patientdatavalue.PatientDataValue;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.web.api.model.ActivityValue;
import org.hisp.dhis.web.api.model.DataValue;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultActivityValueService
    implements IActivityValueService
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private org.hisp.dhis.program.ProgramStageInstanceService programStageInstanceService;

    @Autowired
    private org.hisp.dhis.dataelement.DataElementCategoryService categoryService;

    @Autowired
    private org.hisp.dhis.patientdatavalue.PatientDataValueService dataValueService;

    @Autowired
    private CurrentUserService currentUserService;

    // -------------------------------------------------------------------------
    // DataValueService
    // -------------------------------------------------------------------------

    @Override
    public String saveValues( ActivityValue activityValue )
    {

        Collection<OrganisationUnit> units = currentUserService.getCurrentUser().getOrganisationUnits();
        OrganisationUnit unit = null;

        if ( units.size() > 0 )
        {
            unit = units.iterator().next();
        }
        else
        {
            return "INVALID_SERVICE_PROVIDING_UNIT";
        }

        ProgramStageInstance programStageInstance = programStageInstanceService.getProgramStageInstance( activityValue
            .getProgramInstanceId() );

        if ( programStageInstance == null )
        {
            return "INVALID_PROGRAM_STAGE";
        }

        programStageInstance.getProgramStage();
        Collection<org.hisp.dhis.dataelement.DataElement> dataElements = new ArrayList<org.hisp.dhis.dataelement.DataElement>();

        for ( ProgramStageDataElement de : programStageInstance.getProgramStage().getProgramStageDataElements() )
        {
            dataElements.add( de.getDataElement() );
        }

        programStageInstance.getProgramStage().getProgramStageDataElements();
        Collection<Integer> dataElementIds = new ArrayList<Integer>( activityValue.getDataValues().size() );

        for ( DataValue dv : activityValue.getDataValues() )
        {
            dataElementIds.add( dv.getId() );
        }

        if ( dataElements.size() != dataElementIds.size() )
        {
            return "INVALID_PROGRAM_STAGE";
        }

        Map<Integer, org.hisp.dhis.dataelement.DataElement> dataElementMap = new HashMap<Integer, org.hisp.dhis.dataelement.DataElement>();
        for ( org.hisp.dhis.dataelement.DataElement dataElement : dataElements )
        {
            if ( !dataElementIds.contains( dataElement.getId() ) )
            {
                return "INVALID_PROGRAM_STAGE";
            }
            dataElementMap.put( dataElement.getId(), dataElement );
        }

        // Everything is fine, hence save
        saveDataValues( activityValue, programStageInstance, dataElementMap, unit,
            categoryService.getDefaultDataElementCategoryOptionCombo() );

        return "SUCCESS";

    }

    // -------------------------------------------------------------------------
    // Supportive method
    // -------------------------------------------------------------------------

    private void saveDataValues( ActivityValue activityValue, ProgramStageInstance programStageInstance,
        Map<Integer, DataElement> dataElementMap, OrganisationUnit orgUnit, DataElementCategoryOptionCombo optionCombo )
    {

        org.hisp.dhis.dataelement.DataElement dataElement;
        String value;

        for ( DataValue dv : activityValue.getDataValues() )
        {
            value = dv.getVal();
            System.out.println("COC ID: " + dv.getCategoryOptComboID());
            DataElementCategoryOptionCombo cateOptCombo = categoryService.getDataElementCategoryOptionCombo( dv
                .getCategoryOptComboID() );
            System.out.println(cateOptCombo);
            if ( value != null && value.trim().length() == 0 )
            {
                value = null;
            }

            if ( value != null )
            {
                value = value.trim();
            }

            dataElement = dataElementMap.get( dv.getId() );
            PatientDataValue dataValue = dataValueService.getPatientDataValue( programStageInstance, dataElement,
                orgUnit );

            if ( dataValue == null )
            {
                if ( value != null )
                {
                    if ( programStageInstance.getExecutionDate() == null )
                    {
                        programStageInstance.setExecutionDate( new Date() );
                        programStageInstanceService.updateProgramStageInstance( programStageInstance );
                    }

                    dataValue = new PatientDataValue( programStageInstance, dataElement, cateOptCombo, orgUnit,
                        new Date(), value, false );
                    ;
                    dataValueService.savePatientDataValue( dataValue );
                }
            }
            else
            {
                if ( programStageInstance.getExecutionDate() == null )
                {
                    programStageInstance.setExecutionDate( new Date() );
                    programStageInstanceService.updateProgramStageInstance( programStageInstance );
                }

                dataValue.setValue( value );
                dataValue.setOptionCombo( optionCombo );
                dataValue.setProvidedByAnotherFacility( false );
                dataValue.setTimestamp( new Date() );

                dataValueService.updatePatientDataValue( dataValue );
            }
        }
    }
}
