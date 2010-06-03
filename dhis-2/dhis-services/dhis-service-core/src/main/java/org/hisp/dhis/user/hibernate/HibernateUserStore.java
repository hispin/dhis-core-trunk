package org.hisp.dhis.user.hibernate;

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
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.system.util.AuditLogLevel;
import org.hisp.dhis.system.util.AuditLogUtil;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserSetting;
import org.hisp.dhis.user.UserStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nguyen Hong Duc
 * @version $Id: HibernateUserStore.java 6530 2008-11-28 15:02:47Z eivindwa $
 */
@Transactional
public class HibernateUserStore
    implements UserStore
{
    private Logger logger = Logger.getLogger( getClass() );
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SessionFactory sessionFactory;
    
    private CurrentUserService currentUserService;
    
    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    // -------------------------------------------------------------------------
    // User
    // -------------------------------------------------------------------------

    public int addUser( User user )
    {
        Session session = sessionFactory.getCurrentSession();

        logger.log( AuditLogLevel.AUDIT_TRAIL, 
            AuditLogUtil.logMessage( currentUserService.getCurrentUsername(),
            AuditLogUtil.ACTION_ADD , 
            User.class.getSimpleName(), 
            user.getName()) );
        
        return (Integer) session.save( user );
    }

    public void updateUser( User user )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( user );
        
        logger.log( AuditLogLevel.AUDIT_TRAIL, 
            AuditLogUtil.logMessage( currentUserService.getCurrentUsername(),
                AuditLogUtil.ACTION_EDIT , 
                User.class.getSimpleName(), 
                user.getName()) );
    }

    public User getUser( int id )
    {
        Session session = sessionFactory.getCurrentSession();

        return (User) session.get( User.class, id );
    }
    
    @SuppressWarnings( "unchecked" )
    public Collection<User> getAllUsers()
    {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery( "from User" ).list();
    }

    public Collection<User> getUsersByOrganisationUnit( OrganisationUnit organisationUnit )
    {   
        Collection<User> users = getAllUsers();
        
        Iterator<User> iterator = users.iterator();
        
        while( iterator.hasNext() )
        {
            if( ! iterator.next().getOrganisationUnits().contains( organisationUnit ) )
            {
        	iterator.remove();
            }
        }
        
        return users;
    }

    public Collection<User> getUsersWithoutOrganisationUnit()
    {    	
    	Collection<User> users = getAllUsers();
        
        Iterator<User> iterator = users.iterator();
        
        while( iterator.hasNext() )
        {
            if( iterator.next().getOrganisationUnits().size() > 0 )
            {
        	iterator.remove();
            }
        }
        
        return users;
    }

    public void deleteUser( User user )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( user );
        
        logger.log( AuditLogLevel.AUDIT_TRAIL, 
            AuditLogUtil.logMessage( currentUserService.getCurrentUsername(),
            AuditLogUtil.ACTION_DELETE , 
            User.class.getSimpleName(), 
            user.getName()) );
    }

    // -------------------------------------------------------------------------
    // UserCredentials
    // -------------------------------------------------------------------------

    public User addUserCredentials( UserCredentials userCredentials )
    {
        Session session = sessionFactory.getCurrentSession();

        int id = (Integer) session.save( userCredentials );

        return getUser( id );
    }

    public void updateUserCredentials( UserCredentials userCredentials )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( userCredentials );
    }

    public UserCredentials getUserCredentials( User user )
    {
        Session session = sessionFactory.getCurrentSession();

        return (UserCredentials) session.get( UserCredentials.class, user.getId() );
    }

    public UserCredentials getUserCredentialsByUsername( String username )
    {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( "from UserCredentials uc where uc.username = :username" );

        query.setString( "username", username );
        query.setCacheable( true );

        return (UserCredentials) query.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<UserCredentials> getAllUserCredentials()
    {
        Session session = sessionFactory.getCurrentSession();
        
        return session.createCriteria( UserCredentials.class ).list();
    }

    public void deleteUserCredentials( UserCredentials userCredentials )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( userCredentials );
    }

    // -------------------------------------------------------------------------
    // UserAuthorityGroup
    // -------------------------------------------------------------------------

    public int addUserAuthorityGroup( UserAuthorityGroup userAuthorityGroup )
    {
        Session session = sessionFactory.getCurrentSession();

        return (Integer) session.save( userAuthorityGroup );
    }

    public void updateUserAuthorityGroup( UserAuthorityGroup userAuthorityGroup )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( userAuthorityGroup );
    }
    
    public UserAuthorityGroup getUserAuthorityGroup( int id )
    {
        Session session = sessionFactory.getCurrentSession();

        return (UserAuthorityGroup) session.get( UserAuthorityGroup.class, id );
    }

    public UserAuthorityGroup getUserAuthorityGroupByName( String name )
    {
        Session session = sessionFactory.getCurrentSession();
        
        Criteria criteria = session.createCriteria( UserAuthorityGroup.class );
        
        criteria.add( Restrictions.eq( "name", name ) );
        
        return (UserAuthorityGroup) criteria.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<UserAuthorityGroup> getAllUserAuthorityGroups()
    {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery( "from UserAuthorityGroup" ).list();
    }

    public void deleteUserAuthorityGroup( UserAuthorityGroup userAuthorityGroup )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( userAuthorityGroup );
    }

    // -------------------------------------------------------------------------
    // UserSettings
    // -------------------------------------------------------------------------

    public void addUserSetting( UserSetting userSetting )
    {
        Session session = sessionFactory.getCurrentSession();

        session.save( userSetting );
    }

    public void updateUserSetting( UserSetting userSetting )
    {
        Session session = sessionFactory.getCurrentSession();

        session.update( userSetting );
    }

    public UserSetting getUserSetting( User user, String name )
    {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( "from UserSetting us where us.user = :user and us.name = :name" );

        query.setEntity( "user", user );
        query.setString( "name", name );
        query.setCacheable( true );

        return (UserSetting) query.uniqueResult();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<UserSetting> getAllUserSettings( User user )
    {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery( "from UserSetting us where us.user = :user" );

        query.setEntity( "user", user );

        return query.list();
    }

    public void deleteUserSetting( UserSetting userSetting )
    {
        Session session = sessionFactory.getCurrentSession();

        session.delete( userSetting );
    }
}
