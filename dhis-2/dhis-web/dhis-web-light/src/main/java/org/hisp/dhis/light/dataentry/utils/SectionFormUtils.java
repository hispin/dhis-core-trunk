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

package org.hisp.dhis.light.dataentry.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.hisp.dhis.dataanalysis.DataAnalysisService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.datavalue.DeflatedDataValue;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.minmax.MinMaxDataElement;
import org.hisp.dhis.minmax.MinMaxDataElementService;
import org.hisp.dhis.minmax.validation.MinMaxValuesGenerationService;
import org.hisp.dhis.options.SystemSettingManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.system.util.ListUtils;
import org.hisp.dhis.validation.ValidationResult;
import org.hisp.dhis.validation.ValidationRule;
import org.hisp.dhis.validation.ValidationRuleService;

/**
 * @author mortenoh
 */
public class SectionFormUtils
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private DataAnalysisService stdDevOutlierAnalysisService;

    public void setStdDevOutlierAnalysisService( DataAnalysisService stdDevOutlierAnalysisService )
    {
        this.stdDevOutlierAnalysisService = stdDevOutlierAnalysisService;
    }

    private DataAnalysisService minMaxOutlierAnalysisService;

    public void setMinMaxOutlierAnalysisService( DataAnalysisService minMaxOutlierAnalysisService )
    {
        this.minMaxOutlierAnalysisService = minMaxOutlierAnalysisService;
    }

    private SystemSettingManager systemSettingManager;

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }

    private MinMaxValuesGenerationService minMaxValuesGenerationService;

    public void setMinMaxValuesGenerationService( MinMaxValuesGenerationService minMaxValuesGenerationService )
    {
        this.minMaxValuesGenerationService = minMaxValuesGenerationService;
    }

    private MinMaxDataElementService minMaxDataElementService;

    public void setMinMaxDataElementService( MinMaxDataElementService minMaxDataElementService )
    {
        this.minMaxDataElementService = minMaxDataElementService;
    }

    private ValidationRuleService validationRuleService;

    public void setValidationRuleService( ValidationRuleService validationRuleService )
    {
        this.validationRuleService = validationRuleService;
    }

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }

    // -------------------------------------------------------------------------
    // Utils
    // -------------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public Map<String, DeflatedDataValue> getValidationViolations( OrganisationUnit organisationUnit, DataSet dataSet,
        Period period )
    {
        Map<String, DeflatedDataValue> validationErrorMap = new HashMap<String, DeflatedDataValue>();

        Collection<MinMaxDataElement> minmaxs = minMaxDataElementService.getMinMaxDataElements( organisationUnit,
            dataSet.getDataElements() );

        Collection<DeflatedDataValue> deflatedDataValues = new HashSet<DeflatedDataValue>();

        if ( minmaxs == null )
        {
            Double factor = (Double) systemSettingManager.getSystemSetting(
                SystemSettingManager.KEY_FACTOR_OF_DEVIATION, 2.0 );

            Collection<DeflatedDataValue> stdDevs = stdDevOutlierAnalysisService.analyse( organisationUnit,
                dataSet.getDataElements(), ListUtils.getCollection( period ), factor );

            Collection<DeflatedDataValue> minMaxs = minMaxOutlierAnalysisService.analyse( organisationUnit,
                dataSet.getDataElements(), ListUtils.getCollection( period ), null );

            deflatedDataValues = CollectionUtils.union( stdDevs, minMaxs );
        }
        else
        {
            deflatedDataValues = minMaxValuesGenerationService.findOutliers( organisationUnit,
                ListUtils.getCollection( period ), minmaxs );
        }

        for ( DeflatedDataValue deflatedDataValue : deflatedDataValues )
        {
            String key = String.format( "DE%dOC%d", deflatedDataValue.getDataElementId(),
                deflatedDataValue.getCategoryOptionComboId() );
            validationErrorMap.put( key, deflatedDataValue );
        }

        return validationErrorMap;
    }

    public List<String> getValidationRuleViolations( OrganisationUnit organisationUnit, DataSet dataSet, Period period )
    {
        List<ValidationResult> validationRuleResults = new ArrayList<ValidationResult>( validationRuleService.validate(
            dataSet, period, organisationUnit ) );

        List<String> validationRuleViolations = new ArrayList<String>( validationRuleResults.size() );

        for ( ValidationResult result : validationRuleResults )
        {
            ValidationRule rule = result.getValidationRule();

            StringBuffer sb = new StringBuffer();
            sb.append( expressionService.getExpressionDescription( rule.getLeftSide().getExpression() ) );
            sb.append( " " + rule.getOperator().getMathematicalOperator() + " " );
            sb.append( expressionService.getExpressionDescription( rule.getRightSide().getExpression() ) );

            validationRuleViolations.add( sb.toString() );
        }

        return validationRuleViolations;
    }

    public Map<String, String> getDataValueMap( OrganisationUnit organisationUnit, DataSet dataSet, Period period )
    {
        Map<String, String> dataValueMap = new HashMap<String, String>();
        List<DataValue> values = new ArrayList<DataValue>( dataValueService.getDataValues( organisationUnit, period,
            dataSet.getDataElements() ) );

        for ( DataValue dataValue : values )
        {
            DataElement dataElement = dataValue.getDataElement();
            DataElementCategoryOptionCombo optionCombo = dataValue.getOptionCombo();

            String key = String.format( "DE%dOC%d", dataElement.getId(), optionCombo.getId() );
            String value = dataValue.getValue();

            dataValueMap.put( key, value );
        }

        return dataValueMap;
    }

    // -------------------------------------------------------------------------
    // Static Utils
    // -------------------------------------------------------------------------

    public static boolean valueHigher( String value, int max )
    {
        int integerValue;

        try
        {
            integerValue = Integer.parseInt( value );

            if ( integerValue > max )
            {
                return true;
            }
        }
        catch ( NumberFormatException e )
        {
        }

        return false;
    }

    public static boolean valueLower( String value, int min )
    {
        int integerValue;

        try
        {
            integerValue = Integer.parseInt( value );

            if ( integerValue < min )
            {
                return true;
            }
        }
        catch ( NumberFormatException e )
        {
        }

        return false;
    }
}
