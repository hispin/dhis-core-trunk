
$( document ).ready( function()
{        
  $( "li.menuDropDownItem" ).mouseover( function() // Over dropdown item
  {
    $( this ).css( "background-color", "#4f85bb" );
    $( this ).css( "border", "1px solid #e5e5e5" );
  });
  
  $( "li.menuDropDownItem" ).mouseout( function() // Out dropdown item
  {
    $( this ).css( "background-color", "#2a5a8a" );
    $( this ).css( "border", "1px solid #2a5a8a" );
  });
  
  $( "li.introItem" ).mouseover( function() // Over intro item
  {
    $( this ).css( "background-color", "#a4d2a3" );
    $( this ).css( "border", "1px solid #ffffff" );
  });
  
  $( "li.introItem" ).mouseout( function() // Out intro item
  {
    $( this ).css( "background-color", "#d5efd5" );
    $( this ).css( "border", "1px solid #d5efd5" );
  });
});

// Called from main/Leftbar
function setMainPageNormal()
{
	document.getElementById( 'mainPage' ).style.marginLeft = '270px';
}