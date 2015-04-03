/* global trackerCapture, angular */
                    
trackerCapture.controller('BeneficiaryServiceController', 
    function($scope, 
            $modalInstance, 
            DateUtils,
            DHIS2EventFactory,
            ProgramStageFactory,
            EnrollmentService,
            DialogService,
            beneficiaryPrograms,
            commonBenProgram,
            beneficiary,
            attributesById,
            gridColumns,
            ashaDetails,
            ashaPeriod,
            ashaEvent){    
    
    $scope.beneficiaryPrograms = beneficiaryPrograms;
    $scope.commonBeneficiaryProgram = commonBenProgram;
    $scope.beneficiary = beneficiary;
    $scope.attributesById = attributesById;
    $scope.gridColumns = gridColumns;
    $scope.ashaDetails = ashaDetails;
    $scope.ashaPeriod = ashaPeriod;
    $scope.ashaEvent = ashaEvent;
    $scope.beneficiaryProgramsById = [];
    $scope.programStageIds = [];
    $scope.enrollments = [];
    $scope.enrollmentsByProgram = [];
    
    angular.forEach($scope.beneficiaryPrograms, function(pr){
        $scope.beneficiaryProgramsById[pr.id] = pr;
        angular.forEach(pr.programStages, function(st){
            $scope.programStageIds.push(st.id);
        });            
    });
    
    ProgramStageFactory.getAll().then(function(stages){
        $scope.stages = [];
        angular.forEach(stages, function(stage){
            if($scope.programStageIds.indexOf( stage.id ) !== -1){                
                $scope.stages.push(stage);
            }
        });                        
    });
    
    if($scope.beneficiary && $scope.beneficiary.id){
        EnrollmentService.getByEntity($scope.beneficiary.id).then(function(enrollments){
            angular.forEach(enrollments, function(en){
                if($scope.programStageIds.indexOf( en.program ) !== -1){
                    if(en.status === 'ACTIVE'){
                        $scope.enrollmentsByProgram[en.program] = en;
                        $scope.enrollments.push(en);
                    }
                }
            });
        });
    }
    
    $scope.save = function () {
       
        //check for form validity
        /*if($scope.dueDateInvalid || $scope.eventDateInvalid){
            return false;
        }
        
        if($scope.selectedStage.periodType){
            $scope.dhis2Event.eventDate = $scope.dhis2Event.selectedPeriod.endDate;
            $scope.dhis2Event.dueDate = $scope.dhis2Event.selectedPeriod.endDate;
        }        
        
        var eventDate = DateUtils.formatFromUserToApi($scope.dhis2Event.eventDate);
        var dueDate = DateUtils.formatFromUserToApi($scope.dhis2Event.dueDate);
        var newEvents = {events: []};
        var newEvent = {
                trackedEntityInstance: dummyEvent.trackedEntityInstance,
                program: dummyEvent.program,
                programStage: dummyEvent.programStage,
                enrollment: dummyEvent.enrollment,
                orgUnit: dummyEvent.orgUnit,                        
                dueDate: dueDate,
                eventDate: eventDate,
                notes: [],
                dataValues: [],
                status: 'ACTIVE'
            };            
        newEvents.events.push(newEvent);
        DHIS2EventFactory.create(newEvents).then(function(data){
            if (data.importSummaries[0].status === 'ERROR') {
                var dialogOptions = {
                    headerText: 'event_creation_error',
                    bodyText: data.importSummaries[0].description
                };

                DialogService.showDialog({}, dialogOptions);
            }
            else {
                newEvent.event = data.importSummaries[0].reference;                
                $modalInstance.close(newEvent);
            }
        });*/
        
    };
    
    $scope.cancel = function(){
        $modalInstance.close();
    };
});


