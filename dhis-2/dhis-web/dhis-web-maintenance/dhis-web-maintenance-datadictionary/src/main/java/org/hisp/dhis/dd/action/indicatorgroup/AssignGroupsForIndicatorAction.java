package org.hisp.dhis.dd.action.indicatorgroup;

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

import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorGroup;
import org.hisp.dhis.indicator.IndicatorService;

import com.opensymphony.xwork2.Action;

/**
 * @author Tran Thanh Tri
 * @version $Id: AssignGroupsForIndicatorAction.java 2869 2010-03-27 15:01:079Z
 *          Chau Thu Tran $
 */
public class AssignGroupsForIndicatorAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private Integer indicatorId;

    public void setIndicatorId( Integer indicatorId )
    {
        this.indicatorId = indicatorId;
    }

    private Collection<Integer> indicatorGroups;

    public void setIndicatorGroups( Collection<Integer> indicatorGroups )
    {
        this.indicatorGroups = indicatorGroups;
    }

    @Override
    public String execute()
        throws Exception
    {

        Indicator indicator = indicatorService.getIndicator( indicatorId );

        Set<IndicatorGroup> selectedGroups = new HashSet<IndicatorGroup>();

        for ( Integer id : indicatorGroups )
        {
            IndicatorGroup group = indicatorService.getIndicatorGroup( id );

            selectedGroups.add( group );
            
            group.getMembers().add( indicator );

            indicatorService.updateIndicatorGroup( group );

        }

        Set<IndicatorGroup> removeGroups = new HashSet<IndicatorGroup>( indicatorService
            .getGroupsContainingIndicator( indicator ) );
        removeGroups.removeAll( selectedGroups );

        for ( IndicatorGroup removeGroup : removeGroups )
        {
            removeGroup.getMembers().remove( indicator );
            indicatorService.updateIndicatorGroup( removeGroup );
        }

        return SUCCESS;
    }

}
