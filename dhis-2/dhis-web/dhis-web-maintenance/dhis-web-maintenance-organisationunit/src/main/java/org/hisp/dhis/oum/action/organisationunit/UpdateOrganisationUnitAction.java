package org.hisp.dhis.oum.action.organisationunit;

/*
 * Copyright (c) 2004-2007, University of Oslo
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

import static org.hisp.dhis.system.util.TextUtils.nullIfEmpty;

import java.util.Date;

import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Torgeir Lorange Ostby
 * @version $Id: UpdateOrganisationUnitAction.java 1898 2006-09-22 12:06:56Z torgeilo $
 */
public class UpdateOrganisationUnitAction
    extends ActionSupport
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer id;

    public void setId( Integer id )
    {
        this.id = id;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String shortName;

    public void setShortName( String shortName )
    {
        this.shortName = shortName;
    }

    private String code;

    public void setCode( String code )
    {
        this.code = code;
    }

    private Boolean active;

    public void setActive( Boolean active )
    {
        this.active = active;
    }

    private String openingDate;

    public void setOpeningDate( String openingDate )
    {
        this.openingDate = openingDate;
    }

    private String closedDate;

    public void setClosedDate( String closedDate )
    {
        this.closedDate = closedDate;
    }

    private String type;

    public void setType( String type )
    {
        this.type = type;
    }

    private String comment;

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    private String polygonCoordinates;

    public void setPolygonCoordinates( String polygonCoordinates )
    {
        this.polygonCoordinates = polygonCoordinates;
    }

    private String latitude;

    public void setLatitude( String latitude )
    {
        this.latitude = latitude;
    }
    
    private String longitude;

    public void setLongitude( String longitude )
    {
        this.longitude = longitude;
    }

    private String url;

    public void setUrl( String url )
    {
        this.url = url;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        code = nullIfEmpty( code );
        comment = nullIfEmpty( comment );
        type = nullIfEmpty( type );
        polygonCoordinates = nullIfEmpty( polygonCoordinates );
        latitude = nullIfEmpty( latitude );
        longitude = nullIfEmpty( longitude );
        url = nullIfEmpty( url );

        Date oDate = format.parseDate( openingDate );

        Date cDate = null;

        if ( closedDate != null && closedDate.trim().length() != 0 )
        {
            cDate = format.parseDate( closedDate );
        }

        // ---------------------------------------------------------------------
        // Update organisation unit
        // ---------------------------------------------------------------------

        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( id.intValue() );

        organisationUnit.setName( name );
        organisationUnit.setShortName( shortName );
        organisationUnit.setCode( code );
        organisationUnit.setActive( active.booleanValue() );
        organisationUnit.setOpeningDate( oDate );
        organisationUnit.setClosedDate( cDate );
        organisationUnit.setType( type );
        organisationUnit.setComment( comment );
        organisationUnit.setPolygonCoordinates( polygonCoordinates );
        organisationUnit.setLatitude( latitude );
        organisationUnit.setLongitude( longitude );
        organisationUnit.setUrl( url );

        organisationUnitService.updateOrganisationUnit( organisationUnit );

        return SUCCESS;
    }
}
