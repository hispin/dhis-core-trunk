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

package org.hisp.dhis.patient;

import java.util.Collection;
import java.util.Date;

import org.hisp.dhis.organisationunit.OrganisationUnit;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public interface PatientService
{
    String ID = PatientService.class.getName();

    int savePatient( Patient patient );

    void deletePatient( Patient patient );

    void updatePatient( Patient patient );

    Patient getPatient( int id );

    Collection<Patient> getAllPatients();    

    Collection<Patient> getAllPatients( Boolean isDead );

    Collection<Patient> getPatiensByGender( String gender );

    Collection<Patient> getPatientsByBirthDate( Date birthDate );

    Collection<Patient> getPatientsByNames( String name );
    
    Collection<Patient> getPatients( String searchText );
    
    //Collection<Patient> getPatientsByAttribute( PatientAttribute attribute );
    
    Collection<Patient> getPatientsByOrgUnit( OrganisationUnit organisationUnit );
    
    Collection<Patient> getPatients( OrganisationUnit organisationUnit, String searchText );
    
    Collection<Patient> sortPatientsByAttribute( Collection<Patient> patients, PatientAttribute patientAttribute );
    
    Collection<Patient> getPatient( String firstName, String middleName, String lastName, Date birthdate, String gender);
}
