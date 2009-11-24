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

package org.hisp.dhis.patient.action.relationship;

import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */

public class ValidateRelationshipTypeAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private RelationshipTypeService relationshipTypeService;

    public void setRelationshipTypeService( RelationshipTypeService relationshipTypeService )
    {
        this.relationshipTypeService = relationshipTypeService;
    }

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private String aIsToB;

    public void setaIsToB( String aIsToB )
    {
        this.aIsToB = aIsToB;
    }

    private String bIsToA;

    public void setbIsToA( String bIsToA )
    {
        this.bIsToA = bIsToA;
    }

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }

    private String message;

    public String getMessage()
    {
        return message;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {

        if ( aIsToB == null )
        {
            message = i18n.getString( "please_specify_the_a_side_of_the_relationship_type" );

            return INPUT;
        }

        else
        {
            aIsToB = aIsToB.trim();

            if ( aIsToB.length() == 0 )
            {
                message = i18n.getString( "please_specify_the_a_side_of_the_relationship_type" );

                return INPUT;
            }
        }

        if ( bIsToA == null )
        {
            message = i18n.getString( "please_specify_the_b_side_of_the_relationship_type" );

            return INPUT;
        }

        else
        {
            bIsToA = bIsToA.trim();

            if ( bIsToA.length() == 0 )
            {
                message = i18n.getString( "please_specify_the_b_side_of_the_relationship_type" );

                return INPUT;
            }
        }

        if ( description == null )
        {
            message = i18n.getString( "please_specify_a_description" );

            return INPUT;
        }

        else
        {
            description = description.trim();

            if ( description.length() == 0 )
            {
                message = i18n.getString( "please_specify_a_description" );

                return INPUT;
            }
        }
        
        RelationshipType relationshipType = relationshipTypeService.getRelationshipType( aIsToB, bIsToA );
        
        if( relationshipType != null )
        {
            message = i18n.getString( "relationship_already_exists" );

            return INPUT;
        }
        
        relationshipType = relationshipTypeService.getRelationshipType( bIsToA, aIsToB );
        
        if( relationshipType != null )
        {
            message = i18n.getString( "relationship_already_exists" );

            return INPUT;
        }
        

        // ---------------------------------------------------------------------
        // Validation success
        // ---------------------------------------------------------------------

        message = i18n.getString( "everything_is_ok" );

        return SUCCESS;

    }
}
