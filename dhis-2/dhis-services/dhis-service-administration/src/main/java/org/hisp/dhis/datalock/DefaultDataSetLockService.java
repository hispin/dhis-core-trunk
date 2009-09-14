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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.source.Source;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Brajesh Murari
 * @version $Id$
 */

@Transactional
public class DefaultDataSetLockService
    implements DataSetLockService
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataSetLockStore dataSetLockStore;

    public void setDataSetLockStore( DataSetLockStore dataSetLockStore )
    {
        this.dataSetLockStore = dataSetLockStore;
    }
    
    // -------------------------------------------------------------------------
    // DataSet
    // -------------------------------------------------------------------------
    
    public int addDataSetLock( DataSetLock dataSetLock )
    {
        int id = dataSetLockStore.addDataSetLock( dataSetLock );
        
        return id;
    }

    public void updateDataSetLock( DataSetLock dataSetLock )
    {
        dataSetLockStore.updateDataSetLock( dataSetLock );
    }
    
    public void deleteDataSetLock( DataSetLock dataSetLock )
    {        
        dataSetLockStore.deleteDataSetLock( dataSetLock );
    }

    public Collection<DataSetLock> getAllDataSetLocks()
    {
        return dataSetLockStore.getAllDataSetLocks();
    }
   
    public DataSetLock getDataSetLock( int id )
    {
        return dataSetLockStore.getDataSetLock( id );
    }

    public DataSetLock getDataSetLockByDataSet( DataSet dataSet )
    {
        return dataSetLockStore.getDataSetLockByDataSet( dataSet );
    }
   
    public DataSetLock getDataSetLockByPeriod( Period period )
    {
        return dataSetLockStore.getDataSetLockByPeriod( period );
    }
    
    public DataSetLock getDataSetLockByDataSetAndPeriod( DataSet dataSet, Period period )
    {
        return dataSetLockStore.getDataSetLockByDataSetAndPeriod( dataSet, period );
    }

    public  DataSetLock getDataSetLockByDataSetPeriodAndSource( DataSet dataSet, Period period, Source source )
    {
        return dataSetLockStore.getDataSetLockByDataSetPeriodAndSource( dataSet, period, source );       
    }
    
    public Collection<DataSetLock> getDataSetLocks( Collection<Integer> identifiers )
    {
        if ( identifiers == null )
        {
            return getAllDataSetLocks();
        }        
        
        Collection<DataSetLock> objects = new ArrayList<DataSetLock>();
        
        for ( Integer id : identifiers )
        {
            objects.add( getDataSetLock( id ) );
        }
        
        return objects;
    }

    public Collection<DataSetLock> getDataSetLocksBySource( Source source )
    {
        return dataSetLockStore.getDataSetLocksBySource( source );
    }

    public Collection<DataSetLock> getDataSetLocksBySources( Collection<? extends Source> sources )
    {
        Set<DataSetLock> dataSetLocks = new HashSet<DataSetLock>();
        
        for ( Source source : sources )
        {
            dataSetLocks.addAll( dataSetLockStore.getDataSetLocksBySource( source ) );
        }
        
        return dataSetLocks;
    }

    public Collection<DataSet> getDistinctDataSets( Collection<Integer> dataSetLockIdentifiers )
    {
        Collection<DataSetLock> dataSetLocks = getDataSetLocks( dataSetLockIdentifiers );
        
        Set<DataSet> dataSets = new HashSet<DataSet>();
       
        for ( DataSetLock dataSetLock : dataSetLocks )
        {
            dataSets.add( dataSetLock.getDataSet() );
        }
        
        return dataSets;
    }

    public int getSourcesAssociatedWithDataSetLock( DataSetLock dataSetLock, Collection<? extends Source> sources )
    {
        int count = 0;
        
        for ( Source source : sources )
        {
            if ( dataSetLock.getSources().contains( source ) )
            {
                count++;
            }
        }
        
        return count;
    }
}
