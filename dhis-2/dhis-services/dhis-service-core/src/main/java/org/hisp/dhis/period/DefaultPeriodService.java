package org.hisp.dhis.period;

/*
 * Copyright (c) 2004-2007, University of Oslo
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.source.Source;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.Filter;
import org.hisp.dhis.system.util.FilterUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Kristian Nordal
 * @version $Id: DefaultPeriodService.java 5983 2008-10-17 17:42:44Z larshelg $
 */
@Transactional
public class DefaultPeriodService
    implements PeriodService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodStore periodStore;

    public void setPeriodStore( PeriodStore periodStore )
    {
        this.periodStore = periodStore;
    }

    // -------------------------------------------------------------------------
    // Period
    // -------------------------------------------------------------------------

    public int addPeriod( Period period )
    {
        return periodStore.addPeriod( period );
    }

    public void deletePeriod( Period period )
    {
        periodStore.deletePeriod( period );
    }

    public Period getPeriod( int id )
    {
        return periodStore.getPeriod( id );
    }

    public Period getPeriod( Date startDate, Date endDate, PeriodType periodType )
    {
        return periodStore.getPeriod( startDate, endDate, periodType );
    }

    public Collection<Period> getAllPeriods()
    {
        return periodStore.getAllPeriods();
    }

    public Collection<Period> getPeriods( final Collection<Integer> identifiers )
    {
        Collection<Period> periods = getAllPeriods();
        
        return identifiers == null ? periods : FilterUtils.filter( periods, new Filter<Period>()
            {
                public boolean retain( Period object )
                {
                    return identifiers.contains( object.getId() );
                }
            } );
    }

    public Collection<Period> getPeriodsByPeriodType( PeriodType periodType )
    {
        return periodStore.getPeriodsByPeriodType( periodType );
    }

    public Collection<Period> getPeriodsBetweenDates( Date startDate, Date endDate )
    {
        return periodStore.getPeriodsBetweenDates( startDate, endDate );
    }

    public Collection<Period> getPeriodsBetweenDates( PeriodType periodType, Date startDate, Date endDate )
    {
        return periodStore.getPeriodsBetweenDates( periodType, startDate, endDate );
    }

    public Collection<Period> getIntersectingPeriodsByPeriodType( PeriodType periodType, Date startDate, Date endDate )
    {
        return periodStore.getIntersectingPeriodsByPeriodType( periodType, startDate, endDate );
    }

    public Collection<Period> getIntersectingPeriods( Date startDate, Date endDate )
    {
        return periodStore.getIntersectingPeriods( startDate, endDate );
    }

    public Collection<Period> getBoundaryPeriods( Period period, Collection<Period> periods )
    {
        Collection<Period> immutablePeriods = new ArrayList<Period>( periods );

        Iterator<Period> iterator = immutablePeriods.iterator();

        while ( iterator.hasNext() )
        {
            Period iterated = iterator.next();

            if ( !DateUtils.strictlyBetween( period.getStartDate(), iterated.getStartDate(), iterated.getEndDate() )
                && !DateUtils.strictlyBetween( period.getEndDate(), iterated.getStartDate(), iterated.getEndDate() ) )
            {
                iterator.remove();
            }
        }

        return immutablePeriods;
    }

    public Collection<Period> getInclusivePeriods( Period period, Collection<Period> periods )
    {
        Collection<Period> immutablePeriods = new ArrayList<Period>( periods );

        Iterator<Period> iterator = immutablePeriods.iterator();

        while ( iterator.hasNext() )
        {
            Period iterated = iterator.next();

            if ( !DateUtils.between( iterated.getStartDate(), period.getStartDate(), period.getEndDate() )
                || !DateUtils.between( iterated.getEndDate(), period.getStartDate(), period.getEndDate() ) )
            {
                iterator.remove();
            }
        }

        return immutablePeriods;
    }

    public Collection<Period> getPeriods( Period period, Collection<DataElement> dataElements,
        Collection<? extends Source> sources )
    {
        return periodStore.getPeriods( period, dataElements, sources );
    }

    public Period getRelativePeriod( Date date, int startMonths, int endMonths )
    {
        if ( startMonths >= endMonths )
        {
            throw new IllegalArgumentException( "End months must be greater than start months" );
        }

        PeriodType periodType = periodStore.getPeriodType( RelativePeriodType.class );

        // ---------------------------------------------------------------------
        // Reload PeriodType
        // ---------------------------------------------------------------------

        if ( periodType == null )
        {
            periodType = new RelativePeriodType();

            periodStore.addPeriodType( periodType );
        }

        Calendar cal = PeriodType.createCalendarInstance( date );

        cal.add( Calendar.MONTH, startMonths );
        cal.set( Calendar.DAY_OF_MONTH, cal.getActualMinimum( Calendar.DAY_OF_MONTH ) );

        Date startDate = cal.getTime();

        cal = PeriodType.createCalendarInstance( date );

        cal.add( Calendar.MONTH, endMonths - 1 );
        cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DAY_OF_MONTH ) );

        Date endDate = cal.getTime();

        Period period = new Period( periodType, startDate, endDate );

        // ---------------------------------------------------------------------
        // Persist period if it does not exist
        // ---------------------------------------------------------------------

        Period persistedPeriod = getPeriod( startDate, endDate, periodType );

        if ( persistedPeriod == null )
        {
            addPeriod( period );
        }
        else
        {
            period = persistedPeriod;
        }

        return period;
    }

    public Period getRelativePeriod( Date date, int months )
    {
        if ( months == 0 )
        {
            throw new IllegalArgumentException( "Months cannot be zero" );
        }

        PeriodType periodType = periodStore.getPeriodType( RelativePeriodType.class );

        // ---------------------------------------------------------------------
        // Reload PeriodType
        // ---------------------------------------------------------------------

        if ( periodType == null )
        {
            periodType = new RelativePeriodType();

            periodStore.addPeriodType( periodType );
        }

        Calendar cal = PeriodType.createCalendarInstance( date );

        Date startDate = null;
        Date endDate = null;

        if ( months > 0 )
        {
            cal.set( Calendar.DAY_OF_MONTH, cal.getActualMinimum( Calendar.DAY_OF_MONTH ) );

            startDate = cal.getTime();

            cal.add( Calendar.MONTH, months - 1 );
            cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DAY_OF_MONTH ) );

            endDate = cal.getTime();
        }
        else
        {
            cal.set( Calendar.DAY_OF_MONTH, cal.getActualMaximum( Calendar.DAY_OF_MONTH ) );

            endDate = cal.getTime();

            cal.add( Calendar.MONTH, months + 1 );
            cal.set( Calendar.DAY_OF_MONTH, cal.getActualMinimum( Calendar.DAY_OF_MONTH ) );

            startDate = cal.getTime();
        }

        Period period = new Period( periodType, startDate, endDate );

        // ---------------------------------------------------------------------
        // Persist period if it does not exist
        // ---------------------------------------------------------------------

        Period persistedPeriod = getPeriod( startDate, endDate, periodType );

        if ( persistedPeriod == null )
        {
            addPeriod( period );
        }
        else
        {
            period = persistedPeriod;
        }

        return period;
    }
    
    public List<Period> getPeriods( Period lastPeriod, int historyLength )
    {
        List<Period> periods = new ArrayList<Period>( historyLength );

        CalendarPeriodType periodType = (CalendarPeriodType) lastPeriod.getPeriodType();

        Period period = lastPeriod;
        
        Period p = new Period();

        for ( int i = 0; i < historyLength; ++i )
        {
            p = getPeriodFromDates( period.getStartDate(), period.getEndDate(), periodType );
            
            periods.add( p != null ? p : period );
            
            period = periodType.getPreviousPeriod( period );
        }

        Collections.reverse( periods );

        return periods;
    }

    // -------------------------------------------------------------------------
    // PeriodType
    // -------------------------------------------------------------------------

    public PeriodType getPeriodType( int id )
    {
        return periodStore.getPeriodType( id );
    }

    public List<PeriodType> getAllPeriodTypes()
    {
        return PeriodType.getAvailablePeriodTypes();
    }

    public PeriodType getPeriodTypeByName( String name )
    {
        return PeriodType.getPeriodTypeByName( name );
    }
    
    public PeriodType getPeriodTypeByClass( Class<? extends PeriodType> periodType )
    {
        return periodStore.getPeriodType( periodType );
    }
    
    public Period getPeriodFromDates( Date startDate, Date endDate, PeriodType periodType )
    {
        return periodStore.getPeriodFromDates( startDate, endDate, periodType );
    }
}
