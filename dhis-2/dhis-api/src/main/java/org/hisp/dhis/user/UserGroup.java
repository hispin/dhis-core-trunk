package org.hisp.dhis.user;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserGroup implements Serializable
{
    /**
     * id is the primary key which is auto generated
     */
    private int id;

    /**
     * The name of this Object. Required and unique.
     */
    private String name;
    
    /**
     * Set of related users
     */
    private Set<User> members = new HashSet<User>();
    
    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------     

    public UserGroup()
    {
        
    }
    
    public UserGroup( String name )
    {
        this.name = name;
    }

    public UserGroup( String name, Set<User> members )
    {
        this.name = name;
        this.members = members;
    }

    
    // -------------------------------------------------------------------------
    // hashCode, equals and toString
    // -------------------------------------------------------------------------     

    public boolean equals( Object object )
    {
        if ( this == object )
        {
            return true;
        }
        else if ( object == null )
        {
            return false;
        }
        else if ( !( object instanceof UserGroup ) )
        {
            return false;
        }

        final UserGroup userGroup = (UserGroup) object;

        return name.equals( userGroup.getName() );
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public Set<User> getMembers()
    {
        return members;
    }

    public void setMembers( Set<User> members )
    {
        this.members = members;
    }
    
}
