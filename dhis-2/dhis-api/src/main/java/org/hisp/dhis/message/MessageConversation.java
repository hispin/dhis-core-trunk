package org.hisp.dhis.message;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hisp.dhis.user.User;

/**
 * @author Lars Helge Overland
 */
public class MessageConversation
{
    private int id;
    
    private String key;
    
    private String subject;

    private Set<UserMessage> userMessages = new HashSet<UserMessage>();
    
    private List<Message> messages = new ArrayList<Message>();

    private Date lastUpdated;
    
    private User lastSender;
    
    private transient boolean read;
    
    private transient String lastSenderSurname;
    
    private transient String lastSenderFirstname;
    
    public MessageConversation()
    {
        this.key = UUID.randomUUID().toString();
        this.lastUpdated = new Date();
    }
    
    public MessageConversation( String subject, User lastSender )
    {
        this.key = UUID.randomUUID().toString();
        this.subject = subject;
        this.lastUpdated = new Date();
        this.lastSender = lastSender;
    }
    
    public void addUserMessage( UserMessage userMessage )
    {
        this.userMessages.add( userMessage );
    }
    
    public void addMessage( Message message )
    {
        this.messages.add( message );
    }
    
    public void markRead( User user )
    {
        for ( UserMessage userMessage : userMessages )
        {
            if ( userMessage.getUser() != null && userMessage.getUser().equals( user ) )
            {
                userMessage.setRead( true );
                
                return;
            }
        }
    }

    public void markUnread( User user )
    {
        for ( UserMessage userMessage : userMessages )
        {
            if ( userMessage.getUser() != null && userMessage.getUser().equals( user ) )
            {
                userMessage.setRead( false );
                
                return;
            }
        }
    }
    
    public void markReplied( User sender, Message message )
    {   
        for ( UserMessage userMessage : userMessages )
        {
            if ( userMessage.getUser() != null && !userMessage.getUser().equals( sender ) )
            {
                userMessage.setRead( false );
            }
        }
        
        addMessage( message );
        
        this.lastUpdated = new Date();
        this.lastSender = sender;
    }
    
    public void remove( User user )
    {
        Iterator<UserMessage> iterator = userMessages.iterator();
        
        while ( iterator.hasNext() )
        {
            UserMessage userMessage = iterator.next();
            
            if ( userMessage.getUser() != null && userMessage.getUser().equals( user ) )
            {
                iterator.remove();
                
                return;
            }
        }
    }
    
    public Set<User> getUsers()
    {
        Set<User> users = new HashSet<User>();
        
        for ( UserMessage userMessage : userMessages )
        {
            users.add( userMessage.getUser() );
        }
        
        return users;
    }
    
    public String getLastSenderName()
    {
        return lastSenderFirstname + " " + lastSenderSurname;
    }
        
    public int getId()
    {
        return id;
    }

    public void setId( int id )
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject( String subject )
    {
        this.subject = subject;
    }

    public Set<UserMessage> getUserMessages()
    {
        return userMessages;
    }

    public void setUserMessages( Set<UserMessage> userMessages )
    {
        this.userMessages = userMessages;
    }

    public List<Message> getMessages()
    {
        return messages;
    }

    public void setMessages( List<Message> messages )
    {
        this.messages = messages;
    }

    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated( Date lastUpdated )
    {
        this.lastUpdated = lastUpdated;
    }

    public User getLastSender()
    {
        return lastSender;
    }

    public void setLastSender( User lastSender )
    {
        this.lastSender = lastSender;
    }

    public boolean isRead()
    {
        return read;
    }

    public void setRead( boolean read )
    {
        this.read = read;
    }

    public String getLastSenderSurname()
    {
        return lastSenderSurname;
    }

    public void setLastSenderSurname( String lastSenderSurname )
    {
        this.lastSenderSurname = lastSenderSurname;
    }

    public String getLastSenderFirstname()
    {
        return lastSenderFirstname;
    }

    public void setLastSenderFirstname( String lastSenderFirstname )
    {
        this.lastSenderFirstname = lastSenderFirstname;
    }

    @Override
    public int hashCode()
    {
        return key.hashCode();
    }

    @Override
    public boolean equals( Object object )
    {
        if ( this == object )
        {
            return true;
        }
        
        if ( object == null )
        {
            return false;
        }
        
        if ( getClass() != object.getClass() )
        {
            return false;
        }
        
        final MessageConversation other = (MessageConversation) object;
        
        return key.equals( other.key );
    }
    
    @Override
    public String toString()
    {
        return "[" + subject + "]";
    }
}
