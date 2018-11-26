/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
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

personalInformationAppDirectives.directive('piMobileFooterButton', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            clickFunction: '=',
            isEdit: '=',
            isDisabled: '=',
            disabledMsg: '='
        },
        transclude: true,
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piMain/piMobileFooterButton.html')
    };
}]);

personalInformationAppDirectives.directive('piDeleteButton', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            clickFunction: '=',
            item: '=',
            buttonTitle: '@'
        },
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piMain/piDeleteButton.html')
    };
}]);

personalInformationAppDirectives.directive('enterKey', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.which === 13) {
                scope.$apply(function(){
                    scope.$eval(attrs.enterKey);
                });

                event.preventDefault();
            }
        });
    };
});

personalInformationAppDirectives.directive('notificationBox',['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piMain/piNotificationBox.html'),
        scope: {
            notificationText: '@'
        }
    };
}]);

/*
 * usage:
 * place dropdown-helper="begin" on the element with the data-toggle attribute so that when user keys to the previous focusable element
 * with shift+tab the dropdown menu is closed. Place dropdown-helper="end" on the last item in the menu so when the user keys to the
 * next focusable element with tab the dropdown menu closes.
 */
personalInformationAppDirectives.directive('dropdownHelper', [function () {
    return {
        restrict: 'A',
        link: function (scope, elem, attrs) {

            elem.bind('keydown', function(event) {
                var code = event.keyCode || event.which;

                if (code === 9) {
                    if(attrs.dropdownHelper === 'begin'){
                        if(event.shiftKey){
                            //close dropdown if it is open when shift+tab off element
                            if(elem.parent().hasClass('open')){
                                elem.dropdown('toggle');
                            }
                        }
                    }
                    else if(attrs.dropdownHelper === 'end'){
                        if(!event.shiftKey){
                            //close dropdown when tab off element, toggle button so events fire as expected
                            elem.parents('ul.dropdown-menu').siblings('button.dropdown-btn').dropdown('toggle');
                        }
                    }
                }
            });
            scope.$on('$destroy', function () {
                elem.unbind('keydown');
            });
        }
    };
}]);

personalInformationAppDirectives.directive('menuControls', [function () {
    var PREV = -1, NEXT = 1;

    return {
        restrict: 'A',
        scope: false,
        link: function (scope, elem) {
            var listItems;

            var getSelectedItem = function(){
                var selected = 0;

                listItems = elem.find('li > a');

                listItems.each(function(index){
                    if($(this).is(':focus')){
                        selected = index;
                        return false;
                    }
                });

                return selected;
            };

            var selectItem = function(direction){
                var selected = getSelectedItem();

                selected += direction;

                if (selected >= listItems.length) {
                    selected = 0;
                }
                else if (selected < 0) {
                    selected = listItems.length - 1;
                }

                listItems.eq(selected).focus();
            };

            var clickSelectedItem = function(direction){
                var selected = getSelectedItem();

                listItems.eq(selected).click();

                elem.siblings("button.dropdown-btn").focus();
            };

            elem.bind('keydown', function(event) {
                var code = event.keyCode || event.which;

                switch (code){
                    case 37: //left arrow key
                    case 38: //up arrow key
                        selectItem(PREV);
                        event.preventDefault();
                        event.stopPropagation();
                        break;
                    case 39: //right arrow key
                    case 40: //down arrow key
                        selectItem(NEXT);
                        event.preventDefault();
                        event.stopPropagation();
                        break;
                    case 13: //enter key
                        clickSelectedItem();
                        event.preventDefault();
                        event.stopPropagation();
                        break;
                }
            });
            scope.$on('$destroy', function () {
                elem.unbind('keydown');
            });
        }
    };
}]);

/*
 * place on div that encapsulates the dropdown to bind a variable to the open/close state of the dropdown
 */
personalInformationAppDirectives.directive('dropdownState', [function () {
    return {
        restrict: 'A',
        scope: {
            state: '=dropdownState'
        },
        link: function (scope, elem) {
            elem.on('shown.bs.dropdown hidden.bs.dropdown', function (event) {
                // state will be true when dropdown is open aka shown, false when it closes
                scope.state = (event.type === 'shown');
                scope.$apply();
            });
        }
    };
}]);

/*
 * place on dropdown button to bind a variable that determines whether an item in dropdown has been selected. If the
 * item is truthy the ng-not-empty CSS class is added to the button
 */
personalInformationAppDirectives.directive('dropdownSelected', [function () {
    return {
        restrict: 'A',
        scope: {
            item: '=dropdownSelected'
        },
        link: function (scope, elem) {

            scope.$watch('item', function(newVal, oldVal) {
                if (newVal) {
                    elem.addClass('ng-not-empty');
                }
                else {
                    elem.removeClass('ng-not-empty');
                }
            });
        }
    };
}]);

personalInformationAppDirectives.directive('piInputWatcher', [function () {
    return {
        restrict: 'A',
        require: '?ngModel',
        link: function (scope, elem, attrs, ngModel) {

            // do nothing if ng-model is not present
            if (!ngModel) return;

            scope.$watch(
                // watch the value of the input
                function() {
                    return elem.val();
                },
                // change listener, update ngModel whenever watched value changes
                function(newValue, oldValue) {
                    if (newValue !== oldValue) {
                        ngModel.$setViewValue(newValue);
                    }
                }
            );
        }
    };
}]);
