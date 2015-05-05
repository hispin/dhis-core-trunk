/* global trackerCapture, angular */

trackerCapture.controller('PaymentAdviseController',
        function($scope,
                $filter,
                DateUtils,
                TEIGridService,
                AttributesFactory,
                ProgramFactory,
                ProgramStageFactory,
                CurrentSelection,
                OptionSetService,
                EventReportService) {
                    
    $scope.today = DateUtils.getToday();
    
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];         
    $scope.selectedOuMode = $scope.ouModes[0];
    $scope.report = {};
    
    $scope.optionSets = CurrentSelection.getOptionSets();
    if(!$scope.optionSets){
        $scope.optionSets = [];
        OptionSetService.getAll().then(function(optionSets){
            angular.forEach(optionSets, function(optionSet){                        
                $scope.optionSets[optionSet.id] = optionSet;
            });

            CurrentSelection.setOptionSets($scope.optionSets);
        });
    }
    
    //watch for selection of org unit from tree
    $scope.$watch('selectedOrgUnit', function() {      
        $scope.selectedProgram = null;
        $scope.reportStarted = false;
        $scope.dataReady = false;  
        $scope.programStages = [];
        $scope.stagesById = [];
        if( angular.isObject($scope.selectedOrgUnit)){            
            $scope.loadPrograms($scope.selectedOrgUnit);
        }
    });
    
    //load programs associated with the selected org unit.
    $scope.loadPrograms = function(orgUnit) {        
        $scope.selectedOrgUnit = orgUnit;        
        if (angular.isObject($scope.selectedOrgUnit)){
            ProgramFactory.getProgramsByOu($scope.selectedOrgUnit, $scope.selectedProgram, 'ASHA').then(function(response){
                $scope.programs = response.programs;
                $scope.selectedProgram = response.selectedProgram;
            });
        }
    };
    
    $scope.$watch('selectedProgram', function() {        
        $scope.programStages = [];
        $scope.stagesById = [];
        
        if( angular.isObject($scope.selectedProgram)){            
            $scope.reportStarted = false;
            $scope.dataReady = false;            
            ProgramStageFactory.getByProgram($scope.selectedProgram).then(function(stages){
                $scope.stagesById = [];
                angular.forEach(stages, function(stage){
                    if(stage.BeneficiaryRegistration || stage.ActivityRegistration){
                        $scope.stagesById[stage.id] = stage;
                        $scope.programStages.push(stage);
                    }                    
                });
            });
        }
    });
    
    $scope.generateReport = function(program, report, ouMode){
        
        $scope.selectedProgram = program;
        $scope.report = report;
        $scope.selectedOuMode = ouMode;
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid || !$scope.selectedProgram){
            return false;
        }
        
        $scope.reportStarted = true;
        $scope.reportFinished = false;
            
        AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){            
            var grid = TEIGridService.generateGridColumns(atts, $scope.selectedOuMode.name);   
            $scope.gridColumns = grid.columns;
        });
        
        //fetch TEIs for the selected program and orgunit/mode
        /* params
         * orgUnit, ouMode, program, startDate, endDate, programStatus, 
         * eventStatus, dataElement, dataValue, paging, pager*/
        EventReportService.getEventReport($scope.selectedOrgUnit.id, 
                                            $scope.selectedOuMode.name, 
                                            $scope.selectedProgram.id, 
                                            DateUtils.formatFromUserToApi(report.startDate), 
                                            DateUtils.formatFromUserToApi(report.endDate), 
                                            'ACTIVE',
                                            null,
                                            null,
                                            null,
                                            false,
                                            null).then(function(data){                     
            
            $scope.paymentList = [];
            $scope.teiList = [];
            angular.forEach(data.eventRows, function(row){
                
                if(row.trackedEntityInstance && row.programStage && $scope.stagesById[row.programStage] && 
                            ($scope.stagesById[row.programStage].ActivityRegistration || $scope.stagesById[row.programStage].BeneficiaryRegistration)){                        

                    var paymentRow = {};  
                    var tei = {};
                    tei.trackedEntityInstance = row.trackedEntityInstance;
                    angular.forEach(row.attributes, function(att){
                        var val = AttributesFactory.formatAttributeValue(att, $scope.attributesById, $scope.optionSets, 'USER');                        
                        tei[att.attribute] = val;                        
                    });
                    
                    if( !$filter('filter')($scope.teiList, {trackedEntityInstance: row.trackedEntityInstance}).length > 0){                        
                        $scope.teiList.push(tei);
                    }
                    
                    paymentRow.trackedEntityInstance = row.trackedEntityInstance;
                    paymentRow.eventDate = DateUtils.formatFromApiToUser(row.eventDate);
                    paymentRow.event = row.event;
                    paymentRow.orgUnitName = row.orgUnitName;
                    paymentRow.programStage = row.programStage;
                    paymentRow.name = $scope.stagesById[row.programStage].name;
                    $scope.paymentList.push(paymentRow);
                    
                }
            });

            $scope.reportFinished = true;
            $scope.reportStarted = false;
        });
        
        /*TEIService.search($scope.selectedOrgUnit.id, 
                            $scope.selectedOuMode.name,
                            null,
                            'program=' + $scope.selectedProgram.id,
                            null,
                            $scope.pager,
                            false).then(function(data){                     
            
            //process tei grid
            var teis = TEIGridService.format(data,true, $scope.optionSets);     
            $scope.teiList = [];
            DHIS2EventFactory.getByOrgUnitAndProgram($scope.selectedOrgUnit.id, 
                                                    $scope.selectedOuMode.name, 
                                                    $scope.selectedProgram.id, 
                                                    DateUtils.formatFromUserToApi(report.startDate), 
                                                    DateUtils.formatFromUserToApi(report.endDate)).then(function(eventList){
                $scope.dhis2Events = [];
                angular.forEach(eventList, function(ev){
                    if(ev.trackedEntityInstance && ev.programStage && $scope.stagesById[ev.programStage] && 
                            ($scope.stagesById[ev.programStage].ActivityRegistration || $scope.stagesById[ev.programStage].BeneficiaryRegistration)){                        
                        ev.name = $scope.stagesById[ev.programStage].name;
                        ev.programName = $scope.selectedProgram.name;
                        ev.statusColor = EventUtils.getEventStatusColor(ev); 
                        ev.eventDate = DateUtils.formatFromApiToUser(ev.eventDate);
                        
                        angular.forEach(ev.dataValues, function(dv){
                            ev[dv.dataElement] = dv.value;
                        });
                        
                        if($scope.dhis2Events[ev.trackedEntityInstance]){
                            if(teis.rows[ev.trackedEntityInstance]){
                                $scope.teiList.push(teis.rows[ev.trackedEntityInstance]);
                                delete teis.rows[ev.trackedEntityInstance];
                            }                     
                            $scope.dhis2Events[ev.trackedEntityInstance].push(ev);
                        }
                        else{
                            if(teis.rows[ev.trackedEntityInstance]){
                                $scope.teiList.push(teis.rows[ev.trackedEntityInstance]);
                                delete teis.rows[ev.trackedEntityInstance];
                            }  
                            $scope.dhis2Events[ev.trackedEntityInstance] = [ev];
                        }                        
                        $scope.stagesById[ev.programStage].hasData = true;
                    }
                });
                $scope.reportStarted = false;
                $scope.dataReady = true;
            });
        });*/
    };
    
    function contains(array, obj) {
        var i = array.length;
        while (i--) {
           if (array[i] === obj) {
               return true;
           }
        }
        return false;
    }
});