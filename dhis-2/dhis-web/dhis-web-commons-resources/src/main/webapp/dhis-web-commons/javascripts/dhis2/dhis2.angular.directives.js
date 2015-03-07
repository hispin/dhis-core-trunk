'use strict';

/* Directives */

var d2Directives = angular.module('d2Directives', [])


.directive('d2OuSearch', function() {
    
    return {
        restrict: 'E',
        template: '<div style="margin-top:20px">\n\
                    <img id="searchIcon" src="../images/search.png" style="cursor: pointer" title="{{ \'locate_organisation_unit_by_name\' | translate}}">\n\
                    <span id="searchSpan" style="width:100%;display:none;">\n\
                        <input type="text" id="searchField" name="key"/>\n\
                        <input type="button" value="{{\'find\' | translate}}" onclick="selection.findByName()"/>\n\
                    </span>\n\
                  </div>',
        link: function (scope, element, attrs) {
            
            $("#searchIcon").click(function() {
                $("#searchSpan").toggle();
                $("#searchField").focus();
            });

            $("#searchField").autocomplete({
                source: "../dhis-web-commons/ouwt/getOrganisationUnitsByName.action",
                select: function(event, ui) {
                    $("#searchField").val(ui.item.value);
                    selection.findByName();
                }
            });
        }
    };
})

.directive('inputValidator', function() {
    
    return {
        require: 'ngModel',
        link: function (scope, element, attrs, ctrl) {  

            ctrl.$parsers.push(function (value) {
                return parseFloat(value || '');
            });
        }
    };
})

.directive('selectedOrgUnit', function($timeout, storage) {        

    return {        
        restrict: 'A',        
        link: function(scope, element, attrs){
            
            //once ou tree is loaded, start meta-data download
            $(function() {
                dhis2.ou.store.open().done( function() {
                    selection.load();
                    $( "#orgUnitTree" ).one( "ouwtLoaded", function(event, ids, names) {
                        console.log('Finished loading orgunit tree');
                        
                        //Disable ou selection until meta-data has downloaded
                        $( "#orgUnitTree" ).addClass( "disable-clicks" );
                        
                        $timeout(function() {
                            scope.treeLoaded = true;
                            scope.$apply();
                        });
                        
                        downloadMetaData();
                    });
                });
            });
            
            //listen to user selection, and inform angular         
            selection.setListenerFunction( setSelectedOu, true );
            
            function setSelectedOu( ids, names ) {
                var ou = {id: ids[0], name: names[0]};
                $timeout(function() {
                    scope.selectedOrgUnit = ou;
                    scope.$apply();
                });
            }
        }  
    };
})

.directive('blurOrChange', function() {
    
    return function( scope, elem, attrs) {
        elem.calendarsPicker({
            onSelect: function() {
                scope.$apply(attrs.blurOrChange);
                $(this).change();                                        
            }
        }).change(function() {
            scope.$apply(attrs.blurOrChange);
        });
    };
})

.directive('d2Enter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.d2Enter);
                });
                event.preventDefault();
            }
        });
    };
})

.directive('d2NumberValidation', function(ErrorMessageService, $translate) {
    
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, element, attrs, ctrl) {
            
            function checkValidity(numberType, value){
                var isValid = false;
                switch(numberType){
                    case "number":
                        isValid = dhis2.validation.isNumber(value);
                        break;
                    case "posInt":
                        isValid = dhis2.validation.isPositiveInt(value);
                        break;
                    case "negInt":
                        isValid = dhis2.validation.isNegativeInt(value);
                        break;
                    case "zeroPositiveInt":
                        isValid = dhis2.validation.isZeroOrPositiveInt(value);
                        break;
                    case "int":
                        isValid = dhis2.validation.isInt(value);
                        break;
                    default:
                        isValid = true;
                }
                return isValid;
            }
            
            var errorMessages = ErrorMessageService.getErrorMessages();
            var fieldName = attrs.inputFieldId;
            var numberType = attrs.numberType;
            var isRequired = attrs.ngRequired === 'true';
            var msg = $translate(numberType)+ ' ' + $translate('required');
           
            ctrl.$parsers.unshift(function(value) {
            	if(value){
                    var isValid = checkValidity(numberType, value);                    
                    if(!isValid){
                        errorMessages[fieldName] = $translate('value_must_be_' + numberType);
                    }
                    else{
                        if(isRequired){
                            errorMessages[fieldName] = msg;
                        }
                        else{
                            errorMessages[fieldName] = "";
                        }
                    }
                    
                    ErrorMessageService.setErrorMessages(errorMessages);
                	ctrl.$setValidity(fieldName, isValid);
                    return value;
                }
                
                if(value === ''){
                    if(isRequired){
                        errorMessages[fieldName] = msg;
                    }
                    else{
                        ctrl.$setValidity(fieldName, true);
                        errorMessages[fieldName] = "";
                    }
                    
                    ErrorMessageService.setErrorMessages(errorMessages);
                    return undefined;
                }              
            });
           
            ctrl.$formatters.unshift(function(value) {                
                if(value){
                    var isValid = checkValidity(numberType, value);
                    ctrl.$setValidity(fieldName, isValid);
                    return value;
                }
            });
        }
    };
})

