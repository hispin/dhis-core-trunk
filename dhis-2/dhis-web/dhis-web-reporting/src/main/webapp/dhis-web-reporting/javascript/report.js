function addReport()
{
	if ( $( "#id" ).val().length == 0 && !hasText( "upload" ) )
	{
		setMessage( i18n_please_specify_file );
		return false;
	}
	
	$.postJSON( "validateReport.action", { id:$( "#id" ).val(), "name":$( "#name" ).val() }, function( json )
	{
		if ( json.response == "input" )
		{
			setMessage( json.message );
			return false;
		}
		else if ( json.response == "success" )
		{
        	$( "#reportForm" ).submit();
		}
	} );
}

function removeReport( id )
{
    removeItem( id, "", i18n_confirm_remove_report, "removeReport.action" );
}

function addToDashboard( id )
{
    var dialog = window.confirm( i18n_confirm_add_to_dashboard );

    if ( dialog )
    {
        $.get( "addReportToDashboard.action?id=" + id );
    }
}

function toggleDataSource()
{
	$( ".reportTableDataSource" ).toggle();
	$( ".jdbcDataSource" ).toggle();
}

// -----------------------------------------------------------------------------
// View details
// -----------------------------------------------------------------------------

function showReportDetails( reportId )
{
	jQuery.get( 'getReport.action', { "id": reportId }, function( json )
	{
		setInnerHTML( 'nameField', json.report.name );

		var reportTableName = json.report.reportTableName;
		setInnerHTML( 'reportTableNameField', reportTableName ? reportTableName : '[' + i18n_none + ']' );

		var orgGroupSets = json.report.orgGroupSets;
		setInnerHTML( 'orgGroupSetsField', orgGroupSets == 'true' ? i18n_yes : i18n_no );

		showDetails();
	});
}