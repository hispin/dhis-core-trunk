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
package org.hisp.dhis.datalock;

import java.util.Collection;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;

/**
 * @author Brajesh Murari
 * @version $Id$
 */
public interface DataSetLockStore
{
    String ID = DataSetLockStore.class.getName();

    // -------------------------------------------------------------------------
    // DataSetLockStore
    // -------------------------------------------------------------------------

    /**
     * Adds a DataSetLock.
     * 
     * @param dataSetLock The dataSetLock to add.
     * @return The generated unique identifier for this DataSetLock.
     */
    int addDataSetLock( DataSetLock dataSetLock );

    /**
     * Updates a DataSetLock.
     * 
     * @param dataSetLock The DataSetLock to update.
     */
    void updateDataSetLock( DataSetLock dataSetLock );

    /**
     * Deletes a DataSetLock.
     * 
     * @param dataSetLock The DataSetLock to delete.
     */
    void deleteDataSetLock( DataSetLock dataSetLock );

    /**
     * Get a DataSetLock
     * 
     * @param id The unique identifier for the DataSetLock to get.
     * @return The DataSetLock with the given id or null if it does not exist.
     */
    DataSetLock getDataSetLock( int id );

    /**
     * Returns a Collection of DataSetLocks with the given DataSet.
     * 
     * @param name The dataSetLock.
     * @return A Collection of DataSetLock with the given DataSet.
     */
    Collection<DataSetLock> getDataSetLockByDataSet( DataSet dataSet );

    /**
     * Returns a Collection of DataSetLock with the given period.
     * 
     * @param period The DataSetLock.
     * @return A Collection of DataSetLock with the given DataSetLock.
     */
    Collection<DataSetLock> getDataSetLockByPeriod( Period period );
    
    /**
     * Returns the DataSetLock with the given dataSet and period.
     * 
     * @param dataSet and period The DataSetLock.
     * @return The DataSetLock with the given DataSetLock.
     */
    DataSetLock getDataSetLockByDataSetAndPeriod( DataSet dataSet, Period period );
    
    /**
     * Returns the DataSetLock with the given dataSet, period, and source
     * 
     * @param dataSet, period, source The DataSetLock.
     * @return The DataSetLock with the given DataSetLock.
     */   
    DataSetLock getDataSetLockByDataSetPeriodAndSource( DataSet dataSet, Period period, OrganisationUnit source );
   
    /**
     * Returns all DataSetLocks associated with the specified source.
     */
    Collection<DataSetLock> getDataSetLocksBySource( OrganisationUnit source );

    /**
     * Get all DataSetLocks.
     * 
     * @return A collection containing all DataSetLocks.
     */
    Collection<DataSetLock> getAllDataSetLocks();
    
}