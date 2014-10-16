package org.hisp.dhis.settings.action.system;

/*
 * Copyright (c) 2004-2014, University of Oslo
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

import static org.hisp.dhis.setting.SystemSettingManager.*;

import org.hisp.dhis.configuration.Configuration;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.setting.SystemSettingManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Dang Duy Hieu
 * @version $Id$
 */
public class SetSMTPSettingsAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private SystemSettingManager systemSettingManager;

    @Autowired
    private ConfigurationService configurationService;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String smtpHostName;

    public void setSmtpHostName( String hostName )
    {
        this.smtpHostName = hostName;
    }

    private int smtpPort;

    public void setSmtpPort( int smtpPort )
    {
        this.smtpPort = smtpPort;
    }

    private String smtpUsername;

    public void setSmtpUsername( String username )
    {
        this.smtpUsername = username;
    }

    private String smtpPassword;

    public void setSmtpPassword( String password )
    {
        this.smtpPassword = password;
    }

    private boolean smtpTls;

    public void setSmtpTls( boolean smtpTls )
    {
        this.smtpTls = smtpTls;
    }

    private String emailSender;

    public void setEmailSender( String emailSender )
    {
        this.emailSender = emailSender;
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

    @Override
    public String execute()
    {
        Configuration config = configurationService.getConfiguration();
        
        systemSettingManager.saveSystemSetting( KEY_EMAIL_HOST_NAME, smtpHostName );

        systemSettingManager.saveSystemSetting( KEY_EMAIL_PORT, smtpPort );
        
        config.setSmtpPassword( smtpPassword );
        
        systemSettingManager.saveSystemSetting( KEY_EMAIL_USERNAME, smtpUsername );

        systemSettingManager.saveSystemSetting( KEY_EMAIL_TLS, smtpTls );
        
        systemSettingManager.saveSystemSetting( KEY_EMAIL_SENDER, emailSender );
        
        message = i18n.getString( "settings_updated" );
        
        configurationService.setConfiguration( config );

        return SUCCESS;
    }
}
