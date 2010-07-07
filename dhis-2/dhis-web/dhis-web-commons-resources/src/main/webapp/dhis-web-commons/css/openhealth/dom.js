
$( document ).ready( function()
{        
  $( "li.menuDropDownItem" ).mouseover( function() // Over dropdown item
  {
    $( this ).css( "background-color", "#588db5" );
    $( this ).css( "color", "#ffffff" );
    $( this ).css( "border", "1px solid #f0f0f0" );
  });
  
  $( "li.menuDropDownItem" ).mouseout( function() // Out dropdown item
  {
    $( this ).css( "background-color", "#d6e6f2" );
    $( this ).css( "color", "#407298" );
    $( this ).css( "border", "1px solid #d6e6f2" );
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
	document.getElementById( 'mainPage' ).style.marginLeft = '300px';
}
