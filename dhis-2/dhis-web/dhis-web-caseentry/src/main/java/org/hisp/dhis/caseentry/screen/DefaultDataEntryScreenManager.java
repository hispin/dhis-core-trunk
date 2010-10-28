package org.hisp.dhis.caseentry.screen;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataelement.CalculatedDataElement;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.minmax.MinMaxDataElement;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.patientdatavalue.PatientDataValue;
import org.hisp.dhis.patientdatavalue.PatientDataValueService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageDataElementService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageService;

/**
 * @author Viet Nguyen
 * @version $Id$
 */
public class DefaultDataEntryScreenManager
    implements DataEntryScreenManager
{

    Log logger = LogFactory.getLog( getClass() );

    private static final String DEFAULT_FORM = "defaultform";

    private static final String MULTI_DIMENSIONAL_FORM = "multidimensionalform";

    private static final String EMPTY = "";
    
    private static final String UNKNOW_CLINIC = "unknow_clinic";
    
    private static final String NOTAVAILABLE = "not_available";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private PatientDataValueService patientDataValueService;

    public void setPatientDataValueService( PatientDataValueService patientDataValueService )
    {
        this.patientDataValueService = patientDataValueService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private ProgramStageInstanceService programStageInstanceService;

    public void setProgramStageInstanceService( ProgramStageInstanceService programStageInstanceService )
    {
        this.programStageInstanceService = programStageInstanceService;
    }

    private ProgramStageService programStageService;

    public void setProgramStageService( ProgramStageService programStageService )
    {
        this.programStageService = programStageService;
    }

    private ProgramStageDataElementService programStageDataElementService;
    
    public void setProgramStageDataElementService(
        ProgramStageDataElementService programStageDataElementService )
    {
        this.programStageDataElementService = programStageDataElementService;
    }
    
    // -------------------------------------------------------------------------
    // DataEntryScreenManager implementation
    // -------------------------------------------------------------------------
    public boolean hasMixOfDimensions( ProgramStage programStage )
    {
        Collection<DataElement> dataElements = programStageDataElementService.getListDataElement( programStage );
        if (dataElements.size() > 0 )
        {
            Iterator<DataElement> dataElementIterator = dataElements.iterator();

            DataElementCategoryCombo catCombo = dataElementIterator.next().getCategoryCombo();

            for ( DataElement de : dataElements )
            {
                if ( catCombo != de.getCategoryCombo() )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasMultiDimensionalDataElement( ProgramStage programStage )
    {
        Collection<DataElement> dataElements = programStageDataElementService.getListDataElement( programStage );
        
        for ( DataElement element :dataElements )
        {
            if ( element.isMultiDimensional() )
            {
                return true;
            }
        }

        return false;
    }

    public String getScreenType( ProgramStage dataSet )
    {
        return hasMultiDimensionalDataElement( dataSet ) ? MULTI_DIMENSIONAL_FORM : DEFAULT_FORM;
    }

    public Collection<Integer> getAllCalculatedDataElements( ProgramStage programStage )
    {
        Collection<Integer> calculatedDataElementIds = new HashSet<Integer>();
        
        Collection<DataElement> dataElements = programStageDataElementService.getListDataElement( programStage );
        
        CalculatedDataElement cde;

        for ( DataElement dataElement : dataElements )
        {
            if ( dataElement instanceof CalculatedDataElement )
            {
                cde = (CalculatedDataElement) dataElement;

                calculatedDataElementIds.add( cde.getId() );
            }
        }

        return calculatedDataElementIds;
    }

    public Map<CalculatedDataElement, Map<DataElement, Integer>> getNonSavedCalculatedDataElements(
        ProgramStage programStage )
    {
        Map<CalculatedDataElement, Map<DataElement, Integer>> calculatedDataElementMap = new HashMap<CalculatedDataElement, Map<DataElement, Integer>>();

        Collection<DataElement> dataElements = programStageDataElementService.getListDataElement( programStage );
        
        CalculatedDataElement cde;

        for ( DataElement dataElement : dataElements )
        {
            if ( dataElement instanceof CalculatedDataElement )
            {
                cde = (CalculatedDataElement) dataElement;

                if ( !cde.isSaved() )
                {
                    calculatedDataElementMap.put( cde, dataElementService.getDataElementFactors( cde ) );
                }
            }
        }

        return calculatedDataElementMap;
    }

    public Map<CalculatedDataElement, Integer> populateValuesForCalculatedDataElements(
        OrganisationUnit organisationUnit, ProgramStage programStage, ProgramStageInstance programStageInstance )
    {
        Map<CalculatedDataElement, Integer> calculatedValueMap = new HashMap<CalculatedDataElement, Integer>();

        // -------------------------------------------------------------------
        // populateValuesForCalculatedDataElements(...)
        // Will work on this later.
        // -------------------------------------------------------------------

        /*
         * CalculatedDataElement cde;
         * 
         * Map<String, Integer> factorMap;
         * 
         * PatientDataValue dataValue; int factor; int value = 0;
         * 
         * Collection<String> operandIds = new ArrayList<String>();
         * 
         * for (DataElement dataElement : programStage.getDataElements()) { if
         * (!(dataElement instanceof CalculatedDataElement)) { continue; }
         * 
         * cde = (CalculatedDataElement) dataElement;
         * 
         * if (cde.isSaved()) { continue; }
         * 
         * factorMap = dataElementService.getOperandFactors(cde);
         * 
         * operandIds = dataElementService.getOperandIds(cde);
         * 
         * for (String operandId : operandIds) { factor =
         * factorMap.get(operandId);
         * 
         * String dataElementIdString = operandId.substring(0,
         * operandId.indexOf(SEPARATOR)); String optionComboIdString =
         * operandId.substring(operandId.indexOf(SEPARATOR) + 1,
         * operandId.length());
         * 
         * DataElement element =
         * dataElementService.getDataElement(Integer.parseInt
         * (dataElementIdString)); DataElementCategoryOptionCombo optionCombo =
         * categoryService
         * .getDataElementCategoryOptionCombo(Integer.parseInt(optionComboIdString
         * ));
         * 
         * dataValue = patientDataValueService.getPatientDataValue(
         * programStageInstance, dataElement, organisationUnit );
         * 
         * if (dataValue != null) { value +=
         * Integer.parseInt(dataValue.getValue()) * factor; } }
         * 
         * calculatedValueMap.put(cde, value);
         * 
         * value = 0; }
         */
        return calculatedValueMap;
    }

    public String populateCustomDataEntryScreenForMultiDimensional( String dataEntryFormCode,
        Collection<PatientDataValue> dataValues, Map<CalculatedDataElement, Integer> calculatedValueMap,
        Map<Integer, MinMaxDataElement> minMaxMap, String disabled, Boolean saveMode, I18n i18n,
        ProgramStage programStage, ProgramStageInstance programStageInstance, OrganisationUnit organisationUnit )
    {
        // ---------------------------------------------------------------------
        // Populating Custom Value data
        // ---------------------------------------------------------------------
        Map<Integer, Collection<PatientDataValue>> mapDataValue = new HashMap<Integer, Collection<PatientDataValue>>();

        String result = "";

        result = populateCustomDataEntryForTextBox( dataEntryFormCode, dataValues, calculatedValueMap, disabled,
            saveMode, i18n, programStage, programStageInstance, organisationUnit, mapDataValue );

        result = populateCustomDataEntryForBoolean( result, dataValues, calculatedValueMap, minMaxMap, disabled,
            saveMode, i18n, programStage, programStageInstance, organisationUnit, mapDataValue );

        result = populateCustomDataEntryForMutiDimentionalString( result, dataValues, calculatedValueMap, minMaxMap,
            disabled, saveMode, i18n, programStage, programStageInstance, organisationUnit, mapDataValue );

        result = populateCustomDataEntryForDate( result, dataValues, calculatedValueMap, minMaxMap, disabled, saveMode,
            i18n, programStage, programStageInstance, organisationUnit, mapDataValue );

        result = populateI18nStrings( result, i18n );
        
        return result;
    }

    private String populateCustomDataEntryForTextBox( String dataEntryFormCode,
        Collection<PatientDataValue> dataValues, Map<CalculatedDataElement, Integer> calculatedValueMap,
        String disabled, Boolean saveMode, I18n i18n, ProgramStage programStage, ProgramStageInstance programStageInstance,
        OrganisationUnit organisationUnit, Map<Integer, Collection<PatientDataValue>> mapDataValue )
    {
        // ---------------------------------------------------------------------
        // Inline Javascript to add to HTML before outputting
        // ---------------------------------------------------------------------

        final String jsCodeForInputs = " $DISABLED onchange=\"saveValueCustom( this )\" data=\"{compulsory:$COMPULSORY, optionComboId:$OPTIONCOMBOID, dataElementId:$DATAELEMENTID, dataElementName:'$DATAELEMENTNAME', dataElementType:'$DATAELEMENTTYPE', programStageId:$PROGRAMSTAGEID, programStageName: '$PROGRAMSTAGENAME', orgUnitName:'$ORGUNITNAME'}\"  onkeypress=\"return keyPress(event, this)\"   ";
        // final String historyCode =
        // " ondblclick='javascript:viewHistory( $DATAELEMENTID, $OPTIONCOMBOID, true )' ";
        final String calDataElementCode = " class=\"calculated\" disabled ";

        // ---------------------------------------------------------------------
        // Metadata code to add to HTML before outputting
        // ---------------------------------------------------------------------

        // final String metaDataCode =
        // "<span id=\"value[$DATAELEMENTID].name\" title=\"$DATAELEMENTID].name\" style=\"display:none\">$DATAELEMENTNAME</span>"
        // +
        // "<span id=\"value[$DATAELEMENTID].type\" style=\"display:none\">$DATAELEMENTTYPE</span>";

        StringBuffer sb = new StringBuffer();

        // ---------------------------------------------------------------------
        // Pattern to match data elements in the HTML code
        // ---------------------------------------------------------------------

        Pattern dataElementPattern = Pattern.compile( "(<input.*?)[/]?>", Pattern.DOTALL );
        Matcher dataElementMatcher = dataElementPattern.matcher( dataEntryFormCode );

        // ---------------------------------------------------------------------
        // Pattern to extract data element ID from data element field
        // ---------------------------------------------------------------------

        Pattern identifierPattern = Pattern
            .compile( "\"value\\[([\\p{Digit}.]*)\\].value:value\\[([\\p{Digit}.]*)\\].value:value\\[([\\p{Digit}.]*)\\].value\"" );

        // ---------------------------------------------------------------------
        // Iterate through all matching data element fields
        // ---------------------------------------------------------------------

        Map<Integer, DataElement> dataElementMap = getDataElementMap( programStage );
        
        
        while ( dataElementMatcher.find() )
        {
            // -----------------------------------------------------------------
            // Get HTML input field code
            // -----------------------------------------------------------------
            String compulsory = "null";
            String dataElementCode = dataElementMatcher.group( 1 );

            Matcher identifierMatcher = identifierPattern.matcher( dataElementCode );

            if ( identifierMatcher.find() && identifierMatcher.groupCount() > 0 )
            {

                // -------------------------------------------------------------
                // Get data element ID of data element
                // -------------------------------------------------------------

                int programStageId = Integer.parseInt( identifierMatcher.group( 1 ) );

                int dataElementId = Integer.parseInt( identifierMatcher.group( 2 ) );
                
                int optionComboId = Integer.parseInt( identifierMatcher.group( 3 ) );

                DataElement dataElement = null;
                
                String programStageName = programStage.getName();
                
                if ( programStageId != programStage.getId() )
                {
                    dataElement = dataElementService.getDataElement( dataElementId );
                    
                    ProgramStage otherProgramStage = programStageService.getProgramStage( programStageId );
                    programStageName = otherProgramStage != null ? otherProgramStage.getName() : "N/A";
                    
                }
                else
                {
                    dataElement = dataElementMap.get( dataElementId );
                    ProgramStageDataElement psde = programStageDataElementService.get( programStage, dataElement );
                    compulsory = BooleanUtils.toStringTrueFalse( psde.isCompulsory());
                }
                
                if ( dataElement == null )
                {
                    continue;
                }
                if ( !DataElement.VALUE_TYPE_INT.equals( dataElement.getType() )
                    && !DataElement.VALUE_TYPE_STRING.equals( dataElement.getType() ) )
                {
                    continue;
                }
                // -------------------------------------------------------------
                // Find type of data element
                // -------------------------------------------------------------

                String dataElementType = dataElement.getDetailedNumberType();

                // -------------------------------------------------------------
                // Find existing value of data element in data set
                // -------------------------------------------------------------

                PatientDataValue patientDataValue = null;

                String dataElementValue = EMPTY;

                if ( programStageId != programStage.getId() )
                {
                    Collection<PatientDataValue> patientDataValues = mapDataValue.get( programStageId );

                    if ( patientDataValues == null )
                    {
                        ProgramStage otherProgramStage = programStageService.getProgramStage( programStageId );
                        ProgramStageInstance otherProgramStageInstance = programStageInstanceService
                            .getProgramStageInstance( programStageInstance.getProgramInstance(), otherProgramStage );
                        patientDataValues = patientDataValueService.getPatientDataValues( 
                            otherProgramStageInstance );
                        mapDataValue.put( programStageId, patientDataValues );
                    }

                    patientDataValue = getValue( patientDataValues, dataElementId , optionComboId );
//                    logger.info( "patientDataValue: " + patientDataValue );
                    dataElementValue = patientDataValue != null ? patientDataValue.getValue() : dataElementValue;
                }
                else
                {
                    if ( (dataElement instanceof CalculatedDataElement) )
                    {
                        CalculatedDataElement cde = (CalculatedDataElement) dataElement;

                        if ( cde.isSaved() )
                        {
                            patientDataValue = getValue( dataValues, dataElementId );

                            dataElementValue = patientDataValue != null ? patientDataValue.getValue()
                                : dataElementValue;
                        }
                        else
                        {
                            dataElementValue = String.valueOf( calculatedValueMap.get( cde ) );
                        }
                    }
                    else
                    {
                        patientDataValue = getValue( dataValues, dataElementId );

                        dataElementValue = patientDataValue != null ? patientDataValue.getValue() : dataElementValue;

                    }
                }
                // -------------------------------------------------------------
                // Insert value of data element in output code
                // -------------------------------------------------------------

                if ( dataElementCode.contains( "value=\"\"" ) )
                {
                    dataElementCode = dataElementCode.replace( "value=\"\"", "value=\"" + dataElementValue + "\"" );
                }
                else
                {
                    dataElementCode += "value=\"" + dataElementValue + "\"";
                }

                // -------------------------------------------------------------
                // Remove placeholder view attribute from input field
                // -------------------------------------------------------------

                dataElementCode = dataElementCode.replaceAll( "view=\".*?\"", "" );

                // -------------------------------------------------------------
                // Append Javascript code and meta data (type/min/max) for
                // persisting to output code, and insert value and type for
                // fields
                // -------------------------------------------------------------

                String appendCode = dataElementCode;

                appendCode += jsCodeForInputs;

                if ( (dataElement instanceof CalculatedDataElement) )
                {
                    appendCode += calDataElementCode;
                }

                appendCode += " />";

                if ( !dataElement.getAggregationOperator().equalsIgnoreCase( DataElement.AGGREGATION_OPERATOR_SUM ) )
                {
                    saveMode = true;
                }

                // appendCode += metaDataCode;

                // -----------------------------------------------------------
                // Check if this dataElement is from another programStage then
                // disable
                // If programStagsInstance is completed then disabled it
                // -----------------------------------------------------------
                
                disabled = "";
                if( programStageId == programStage.getId() && !programStageInstance.isCompleted())
                {
                    // -----------------------------------------------------------
                    // Add ProvidedByOtherFacility checkbox
                    // -----------------------------------------------------------
                    appendCode = addProvidedByOtherFacilityCheckbox( appendCode, patientDataValue );

                }else {
                        disabled = "disabled=\"\"";
                }
                
                // -----------------------------------------------------------
                // 
                // -----------------------------------------------------------
                String orgUnitName = i18n.getString(NOTAVAILABLE);
                if( patientDataValue != null )
                {
                    if( patientDataValue.isProvidedByAnotherFacility() )
                    {
                        orgUnitName = i18n.getString(UNKNOW_CLINIC);
                    }else {
                        orgUnitName = patientDataValue.getOrganisationUnit().getName();
                    }
                }
                
                appendCode = appendCode.replace( "$DATAELEMENTID", String.valueOf( dataElementId ) );
                appendCode = appendCode.replace( "$PROGRAMSTAGEID", String.valueOf( programStageId ) );
                appendCode = appendCode.replace( "$PROGRAMSTAGENAME", programStageName );
                appendCode = appendCode.replace( "$ORGUNITNAME",  orgUnitName );
                appendCode = appendCode.replace( "$OPTIONCOMBOID",  String.valueOf( optionComboId ));
                appendCode = appendCode.replace( "$DATAELEMENTNAME", dataElement.getName() );
                appendCode = appendCode.replace( "$DATAELEMENTTYPE", dataElementType );
                appendCode = appendCode.replace( "$DISABLED", disabled );
                appendCode = appendCode.replace( "$COMPULSORY",  compulsory );
                appendCode = appendCode.replace( "$SAVEMODE", "" + saveMode + "" );
//                logger.info( "after add js : " + appendCode );

                dataElementMatcher.appendReplacement( sb, appendCode );
            }
        }

        dataElementMatcher.appendTail( sb );

        return sb.toString();
    }

    private String populateCustomDataEntryForBoolean( String dataEntryFormCode,
        Collection<PatientDataValue> dataValues, Map<CalculatedDataElement, Integer> calculatedValueMap,
        Map<Integer, MinMaxDataElement> minMaxMap, String disabled, Boolean saveMode, I18n i18n,
        ProgramStage programStage, ProgramStageInstance programStageInstance, OrganisationUnit organisationUnit,
        Map<Integer, Collection<PatientDataValue>> mapDataValue )
    {

        // ---------------------------------------------------------------------
        // Inline Javascript to add to HTML before outputting
        // ---------------------------------------------------------------------

        final String jsCodeForBoolean = " name=\"entryselect\" data=\"{compulsory:$COMPULSORY, dataElementId:$DATAELEMENTID, dataElementName:'$DATAELEMENTNAME', dataElementType:'$DATAELEMENTTYPE', programStageId:$PROGRAMSTAGEID, programStageName: '$PROGRAMSTAGENAME', orgUnitName:'$ORGUNITNAME'}\" $DISABLED onchange=\"saveChoiceCustom( $PROGRAMSTAGEID, $DATAELEMENTID,this)\"";
        // final String historyCode =
        // " ondblclick='javascript:viewHistory( $DATAELEMENTID, $OPTIONCOMBOID, true )' ";
        final String calDataElementCode = " class=\"calculated\" disabled ";

        // ---------------------------------------------------------------------
        // Metadata code to add to HTML before outputting
        // ---------------------------------------------------------------------

        final String metaDataCode = "<span id=\"value[$DATAELEMENTID].name\" style=\"display:none\">$DATAELEMENTNAME</span>"
            + "<span id=\"value[$DATAELEMENTID].type\" style=\"display:none\">$DATAELEMENTTYPE</span>";
        StringBuffer sb = new StringBuffer();

        // ---------------------------------------------------------------------
        // Pattern to match data elements in the HTML code
        // ---------------------------------------------------------------------

        Pattern dataElementPattern = Pattern.compile( "(<select.*?)[/]?</select>", Pattern.DOTALL );
        Matcher dataElementMatcher = dataElementPattern.matcher( dataEntryFormCode );

        // ---------------------------------------------------------------------
        // Pattern to extract data element ID from data element field
        // ---------------------------------------------------------------------

        Pattern identifierPattern = Pattern.compile( "value\\[(.*)\\].boolean:value\\[(.*)\\].boolean" );

        // ---------------------------------------------------------------------
        // Iterate through all matching data element fields
        // ---------------------------------------------------------------------

        Map<Integer, DataElement> dataElementMap = getDataElementMap( programStage );

        
        while ( dataElementMatcher.find() )
        {
            // -----------------------------------------------------------------
            // Get HTML input field code
            // -----------------------------------------------------------------
            String compulsory = "null"; 
            String dataElementCode = dataElementMatcher.group( 1 );
            Matcher identifierMatcher = identifierPattern.matcher( dataElementCode );
            if ( identifierMatcher.find() && identifierMatcher.groupCount() > 0 )
            {
                // -------------------------------------------------------------
                // Get data element ID of data element
                // -------------------------------------------------------------

                int programStageId = Integer.parseInt( identifierMatcher.group( 1 ) );
//                logger.info( "programStageId:  " + programStageId );
                int dataElementId = Integer.parseInt( identifierMatcher.group( 2 ) );

                DataElement dataElement = null;

                String programStageName = programStage.getName();

                
                if ( programStageId != programStage.getId() )
                {
                    dataElement = dataElementService.getDataElement( dataElementId );
                    
                    ProgramStage otherProgramStage = programStageService.getProgramStage( programStageId );
                    programStageName = otherProgramStage != null ? otherProgramStage.getName() : "N/A";
                }
                else
                {
                    dataElement = dataElementMap.get( dataElementId );
                    ProgramStageDataElement psde = programStageDataElementService.get( programStage, dataElement );
                    compulsory = BooleanUtils.toStringTrueFalse( psde.isCompulsory());
                }

                if ( dataElement == null )
                {
                    continue;
                }

                if ( !DataElement.VALUE_TYPE_BOOL.equals( dataElement.getType() ) )
                {
                    continue;
                }
                // -------------------------------------------------------------
                // Find type of data element
                // -------------------------------------------------------------

                String dataElementType = dataElement.getType();

                // -------------------------------------------------------------
                // Find existing value of data element in data set
                // -------------------------------------------------------------

                PatientDataValue patientDataValue = null;

                String dataElementValue = EMPTY;

                if ( programStageId != programStage.getId() )
                {
                    Collection<PatientDataValue> patientDataValues = mapDataValue.get( programStageId );

                    if ( patientDataValues == null )
                    {
                        ProgramStage otherProgramStage = programStageService.getProgramStage( programStageId );
                        ProgramStageInstance otherProgramStageInstance = programStageInstanceService
                            .getProgramStageInstance( programStageInstance.getProgramInstance(), otherProgramStage );
                        patientDataValues = patientDataValueService.getPatientDataValues( 
                            otherProgramStageInstance );
                        mapDataValue.put( programStageId, patientDataValues );
                    }

                    patientDataValue = getValue( patientDataValues, dataElementId );
//                    logger.info( "patientDataValue: " + patientDataValue );
                    dataElementValue = patientDataValue != null ? patientDataValue.getValue() : dataElementValue;
                }
                else
                {

                    if ( (dataElement instanceof CalculatedDataElement) )
                    {
                        CalculatedDataElement cde = (CalculatedDataElement) dataElement;

                        if ( cde.isSaved() )
                        {
                            patientDataValue = getValue( dataValues, dataElementId );
                            if ( patientDataValue != null )
                                dataElementValue = patientDataValue.getValue();
                        }
                        else
                        {
                            dataElementValue = String.valueOf( calculatedValueMap.get( cde ) );
                        }
                    }
                    else
                    {
                        patientDataValue = getValue( dataValues, dataElementId );
                        if ( patientDataValue != null )
                            dataElementValue = patientDataValue.getValue();

                    }
                }

                String appendCode = dataElementCode;
                appendCode = appendCode.replace( "name=\"entryselect\"", jsCodeForBoolean );
                // -------------------------------------------------------------
                // Insert value of data element in output code
                // -------------------------------------------------------------
                if ( patientDataValue != null )
                {
    
                    if ( dataElementValue.equalsIgnoreCase( "true" ) )
                    {
                        appendCode = appendCode.replace( "<option value=\"true\">", "<option value=\""
                            + i18n.getString( "true" ) + "\" selected>" );
                    }
    
                    if ( dataElementValue.equalsIgnoreCase( "false" ) )
                    {
                        appendCode = appendCode.replace( "<option value=\"false\">", "<option value=\""
                            + i18n.getString( "false" ) + "\" selected>" );
                    }
    
                }

                appendCode += "</select>";
                // -------------------------------------------------------------
                // Remove placeholder view attribute from input field
                // -------------------------------------------------------------

                dataElementCode = dataElementCode.replaceAll( "view=\".*?\"", "" );

                // -------------------------------------------------------------
                // Insert title information - Data element id, name, type, min,
                // max
                // -------------------------------------------------------------

                if ( dataElementCode.contains( "title=\"\"" ) )
                {
                    dataElementCode = dataElementCode.replace( "title=\"\"", "title=\"-- ID:" + dataElement.getId()
                        + " Name:" + dataElement.getShortName() + " Type:" + dataElement.getType() + "\"" );
                }
                else
                {
                    dataElementCode += "title=\"-- ID:" + dataElement.getId() + " Name:" + dataElement.getShortName()
                        + " Type:" + dataElement.getType() + "\"";
                }

                // -------------------------------------------------------------
                // Append Javascript code and meta data (type/min/max) for
                // persisting to output code, and insert value and type for
                // fields
                // -------------------------------------------------------------

                if ( (dataElement instanceof CalculatedDataElement) )
                {
                    appendCode += calDataElementCode;
                }

                if ( !dataElement.getAggregationOperator().equalsIgnoreCase( DataElement.AGGREGATION_OPERATOR_SUM ) )
                {
                    saveMode = true;
                }

                appendCode += metaDataCode;

                // -----------------------------------------------------------
                // Check if this dataElement is from another programStage then
                // disable
                // If programStagsInstance is completed then disabled it
                // -----------------------------------------------------------
                
//                System.out.println("programStageInstance.isCompleted() : "+programStageInstance.isCompleted() +"id: "+programStageInstance.getId());
                disabled = "";
                if ( programStageId != programStage.getId() || programStageInstance.isCompleted() )
                {
                    disabled = "disabled";
                }
                else 
                {
                    // -----------------------------------------------------------
                    // Add ProvidedByOtherFacility checkbox
                    // -----------------------------------------------------------
                    appendCode = addProvidedByOtherFacilityCheckbox( appendCode, patientDataValue );
                }
                
                // -----------------------------------------------------------
                // 
                // -----------------------------------------------------------
                String orgUnitName = i18n.getString(NOTAVAILABLE);
                if( patientDataValue != null )
                {
                    if( patientDataValue.isProvidedByAnotherFacility() )
                    {
                        orgUnitName = i18n.getString(UNKNOW_CLINIC);
                    }else {
                        orgUnitName = patientDataValue.getOrganisationUnit().getName();
                    }
                }

                appendCode = appendCode.replace( "$DATAELEMENTID", String.valueOf( dataElementId ) );
                appendCode = appendCode.replace( "$PROGRAMSTAGEID", String.valueOf( programStageId ) );
                appendCode = appendCode.replace( "$PROGRAMSTAGENAME",  programStageName );
                appendCode = appendCode.replace( "$ORGUNITNAME",  orgUnitName );
                appendCode = appendCode.replace( "$DATAELEMENTNAME", dataElement.getName() );
                appendCode = appendCode.replace( "$DATAELEMENTTYPE", dataElementType );
                appendCode = appendCode.replace( "$DISABLED", disabled );
                appendCode = appendCode.replace( "$COMPULSORY",  compulsory );
                appendCode = appendCode.replace( "i18n_yes", i18n.getString( "yes" ) );
                appendCode = appendCode.replace( "i18n_no", i18n.getString( "no" ) );
                appendCode = appendCode.replace( "i18n_select_value", i18n.getString( "select_value" ) );
                appendCode = appendCode.replace( "$SAVEMODE", "" + saveMode + "" );
                
                appendCode = appendCode.replaceAll( "\\$", "\\\\\\$" );

                dataElementMatcher.appendReplacement( sb, appendCode );
            }
        }

        dataElementMatcher.appendTail( sb );

        return sb.toString();
    }

    private String populateCustomDataEntryForMutiDimentionalString( String dataEntryFormCode,
        Collection<PatientDataValue> dataValues, Map<CalculatedDataElement, Integer> calculatedValueMap,
        Map<Integer, MinMaxDataElement> minMaxMap, String disabled, Boolean saveMode, I18n i18n,
        ProgramStage programStage, ProgramStageInstance programStageInstance, OrganisationUnit organisationUnit,
        Map<Integer, Collection<PatientDataValue>> mapDataValue )
    {

        // ---------------------------------------------------------------------
        // Inline Javascript to add to HTML before outputting
        // ---------------------------------------------------------------------

        final String jsCodeForCombo = " name=\"entryselect\" $DISABLED data=\"{compulsory:$COMPULSORY, dataElementId:$DATAELEMENTID, dataElementName:'$DATAELEMENTNAME', dataElementType:'$DATAELEMENTTYPE', programStageId:$PROGRAMSTAGEID, programStageName: '$PROGRAMSTAGENAME', orgUnitName:'$ORGUNITNAME'}\" onchange=\"saveChoiceCustom( $PROGRAMSTAGEID, $DATAELEMENTID,this)\"";
        // final String historyCode =
        // " ondblclick='javascript:viewHistory( $DATAELEMENTID, $OPTIONCOMBOID, true )' ";
        final String calDataElementCode = " class=\"calculated\" disabled ";

        // ---------------------------------------------------------------------
        // Metadata code to add to HTML before outputting
        // ---------------------------------------------------------------------

        final String metaDataCode = "<span id=\"value[$DATAELEMENTID].name\" style=\"display:none\">$DATAELEMENTNAME</span>"
            + "<span id=\"value[$DATAELEMENTID].type\" style=\"display:none\">$DATAELEMENTTYPE</span>";
        StringBuffer sb = new StringBuffer();

        // ---------------------------------------------------------------------
        // Pattern to match data elements in the HTML code
        // ---------------------------------------------------------------------

        Pattern dataElementPattern = Pattern.compile( "(<select.*?)[/]?</select>", Pattern.DOTALL );
        Matcher dataElementMatcher = dataElementPattern.matcher( dataEntryFormCode );

        // ---------------------------------------------------------------------
        // Pattern to extract data element ID from data element field
        // ---------------------------------------------------------------------

        Pattern identifierPattern = Pattern
            .compile( "\"value\\[([\\p{Digit}.]*)\\].combo:value\\[([\\p{Digit}.]*)\\].combo\"" );

        // ---------------------------------------------------------------------
        // Iterate through all matching data element fields
        // ---------------------------------------------------------------------

        Map<Integer, DataElement> dataElementMap = getDataElementMap( programStage );
        
       
        
        while ( dataElementMatcher.find() )
        {
            // -----------------------------------------------------------------
            // Get HTML input field code
            // -----------------------------------------------------------------

            String dataElementCode = dataElementMatcher.group( 1 );
            
//            System.out.println("boolean dataElementCode : "+dataElementCode);
            
            Matcher identifierMatcher = identifierPattern.matcher( dataElementCode );
            
            String compulsory = "null";
            
            if ( identifierMatcher.find() && identifierMatcher.groupCount() > 0 )
            {

                // -------------------------------------------------------------
                // Get data element ID of data element
                // -------------------------------------------------------------
                int programStageId = Integer.parseInt( identifierMatcher.group( 1 ) );
                int dataElementId = Integer.parseInt( identifierMatcher.group( 2 ) );

                DataElement dataElement = null;

                String programStageName = programStage.getName();

                
                if ( programStageId != programStage.getId() )
                {
                    dataElement = dataElementService.getDataElement( dataElementId );
                    
                    ProgramStage otherProgramStage = programStageService.getProgramStage( programStageId );
                    programStageName = otherProgramStage != null ? otherProgramStage.getName() : "N/A";
                }
                else
                {
                    dataElement = dataElementMap.get( dataElementId ); 
                    ProgramStageDataElement psde = programStageDataElementService.get( programStage, dataElement );
                    compulsory = BooleanUtils.toStringTrueFalse( psde.isCompulsory());
                }

                if ( dataElement == null )
                {
                    continue;
                }
                if ( !DataElement.VALUE_TYPE_STRING.equals( dataElement.getType() ) )
                {
                    continue;
                }
                // -------------------------------------------------------------
                // Find type of data element
                // -------------------------------------------------------------

                String dataElementType = dataElement.getType();

                // -------------------------------------------------------------
                // Find existing value of data element in data set
                // -------------------------------------------------------------
                PatientDataValue patientDataValue = null;
                String dataElementValue = EMPTY;
                if ( programStageId != programStage.getId() )
                {
                    Collection<PatientDataValue> patientDataValues = mapDataValue.get( programStageId );

                    if ( patientDataValues == null )
                    {
                        ProgramStage otherProgramStage = programStageService.getProgramStage( programStageId );
                        ProgramStageInstance otherProgramStageInstance = programStageInstanceService
                            .getProgramStageInstance( programStageInstance.getProgramInstance(), otherProgramStage );
                        patientDataValues = patientDataValueService.getPatientDataValues( 
                            otherProgramStageInstance );
                        mapDataValue.put( programStageId, patientDataValues );
                    }

                    patientDataValue = getValue( patientDataValues, dataElementId );
//                    logger.info( "patientDataValue: " + patientDataValue );
                    dataElementValue = patientDataValue != null ? patientDataValue.getValue() : dataElementValue;
                }
                else
                {
                    if ( (dataElement instanceof CalculatedDataElement) )
                    {
                        CalculatedDataElement cde = (CalculatedDataElement) dataElement;

                        if ( cde.isSaved() )
                        {
                            patientDataValue = getValue( dataValues, dataElementId );

                            dataElementValue = patientDataValue != null ? patientDataValue.getValue()
                                : dataElementValue;
                        }
                        else
                        {
                            dataElementValue = String.valueOf( calculatedValueMap.get( cde ) );
                        }
                    }
                    else
                    {
                        patientDataValue = getValue( dataValues, dataElementId );

                        dataElementValue = patientDataValue != null ? patientDataValue.getValue() : dataElementValue;
                    }
                }

                String appendCode = dataElementCode;
                appendCode = appendCode.replace( "name=\"entryselect\"", jsCodeForCombo );
                // -------------------------------------------------------------
                // Insert value of data element in output code
                // -------------------------------------------------------------
                if( patientDataValue != null )
                {
//                    System.out.println("optioncomboId: "+patientDataValue.getOptionCombo().getId() + " ===name: "+ patientDataValue.getOptionCombo().getName());
                    appendCode = appendCode.replace( "id=\"combo["+patientDataValue.getOptionCombo().getId()+"].combo\"", "id=\"combo["+patientDataValue.getOptionCombo().getId()+"].combo\" selected=\"selected\"");
//                    System.out.println("appendCode: "+appendCode);
                }

                appendCode += "</select>";
                // -------------------------------------------------------------
                // Remove placeholder view attribute from input field
                // -------------------------------------------------------------

                dataElementCode = dataElementCode.replaceAll( "view=\".*?\"", "" );

                // -------------------------------------------------------------
                // Insert title information - Data element id, name, type, min,
                // max
                // -------------------------------------------------------------

                if ( dataElementCode.contains( "title=\"\"" ) )
                {
                    dataElementCode = dataElementCode.replace( "title=\"\"", "title=\"-- ID:" + dataElement.getId()
                        + " Name:" + dataElement.getShortName() + " Type:" + dataElement.getType() + "\"" );
                }
                else
                {
                    dataElementCode += "title=\"-- ID:" + dataElement.getId() + " Name:" + dataElement.getShortName()
                        + " Type:" + dataElement.getType() + "\"";
                }

                // -------------------------------------------------------------
                // Append Javascript code and meta data (type/min/max) for
                // persisting to output code, and insert value and type for
                // fields
                // -------------------------------------------------------------

                if ( (dataElement instanceof CalculatedDataElement) )
                {
                    appendCode += calDataElementCode;
                }

                if ( !dataElement.getAggregationOperator().equalsIgnoreCase( DataElement.AGGREGATION_OPERATOR_SUM ) )
                {
                    saveMode = true;
                }

                appendCode += metaDataCode;

                // -----------------------------------------------------------
                // Check if this dataElement is from another programStage then
                // disable
                // If programStagsInstance is completed then disabled it
                // -----------------------------------------------------------
                disabled = "";
                if ( programStageId != programStage.getId() || programStageInstance.isCompleted() )
                {
                    disabled = "disabled";
                }
                else
                {
                    // -----------------------------------------------------------
                    // Add ProvidedByOtherFacility checkbox
                    // -----------------------------------------------------------
                    appendCode = addProvidedByOtherFacilityCheckbox( appendCode, patientDataValue );
                }

                // -----------------------------------------------------------
                // 
                // -----------------------------------------------------------
                String orgUnitName = i18n.getString(NOTAVAILABLE);
                if( patientDataValue != null )
                {
                    if( patientDataValue.isProvidedByAnotherFacility() )
                    {
                        orgUnitName = i18n.getString(UNKNOW_CLINIC);
                    }else {
                        orgUnitName = patientDataValue.getOrganisationUnit().getName();
                    }
                }
                
                appendCode = appendCode.replace( "$DATAELEMENTID", String.valueOf( dataElementId ) );
                appendCode = appendCode.replace( "$PROGRAMSTAGEID", String.valueOf( programStageId ) );
                appendCode = appendCode.replace( "$PROGRAMSTAGENAME", programStageName );
                appendCode = appendCode.replace( "$ORGUNITNAME",  orgUnitName );
                appendCode = appendCode.replace( "$DATAELEMENTNAME", dataElement.getName() );
                appendCode = appendCode.replace( "$DATAELEMENTTYPE", dataElementType );
                appendCode = appendCode.replace( "$DISABLED", disabled );
                appendCode = appendCode.replace( "$COMPULSORY",  compulsory );
                appendCode = appendCode.replace( "i18n_select_value", i18n.getString( "select_value" ) );
                appendCode = appendCode.replace( "$SAVEMODE", "" + saveMode + "" );
                appendCode = appendCode.replaceAll( "\\$", "\\\\\\$" );

                dataElementMatcher.appendReplacement( sb, appendCode );
            }
        }

        dataElementMatcher.appendTail( sb );

        return sb.toString();
    }

   

    private String populateCustomDataEntryForDate( String dataEntryFormCode, Collection<PatientDataValue> dataValues,
        Map<CalculatedDataElement, Integer> calculatedValueMap, Map<Integer, MinMaxDataElement> minMaxMap,
        String disabled, Boolean saveMode, I18n i18n, ProgramStage programStage,
        ProgramStageInstance programStageInstance, OrganisationUnit organisationUnit,
        Map<Integer, Collection<PatientDataValue>> mapDataValue )
    {

        // ---------------------------------------------------------------------
        // Inline Javascript to add to HTML before outputting
        // ---------------------------------------------------------------------

        final String jsCodeForDate = " name=\"entryfield\" $DISABLED onchange=\"saveDateCustom( this )\" data=\"{compulsory:$COMPULSORY, dataElementId:$DATAELEMENTID, dataElementName:'$DATAELEMENTNAME', dataElementType:'$DATAELEMENTTYPE', programStageId:$PROGRAMSTAGEID, programStageName: '$PROGRAMSTAGENAME', orgUnitName:'$ORGUNITNAME'}\"";
        // final String historyCode =
        // " ondblclick='javascript:viewHistory( $DATAELEMENTID, $OPTIONCOMBOID, true )' ";
        //final String calDataElementCode = " class=\"calculated\" disabled ";

        // ---------------------------------------------------------------------
        // Metadata code to add to HTML before outputting
        // ---------------------------------------------------------------------

        final String metaDataCode = "<span id=\"value[$DATAELEMENTID].name\" style=\"display:none\">$DATAELEMENTNAME</span>"
            + "<span id=\"value[$DATAELEMENTID].type\" style=\"display:none\">$DATAELEMENTTYPE</span>";
        StringBuffer sb = new StringBuffer();

        // ---------------------------------------------------------------------
        // Pattern to match data elements in the HTML code
        // ---------------------------------------------------------------------

        Pattern dataElementPattern = Pattern.compile( "(<input.*?)[/]?</script>" );
        Matcher dataElementMatcher = dataElementPattern.matcher( dataEntryFormCode );

        // ---------------------------------------------------------------------
        // Pattern to extract data element ID from data element field
        // ---------------------------------------------------------------------

        Pattern identifierPattern = Pattern.compile( "id=\"value\\[([\\p{Digit}.]*)\\].date:value\\[([\\p{Digit}.]*)\\].date\"" );

        // ---------------------------------------------------------------------
        // Iterate through all matching data element fields
        // ---------------------------------------------------------------------

        Map<Integer, DataElement> dataElementMap = getDataElementMap( programStageInstance.getProgramStage() );

        
        while ( dataElementMatcher.find() )
        {
            // -----------------------------------------------------------------
            // Get HTML input field code
            // -----------------------------------------------------------------
            String compulsory = "null";
            String dataElementCode = dataElementMatcher.group( 1 );
//            System.out.println("DATE+++++++=====================================================");
//            System.out.println(dataElementCode);
            Matcher identifierMatcher = identifierPattern.matcher( dataElementCode );

            if ( identifierMatcher.find() && identifierMatcher.groupCount() > 0 )
            {
                // -------------------------------------------------------------
                // Get data element ID of data element
                // -------------------------------------------------------------
                int programStageId = Integer.parseInt( identifierMatcher.group( 1 ) );
                int dataElementId = Integer.parseInt( identifierMatcher.group( 2 ) );

                DataElement dataElement = null;

                String programStageName = programStage.getName();

                
                if ( programStageId != programStage.getId() )
                {
                    dataElement = dataElementService.getDataElement( dataElementId );
                    
                    ProgramStage otherProgramStage = programStageService.getProgramStage( programStageId );
                    programStageName = otherProgramStage != null ? otherProgramStage.getName() : "N/A";
                }
                else
                {
                    dataElement = dataElementMap.get( dataElementId ); 
                    ProgramStageDataElement psde = programStageDataElementService.get( programStage, dataElement );
                    compulsory = BooleanUtils.toStringTrueFalse( psde.isCompulsory());
                }

                if ( dataElement == null )
                {
                    continue;
                }
                if ( !DataElement.VALUE_TYPE_DATE.equals( dataElement.getType() ) )
                {
                    continue;
                }
                // -------------------------------------------------------------
                // Find type of data element
                // -------------------------------------------------------------

                String dataElementType = dataElement.getType();

                // -------------------------------------------------------------
                // Find existing value of data element in data set
                // -------------------------------------------------------------

                PatientDataValue patientDataValue = null;
                String dataElementValue = EMPTY;

                if ( programStageId != programStage.getId() )
                {
                    Collection<PatientDataValue> patientDataValues = mapDataValue.get( programStageId );

                    if ( patientDataValues == null )
                    {
                        ProgramStage otherProgramStage = programStageService.getProgramStage( programStageId );
                        ProgramStageInstance otherProgramStageInstance = programStageInstanceService
                            .getProgramStageInstance( programStageInstance.getProgramInstance(), otherProgramStage );
                        patientDataValues = patientDataValueService.getPatientDataValues( 
                            otherProgramStageInstance );
                        mapDataValue.put( programStageId, patientDataValues );
                    }

                    patientDataValue = getValue( patientDataValues, dataElementId );
//                    logger.info( "patientDataValue: " + patientDataValue );
                    dataElementValue = patientDataValue != null ? patientDataValue.getValue() : dataElementValue;
                }
                else
                {

                    if ( (dataElement instanceof CalculatedDataElement) )
                    {
                        CalculatedDataElement cde = (CalculatedDataElement) dataElement;

                        if ( cde.isSaved() )
                        {
                            patientDataValue = getValue( dataValues, dataElementId );

                            dataElementValue = patientDataValue != null ? patientDataValue.getValue()
                                : dataElementValue;
                        }
                        else
                        {
                            dataElementValue = String.valueOf( calculatedValueMap.get( cde ) );
                        }
                    }
                    else
                    {
                        patientDataValue = getValue( dataValues, dataElementId );

                        dataElementValue = patientDataValue != null ? patientDataValue.getValue() : dataElementValue;

                    }
                }

                // -------------------------------------------------------------
                // Insert value of data element in output code
                // -------------------------------------------------------------

                if ( dataElementCode.contains( "value=\"\"" ) )
                {
                    dataElementCode = dataElementCode.replace( "value=\"\"", "value=\"" + dataElementValue + "\"" );
                }
                else
                {
                    dataElementCode += "value=\"" + dataElementValue + "\"";
                }

                // -------------------------------------------------------------
                // Remove placeholder view attribute from input field
                // -------------------------------------------------------------

                dataElementCode = dataElementCode.replaceAll( "view=\".*?\"", "" );

                // -------------------------------------------------------------
                // Append Javascript code and meta data (type/min/max) for
                // persisting to output code, and insert value and type for
                // fields
                // -------------------------------------------------------------

                String appendCode = dataElementCode;
                appendCode = appendCode.replace( "name=\"entryfield\"", jsCodeForDate );
                appendCode += "</script>";

                if ( !dataElement.getAggregationOperator().equalsIgnoreCase( DataElement.AGGREGATION_OPERATOR_SUM ) )
                {
                    saveMode = true;
                }

                appendCode += metaDataCode;

                // -----------------------------------------------------------
                // Check if this dataElement is from another programStage then
                // disable
                // If programStagsInstance is completed then disabled it
                // -----------------------------------------------------------
                disabled = "";
                if ( programStageId != programStage.getId() || programStageInstance.isCompleted() )
                {
                    disabled = "disabled=\"\"";
                }
                else
                {
                    // -----------------------------------------------------------
                    // Add ProvidedByOtherFacility checkbox
                    // -----------------------------------------------------------
                    appendCode = addProvidedByOtherFacilityCheckbox( appendCode, patientDataValue );
                }
                
                // -----------------------------------------------------------
                // Get Org Unit name
                // -----------------------------------------------------------
                String orgUnitName = i18n.getString(NOTAVAILABLE);
                if( patientDataValue != null )
                {
                    if( patientDataValue.isProvidedByAnotherFacility() )
                    {
                        orgUnitName = i18n.getString(UNKNOW_CLINIC);
                    }else {
                        orgUnitName = patientDataValue.getOrganisationUnit().getName();
                    }
                }

                appendCode = appendCode.replace( "$DATAELEMENTID", String.valueOf( dataElementId ) );
                appendCode = appendCode.replace( "$PROGRAMSTAGEID", String.valueOf( programStageId ) );
                appendCode = appendCode.replace( "$PROGRAMSTAGENAME",  programStageName );
                appendCode = appendCode.replace( "$ORGUNITNAME",  orgUnitName );
                appendCode = appendCode.replace( "$DATAELEMENTNAME", dataElement.getName() );
                appendCode = appendCode.replace( "$DATAELEMENTTYPE", dataElementType );
                appendCode = appendCode.replace( "$DISABLED", disabled );
                appendCode = appendCode.replace( "$COMPULSORY",  compulsory );
                appendCode = appendCode.replace( "$SAVEMODE", "" + saveMode + "" );
                appendCode = appendCode.replaceAll( "\\$", "\\\\\\$" );

                dataElementMatcher.appendReplacement( sb, appendCode );
            }
        }

        dataElementMatcher.appendTail( sb );

        return sb.toString();
    }

   
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Returns the value of the PatientDataValue in the Collection of DataValues
     * with the given data element identifier and category option combo id.
     */
    private PatientDataValue getValue( Collection<PatientDataValue> dataValues, int dataElementId,
        int categoryOptionComboId )
    {
        for ( PatientDataValue dataValue : dataValues )
        {
            if ( dataValue.getOptionCombo() != null )
            {
                if ( dataValue.getDataElement().getId() == dataElementId
                    && dataValue.getOptionCombo().getId() == categoryOptionComboId )
                {
                    return dataValue;
                }
            }
        }

        return null;
    }

    /**
     * Returns the value of the PatientDataValue in the Collection of DataValues
     * with the given data element identifier.
     */
    private PatientDataValue getValue( Collection<PatientDataValue> dataValues, int dataElementId )
    {
        for ( PatientDataValue dataValue : dataValues )
        {
            if ( dataValue.getDataElement().getId() == dataElementId )
            {
                return dataValue;
            }
        }

        return null;
    }

    /**
     * Returns a Map of all DataElements in the given ProgramStage where the key
     * is the DataElement identifier and the value is the DataElement.
     */
    private Map<Integer, DataElement> getDataElementMap( ProgramStage programStage )
    {
        Collection<DataElement> dataElements = programStageDataElementService.getListDataElement( programStage );
       
        if ( programStage == null )
        {
            return null;
        }
        Map<Integer, DataElement> map = new HashMap<Integer, DataElement>();

        for ( DataElement element : dataElements )
        {
            map.put( element.getId(), element );
        }

        return map;
    }

    /**
     * Append a ProvidedByOtherFacility Checkbox to the html code
     * 
     * @param appendCode: current html code
     * @param patientDataValue: currrent PatientDataValue
     * @return full html code after append the check box
     */
    private String addProvidedByOtherFacilityCheckbox( String appendCode, PatientDataValue patientDataValue )
    {
        appendCode += "<label for=\"$PROGRAMSTAGEID_$DATAELEMENTID_facility\" title=\"is provided by another Facility ?\" ></label><input name=\"providedByAnotherFacility\"  title=\"is provided by another Facility ?\"  id=\"$PROGRAMSTAGEID_$DATAELEMENTID_facility\"  type=\"checkbox\" ";
//        appendCoe +="<input name=\"providedByAnotherFacility\"  title=\"is provided by another Facility ?\"  id=\"$PROGRAMSTAGEID_$DATAELEMENTID_facility\"  type=\"checkbox\" ";

        if ( patientDataValue != null && patientDataValue.isProvidedByAnotherFacility() )
        {
            appendCode += " checked=\"checked\" ";
        }
        appendCode += "onChange=\"updateProvidingFacilityCustom( $PROGRAMSTAGEID, $DATAELEMENTID, this )\"  >";
        
        return appendCode;

    }

    private String populateI18nStrings( String dataEntryFormCode, I18n i18n )
    {
         StringBuffer sb = new StringBuffer();
                
         // ---------------------------------------------------------------------
         // Pattern to match i18n strings in the HTML code
         // ---------------------------------------------------------------------

         //Pattern i18nPattern = Pattern.compile( "(<i18n::.*?)[/]?>", Pattern.DOTALL );
         Pattern i18nPattern = Pattern.compile( "(<i18n.*?)[/]?</i18n>", Pattern.DOTALL );
         Matcher i18nMatcher = i18nPattern.matcher( dataEntryFormCode );
       
         // ---------------------------------------------------------------------
         // Iterate through all matching i18n element fields
         // ---------------------------------------------------------------------

         while ( i18nMatcher.find() )
         {             
             String i18nCode = i18nMatcher.group( 1 );

             i18nCode = i18nCode.replaceAll("<i18n>", "");                      
             
             i18nCode = i18n.getString( i18nCode );
             
             i18nMatcher.appendReplacement( sb, i18nCode );             
         }

         i18nMatcher.appendTail( sb );
         
         String result = sb.toString();
         
         result.replaceAll("</i18n>", "");

         return result;
    }

}
