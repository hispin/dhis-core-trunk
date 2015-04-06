/* global trackerCapture, angular, dhis2 */

trackerCapture.controller('PaymentController',
        function($scope,
                $filter,
                $modalInstance,
                $translate,
                payments,
                paymentRate,
                programs,
                programsById,
                stages,
                stagesById,
                orgUnitName,
                ashaDetails,
                ashaEvent,
                ashaPeriod,
                slipType) {

    $scope.payments = payments;
    $scope.paymentRate = paymentRate;
    $scope.programs = programs;
    $scope.programsById = programsById;
    $scope.stages = stages;
    $scope.stagesById = stagesById;
    $scope.ashaDetails = ashaDetails;
    $scope.ashaEvent = ashaEvent;
    
    $scope.paymentHeaders = [];
    $scope.paymentHeaders.push({name: $translate('org_unit'), value: orgUnitName});
    $scope.paymentHeaders.push({name: $translate('period'), value: ashaPeriod.name});
    
    $scope.paymentTableHeaders = [];
    $scope.paymentTableHeaders.push({id: 'program', value: $translate('program')});
    
   
    $scope.paymentReport = [];     
    $scope.totalPaymentAmount = new Number(0);
    if(slipType === 'ACTIVITY'){
        //$scope.totalPaymentAmount = 0;
        angular.forEach($scope.programs, function(program){
            $scope.paymentReport[program.id] = {hasData: false, rate: 'rate', claimed: 0, pending: 0, rejected: 0, approved: 0, sanctioned: 0};
            angular.forEach($filter('filter')($scope.payments, {program: program.id}), function(payment){
                var obj = $scope.paymentReport[payment.program];
                $scope.paymentReport[program.id] = {hasData: true, 
                                                        program: $scope.programsById[payment.program].name,
                                                        rate: getStageRate(payment.programStage),
                                                        programStageId: payment.programStage,
                                                        claimed: obj.claimed + 1, 
                                                        pending: !payment.currentApprovalStatus ? obj.pending + 1: obj.pending,
                                                        rejected: payment.currentApprovalStatus === 'Rejected' ? obj.rejected + 1 : obj.rejected,
                                                        approved: payment.currentApprovalStatus === 'Approved' ? obj.approved + 1 : obj.approved,
                                                        sanctioned: ''
                                                    };
            });
        });
        
        for(var key in $scope.paymentReport){            
            if($scope.paymentReport[key] && $scope.paymentReport[key].hasData){                
                $scope.paymentReport[key] = calculatePayment( $scope.paymentReport[key] );
                
                if( dhis2.validation.isNumber( $scope.paymentReport[key].sanctioned ) ){
                    $scope.totalPaymentAmount = $scope.totalPaymentAmount + new Number( $scope.paymentReport[key].sanctioned );
                }
                
            }
        }
    }
    else if (slipType === 'SERVICE'){
        //$scope.totalPaymentAmount = 0;
        var report = [];
        angular.forEach($scope.stages, function(st){
            report[st.id] = {hasData: false, rate: 'rate', claimed: 0, pending: 0, rejected: 0, approved: 0, sanctioned: 0};
            angular.forEach($filter('filter')($scope.payments, {programStage: st.id}), function(payment){
                var obj = report[st.id];
                report[st.id] = {hasData: true, 
                                                        program: $scope.programsById[payment.program].name,
                                                        service: $scope.stagesById[payment.programStage].name,
                                                        programId: payment.program,
                                                        programStageId: payment.programStage,
                                                        rate: getStageRate(payment.programStage), 
                                                        claimed: obj.claimed + 1, 
                                                        pending: !payment.currentApprovalStatus ? obj.pending + 1: obj.pending,
                                                        rejected: payment.currentApprovalStatus === 'Rejected' ? obj.rejected + 1 : obj.rejected,
                                                        approved: payment.currentApprovalStatus === 'Approved' ? obj.approved + 1 : obj.approved,
                                                        sanctioned: ''
                                                    };
            });
        });
        
        for(var key in report){
            
            if(report[key] && report[key].hasData){
                
                var r = calculatePayment( report[key] );
                
                $scope.paymentReport.push( r );
                
                if( dhis2.validation.isNumber( r.sanctioned ) ){
                    $scope.totalPaymentAmount = $scope.totalPaymentAmount + new Number( r.sanctioned );
                }                
            }
        }
        
        $scope.paymentTableHeaders.push({id: 'service', value: $translate('service')});
    }
    
    
    $scope.paymentTableHeaders.push({id: 'rate', value: $translate('rate')});
    $scope.paymentTableHeaders.push({id: 'claimed', value: $translate('claimed')});
    $scope.paymentTableHeaders.push({id: 'pending', value: $translate('pending')});
    $scope.paymentTableHeaders.push({id: 'rejected', value: $translate('rejected')});
    $scope.paymentTableHeaders.push({id: 'approved', value: $translate('approved')});
    $scope.paymentTableHeaders.push({id: 'sanctioned', value: $translate('sanctioned')});
    
    $scope.close = function () {
        $modalInstance.close($scope.gridColumns);
    }; 
    
    function getStageRate( stageId ){        
        
        var r = $scope.stagesById[stageId].PaymentRate;
        var rateCode = $scope.paymentRate.code['"' + r + '"'];
        
        if( rateCode && rateCode.id && $scope.paymentRate.value[rateCode.id]){
            var rate = $scope.paymentRate.value[rateCode.id];

            if( dhis2.validation.isNumber(rate)){
               rate = new Number(rate);               
               return rate;
            }
        }
        
        return null;
    };
    
    function calculatePayment( obj ){
        
        var amount = new Number(obj.approved);
        var rate = obj.rate;
        
        if( amount > 0 ){                    
            if( dhis2.validation.isNumber( rate ) ){
                rate = new Number(rate);
                obj.sanctioned = rate*amount;
            }
            else{
                obj.sanctioned = $translate('no_rate_defined');
            }            
        }
        
        return obj;
    };
});