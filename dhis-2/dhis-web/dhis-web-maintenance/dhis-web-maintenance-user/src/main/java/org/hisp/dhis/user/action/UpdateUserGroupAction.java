package org.hisp.dhis.user.action;

/*
 * Copyright (c) 2004-2012, University of Oslo
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

import com.opensymphony.xwork2.Action;
import org.hisp.dhis.attribute.AttributeService;
import org.hisp.dhis.system.util.AttributeUtils;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;
import org.hisp.dhis.user.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdateUserGroupAction
    implements Action
{
    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    private UserGroupService userGroupService;

    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }

    private AttributeService attributeService;

    public void setAttributeService( AttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    // -------------------------------------------------------------------------
    // Parameters
    // -------------------------------------------------------------------------

    private List<Integer> groupMembersList;

    public void setGroupMembersList( List<Integer> groupMembersList )
    {
        this.groupMembersList = groupMembersList;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private Integer userGroupId;

    public void setUserGroupId( Integer userGroupId )
    {
        this.userGroupId = userGroupId;
    }

    private List<String> jsonAttributeValues;

    public void setJsonAttributeValues( List<String> jsonAttributeValues )
    {
        this.jsonAttributeValues = jsonAttributeValues;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        Set<User> userList = new HashSet<User>();

        for ( Integer groupMember : groupMembersList )
        {
            User user = userService.getUser( groupMember );
            userList.add( user );
        }

        UserGroup userGroup = userGroupService.getUserGroup( userGroupId );

        userGroup.setName( name );
        userGroup.setMembers( userList );

        if ( jsonAttributeValues != null )
        {
            AttributeUtils.updateAttributeValuesFromJson( userGroup.getAttributeValues(), jsonAttributeValues,
                attributeService );
        }

        userGroupService.updateUserGroup( userGroup );

        return SUCCESS;
    }
}