.directive('typeaheadOpenOnFocus', function () {
  	
  	return {
        require: ['typeahead', 'ngModel'],
        link: function (scope, element, attr, ctrls) {
            element.bind('focus', function () {
                ctrls[0].getMatchesAsync(ctrls[1].$viewValue);                
                scope.$watch(attr.ngModel, function(value) {
                    if(value === '' || angular.isUndefined(value)){
                        ctrls[0].getMatchesAsync(ctrls[1].$viewValue);
                    }                
                });
            });
        }
    };
})

.directive('d2TypeaheadValidation', function() {
    
    return {
        require: ['typeahead', 'ngModel'],
        restrict: 'A',
        link: function (scope, element, attrs, ctrls) {
            element.bind('blur', function () {                
                if(ctrls[1].$viewValue && !ctrls[1].$modelValue && ctrls[0].active === -1){
                    ctrls[1].$setViewValue();
                    ctrls[1].$render();
                }                
            });
        }
    };
})

.directive('d2PopOver', function($compile, $templateCache){
    
    return {        
        restrict: 'EA',
        link: function(scope, element, attrs){
            var content = $templateCache.get("popover.html");
            content = $compile(content)(scope);
            var options = {
                    content: content,
                    placement: 'bottom',
                    trigger: 'hover',
                    html: true,
                    title: scope.title               
                };            
            $(element).popover(options);
        },
        scope: {
            content: '=',
            title: '@details',
            template: "@template"
        }
    };
})

.directive('d2Sortable', function($timeout) {        

    return {        
        restrict: 'A',        
        link: function(scope, element, attrs){
            element.sortable({
                connectWith: ".connectedSortable",
                placeholder: "ui-state-highlight",
                tolerance: "pointer",
                handle: '.handle',
                change: function(event, ui){
                    $timeout(function() {
                        scope.widgetsOrder = getSortedItems(ui);
                        scope.$apply();
                    });
                    
                },
                receive: function(event, ui){
                    $timeout(function() {
                        scope.widgetsOrder = getSortedItems(ui);
                        scope.$apply();
                    });
                }
            });
            
            var getSortedItems = function(ui){
                var biggerWidgets = $("#biggerWidget").sortable( "toArray");
                var smallerWidgets = $("#smallerWidget").sortable( "toArray");
                var movedIsIdentifeid = false;

                //look for the moved item in the bigger block
                for(var i=0; i<biggerWidgets.length && !movedIsIdentifeid; i++){
                    if(biggerWidgets[i] === ""){
                        biggerWidgets[i] = ui.item[0].id;
                        movedIsIdentifeid = true;
                    }
                }

                //look for the moved item in the smaller block
                for(var i=0; i<smallerWidgets.length && !movedIsIdentifeid; i++){
                    if(smallerWidgets[i] === ""){
                        smallerWidgets[i] = ui.item[0].id;
                        movedIsIdentifeid = true;
                    }
                }
                return {smallerWidgets: smallerWidgets, biggerWidgets: biggerWidgets};
            };
        }  
    };
})

.directive('serversidePaginator', function factory() {
    
    return {
        restrict: 'E',
        controller: function ($scope, Paginator) {
            $scope.paginator = Paginator;
        },
        templateUrl: '../dhis-web-commons/paging/serverside-pagination.html'
    };
})

.directive('draggableModal', function(){
    
    return {
      	restrict: 'EA',
      	link: function(scope, element) {
        	element.draggable();
      	}
    };  
})

