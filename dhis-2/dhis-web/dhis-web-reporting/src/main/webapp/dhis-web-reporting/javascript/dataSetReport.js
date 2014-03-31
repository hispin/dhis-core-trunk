
dhis2.util.namespace( 'dhis2.dsr' );

dhis2.dsr.currentPeriodOffset = 0;
dhis2.dsr.periodTypeFactory = new PeriodType();
dhis2.dsr.currentDataSetReport = null;
dhis2.dsr.permissions = null;

//------------------------------------------------------------------------------
// Get and set methods
//------------------------------------------------------------------------------

function getDataSetReport()
{
    var dataSetReport = {
        ds: $( "#dataSetId" ).val(),
        cc: $( "#dataSetId :selected" ).data( "categorycombo" ),
        periodType: $( "#periodType" ).val(),
        pe: $( "#periodId" ).val(),
        ou: selectionTreeSelection.getSelectedUid()[0],
        selectedUnitOnly: $( "#selectedUnitOnly" ).is( ":checked" ),
        offset: dhis2.dsr.currentPeriodOffset
    };
        
    var dims = [];
    var cps = [];
    
    $( ".dimension" ).each( function( index, value ) {
    	var dim = $( this ).data( "uid" );
    	var item = $( this ).val();
    	
    	if ( dim && item && item != -1 )
    	{
    		var dimQuery = dim + ":" + item;
    		dims.push( dimQuery );
    		cps.push( item );
    	}
    } );
    
    dataSetReport.dimension = dims;
    dataSetReport.cp = cps;
    
    return dataSetReport;
}

function setDataSetReport( dataSetReport )
{
	$( "#dataSetId" ).val( dataSetReport.dataSet );
	$( "#periodType" ).val( dataSetReport.periodType );
	
	dhis2.dsr.currentPeriodOffset = dataSetReport.offset;
	
	displayPeriods();
	$( "#periodId" ).val( dataSetReport.period );
	
	selectionTreeSelection.setMultipleSelectionAllowed( false );
	selectionTree.buildSelectionTree();
	
	$( "body" ).on( "oust.selected", function() 
	{
		$( "body" ).off( "oust.selected" );
		generateDataSetReport();
	} );
}

//------------------------------------------------------------------------------
// Data set
//------------------------------------------------------------------------------

/**
 * Callback for changes to data set selection.
 */
dhis2.dsr.dataSetSelected = function()
{
	var ds = $( "#dataSetId" ).val();
	var cc = $( "#dataSetId :selected" ).data( "categorycombo" );
	
	if ( cc && cc != dhis2.dsr.metaData.defaultCategoryCombo ) {
		var categoryCombo = dhis2.dsr.metaData.categoryCombos[cc];
		var categoryIds = categoryCombo.categories;
		
		dhis2.dsr.setAttributesMarkup( categoryIds );		
	}
	else {
		$( "#attributeComboDiv" ).html( "" ).hide();
	}
}

/**
* Sets markup for drop down boxes for the given categories in the selection div.
*/
dhis2.dsr.setAttributesMarkup = function( categoryIds )
{
	if ( !categoryIds || categoryIds.length == 0 ) {
		return;
	}
	
	var categoryRx = [];	
	$.each( categoryIds, function( idx, id ) {
		categoryRx.push( $.get( "../api/categories/" + id + ".json" ) );
	} );

	$.when.apply( $, categoryRx ).done( function() {
		var html = '';
		var args = dhis2.util.normalizeArguments( arguments );
		
		$.each( args, function( idx, cat ) {
			var category = cat[0];
			
			html += '<div class="inputSection">';
			html += '<label>' + category.name + '</label>';
			html += '<select class="dimension" data-uid="' + category.id + '" style="width:330px">';
			html += '<option value="-1">[ ' + i18n_select_option_view_all + ' ]</option>';
			
			$.each( category.items, function( idx, option ) {
				html += '<option value="' + option.id + '">' + option.name + '</option>';
			} );
			
			html += '</select>';
			html += '</div>';
		} );

		$( "#attributeComboDiv" ).show().html( html );
	} );
}

/**
 * Indicates whether all attributes have a valid selection.
 */
