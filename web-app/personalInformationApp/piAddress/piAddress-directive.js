/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationAppDirectives.directive('personAddress', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piViewAddress.html')
    };
}]);

personalInformationAppDirectives.directive('personAddressList', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piAddressList.html')
    };
}]);
