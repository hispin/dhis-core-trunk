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

package org.hisp.dhis.reportexcel.datasetcompleted.action;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.comparator.DataSetNameComparator;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.comparator.PeriodComparator;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserStore;

import com.opensymphony.xwork2.Action;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */
public class GetDataSetAndPeriodByPeriodTypeAction
    implements Action
{

    // -------------------------------------------
    // Dependency
    // -------------------------------------------

    private PeriodService periodService;

    private DataSetService dataSetService;

    private CurrentUserService currentUserService;

    private UserStore userStore;

    // -------------------------------------------
    // Input
    // -------------------------------------------

    private String periodTypeName;

    // -------------------------------------------
    // Output
    // -------------------------------------------

    private List<Period> periods;

    private List<DataSet> dataSets;

    // -------------------------------------------
    // Getter & Setter
    // -------------------------------------------

    public List<Period> getPeriods()
    {
        return periods;
    }

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    public List<DataSet> getDataSets()
    {
        return dataSets;
    }

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    public void setPeriodTypeName( String periodTypeName )
    {
        this.periodTypeName = periodTypeName;
    }

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    public void setUserStore( UserStore userStore )
    {
        this.userStore = userStore;
    }

    public String execute()
        throws Exception
    {
        PeriodType periodType = periodService.getPeriodTypeByName( periodTypeName );

        User currentUser = currentUserService.getCurrentUser();

        dataSets = new ArrayList<DataSet>();

        if ( !currentUserService.currentUserIsSuper() )
        {
            UserCredentials credentials = userStore.getUserCredentials( currentUser );

            for ( UserAuthorityGroup group : credentials.getUserAuthorityGroups() )
            {
                dataSets.addAll( group.getDataSets() );
            }
        }
        else
        {
            dataSets = new ArrayList<DataSet>( dataSetService.getDataSetsByPeriodType( periodType ) );
        }

        Collections.sort( dataSets, new DataSetNameComparator() );

        periods = new ArrayList<Period>( periodService.getPeriodsByPeriodType( periodType ) );

        Collections.sort( periods, new PeriodComparator() );

        return SUCCESS;
    }

}
