package org.hisp.dhis.datastatistics;

/*
 * Copyright (c) 2004-2016, University of Oslo
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

import org.hisp.dhis.chart.Chart;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dashboard.Dashboard;
import org.hisp.dhis.eventchart.EventChart;
import org.hisp.dhis.eventreport.EventReport;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.mapping.Map;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Yrjan A. F. Fraschetti
 * @author Julie Hill Roa
 */
@Transactional
public class DefaultDataStatisticsService 
    implements DataStatisticsService
{
    @Autowired
    private DataStatisticsStore hibernateDataStatisticsStore;

    @Autowired
    private DataStatisticsEventStore hibernateDataStatisticsEventStore;

    @Autowired
    private UserService userService;

    @Autowired
    private IdentifiableObjectManager identifiableObjectManager;

    /**
     * Adds an DataStatistics event in the database
     *
     * @param event object to be saved
     * @return id of the object in the database
     */
    public int addEvent( DataStatisticsEvent event )
    {
        return hibernateDataStatisticsEventStore.save( event );
    }

    /**
     * Gets number of saved Reports from a start date too a end date
     *
     * @param startDate start date
     * @param endDate end date
     * @param eventInterval event interval.
     * @return list of reports
     */
    @Override
    public List<AggregatedStatistics> getReports( Date startDate, Date endDate, EventInterval eventInterval )
    {
        String sql = "";
        
        switch ( eventInterval )
        {
            case DAY:
                sql = getDaySql( startDate, endDate );
                break;
            case WEEK:
                sql = getWeekSql( startDate, endDate );
                break;
            case MONTH:
                sql = getMonthSql( startDate, endDate );
                break;
            case YEAR:
                sql = getYearSql( startDate, endDate );
                break;
            default:
                sql = getDaySql( startDate, endDate );
        }
        
        return hibernateDataStatisticsStore.getSnapshotsInInterval( sql, eventInterval );
    }

    /**
     * Creating a SQL for retrieving aggregated data with group by YEAR
     *
     * @param start start date
     * @param end  end date
     * @return SQL string
     */
    private String getYearSql( Date start, Date end )
    {
        return "select extract(year from created) as yr, " +
            commonSql( start, end ) +
            " order by yr;";
    }

    /**
     * Creating a SQL for retrieving aggregated data with group by YEAR, MONTH
     *
     * @param start start date
     * @param end  end date
     * @return SQL string
     */
    private String getMonthSql( Date start, Date end )
    {
        return "select extract(year from created) as yr, " +
            "extract(month from created) as mnt, " +
            commonSql( start, end ) +
            ", mnt order by yr, mnt;";
    }

    /**
     * Creating a SQL for retrieving aggregated data with group by YEAR, WEEK
     *
     * @param start start date
     * @param end end date
     * @return SQL string
     */
    private String getWeekSql( Date start, Date end )
    {
        return "select extract(year from created) as yr, " +
            "extract(week from created) as week, " +
            commonSql( start, end ) +
            ", week order by yr, week;";
    }

    /**
     * Creating a SQL for retrieving aggregated data with group by YEAR, DAY
     *
     * @param start start date
     * @param end end date
     * @return SQL string
     */
    private String getDaySql( Date start, Date end )
    {
        return "select extract(year from created) as yr, " +
            "extract(month from created) as mnt,"+
            "extract(day from created) as day, " +
            commonSql( start, end ) +
            ", mnt, day order by yr, mnt, day;";
    }

    /**
     * private method: part of sql witch is always the same in the different intervals YEAR, MONTH, WEEK and DAY
     *
     * @param start start date
     * @param end end date
     * @return sql string
     */
    private String commonSql( Date start, Date end )
    {
        return "max(active_users) as activeUsers," +
            "cast(round(cast(sum(mapviews) as numeric),0) as int) as mapViews," +
            "cast(round(cast(sum(chartviews) as numeric),0) as int) as chartViews," +
            "cast(round(cast(sum(reporttableviews) as numeric),0) as int) as reportTablesViews, " +
            "cast(round(cast(sum(eventreportviews) as numeric),0) as int) as eventReportViews, " +
            "cast(round(cast(sum(eventchartviews) as numeric),0) as int) as eventChartViews," +
            "cast(round(cast(sum(dashboardviews) as numeric),0) as int) as dashboardViews, " +
            "cast(round(cast(sum(indicatorviews) as numeric),0) as int) as indicatorsViews, " +
            "cast(round(cast(sum(totalviews) as numeric),0) as int) as totalViews," +
            "cast(round(cast(sum(average_views) as numeric),0) as int) as averageViews, " +
            "cast(round(cast(sum(maps) as numeric),0) as int) as savedMaps," +
            "cast(round(cast(sum(charts) as numeric),0) as int) as savedCharts," +
            "cast(round(cast(sum(reporttables) as numeric),0) as int) as savedReportTables," +
            "cast(round(cast(sum(eventreports) as numeric),0) as int) as savedEventReports," +
            "cast(round(cast(sum(eventcharts) as numeric),0) as int) as savedEventCharts," +
            "cast(round(cast(sum(dashborards) as numeric),0) as int) as savedDashboards, " +
            "cast(round(cast(sum(indicators) as numeric),0) as int) as savedIndicators," +
            "max(users) as users from datastatistics " +
            "where (created between '" + start + "'and '" + end + "') group by yr";
    }

    /**
     * Gets all important information and creates a DataStatistics object 
     * and persists it.
     */
    @Override
    public int saveSnapshot( )
    {
        Date now = new Date(  );
        Date startDate = new Date( );
        Calendar c = Calendar.getInstance();
        c.setTime( startDate );
        c.add( Calendar.DATE, -1 );
        startDate = c.getTime( );

        int totalUsers = identifiableObjectManager.getCount( User.class );
        double savedMaps = identifiableObjectManager.getCountByCreated( Map.class, startDate );
        double savedCharts = identifiableObjectManager.getCountByCreated( Chart.class, startDate );
        double savedReportTables = identifiableObjectManager.getCountByCreated( ReportTable.class, startDate );
        double savedEventReports = identifiableObjectManager.getCountByCreated( EventReport.class, startDate );
        double savedEventCharts = identifiableObjectManager.getCountByCreated( EventChart.class, startDate );
        double savedDashboards = identifiableObjectManager.getCountByCreated( Dashboard.class, startDate );
        double savedIndicators = identifiableObjectManager.getCountByCreated( Indicator.class, startDate );
        int activeUsers = userService.getActiveUsersCount( 1 );

        double chartViews = 0;
        double mapViews = 0;
        double dashboardViews = 0;
        double reportTablesViews = 0;
        double eventReportViews = 0;
        double eventChartViews = 0;
        double indicatorsViews = 0;
        double totalNumberOfViews = 0;
        double averageNumberofViews = 0;

        String sql = "select eventtype as eventtype, count(eventtype) as numberofviews from datastatisticsevent where (timestamp between '" + 
            startDate + "' and '" + now + "') group by eventtype;";

        List<int[]> list = hibernateDataStatisticsEventStore.getDataStatisticsEventCount( sql );

        for ( int i = 0; i < list.size(); i++ )
        {
            int[] temp = (int[]) list.get( i );
            
            switch ( temp[0] )
            {
                case 0:
                    chartViews = temp[1];
                    totalNumberOfViews += chartViews;
                    break;
                case 1: 
                    mapViews = temp[1];
                    totalNumberOfViews += mapViews;
                    break;
                case 2: 
                    dashboardViews = temp[1];
                    totalNumberOfViews += dashboardViews;
                    break;
                case 3: 
                    reportTablesViews = temp[1];
                    totalNumberOfViews += reportTablesViews;
                    break;
                case 4: 
                    eventReportViews = temp[1];
                    totalNumberOfViews += eventReportViews;
                    break;
                case 5: 
                    eventChartViews = temp[1];
                    totalNumberOfViews += eventChartViews;
                    break;
                case 6: 
                    indicatorsViews = temp[1];
                    totalNumberOfViews += indicatorsViews;
                    break;
            }
        }
        
        if ( activeUsers != 0 )
        {
            averageNumberofViews = totalNumberOfViews/activeUsers;
        }
        else
        {
            averageNumberofViews = totalNumberOfViews;
        }

        DataStatistics dataStatistics = new DataStatistics( activeUsers, mapViews, chartViews,
            reportTablesViews, eventReportViews, eventChartViews, dashboardViews,
            indicatorsViews, totalNumberOfViews, averageNumberofViews, savedMaps,
            savedCharts, savedReportTables, savedEventReports,
            savedEventCharts, savedDashboards, savedIndicators,
            totalUsers );

        return hibernateDataStatisticsStore.save( dataStatistics );
    }
}
