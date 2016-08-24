/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

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
