/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationAppDirectives.directive('raceAndEthnNavigation', [function () {
    return {
        restrict: 'A',
        link: function(scope, ele, attr) {
            ele.on('click', function(event) {
                if(scope.isMobileView()) {
                    sessionStorage.setItem('startingTab', 'additionalDetails');
                }
            });
        }
    };
}]);