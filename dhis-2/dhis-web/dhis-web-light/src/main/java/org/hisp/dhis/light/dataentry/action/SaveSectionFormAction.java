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

package org.hisp.dhis.light.dataentry.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.CompleteDataSetRegistration;
import org.hisp.dhis.dataset.CompleteDataSetRegistrationService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.datavalue.DeflatedDataValue;
import org.hisp.dhis.light.dataentry.utils.SectionFormUtils;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.util.ContextUtils;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * @author mortenoh
 */
public class SaveSectionFormAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private CompleteDataSetRegistrationService registrationService;

    public void setRegistrationService( CompleteDataSetRegistrationService registrationService )
    {
        this.registrationService = registrationService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private SectionFormUtils sectionFormUtils;

    public void setSectionFormUtils( SectionFormUtils sectionFormUtils )
    {
        this.sectionFormUtils = sectionFormUtils;
    }

    public SectionFormUtils getSectionFormUtils()
    {
        return sectionFormUtils;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private Integer organisationUnitId;

    public void setOrganisationUnitId( Integer organisationUnitId )
    {
        this.organisationUnitId = organisationUnitId;
    }

    public Integer getOrganisationUnitId()
    {
        return organisationUnitId;
    }

    private String periodId;

    public void setPeriodId( String periodId )
    {
        this.periodId = periodId;
    }

    public String getPeriodId()
    {
        return periodId;
    }

    private Integer dataSetId;

    public void setDataSetId( Integer dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    public Integer getDataSetId()
    {
        return dataSetId;
    }

    private DataSet dataSet;

    public DataSet getDataSet()
    {
        return dataSet;
    }

    private Map<String, String> dataValues = new HashMap<String, String>();

    public Map<String, String> getDataValues()
    {
        return dataValues;
    }

    private Map<String, DeflatedDataValue> validationViolations = new HashMap<String, DeflatedDataValue>();

    public Map<String, DeflatedDataValue> getValidationViolations()
    {
        return validationViolations;
    }

    private List<String> validationRuleViolations = new ArrayList<String>();

    public List<String> getValidationRuleViolations()
    {
        return validationRuleViolations;
    }

    private Boolean complete = false;

    public void setComplete( Boolean complete )
    {
        this.complete = complete;
    }

    public Boolean getComplete()
    {
        return complete;
    }

    private Boolean validated;

    public void setValidated( Boolean validated )
    {
        this.validated = validated;
    }

    public Boolean getValidated()
    {
        return validated;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
    {
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );

        Period period = periodService.getPeriodByExternalId( periodId );

        String storedBy = currentUserService.getCurrentUsername();

        boolean needsValidation = false;

        dataSet = dataSetService.getDataSet( dataSetId );

        if ( storedBy == null )
        {
            storedBy = "[unknown]";
        }

        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(
            ServletActionContext.HTTP_REQUEST );
        Map<String, String> parameterMap = ContextUtils.getParameterMap( request );

        for ( String key : parameterMap.keySet() )
        {
            if ( key.startsWith( "DE" ) && key.indexOf( "OC" ) != -1 )
            {
                String[] splitKey = key.split( "OC" );
                Integer dataElementId = Integer.parseInt( splitKey[0].substring( 2 ) );
                Integer optionComboId = Integer.parseInt( splitKey[1] );
                String value = parameterMap.get( key );

                DataElement dataElement = dataElementService.getDataElement( dataElementId );
                DataElementCategoryOptionCombo optionCombo = categoryService
                    .getDataElementCategoryOptionCombo( optionComboId );

                DataValue dataValue = dataValueService
                    .getDataValue( organisationUnit, dataElement, period, optionCombo );

                value = value.trim();

                if ( value == null || value.length() == 0 || !SectionFormUtils.isInteger( value ) )
                {
                    if ( dataValue != null )
                    {
                        dataValueService.deleteDataValue( dataValue );
                    }

                    continue;
                }

                if ( dataValue == null )
                {
                    needsValidation = true;

                    dataValue = new DataValue( dataElement, period, organisationUnit, value, storedBy, new Date(),
                        null, optionCombo );
                    dataValueService.addDataValue( dataValue );
                }
                else
                {
                    if ( !dataValue.getValue().equals( value ) )
                    {
                        needsValidation = true;

                        dataValue.setValue( value );
                        dataValue.setTimestamp( new Date() );
                        dataValue.setStoredBy( storedBy );

                        dataValueService.updateDataValue( dataValue );
                    }
                }
            }
        }

        CompleteDataSetRegistration registration = registrationService.getCompleteDataSetRegistration( dataSet, period,
            organisationUnit );

        if ( registration == null && complete )
        {
            registration = new CompleteDataSetRegistration();
            registration.setDataSet( dataSet );
            registration.setPeriod( period );
            registration.setSource( organisationUnit );
            registration.setDate( new Date() );

            registrationService.saveCompleteDataSetRegistration( registration );
        }
        else if ( registration != null && !complete )
        {
            registrationService.deleteCompleteDataSetRegistration( registration );
        }

        dataValues = sectionFormUtils.getDataValueMap( organisationUnit, dataSet, period );

        validationViolations = sectionFormUtils.getValidationViolations( organisationUnit, dataSet, period );

        validationRuleViolations = sectionFormUtils.getValidationRuleViolations( organisationUnit, dataSet, period );

        if ( needsValidation && validationViolations.size() > 0 )
        {
            return ERROR;
        }

        return SUCCESS;
    }
}
