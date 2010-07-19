
// -----------------------------------------------------------------------------
// View details
// -----------------------------------------------------------------------------

function showDataElementGroupDetails( dataElementGroupId )
{
    var request = new Request();
    request.setResponseTypeXML( 'dataElementGroup' );
    request.setCallbackSuccess( dataElementGroupReceived );
    request.send( 'getDataElementGroup.action?id=' + dataElementGroupId );
}

function dataElementGroupReceived( dataElementGroupElement )
{
    setFieldValue( 'nameField', getElementValue( dataElementGroupElement, 'name' ) );
    setFieldValue( 'memberCountField', getElementValue( dataElementGroupElement, 'memberCount' ) );

    showDetails();
}

// -----------------------------------------------------------------------------
// Remove data element group
// -----------------------------------------------------------------------------

function removeDataElementGroup( dataElementGroupId, dataElementGroupName )
{
    removeItem( dataElementGroupId, dataElementGroupName, i18n_confirm_delete, "removeDataElementGroup.action" );
}

// -----------------------------------------------------------------------------
// Add data element group
// -----------------------------------------------------------------------------

function validateAddDataElementGroup()
{
    var request = new Request();
    request.setResponseTypeXML( 'message' );
    request.setCallbackSuccess( addValidationCompleted );
    
    var params = 'name=' + getFieldValue( 'name' );

    request.sendAsPost( params );
    request.send( "validateDataElementGroup.action" );
    return false;
}

function addValidationCompleted( messageElement )
{
    var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;

    if ( type == 'success' )
    {
        var availableDataElements = document.getElementById( 'availableDataElements' );
        availableDataElements.selectedIndex = -1;
        
        selectAllById( 'groupMembers' );
        
        document.getElementById( 'addDataElementGroupForm' ).submit();
    }
    else if ( type == 'error' )
    {
        window.alert( i18n_adding_data_element_group_failed + ':' + '\n' + message );
    }
    else if ( type == 'input' )
    {
        setMessage( message );
    }
}

// -----------------------------------------------------------------------------
// Update data element group
// -----------------------------------------------------------------------------

function validateUpdateDataElementGroup()
{
    var request = new Request();
    request.setResponseTypeXML( 'message' );
    request.setCallbackSuccess( updateValidationCompleted );
    
    var params = 'id=' + getFieldValue( 'id' ) +
        '&name=' + getFieldValue( 'name' );

    request.sendAsPost( params );
    request.send( "validateDataElementGroup.action" );
    return false;
}

function updateValidationCompleted( messageElement )
{
    var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;
    
    if ( type == 'success' )
    {
        var availableDataElements = document.getElementById( 'availableDataElements' );
        availableDataElements.selectedIndex = -1;
        
        selectAllById( 'groupMembers' );

        document.getElementById( 'updateDataElementGroupForm' ).submit();
    }
    else if ( type == 'error' )
    {
        window.alert( i18n_saving_data_element_group_failed + ':' + '\n' + message );
    }
    else if ( type == 'input' )
    {
        setMessage( message );
    }
}