dhis2.dsr.attributesSelected = function( dataSetReport )
{
	if ( dhis2.dsr.metaData.defaultCategoryCombo == dataSetReport.cc ) {
		return true; // Default category combo requires no selection
	}
	
	var cc = dataSetReport.cc;
	var categoryCombo = dhis2.dsr.metaData.categoryCombos[cc];
	
	if ( !categoryCombo || !categoryCombo.categories ) {
		return false;
	}
		
	var expected = categoryCombo.categories.length;
	var actual = dataSetReport.cp.length;
	
	return !!( expected == actual );
}

//------------------------------------------------------------------------------
// Period
//------------------------------------------------------------------------------

function displayPeriods()
{
    var periodType = $( "#periodType" ).val();
    var periods = dhis2.dsr.periodTypeFactory.get( periodType ).generatePeriods( dhis2.dsr.currentPeriodOffset );
    periods = dhis2.dsr.periodTypeFactory.reverse( periods );
    periods = dhis2.dsr.periodTypeFactory.filterFuturePeriodsExceptCurrent( periods );

    $( "#periodId" ).removeAttr( "disabled" );
    clearListById( "periodId" );

    for ( i in periods )
    {
        addOptionById( "periodId", periods[i].iso, periods[i].name );
    }
}

function displayNextPeriods()
{
    if ( dhis2.dsr.currentPeriodOffset < 0 ) // Cannot display future periods
    {
        dhis2.dsr.currentPeriodOffset++;
        displayPeriods();
    }
}

function displayPreviousPeriods()
{
    dhis2.dsr.currentPeriodOffset--;
    displayPeriods();
}

//------------------------------------------------------------------------------
// Run report
//------------------------------------------------------------------------------

function drillDownDataSetReport( orgUnitId, orgUnitUid )
{
	selectionTree.clearSelectedOrganisationUnits();
	selectionTreeSelection.select( orgUnitId );
	
	var dataSetReport = getDataSetReport();
	dataSetReport["ou"] = orgUnitUid;
	displayDataSetReport( dataSetReport );
}

function generateDataSetReport()
{
	var dataSetReport = getDataSetReport();
	displayDataSetReport( dataSetReport );
}

function displayDataSetReport( dataSetReport )
{	
    if ( !dataSetReport.ds )
    {
        setHeaderMessage( i18n_select_data_set );
        return false;
    }
    if ( !dataSetReport.pe )
    {
        setHeaderMessage( i18n_select_period );
        return false;
    }
    if ( !selectionTreeSelection.isSelected() )
    {
        setHeaderMessage( i18n_select_organisation_unit );
        return false;
    }
    
    dhis2.dsr.currentDataSetReport = dataSetReport;
    
    hideHeaderMessage();
    hideCriteria();
    hideContent();
    showLoader();
	    
    var url = dhis2.dsr.getDataSetReportUrl( dataSetReport );
    
    $.get( url, function( data ) {
    	$( '#content' ).html( data );
    	hideLoader();
    	showContent();
    	dhis2.dsr.showApproval();
    	setTableStyles();
    } );
}

/**
 * Generates the URL for the given data set report.
 */
dhis2.dsr.getDataSetReportUrl = function( dataSetReport )
{
    var url = "generateDataSetReport.action" +
    	"?ds=" + dataSetReport.ds + 
    	"&pe=" + dataSetReport.pe + 
    	"&ou=" + dataSetReport.ou +
    	"&selectedUnitOnly=" + dataSetReport.selectedUnitOnly;
    
    $.each( dataSetReport.dimension, function( inx, val ) {
    	url += "&dimension=" + val;
    } );
    
    return url;
}

/**
 * Generates the URL for the approval of the given data set report.
 */
dhis2.dsr.getDataApprovalUrl = function( dataSetReport )
{
    var url = "../api/dataApprovals" +
        "?ds=" + dataSetReport.ds +
        "&pe=" + dataSetReport.pe +
        "&ou=" + dataSetReport.ou;

    if ( dataSetReport.cc && dataSetReport.cp && dataSetReport.cp.length > 0 ) {
        url += "&cc=" + dataSetReport.cc;
        url += "&cp=";

        $.each( dataSetReport.cp, function( idx, item ) {
            url += item + ";";
        } );

        url = url.slice( 0, -1 );
    }

    return url;
}

/**
 * Generates the URL for the acceptance of the given data set report approval.
 */
