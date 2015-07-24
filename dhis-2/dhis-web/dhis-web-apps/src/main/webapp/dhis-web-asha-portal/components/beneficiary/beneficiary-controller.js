/* global trackerCapture, angular */

trackerCapture.controller('BeneficiaryController',
        function($scope,
                $translate,
                $modal,
                orderByFilter,
                ProgramFactory,
                ProgramStageFactory,
                AttributesFactory,
                EntityQueryFactory,
                DHIS2EventFactory,
                RegistrationService,
                OptionSetService,
                SessionStorageService,
                EnrollmentService,
                EventReportService,
                TEIService,
                TEIGridService,
                DateUtils,
                ModalService,
                DialogService,
                Paginator,
                AshaPortalUtils,
                CurrentSelection) {
    
    $scope.approvalAuthorityLevel = AshaPortalUtils.getApprovalAuthorityLevel();
    
    $scope.selectedService = {};
    
    $scope.showAddNewServiceDiv = false;
    $scope.showRegistrationDiv = false;
    
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];
    $scope.selectedOuMode = $scope.ouModes[0];
    $scope.searchMode = { listAll: 'LIST_ALL', freeText: 'FREE_TEXT', attributeBased: 'ATTRIBUTE_BASED' };
    $scope.searchText = {value: null};    
    
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    $scope.selectedEnrollment = {dateOfEnrollment: DateUtils.getToday(), dateOfIncident: DateUtils.getToday()};
    
    //Paging
    $scope.pager = {pageSize: 50, page: 1, toolBarDisplay: 5};   
    
    $scope.attributesById = CurrentSelection.getAttributesById();
    if(!$scope.attributesById){
        $scope.attributesById = [];
        AttributesFactory.getAll().then(function(atts){
            angular.forEach(atts, function(att){
                att.allowDataEntry = true;
                $scope.attributesById[att.id] = att;
            });
            
            CurrentSelection.setAttributesById($scope.attributesById);
        });
    }    
    
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
    
    function resetFields(){
        $scope.teiFetched = false;    
        $scope.emptySearchText = false;
        $scope.emptySearchAttribute = false;
        $scope.showSearchDiv = false;
        $scope.showRegistrationDiv = false;  
        $scope.showTrackedEntityDiv = false;
        $scope.trackedEntityList = null; 
        $scope.teiCount = null;

        $scope.queryUrl = null;
        $scope.programUrl = null;
        $scope.attributeUrl = {url: null, hasValue: false};
    };
    
    function getOwnerDetails(){

        $scope.newBen = {};
        $scope.ben = {};
        
        var benOwners = CurrentSelection.getBenOrActOwners();
        $scope.orgUnitName = benOwners.orgUnitName;
        $scope.ashaDetails = benOwners.asha;
        $scope.ashaPeriod = benOwners.period;
        $scope.ashaEvent = benOwners.ashaEvent;
        $scope.paymentRate = benOwners.paymentRate;
        
        ProgramFactory.getBeneficairyPrograms().then(function(response){
            $scope.beneficiaryProgramsById = [];
            $scope.programStageIds = [];
            $scope.stagesById = [];
            $scope.enrollments = [];
            $scope.enrollmentsByProgram = [];

            if(response && response.beneficiaryPrograms && response.commonBenProgram){
                
                $scope.beneficiaryPrograms = response.beneficiaryPrograms;
                $scope.commonBeneficiaryProgram = response.commonBenProgram;
                
                $scope.beneficiaryPrograms = orderByFilter($scope.beneficiaryPrograms, '-id');

                angular.forEach($scope.beneficiaryPrograms, function(pr){
                    $scope.beneficiaryProgramsById[pr.id] = pr;
                    angular.forEach(pr.programStages, function(st){
                        $scope.programStageIds.push(st.id);
                    });            
                });

                if($scope.commonBeneficiaryProgram){
                    AttributesFactory.getByProgram($scope.commonBeneficiaryProgram).then(function(atts){
                        $scope.attributes = atts;
                        var grid = TEIGridService.generateGridColumns($scope.attributes, $scope.selectedOuMode.name);
                        $scope.gridColumns = grid.columns;
                        $scope.serviceGridColumns = [];

                        angular.forEach($scope.gridColumns, function(col){
                            $scope.serviceGridColumns.push(col);
                        });

                        ProgramStageFactory.getAll().then(function(stages){
                            $scope.stages = [];
                            $scope.dataElementForServiceOwner = null;
                            $scope.dataElementForPaymentSanctioned = null;
                            $scope.dataElementForCurrentApprovalLevel = null;
                            $scope.dataElementForCurrentApprovalStatus = null;
                            angular.forEach(stages, function(stage){
                                if($scope.programStageIds.indexOf( stage.id ) !== -1){                
                                    $scope.stages.push(stage);
                                    $scope.stagesById[stage.id] = stage;
                                }

                                for( var i=0; 
                                     i<stage.programStageDataElements.length && 
                                     !$scope.dataElementForServiceOwner || 
                                     !$scope.dataElementForPaymentSanctioned ||
                                     !$scope.dataElementForCurrentApprovalLevel ||
                                     !$scope.dataElementForCurrentApprovalStatus; 
                                     i++){
                                    if( stage.programStageDataElements[i] && stage.programStageDataElements[i].dataElement ) {

                                        if( stage.programStageDataElements[i].dataElement.PaymentSanctioned ){
                                            $scope.dataElementForPaymentSanctioned = stage.programStageDataElements[i].dataElement;
                                        }                                    
                                        if( stage.programStageDataElements[i].dataElement.ServiceOwner ){
                                            $scope.dataElementForServiceOwner = stage.programStageDataElements[i].dataElement;
                                        }                                    
                                        if( stage.programStageDataElements[i].dataElement.ApprovalLevel ){
                                            $scope.dataElementForCurrentApprovalLevel = stage.programStageDataElements[i].dataElement;
                                        }                                    
                                        if( stage.programStageDataElements[i].dataElement.ApprovalStatus ){
                                            $scope.dataElementForCurrentApprovalStatus = stage.programStageDataElements[i].dataElement;
                                        }
                                    }
                                }
                            });
                            
                            $scope.serviceGridColumns.push({name: $translate('service'), id: 'serviceName', type: 'string', displayInListNoProgram: false, showFilter: false, show: true});
                            $scope.serviceGridColumns.push({name: $translate('service_date'), id: 'eventDate', type: 'date', displayInListNoProgram: false, showFilter: false, show: true});
                            //$scope.serviceGridColumns.push({name: $translate('current_approval_status'), id: $scope.dataElementForCurrentApprovalStatus.id, type: 'string', displayInListNoProgram: false, showFilter: false, show: true});                            
                            
                            if(!$scope.dataElementForServiceOwner || 
                                    !$scope.dataElementForPaymentSanctioned ||
                                    !$scope.dataElementForCurrentApprovalLevel ||
                                    !$scope.dataElementForCurrentApprovalStatus){
                                
                                
                                //invalid db configuration
                                var dialogOptions = {
                                        headerText: 'invalid_db_configuration',
                                        bodyText: $translate('stage_missing_service_owner_config')
                                    };
                                DialogService.showDialog({}, dialogOptions);
                                $scope.enrollmentSuccess = false;
                                return;    
                            }
                            
                            $scope.search($scope.searchMode.listAll);
                            
                            $scope.getServicesProvided();
                            
                        });
                    });
                }
            }
        });
    };
    
    //listen to current ASHA and reporting period
    $scope.$on('beneficiaryRegistration', function(event, args){
        $scope.optionSets = args.optionSets;
        getOwnerDetails();
    });    
    
    $scope.registerBeneficiary = function(destination){        
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            return false;
        }                   
        
        if( !$scope.commonBeneficiaryProgram || !$scope.commonBeneficiaryProgram.id){
            var dialogOptions = {
                headerText: 'invalid_db_configuration',
                bodyText: 'common_beneficiary_program_undefined'
            };

            DialogService.showDialog({}, dialogOptions);
            return false;
        }
        
        //form is valid, continue the registration        
        $scope.newBen.trackedEntity = $scope.ben.trackedEntity = $scope.commonBeneficiaryProgram.trackedEntity.id;
        $scope.newBen.orgUnit = $scope.ben.orgUnit = $scope.selectedOrgUnit.id;
        $scope.newBen.attributes = $scope.ben.attributes = [];
        
        //get tei attributes and their values
        //but there could be a case where attributes are non-mandatory and
        //registration form comes empty, in this case enforce at least one value        
        var result = RegistrationService.processForm($scope.ben, $scope.newBen, $scope.attributesById);
        $scope.formEmpty = result.formEmpty;
        $scope.ben = result.tei;
        
        if($scope.formEmpty){//registration form is empty
            return false;
        }

        RegistrationService.registerOrUpdate($scope.ben, $scope.optionSets, $scope.attributesById).then(function(response){

            if(response.status === 'SUCCESS'){
                
                $scope.ben.trackedEntityInstance = response.reference;
                $scope.newBen.trackedEntityInstance = $scope.newBen.id = response.reference;
                
                var enrollment = {};
                enrollment.trackedEntityInstance = $scope.ben.trackedEntityInstance;
                enrollment.program = $scope.commonBeneficiaryProgram.id;
                enrollment.status = 'ACTIVE';
                enrollment.orgUnit = $scope.selectedOrgUnit.id;
                enrollment.dateOfEnrollment = $scope.selectedEnrollment.dateOfEnrollment;
                enrollment.dateOfIncident = $scope.selectedEnrollment.dateOfIncident === '' ? $scope.selectedEnrollment.dateOfEnrollment : $scope.selectedEnrollment.dateOfIncident;
                enrollment.followup = false;

                EnrollmentService.enroll(enrollment).then(function(data){
                    if(data.status !== 'SUCCESS'){
                        //enrollment has failed
                        var dialogOptions = {
                                headerText: 'enrollment_error',
                                bodyText: data.description
                            };
                        DialogService.showDialog({}, dialogOptions);
                        $scope.enrollmentSuccess = false;
                        return;
                    }
                    
                    $scope.showRegistrationDiv = false;
                    
                    if($scope.trackedEntityList.rows.length){
                        $scope.trackedEntityList.rows.push($scope.newBen);
                    }
                    else{
                        $scope.trackedEntityList.rows = [];
                        $scope.trackedEntityList.rows.push($scope.newBen);
                    }                    
                    
                    if(destination === 'SERVICE'){                        
                        $scope.showAddNewService($scope.newBen);
                    }
                    
                    //reset form
                    $scope.newBen = {};
                    $scope.ben= {};
                    $scope.outerForm.submitted = false;
                });               
            }
            else{//update/registration has failed
                var dialogOptions = {
                        headerText: $scope.ben && $scope.ben.trackedEntityInstance ? 'update_error' : 'registration_error',
                        bodyText: response.description
                    };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
        });
    };
    
    $scope.getServicesProvided = function(){        
        $scope.servicesProvided = [];
        
        if($scope.dataElementForServiceOwner && 
                $scope.dataElementForServiceOwner.id &&
                $scope.dataElementForCurrentApprovalLevel && 
                $scope.dataElementForCurrentApprovalLevel.id){
            
            EventReportService.getEventReport($scope.selectedOrgUnit.id, 
                                                $scope.ouModes[1].name, 
                                                null, 
                                                null, 
                                                null, 
                                                'ACTIVE',
                                                'VISITED', 
                                                $scope.dataElementForServiceOwner.id,
                                                $scope.ashaEvent,
                                                false,
                                                null).then(function(data){               

                angular.forEach(data.eventRows, function(row){
                    var serviceProvided = {};                    
                    angular.forEach(row.attributes, function(att){
                        var val = AttributesFactory.formatAttributeValue(att, $scope.attributesById, $scope.optionSets, 'USER');                        
                        serviceProvided[att.attribute] = val;                        
                    });

                    if($scope.stagesById[row.programStage] && $scope.beneficiaryProgramsById[row.program]){
                        serviceProvided.serviceName = $scope.stagesById[row.programStage].name;
                        serviceProvided.programName = $scope.beneficiaryProgramsById[row.program].name; 
                    }

                    serviceProvided.dueDate = serviceProvided.eventDate = DateUtils.formatFromApiToUser(row.dueDate);
                    serviceProvided.status = 'VISITED';
                    serviceProvided.event = row.event;
                    serviceProvided.enrollment = row.enrollment;
                    serviceProvided.orgUnit = row.orgUnit;
                    serviceProvided.program = row.program;
                    serviceProvided.programStage = row.programStage;
                    serviceProvided.trackedEntityInstance = row.trackedEntityInstance;
                    serviceProvided.notes = row.notes ? row.notes : [];

                    angular.forEach(row.dataValues, function(dv){
                        if(dv.dataElement && dv.value){                            
                            if(dv.dataElement === $scope.dataElementForCurrentApprovalLevel.id){
                                serviceProvided[dv.dataElement] = new Number(dv.value);
                            }
                            else{
                                if(dv.dataElement === $scope.dataElementForCurrentApprovalStatus.id){
                                    serviceProvided.currentApprovalStatus = dv.value;
                                }
                                else{
                                    serviceProvided[dv.dataElement] = dv.value;
                                }                                
                            }
                        }                                            
                    }); 
                    
                    if(serviceProvided.currentApprovalStatus === 'Rejected' && serviceProvided[$scope.dataElementForCurrentApprovalLevel.id] < $scope.approvalAuthorityLevel){                        
                        /*console.log('serviceProvided:  ', serviceProvided);
                        console.log('levels:  ', serviceProvided[$scope.dataElementForCurrentApprovalLevel.id], ' - ', $scope.approvalAuthorityLevel);*/
                    }
                    else{
                        $scope.servicesProvided.push(serviceProvided);
                    }
                });
                
                //sort services provided by their provision dates - this is default
                $scope.servicesProvided = orderByFilter($scope.servicesProvided, '-provisionDate');

            });
        }
        else{
            //invalid db configuration
            var dialogOptions = {
                    headerText: 'invalid_db_configuration',
                    bodyText: $translate('stage_missing_service_owner_config')
                };
            DialogService.showDialog({}, dialogOptions);
            $scope.enrollmentSuccess = false;
            return;
        }
        
        
    };
    
    $scope.addNewService = function(){
        
        if( $scope.selectedService.program && 
                $scope.selectedService.program.id && 
                $scope.selectedService.service &&
                $scope.selectedService.service.id &&
                $scope.selectedService.dueDate &&
                $scope.dataElementForServiceOwner.id &&
                $scope.ashaEvent){
            
            var dhis2Event = {};            
            dhis2Event.trackedEntityInstance = $scope.selectedBeneficiary.id;
            dhis2Event.program = $scope.selectedService.program.id;
            dhis2Event.programStage = $scope.selectedService.service.id;
            dhis2Event.orgUnit = $scope.selectedOrgUnit.id;
            dhis2Event.status = 'VISITED';
            dhis2Event.dueDate = dhis2Event.eventDate = DateUtils.formatFromUserToApi($scope.selectedService.dueDate);
            dhis2Event.dataValues = [
                                        {dataElement: $scope.dataElementForServiceOwner.id, value: $scope.ashaEvent},
                                        {dataElement: $scope.dataElementForCurrentApprovalStatus.id, value: 'Pending'},
                                        {dataElement: $scope.dataElementForCurrentApprovalLevel.id, value: $scope.approvalAuthorityLevel}
                                    ];
            $scope.selectedServiceStage = $scope.stagesById[$scope.selectedService.service.id];
            
            $scope.selectedEnrollment = $scope.beneficiaryEnrollmentsByProgram[$scope.selectedService.program.id];
            
            if($scope.selectedEnrollment && $scope.selectedEnrollment.enrollment){
                dhis2Event.enrollment = $scope.selectedEnrollment.enrollment;
                var dhis2Events = {events: [dhis2Event]};
                DHIS2EventFactory.create(dhis2Events).then(function(data){
                    appendNewService(data);
                });
            }
            else{
                $scope.selectedEnrollment = {};
                $scope.selectedEnrollment.dateOfEnrollment = $scope.selectedEnrollment.dateOfIncident = DateUtils.formatFromUserToApi(DateUtils.getToday());
                $scope.selectedEnrollment.trackedEntityInstance = $scope.selectedBeneficiary.id;
                $scope.selectedEnrollment.program = $scope.selectedService.program.id;
                $scope.selectedEnrollment.status = 'ACTIVE';
                $scope.selectedEnrollment.orgUnit = $scope.selectedOrgUnit.id;
                $scope.selectedEnrollment.followup = false;
                
                EnrollmentService.enroll($scope.selectedEnrollment).then(function(data){
                    if(data.status !== 'SUCCESS'){
                        //enrollment has failed
                        var dialogOptions = {
                                headerText: 'enrollment_error',
                                bodyText: data.description
                            };
                        DialogService.showDialog({}, dialogOptions);
                        return;
                    }
                    else{
                        $scope.selectedEnrollment.enrollment = data.reference;
                        dhis2Event.enrollment = $scope.selectedEnrollment.enrollment;
                        var dhis2Events = {events: [dhis2Event]};
                        DHIS2EventFactory.create(dhis2Events).then(function(data){                            
                            appendNewService(data);
                        });
                    }
                });
            }
        }
        else{
            //invalid db configuration
            var dialogOptions = {
                    headerText: 'missing_config',
                    bodyText: $translate('stage_missing_service_owner_config')
                };
            DialogService.showDialog({}, dialogOptions);
            $scope.enrollmentSuccess = false;
            return;
        }
    };
    
    $scope.saveServiceApproval = function(service){
        
        var stage = $scope.stagesById[service.programStage];
        
        if( stage && stage.id ){                 
            
            var modalInstance = $modal.open({
                templateUrl: 'components/approval/approval.html',
                controller: 'ApprovalController',
                resolve: {
                    optionSets: function () {
                        return $scope.optionSets;
                    },
                    dataElementForCurrentApprovalLevelId: function () {
                        return $scope.dataElementForCurrentApprovalLevel.id;
                    },
                    dataElementForCurrentApprovalStatusId: function () {
                        return $scope.dataElementForCurrentApprovalStatus.id;
                    },                
                    stage: function () {
                        return stage;
                    },
                    event: function () {
                        return service;
                    }
                }
            });

            modalInstance.result.then(function (ev) {
                if (angular.isObject(ev)) {
                    service = ev;
                }
            }, function (ev) {
                if (angular.isObject(ev)) {
                    service = ev;
                }
            });
        }
    };
    
    function appendNewService(obj){

        if (obj.importSummaries[0].status === 'ERROR') {
            var dialogOptions = {
                headerText: 'service_registration_error',
                bodyText: obj.importSummaries[0].description
            };

            DialogService.showDialog({}, dialogOptions);
        }
        else{            
            var newService = angular.copy($scope.selectedBeneficiary);
            newService.eventDate = $scope.selectedService.dueDate;
            newService.dueDate = $scope.selectedService.dueDate;
            newService.status = 'VISITED';
            newService.enrollment = $scope.selectedEnrollment.enrollment;
            newService.event = obj.importSummaries[0].reference;
            newService.program = $scope.selectedService.program.id;
            newService.programStage = $scope.selectedService.service.id;
            newService.serviceName = $scope.selectedService.service.name;
            newService.programName = $scope.selectedService.program.name;
            newService.trackedEntityInstance = $scope.selectedBeneficiary.id;
            newService.currentApprovalStatus = 'Pending';
            newService[$scope.dataElementForCurrentApprovalLevel.id] = $scope.currentApprovalLevel;
            newService[$scope.dataElementForCurrentApprovalStatus.id] = 'Pending';
            newService[$scope.dataElementForServiceOwner.id] = $scope.ashaEvent;
            
            if( !$scope.servicesProvided ){
                $scope.servicesProvided = [];
            }
            
            $scope.servicesProvided.splice($scope.servicesProvided.length,0, newService);
        }
        
        $scope.selectedService = {};
    }
    
    $scope.generatePaymentSlip = function(){

        var modalInstance = $modal.open({
            templateUrl: 'components/payment/service-payment-slip.html',
            controller: 'PaymentController',
            windowClass: 'modal-full-window',
            resolve: {
                payments: function(){
                    return $scope.servicesProvided;
                },
                paymentRate: function(){
                    return $scope.paymentRate;
                },
                orgUnitName: function(){
                    return $scope.orgUnitName;
                },
                programs: function(){
                    return $scope.beneficiaryPrograms;
                },
                programsById: function(){
                    return $scope.beneficiaryProgramsById;
                },
                stages: function(){
                    return $scope.stages;
                },
                stagesById: function(){
                    return $scope.stagesById;
                },
                ashaDetails: function(){
                    return $scope.ashaDetails;
                },
                ashaPeriod: function(){
                    return $scope.ashaPeriod;
                },
                ashaEvent: function(){
                    return $scope.ashaEvent;
                },
                slipType: function(){
                    return 'SERVICE';
                }
            }
        });
        
        modalInstance.result.then(function () {                 
        });
    };
    
    $scope.sortGrid = function(gridHeader){
        if ($scope.sortColumn && $scope.sortColumn.id === gridHeader.id){
            $scope.reverse = !$scope.reverse;
            return;
        }        
        $scope.sortColumn = gridHeader;
        if($scope.sortColumn.valueType === 'date'){
            $scope.reverse = true;
        }
        else{
            $scope.reverse = false;    
        }
    };
    
    $scope.d2Sort = function(tei){        
        if($scope.sortColumn && $scope.sortColumn.valueType === 'date'){            
            var d = tei[$scope.sortColumn.id];         
            return DateUtils.getDate(d);
        }
        return tei[$scope.sortColumn.id];
    };
    
    $scope.search = function(mode){   
        $scope.sortColumn = {};
        
        if( !$scope.commonBeneficiaryProgram ){
            console.log('There needs to be at least one beneficiary program');
            return false;
        }
        
        resetFields();
        
        $scope.selectedSearchMode = mode;        
   
        if($scope.commonBeneficiaryProgram){
            $scope.programUrl = 'program=' + $scope.commonBeneficiaryProgram.id;
        }        
        
        //check search mode
        if( $scope.selectedSearchMode === $scope.searchMode.freeText ){ 
            
            if(!$scope.searchText.value){                
                $scope.emptySearchText = true;
                $scope.teiFetched = false;   
                $scope.teiCount = null;
                return;
            }       
 
            $scope.queryUrl = 'query=LIKE:' + $scope.searchText.value;                     
        }
        
        if( $scope.selectedSearchMode === $scope.searchMode.attributeBased ){            
            $scope.searchText.value = null;
            $scope.attributeUrl = EntityQueryFactory.getAttributesQuery($scope.attributes, $scope.enrollment);
            
            if(!$scope.attributeUrl.hasValue && !$scope.commonBeneficiaryProgram){
                $scope.emptySearchAttribute = true;
                $scope.teiFetched = false;   
                $scope.teiCount = null;
                return;
            }
        }
        
        $scope.fetchTei();
    };
    
    $scope.fetchTei = function(){

        $scope.selectedBeneficiary = null;
        
        //get events for the specified parameters
        TEIService.search($scope.selectedOrgUnit.id, 
                                            $scope.selectedOuMode.name,
                                            $scope.queryUrl,
                                            $scope.programUrl,
                                            $scope.attributeUrl.url,
                                            $scope.pager,
                                            true).then(function(data){            
            
            if(data.rows){
                $scope.teiCount = data.rows.length;
            }                    
            
            if( data.metaData.pager ){
                $scope.pager = data.metaData.pager;
                $scope.pager.toolBarDisplay = 5;

                Paginator.setPage($scope.pager.page);
                Paginator.setPageCount($scope.pager.pageCount);
                Paginator.setPageSize($scope.pager.pageSize);
                Paginator.setItemCount($scope.pager.total);                    
            }
            
            //process tei grid
            $scope.trackedEntityList = TEIGridService.format(data,false, $scope.optionSets);            
            $scope.showTrackedEntityDiv = true;
            $scope.teiFetched = true;            
            
            if(!$scope.sortColumn.id){                                      
                $scope.sortGrid({id: 'created', name: $translate('registration_date'), valueType: 'date', displayInListNoProgram: false, showFilter: false, show: false});
            }
        });
    };
    
    $scope.jumpToPage = function(){
        if($scope.pager && $scope.pager.page && $scope.pager.pageCount && $scope.pager.page > $scope.pager.pageCount){
            $scope.pager.page = $scope.pager.pageCount;
        }
        $scope.search();
    };
    
    $scope.resetPageSize = function(){
        $scope.pager.page = 1;        
        $scope.search();
    };
    
    $scope.getPage = function(page){    
        $scope.pager.page = page;
        $scope.search();
    };
    
    $scope.showAddServiceRow = function(){
        $scope.showAddNewServiceDiv = !$scope.showAddNewServiceDiv;        
        $scope.showSearchDiv = false;
        $scope.showRegistrationDiv = false;
    }; 
    
    $scope.showRegistration = function(){
        $scope.showRegistrationDiv = !$scope.showRegistrationDiv;
    };
    
    $scope.showHideSearch = function(simpleSearch){
        if(simpleSearch){
            $scope.showSearchDiv = false;
        }
        else{
            $scope.showSearchDiv = !$scope.showSearchDiv;
        }        
    };
    
    $scope.showAddNewService = function(selectedBeneficiary){        
        $scope.beneficiaryEnrollments = [];
        $scope.beneficiaryEnrollmentsByProgram = [];
        $scope.selectedBeneficiary = selectedBeneficiary;
        
        if($scope.selectedBeneficiary && $scope.selectedBeneficiary.id){            
            EnrollmentService.getByEntity($scope.selectedBeneficiary.id).then(function(response){                
                angular.forEach(response.enrollments, function(en){
                    if($scope.beneficiaryProgramsById[en.program]){
                        if(en.status === 'ACTIVE'){
                            $scope.beneficiaryEnrollmentsByProgram[en.program] = en;
                            $scope.beneficiaryEnrollments.push(en);
                        }
                    }
                });
            });           
            $scope.showAddNewServiceDiv = true;
        }
    };
    
    $scope.hideAddNewService = function(){
        $scope.showAddNewServiceDiv = false;
        $scope.selectedBeneficiary = null;
    };
    
    $scope.deleteService = function(service){
        
        var modalOptions = {
            closeButtonText: 'cancel',
            actionButtonText: 'delete',
            headerText: 'delete',
            bodyText: 'are_you_sure_to_delete'
        };

        ModalService.showModal({}, modalOptions).then(function(result){
            
            DHIS2EventFactory.delete(service).then(function(data){
                
                var continueLoop = true, index = -1;
                for(var i=0; i< $scope.servicesProvided.length && continueLoop; i++){
                    if($scope.servicesProvided[i].event === service.event ){
                        continueLoop = false;
                        index = i;
                    }
                }
                if(index !== -1){
                    $scope.servicesProvided.splice(index,1);
                }                
            });
        });        
    };
    
    $scope.showNotes = function(dhis2Event){        
        var modalInstance = $modal.open({
            templateUrl: 'components/approval/comment.html',
            controller: 'CommentController',
            resolve: {
                dhis2Event: function () {
                    return dhis2Event;
                }
            }
        });

        modalInstance.result.then(function (){
        });
    };
});
