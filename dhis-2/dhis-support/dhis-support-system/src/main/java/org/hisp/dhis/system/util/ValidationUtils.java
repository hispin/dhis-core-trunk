package org.hisp.dhis.system.util;

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

import java.util.Locale;

import org.apache.commons.validator.DateValidator;
import org.apache.commons.validator.EmailValidator;
import org.apache.commons.validator.UrlValidator;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class ValidationUtils
{
    /**
     * Validates whether an email string is valid.
     * 
     * @param email the email string.
     * @return true if the email string is valid, false otherwise.
     */
    public static boolean emailIsValid( String email )
    {
        return EmailValidator.getInstance().isValid( email );
    }
    
    /**
     * Validates whether a date string is valid for the given Locale.
     * 
     * @param date the date string.
     * @param locale the Locale
     * @return true if the date string is valid, false otherwise.
     */
    public static boolean dateIsValid( String date, Locale locale )
    {
        return DateValidator.getInstance().isValid( date, locale );
    }

    /**
     * Validates whether a date string is valid for the default Locale.
     * 
     * @param date the date string.
     * @return true if the date string is valid, false otherwise.
     */
    public static boolean dateIsValid( String date )
    {
        return dateIsValid( date, null );
    }
    
    /**
     * Validates whether an URL string is valid.
     * 
     * @param url the URL string.
     * @return true if the URL string is valid, false otherwise.
     */
    public static boolean urlIsValid( String url )
    {
        return new UrlValidator().isValid( url );
    }
    
    /**
     * Validates whether a password is valid.
     * 
     * @param password the password.
     * @return true if the password is valid, false otherwise.
     */
    public static boolean passwordIsValid( String password )
    {
        return password != null && password.length() >= 5 && password.length() < 50;
    }
}
