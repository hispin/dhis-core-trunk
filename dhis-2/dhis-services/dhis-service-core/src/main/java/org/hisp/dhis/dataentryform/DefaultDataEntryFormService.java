package org.hisp.dhis.dataentryform;

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

import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataentryform.DataEntryFormAssociation;
import org.hisp.dhis.dataentryform.DataEntryFormAssociationService;
import org.hisp.dhis.dataentryform.DataEntryFormService;
import org.hisp.dhis.dataentryform.DataEntryFormStore;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.program.ProgramStage;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Bharath Kumar
 * @version $Id$
 */
@Transactional
public class DefaultDataEntryFormService
    implements DataEntryFormService
{
    // ------------------------------------------------------------------------
    // Dependencies
    // ------------------------------------------------------------------------

    private DataEntryFormStore dataEntryFormStore;

    public void setDataEntryFormStore( DataEntryFormStore dataEntryFormStore )
    {
        this.dataEntryFormStore = dataEntryFormStore;
    }

    private DataEntryFormAssociationService dataEntryFormAssociationService;

    public void setDataEntryFormAssociationService( DataEntryFormAssociationService dataEntryFormAssociationService )
    {
        this.dataEntryFormAssociationService = dataEntryFormAssociationService;
    }

    // ------------------------------------------------------------------------
    // Implemented Methods
    // ------------------------------------------------------------------------

    public int addDataEntryForm( DataEntryForm dataEntryForm, String associationTableName, int associationId )
    {
        int dataEntryFormId = dataEntryFormStore.addDataEntryForm( dataEntryForm );

        DataEntryForm form = dataEntryFormStore.getDataEntryForm( dataEntryFormId );

        DataEntryFormAssociation dataAssociation = new DataEntryFormAssociation( associationTableName, associationId, form );

        dataEntryFormAssociationService.addDataEntryFormAssociation( dataAssociation );

        return dataEntryFormId;

    }

    public void updateDataEntryForm( DataEntryForm dataEntryForm )
    {
        dataEntryFormStore.updateDataEntryForm( dataEntryForm );
    }

    public void deleteDataEntryForm( DataEntryForm dataEntryForm )
    {
        DataEntryFormAssociation entryFormAssociation = dataEntryFormAssociationService
            .getDataEntryFormAssociationByDataEntryForm( dataEntryForm );

        if ( entryFormAssociation != null )
        {
            dataEntryFormAssociationService.deleteDataEntryFormAssociation( entryFormAssociation );
        }

        dataEntryFormStore.deleteDataEntryForm( dataEntryForm );
    }

    public DataEntryForm getDataEntryForm( int id )
    {
        return dataEntryFormStore.getDataEntryForm( id );
    }

    public DataEntryForm getDataEntryFormByName( String name )
    {
        return dataEntryFormStore.getDataEntryFormByName( name );
    }

    public DataEntryForm getDataEntryFormByDataSet( DataSet dataSet )
    {
        if ( dataSet != null )
        {
            DataEntryFormAssociation dataAssociation = dataEntryFormAssociationService.getDataEntryFormAssociation(
                DataEntryFormAssociation.DATAENTRY_ASSOCIATE_DATASET, dataSet.getId() );
            
            if ( dataAssociation != null )
            {
                return dataAssociation.getDataEntryForm();
            }
        }

        return null;
    }

    public DataEntryForm getDataEntryFormByProgramStage( ProgramStage programStage )
    {
        if ( programStage != null )
        {
            DataEntryFormAssociation dataAssociation = dataEntryFormAssociationService.getDataEntryFormAssociation(
                DataEntryFormAssociation.DATAENTRY_ASSOCIATE_PROGRAMSTAGE, programStage.getId() );
            
            if ( dataAssociation != null )
            {
                return dataAssociation.getDataEntryForm();
            }
        }

        return null;
    }

    public Collection<DataEntryForm> getAllDataEntryForms()
    {
        return dataEntryFormStore.getAllDataEntryForms();
    }
}