.directive('d2GoogleMap', function ($parse, $http, $translate, CurrentSelection) {
    return {
        restrict: 'E',
        replace: true,
        template: '<div></div>',
        link: function(scope, element, attrs){

            var ouLevels = CurrentSelection.getOuLevels();
            
            //remove angular bootstrap ui modal draggable
            $(".modal-content").draggable({ disabled: true });            
            
            //get a default center
            var latCenter = 12.31, lngCenter = 51.48;            
            
            //if there is any marker already - use it as center
            if(angular.isObject(scope.location)){
                if(scope.location.lat && scope.location.lng){
                    latCenter = scope.location.lat;
                    lngCenter = scope.location.lng;
                }                
            }
            
            //default map configurations 
            var mapOptions = {
                zoom: 4,
                center: new google.maps.LatLng(latCenter, lngCenter),
                mapTypeId: google.maps.MapTypeId.ROADMAP
            },
                featureStyle = {
                strokeWeight: 2,
                strokeOpacity: 0.4,
                fillOpacity: 0.4,
                fillColor: 'green'
            };            
            
            var map = new google.maps.Map(document.getElementById(attrs.id), mapOptions);
            
            var marker = new google.maps.Marker({
                map: map
            });
            
            if(angular.isObject(scope.location)){
                if(scope.location.lat && scope.location.lng){                    
                    addMarker({lat: scope.location.lat, lng: scope.location.lng});                    
                }                
            }
            
            var currentLayer = 0, currentGeojson, currentGeojsonFeatures;
            
            var contextMenuOptions={};
            contextMenuOptions.classNames={menu:'map_context_menu', menuSeparator:'map_context_menu_separator'};

            //create an array of MapContextMenuItem objects            
            var menuItems=[];            
            menuItems.push({className: 'map_context_menu_item', eventName: 'zoom_in', id: 'zoomIn', label: '<i class="fa fa-search-plus"></i>   ' + $translate('zoom_in')});
            menuItems.push({className: 'map_context_menu_item', eventName: 'zoom_out', id: 'zoomOut', label: '<i class="fa fa-search-minus"></i>   ' + $translate('zoom_out')});
            menuItems.push({}); 
            menuItems.push({className: 'map_context_menu_item', eventName: 'centerMap', label: '<i class="fa fa-crosshairs"></i>   ' + $translate('center_map')});
            menuItems.push({});
            menuItems.push({className: 'map_context_menu_item', eventName: 'captureCoordinate', label: '<i class="fa fa-map-marker"></i>   ' + $translate('capture_coordinate')});
            contextMenuOptions.menuItems=menuItems;
            var mapContextMenu = new MapContextMenu(map, contextMenuOptions);   
            
            function getGeoJsonByOuLevel(initialize, event, mode){
                var url = '';
                if(initialize){                        
                    currentLayer = 0;
                    url = '../api/organisationUnits.geojson?level=' + ouLevels[currentLayer].level;
                }
                else{
                    if(mode === 'IN'){
                        currentLayer++;
                        url = '../api/organisationUnits.geojson?level=' + ouLevels[currentLayer].level + '&parent=' + event.feature.D;
                    }
                    if(mode === 'OUT'){
                        currentLayer--;
                        var parents = event.feature.k.parentGraph.substring(1,event.feature.k.parentGraph.length-1).split('/');                        
                        url = '../api/organisationUnits.geojson?level=' + ouLevels[currentLayer].level + '&parent=' + parents[parents.length-2];
                    }                    
                }
                
                $http.get( url ).then(function(response){                            
                    currentGeojson = response.data;
                    currentGeojsonFeatures = map.data.addGeoJson(currentGeojson);
                    
                    if(initialize){
                        google.maps.event.addListenerOnce(map, 'idle', function(){
                            google.maps.event.trigger(map, 'resize');
                            map.data.setStyle(featureStyle);
                            centerMap();
                        });
                    }
                    else{
                        centerMap();
                    }                    
                });
            }
            
            function addMarker(loc){
                var latLng = new google.maps.LatLng(loc.lat, loc.lng);
                marker.setPosition(latLng);
            }
            
            function applyMarker(event){
                scope.$apply(function(){
                    addMarker({
                       lat: event.latLng.lat(),
                       lng: event.latLng.lng()
                    });
                    $parse(attrs.location).assign(scope.$parent, {lat: event.latLng.lat(), lng: event.latLng.lng()});                    
                });
            }
            
            function centerMap(){                
                if(currentGeojson && currentGeojson.features){
                    var latLngBounds = getMapCenter(currentGeojson);
                    map.fitBounds(latLngBounds);
                    map.panToBounds(latLngBounds);                    
                }              
            }

            var overLays = [];            
            function getMapCenter(geoJson){
                map.data.setStyle(featureStyle);
                if(!geoJson || !geoJson.features){
                    return;
                }               

                var latLngBounds = new google.maps.LatLngBounds();
                overLays = [];
                angular.forEach(geoJson.features, function(feature){                    
                    var customTxt = '<div>' + feature.properties.name + '</div>';
                    if(feature.geometry.type === 'MultiPolygon'){
                        var polygonPoints = new google.maps.LatLngBounds();
                        angular.forEach(feature.geometry.coordinates[0][0], function(coordinate){
                            latLngBounds.extend(new google.maps.LatLng(coordinate[1],coordinate[0]));
                            polygonPoints.extend(new google.maps.LatLng(coordinate[1],coordinate[0]));                            
                        });
                        var txt = new Dhis2TextOverlay(polygonPoints.getCenter(), customTxt, "polygon-name", map);
                        overLays.push(txt);
                    }
                    else if(feature.geometry.type === 'Point'){
                        latLngBounds.extend(new google.maps.LatLng(feature.geometry.coordinates[1],feature.geometry.coordinates[0]));                        
                        var txt = new Dhis2TextOverlay(new google.maps.LatLng(feature.geometry.coordinates[1],feature.geometry.coordinates[0]),customTxt, "polygon-name",map );
                        overLays.push(txt);
                    }
                });

                return latLngBounds;                
            }            
            
            function initializeMap(){                
                getGeoJsonByOuLevel(true, null);
            }
            
            function zoomMap(event, mode){
                
                for(var i = 0; i < currentGeojsonFeatures.length; i++){
                    map.data.remove(currentGeojsonFeatures[i]);
                }
                
                for(var i=0; i<overLays.length; i++){
                    overLays[i].setMap(null);
                }
                
                getGeoJsonByOuLevel(false, event, mode);
            }
            
            function enableDisableZoom(){
                if(currentLayer >= ouLevels.length-1){                    
                    $("#zoomIn").addClass('disabled-context-menu-item');
                    $("#zoomIn").removeClass('enable-context-menu-item');
                    $('#zoomIn').attr('disabled', "disabled");
                }else{
                    $("#zoomIn").removeClass('disabled-context-menu-item');
                    $("#zoomIn").addClass('enable-context-menu-item');
                    $('#zoomIn').attr('disabled', "");
                }
                if(currentLayer === 0){
                    $("#zoomOut").addClass('disabled-context-menu-item');
                    $("#zoomOut").removeClass('enable-context-menu-item');
                    $('#zoomOut').attr('disabled', "disabled");
                }
                else{
                    $("#zoomOut").removeClass('disabled-context-menu-item');
                    $("#zoomOut").addClass('enable-context-menu-item');
                    $('#zoomIn').attr('disabled', "");
                }
            }
            
            //get lable for current polygon
            map.data.addListener('mouseover', function(e) {
                $("#polygon-label").text( e.feature.k.name );
                map.data.revertStyle();
                map.data.overrideStyle(e.feature, {fillOpacity: 0.8});
            });
            
            //remove polygon label
            map.data.addListener('mouseout', function() {
                $("#polygon-label").text( '' );
                map.data.revertStyle();
            });
            
            //drill-down based on polygons assigned to orgunits
            map.data.addListener('rightclick', function(e){
                mapContextMenu.show(e);
                enableDisableZoom();
            });
            
            //drill-down based on points assigned to orgunits
            google.maps.event.addListener(marker, 'rightclick', function(e){
                mapContextMenu.show(e);
                enableDisableZoom();
            });
            
            //capturing coordinate from anywhere in the map - incase no polygons are defined
            google.maps.event.addListener(map, 'rightclick', function(e){                
                mapContextMenu.show(e);
            });            
            
            //remove context menu onclick
            map.data.addListener('click', function(e) {                
                mapContextMenu.hide();
            });
            
            google.maps.event.addListener(map, 'click', function(e){                
                mapContextMenu.hide();
            }); 
            
            //listen for the clicks on mapContextMenu
            google.maps.event.addListener(mapContextMenu, 'menu_item_selected', function(e, latLng, eventName){              
                switch(eventName){                    
                    case 'zoom_in':
                        if(e.feature){
                            zoomMap(e, 'IN');
                        }
                        else{
                            map.setZoom(map.getZoom()+1);
                        }
                        
                        break;                    
                    case 'zoom_out':
                        if(e.feature){
                            zoomMap(e, 'OUT');
                        }
                        else{
                            map.setZoom(map.getZoom()-1);
                        }
                        
                        break;
                    case 'centerMap':
                        map.panTo(latLng);
                        break;
                    case 'captureCoordinate':
                        applyMarker(e);
                        break;
                }
            });
    
            initializeMap();
        }
    };
})
.directive('d2CustomForm', function($compile) {
    return{ 
        restrict: 'E',
        link: function(scope, elm, attrs){
            scope.$watch('customForm', function(){
                if(angular.isObject(scope.customForm)){
                    elm.html(scope.customForm.htmlCode);
                    $compile(elm.contents())(scope);
                }                
            });
        }
    };
})

