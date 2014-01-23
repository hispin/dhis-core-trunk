package org.hisp.dhis.patient.action.patientattribute;

/*
 * Copyright (c) 2004-2013, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientAttributeOption;
import org.hisp.dhis.patient.PatientAttributeOptionService;
import org.hisp.dhis.patient.PatientAttributeService;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class UpdatePatientAttributeAction
    implements Action
{
    public static final String PREFIX_ATTRIBUTE_OPTION = "attrOption";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PatientAttributeService patientAttributeService;

    public void setPatientAttributeService( PatientAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
    }

    private PatientAttributeOptionService patientAttributeOptionService;

    public void setPatientAttributeOptionService( PatientAttributeOptionService patientAttributeOptionService )
    {
        this.patientAttributeOptionService = patientAttributeOptionService;
    }

    private PatientAttributeValueService patientAttributeValueService;

    public void setPatientAttributeValueService( PatientAttributeValueService patientAttributeValueService )
    {
        this.patientAttributeValueService = patientAttributeValueService;
    }

    @Autowired
    private PeriodService periodService;

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private int id;

    public void setId( int id )
    {
        this.id = id;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }

    private String valueType;

    public void setValueType( String valueType )
    {
        this.valueType = valueType;
    }

    private Boolean mandatory;

    public void setMandatory( Boolean mandatory )
    {
        this.mandatory = mandatory;
    }

    private Boolean unique;

    public void setUnique( Boolean unique )
    {
        this.unique = unique;
    }

    private List<String> attrOptions;

    public void setAttrOptions( List<String> attrOptions )
    {
        this.attrOptions = attrOptions;
    }

    private Boolean inherit;

    public void setInherit( Boolean inherit )
    {
        this.inherit = inherit;
    }

    private String expression;

    public void setExpression( String expression )
    {
        this.expression = expression;
    }

    // For Local ID type

    private Boolean orgunitScope;

    public void setOrgunitScope( Boolean orgunitScope )
    {
        this.orgunitScope = orgunitScope;
    }

    private Boolean programScope;

    public void setProgramScope( Boolean programScope )
    {
        this.programScope = programScope;
    }

    private String periodTypeName;

    public void setPeriodTypeName( String periodTypeName )
    {
        this.periodTypeName = periodTypeName;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        PatientAttribute patientAttribute = patientAttributeService.getPatientAttribute( id );

        patientAttribute.setName( name );
        patientAttribute.setDescription( description );
        patientAttribute.setValueType( valueType );
        patientAttribute.setExpression( expression );
        patientAttribute.setDisplayOnVisitSchedule( false );

        mandatory = (mandatory == null) ? false : true;
        patientAttribute.setMandatory( mandatory );

        unique = (unique == null) ? false : true;
        patientAttribute.setUnique( unique );

        inherit = (inherit == null) ? false : true;
        patientAttribute.setInherit( inherit );

        HttpServletRequest request = ServletActionContext.getRequest();

        Collection<PatientAttributeOption> attributeOptions = patientAttributeOptionService.get( patientAttribute );

        if ( attributeOptions != null && attributeOptions.size() > 0 )
        {
            String value = null;
            for ( PatientAttributeOption option : attributeOptions )
            {
                value = request.getParameter( PREFIX_ATTRIBUTE_OPTION + option.getId() );
                if ( StringUtils.isNotBlank( value ) )
                {
                    option.setName( value.trim() );
                    patientAttributeOptionService.updatePatientAttributeOption( option );
                    patientAttributeValueService.updatePatientAttributeValues( option );
                }
            }
        }

        if ( attrOptions != null )
        {
            PatientAttributeOption opt = null;
            for ( String optionName : attrOptions )
            {
                opt = patientAttributeOptionService.get( patientAttribute, optionName );
                if ( opt == null )
                {
                    opt = new PatientAttributeOption();
                    opt.setName( optionName );
                    opt.setPatientAttribute( patientAttribute );
                    patientAttribute.addAttributeOptions( opt );
                    patientAttributeOptionService.addPatientAttributeOption( opt );
                }
            }
        }

        if ( valueType.equals( PatientAttribute.VALUE_TYPE_LOCAL_ID ) )
        {
            orgunitScope = (orgunitScope == null) ? false : orgunitScope;
            programScope = (programScope == null) ? false : programScope;

            if ( !StringUtils.isEmpty( periodTypeName ) )
            {
                PeriodType periodType = periodService.getPeriodTypeByName( periodTypeName );
                periodType = periodService.reloadPeriodType( periodType );
                patientAttribute.setPeriodType( periodType );
            }
            else
            {
                patientAttribute.setPeriodType( null );
            }

            patientAttribute.setOrgunitScope( orgunitScope );
            patientAttribute.setProgramScope( programScope );
        }

        patientAttributeService.updatePatientAttribute( patientAttribute );

        return SUCCESS;
    }
}
