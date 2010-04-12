package org.hisp.dhis.dd.action.dataelementgroup;

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
import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;

import com.opensymphony.xwork2.Action;

/**
 * @author Tran Thanh Tri
 * @version $Id: AssignGroupsForDataElementAction.java 2869 2010-03-27 14:26:09Z Chau Thu Tran $
 */

public class AssignGroupsForDataElementAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private Integer dataElementId;

    public void setDataElementId( Integer dataElementId )
    {
        this.dataElementId = dataElementId;
    }

    private Collection<Integer> dataElementGroups;

    public void setDataElementGroups( Collection<Integer> dataElementGroups )
    {
        this.dataElementGroups = dataElementGroups;
    }

    @Override
    public String execute()
        throws Exception
    {
        DataElement dataElement = dataElementService.getDataElement( dataElementId );

        Set<DataElementGroup> selectedGroups = new HashSet<DataElementGroup>();

        for ( Integer id : dataElementGroups )
        {
            DataElementGroup dataElementGroup = dataElementService.getDataElementGroup( id );

            selectedGroups.add( dataElementGroup );

            dataElementGroup.getMembers().add( dataElement );

            dataElementService.updateDataElementGroup( dataElementGroup );

        }
        
        Set<DataElementGroup>removeGroups = new HashSet<DataElementGroup>( dataElementService
            .getGroupsContainingDataElement( dataElement ) );
        removeGroups.removeAll( selectedGroups );
        
        for ( DataElementGroup removeGroup : removeGroups )
        {
            removeGroup.getMembers().remove( dataElement );
            dataElementService.updateDataElementGroup( removeGroup );
        }

        return SUCCESS;
    }
}
