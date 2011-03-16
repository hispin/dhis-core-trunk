package org.hisp.dhis.web.api.model;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MobileModel
    implements DataStreamSerializable
{
    private ActivityPlan activityPlan;

    private List<Program> programs;
    
    private Date serverCurrentDate;

    private List<DataSet> datasets;

    public ActivityPlan getActivityPlan()
    {
        return activityPlan;
    }

    public void setActivityPlan( ActivityPlan activityPlan )
    {
        this.activityPlan = activityPlan;
    }

    public List<Program> getPrograms()
    {
        return programs;
    }

    public void setPrograms( List<Program> programs )
    {
        this.programs = programs;
    }
    

    public Date getServerCurrentDate() {
		return serverCurrentDate;
	}

	public void setServerCurrentDate(Date serverCurrentDate) {
		this.serverCurrentDate = serverCurrentDate;
	}

	public List<DataSet> getDatasets()
    {
        return datasets;
    }

    public void setDatasets( List<DataSet> datasets )
    {
        this.datasets = datasets;
    }

    @Override
    public void serialize( DataOutputStream dout )
        throws IOException
    {

        if ( programs == null )
        {
            dout.writeInt( 0 );
        }
        else
        {
            dout.writeInt( programs.size() );

            for ( Program prog : programs )
            {
                prog.serialize( dout );
            }
        }

        // Write ActivityPlans
        if ( this.activityPlan == null )
        {
            dout.writeInt( 0 );
        }
        else
        {
            this.activityPlan.serialize( dout );
        }
        
        dout.writeLong(serverCurrentDate.getTime());

        // Write DataSets
        if ( datasets == null )
        {
            dout.writeInt( 0 );
        }
        else
        {
            dout.writeInt( datasets.size() );
            for ( DataSet ds : datasets )
            {
                ds.serialize( dout );
            }
        }
    }

    @Override
    public void deSerialize( DataInputStream dataInputStream )
        throws IOException
    {
        // FIXME: Get implementation from client

    }

}
