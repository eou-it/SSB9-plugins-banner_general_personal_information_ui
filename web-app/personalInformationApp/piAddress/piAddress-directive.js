/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationAppDirectives.directive('personViewAddress', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piViewAddress.html')
    };
}]);

personalInformationAppDirectives.directive('personViewAddressList', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piViewAddressList.html')
    };
}]);

personalInformationAppDirectives.directive('personPreEditAddress', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piPreEditAddress.html')
    };
}]);

personalInformationAppDirectives.directive('personPreEditAddressList', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piPreEditAddressList.html')
    };
}]);

personalInformationAppDirectives.directive('setStartingTab', [function () {
    return {
        restrict: 'A',
        link: {
            pre: function(scope, ele, attr) {
                if(attr.setStartingTab === scope.startingTab){
                    attr.active = true;
                }
            }
        }
    };
}]);