.directive('d2ContextMenu', function(ContextMenuSelectedItem) {
        
    return {        
        restrict: 'A',
        link: function(scope, element, attrs){
            var contextMenu = $("#contextMenu");                   
            
            element.click(function (e) {
                var selectedItem = $.parseJSON(attrs.selectedItem);
                ContextMenuSelectedItem.setSelectedItem(selectedItem);
                
                var menuHeight = contextMenu.height();
                var menuWidth = contextMenu.width();
                var winHeight = $(window).height();
                var winWidth = $(window).width();

                var pageX = e.pageX;
                var pageY = e.pageY;

                contextMenu.show();

                if( (menuWidth + pageX) > winWidth ) {
                  pageX -= menuWidth;
                }

                if( (menuHeight + pageY) > winHeight ) {
                  pageY -= menuHeight;

                  if( pageY < 0 ) {
                      pageY = e.pageY;
                  }
                }
                
                contextMenu.css({
                    left: pageX,
                    top: pageY
                });

                return false;
            });
            
            contextMenu.on("click", "a", function () {                    
                contextMenu.hide();
            });

            $(document).click(function () {                                        
                contextMenu.hide();
            });
        }     
    };
})

.directive('d2Date', function(DateUtils, CalendarService, ErrorMessageService, $translate, $parse) {
    return {
        restrict: 'A',
        require: 'ngModel',        
        link: function(scope, element, attrs, ctrl) {    
            
            var errorMessages = ErrorMessageService.getErrorMessages();
            var fieldName = attrs.inputFieldId;
            var isRequired = attrs.ngRequired === 'true';
            var calendarSetting = CalendarService.getSetting();            
            var dateFormat = 'yyyy-mm-dd';
            if(calendarSetting.keyDateFormat === 'dd-MM-yyyy'){
                dateFormat = 'dd-mm-yyyy';
            }            
            
            var minDate = $parse(attrs.minDate)(scope), 
                maxDate = $parse(attrs.maxDate)(scope),
                calendar = $.calendars.instance(calendarSetting.keyCalendar);
            
            element.calendarsPicker({
                changeMonth: true,
                dateFormat: dateFormat,
                yearRange: '-120:+30',
                minDate: minDate,
                maxDate: maxDate,
                calendar: calendar,
                duration: "fast",
                showAnim: "",
                renderer: $.calendars.picker.themeRollerRenderer,
                onSelect: function(date) {
                    $(this).change();
                }
            })
            .change(function() {                
                if(this.value){                    
                    var rawDate = this.value;
                    var convertedDate = DateUtils.format(this.value);

                    var isValid = rawDate == convertedDate;
                    
                    if(!isValid){
                        errorMessages[fieldName] = $translate('date_required');
                    }
                    else{
                        if(isRequired){
                            errorMessages[fieldName] = $translate('required');
                        }
                        else{
                            errorMessages[fieldName] = "";
                        }
                        if(maxDate === 0){                    
                            isValid = !moment(convertedDate, calendarSetting.momentFormat).isAfter(DateUtils.getToday());
                            if(!isValid){
                                errorMessages[fieldName] = $translate('future_date_not_allowed');                            
                            }                           
                        }
                    }                                        
                    ctrl.$setViewValue(this.value);
                    ctrl.$setValidity(fieldName, isValid);
                }
                else{
                    if(!isRequired){
                        ctrl.$setViewValue(this.value);
                        ctrl.$setValidity(fieldName, !isRequired);
                        errorMessages[fieldName] = "";
                    }
                    else{
                        errorMessages[fieldName] = $translate('required');                        
                    }
                }
                
                ErrorMessageService.setErrorMessages(errorMessages);
                this.focus();
                scope.$apply();
            });    
        }      
    };   
});