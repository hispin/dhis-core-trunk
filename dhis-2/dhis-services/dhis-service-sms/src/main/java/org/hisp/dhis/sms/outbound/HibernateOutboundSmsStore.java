package org.hisp.dhis.sms.outbound;

/*
 * Copyright (c) 2004-2012, University of Oslo
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class HibernateOutboundSmsStore
    extends HibernateGenericStore<OutboundSms>
    implements OutboundSmsStore
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public int saveOutboundSms( OutboundSms sms )
    {
        checkDate( sms );
        //return (Integer) sessionFactory.getCurrentSession().save( sms );
        return save( sms );
    }

    private void checkDate( OutboundSms sms )
    {
        if ( sms.getDate() == null )
        {
            sms.setDate( new Date() );
        }
    }

    @Override
    public OutboundSms getOutboundSmsbyId( int id )
    {
        /*Session session = sessionFactory.getCurrentSession();
        return (OutboundSms) session.get( OutboundSms.class, id );*/
        return get( id );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public List<OutboundSms> getAllOutboundSms()
    {
        /*Session session = sessionFactory.getCurrentSession();
        return session.createCriteria( OutboundSms.class ).addOrder( Order.desc( "date" ) ).list();*/
        return getCriteria().addOrder( Order.desc( "date" ) ).list();
    }

    @Override
    public List<OutboundSms> get( OutboundSmsStatus status )
    {
        int realStatus = 0;

        if ( status.equals( OutboundSmsStatus.OUTBOUND ) )
        {
            realStatus = OutboundSmsStatus.OUTBOUND.ordinal();
        }
        else if ( status.equals( OutboundSmsStatus.SENT ) )
        {
            realStatus = OutboundSmsStatus.SENT.ordinal();
        }
        else
        {
            realStatus = OutboundSmsStatus.ERROR.ordinal();
        }

        String sql = "select osm.id as outboundsmsid, message, ore.elt as phonenumber, date "
            + "from outbound_sms osm inner join outbound_sms_recipients ore "
            + "on osm.id=ore.outbound_sms_id where status = " + realStatus;
        
        try
        {
            List<OutboundSms> OutboundSmsList = jdbcTemplate.query( sql, new RowMapper<OutboundSms>()
            {
                public OutboundSms mapRow( ResultSet rs, int rowNum )
                    throws SQLException
                {
                    OutboundSms outboundSms = new OutboundSms( rs.getString( 2 ), rs.getString( 3 ) );
                    outboundSms.setId( rs.getInt( 1 ) );
                    outboundSms.setDate( rs.getDate( 4 ) );
                    return outboundSms;
                }
            } );

            return OutboundSmsList;
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateOutboundSms( OutboundSms sms )
    {
        update( sms );
        //sessionFactory.getCurrentSession().update( sms );
    }

    @Override
    public void deleteOutboundSms( OutboundSms sms )
    {
        delete( sms );
        //sessionFactory.getCurrentSession().delete( sms );
    }
}