dhis2.dsr.getDataApprovalAcceptanceUrl = function( dataSetReport )
{
    var url = "../api/dataApprovals/acceptances" +
        "?ds=" + dataSetReport.ds +
        "&pe=" + dataSetReport.pe +
        "&ou=" + dataSetReport.ou;

    if ( dataSetReport.cc && dataSetReport.cp && dataSetReport.cp.length > 0 ) {
        url += "&cc=" + dataSetReport.cc;
        url += "&cp=";

        $.each( dataSetReport.cp, function( idx, item ) {
            url += item + ";";
        } );

        url = url.slice( 0, -1 );
    }

    return url;
}

function exportDataSetReport( type )
{
	var dataSetReport = dhis2.dsr.currentDataSetReport;
	
	var url = dhis2.dsr.getDataSetReportUrl( dataSetReport ) + "&type=" + type;
	    
	window.location.href = url;
}

function setUserInfo( username )
{
	$( "#userInfo" ).load( "../dhis-web-commons-ajax-html/getUser.action?username=" + username, function() {
		$( "#userInfo" ).dialog( {
	        modal : true,
	        width : 350,
	        height : 350,
	        title : "User"
	    } );
	} );	
}

function showCriteria()
{
	$( "#criteria" ).show( "fast" );
}

function hideCriteria()
{
	$( "#criteria" ).hide( "fast" );
}

function showContent()
{
	$( "#content" ).show( "fast" );
	$( ".downloadButton" ).show();
	$( "#interpretationArea" ).autogrow();
}

function hideContent()
{
	$( "#content" ).hide( "fast" );
	$( ".downloadButton" ).hide();
}

function showAdvancedOptions()
{
	$( "#advancedOptionsLink" ).hide();
	$( "#advancedOptions" ).show();
}

dhis2.dsr.showApproval = function()
{
	var dataSetReport = dhis2.dsr.currentDataSetReport;
	
	var approval = $( "#dataSetId :selected" ).data( "approval" );
	// var attributesSelected = dhis2.dsr.attributesSelected( dataSetReport );

	$( "#approvalNotification" ).hide();
    $( "#approvalDiv" ).hide();

	if ( !approval /* || !attributesSelected */ ) {
		return;
	}
	
	var url = dhis2.dsr.getDataApprovalUrl( dataSetReport );
	
	$.getJSON( url, function( json ) {
		if ( !json || !json.state ) {
			return;
		}

        dhis2.dsr.permissions = json;
		
		var state = json.state;

        $( "#approveButton" ).hide();
        $( "#unapproveButton" ).hide();
        $( "#acceptButton" ).hide();
        $( "#unacceptButton" ).hide();

        switch (state)
        {
            case "UNAPPROVED_WAITING":
                $( "#approvalNotification" ).show().html( i18n_waiting_for_lower_level_approval );
                break;

            case "UNAPPROVED_READY":
                $( "#approvalNotification" ).show().html( i18n_ready_for_approval );
                if ( json.mayApprove ) {
                    $( "#approvalDiv" ).show();
                    $( "#approveButton" ).show();
                }
                break;

            case "APPROVED_HERE":
                $( "#approvalNotification" ).show().html( i18n_approved );
                if ( json.mayUnapprove ) {
                    $( "#approvalDiv" ).show();
                    $( "#unapproveButton" ).show();
                }
                if ( json.mayAccept ) {
                    $( "#approvalDiv" ).show();
                    $( "#acceptButton" ).show();
                }
                break;

            case "ACCEPTED_HERE":
                $( "#approvalNotification" ).show().html( i18n_approved );
                if ( json.mayUnapprove ) {
                    $( "#approvalDiv" ).show();
                    $( "#unapproveButton" ).show();
                }
                if ( json.mayUnccept ) {
                    $( "#approvalDiv" ).show();
                    $( "#unacceptButton" ).show();
                }
                break;
        }
	} );
}

//------------------------------------------------------------------------------
// Approval
//------------------------------------------------------------------------------

dhis2.dsr.approveData = function()
{
	if ( !confirm( i18n_confirm_approval ) ) {
		return false;
	}
	
	var dataSetReport = dhis2.dsr.currentDataSetReport;
	var url = dhis2.dsr.getDataApprovalUrl( dataSetReport );
	
	$.ajax( {
		url: url,
		type: "post",
		success: function() {
            $( "#approvalNotification" ).show().html( i18n_approved );
            $( "#approvalDiv" ).hide();
			$( "#approveButton" ).hide();
            if ( dhis2.dsr.permissions.mayUnapprove ) {
                $( "#approvalDiv" ).show();
                $( "#unapproveButton" ).show();
            }
            if ( dhis2.dsr.permissions.mayAccept ) {
                $( "#approvalDiv" ).show();
                $( "#acceptButton" ).show();
            }
		},
		error: function( xhr, status, error ) {
			alert( xhr.responseText );
		}
	} );
}

