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

package org.hisp.dhis.patient.action.validation;

import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.validation.ValidationCriteria;
import org.hisp.dhis.validation.ValidationCriteriaService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * @version UpdateValidationCriteriaAction.java Apr 29, 2010 10:44:36 AM
 */
public class UpdateValidationCriteriaAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------

    private ValidationCriteriaService validationCriteriaService;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private int id;

    private String name;

    private String description;

    private String property;

    private int operator;

    private String value;

    private I18nFormat format;

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setValidationCriteriaService( ValidationCriteriaService validationCriteriaService )
    {
        this.validationCriteriaService = validationCriteriaService;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public void setProperty( String property )
    {
        this.property = property;
    }

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    public void setOperator( int operator )
    {
        this.operator = operator;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        ValidationCriteria criteria = validationCriteriaService.getValidationCriteria( id );

        criteria.setName( name );
        criteria.setDescription( description );
        criteria.setProperty( property );
        criteria.setOperator( operator );
        criteria.setValue( getObject() );

        validationCriteriaService.updateValidationCriteria( criteria );

        return SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Support method
    // -------------------------------------------------------------------------

    private Object getObject()
        throws Exception
    {
        Type type = Patient.class.getMethod( "get" + StringUtils.capitalize( property ) ).getReturnType();

        // Get value
        if ( type == Integer.class || type == Integer.TYPE )
        {
            return Integer.valueOf( value );
        }
        else if ( type.equals( Boolean.class ) || type == Boolean.TYPE )
        {
            return Boolean.valueOf( value );
        }
        else if ( type.equals( Date.class ) )
        {
            return format.parseDate( value.trim() );
        }
        else if ( type.equals( Character.class ) || type == Character.TYPE )
        {
            return Character.valueOf ( value.charAt( 0 ) );
        }

        return value;
    }
}