dhis2.dsr.unapproveData = function()
{
	if ( !confirm( i18n_confirm_unapproval ) ) {
		return false;
	}
	
	var dataSetReport = dhis2.dsr.currentDataSetReport;
	var url = dhis2.dsr.getDataApprovalUrl( dataSetReport );
	
	$.ajax( {
		url: url,
		type: "delete",
		success: function() {
            $( "#approvalNotification" ).show().html( i18n_ready_for_approval );
            $( "#approvalDiv" ).hide();
            $( "#unapproveButton" ).hide();
            $( "#acceptButton" ).hide();
            $( "#unacceptButton" ).hide();
            if ( dhis2.dsr.permissions.mayApprove ) {
                $( "#approvalDiv" ).show();
                $( "#approveButton" ).show();
            }
		},
		error: function( xhr, status, error ) {
			alert( xhr.responseText );
		}
	} );
}

dhis2.dsr.acceptData = function()
{
    if ( !confirm( i18n_confirm_accept ) ) {
        return false;
    }

    var dataSetReport = dhis2.dsr.currentDataSetReport;
    var url = dhis2.dsr.getDataApprovalAcceptanceUrl( dataSetReport );

    $.ajax( {
        url: url,
        type: "post",
        success: function() {
            $( "#approvalNotification" ).show().html( i18n_approved_and_accepted );
            $( "#approvalDiv" ).hide();
            $( "#acceptButton" ).hide();
            if ( dhis2.dsr.permissions.mayUnapprove ) {
                $( "#approvalDiv" ).show();
                $( "#unapproveButton" ).show();
            }
            if ( dhis2.dsr.permissions.mayUnaccept ) {
                $( "#approvalDiv" ).show();
                $( "#unacceptButton" ).show();
            }
        },
        error: function( xhr, status, error ) {
            alert( xhr.responseText );
        }
    } );
}

dhis2.dsr.unacceptData = function()
{
    if ( !confirm( i18n_confirm_unaccept ) ) {
        return false;
    }

    var dataSetReport = dhis2.dsr.currentDataSetReport;
    var url = dhis2.dsr.getDataApprovalAcceptanceUrl( dataSetReport );

    $.ajax( {
        url: url,
        type: "delete",
        success: function() {
            $( "#approvalNotification" ).show().html( i18n_approved );
            $( "#approvalDiv" ).hide();
            $( "#unacceptButton" ).hide();
            if ( dhis2.dsr.permissions.mayUnapprove ) {
                $( "#approvalDiv" ).show();
                $( "#unapproveButton" ).show();
            }
            if ( dhis2.dsr.permissions.mayAccept ) {
                $( "#approvalDiv" ).show();
                $( "#acceptButton" ).show();
            }
        },
        error: function( xhr, status, error ) {
            alert( xhr.responseText );
        }
    } );
}

//------------------------------------------------------------------------------
// Share
//------------------------------------------------------------------------------

function shareInterpretation()
{
	var dataSetReport = getDataSetReport();
    var text = $( "#interpretationArea" ).val();
    
    if ( text.length && $.trim( text ).length )
    {
    	text = $.trim( text );
    	
	    var url = "../api/interpretations/dataSetReport/" + $( "#currentDataSetId" ).val() +
	    	"?pe=" + dataSetReport.pe +
	    	"&ou=" + dataSetReport.ou;
	    	    
	    $.ajax( url, {
	    	type: "POST",
	    	contentType: "text/html",
	    	data: text,
	    	success: function() {	    		
	    		$( "#interpretationArea" ).val( "" );
	    		setHeaderDelayMessage( i18n_interpretation_was_shared );
	    	}    	
	    } );
    }
}

//------------------------------------------------------------------------------
// Hooks in custom forms - must be present to avoid errors in forms
//------------------------------------------------------------------------------

function onValueSave( fn )
{
	// Do nothing
}

function onFormLoad( fn )
{
	// Do nothing
}
