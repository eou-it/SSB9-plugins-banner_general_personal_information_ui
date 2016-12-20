/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationAppDirectives.directive('piPopOver', ['$filter', 'personalInformationService',
    function($filter, personalInformationService) {

    var link = function (scope, element, attrs) {
        var width = attrs.popoverWidth || '300px',
            position = attrs.popoverPosition || 'top',
            slideLeft = attrs.slideLeft === 'true',
            msg = $filter('i18n')(attrs.popoverMsg),
            template = '<div class="popover pi-tooltip" style="width: ' + width + ';"><div class="popover-content"></div><div class="arrow"></div></div>';

        scope.togglePopover = function(event) {
            // Prevent the hidePopover directive from handling the event, immediately closing the popover
            if (event) {
                event.stopImmediatePropagation();
            }

            // Toggle popover open/closed
            if (element.next('.popover.in').length !== 0) {
                // Popover is already open, toggle it closed
                element.popover('destroy');
            } else {
                // Destroy any existing popovers
                personalInformationService.destroyAllPopovers();

                // Open popover
                element.popover({
                    template: template,
                    content: msg,
                    trigger: 'manual',
                    placement: position,
                    html: true
                });

                // Offset popover to left if so configured
                if (slideLeft) {
                    element.on('shown.bs.popover', function (event) {
                        // Slide the popover 80px to the left, while offsetting the arrow to
                        // remain pointing at its element.
                        var popoverElement = $(event.target).next(),
                            popoverLeftOffsetPx = -80,
                            arrowLeftOffsetPct = 79.5;

                        popoverElement.css('left', parseInt(popoverElement.css('left')) + popoverLeftOffsetPx + 'px');
                        popoverElement.find('.arrow').css('left', arrowLeftOffsetPct + '%');
                    });
                }

                element.popover('show');

                // Make message available to screen reader.  This function call results in message being
                // set in audible message div as well as that div being made visibile, which causes readers
                // to pick up on the role=alert and aria-live=assertive attributes, reading the message.
                personalInformationService.setPlayAudibleMessage(msg, element);
            }
        };

        // Ensure view is updated when audible message div is updated
        scope.$on(personalInformationService.AUDIBLE_MSG_UPDATED, function() {
            scope.$apply();
        });

        element.on('click', function(event) {
            scope.togglePopover(event);
        });
    };

    return {
        restrict: 'A',
        link : link
    };
}]);

personalInformationAppDirectives.directive('hidePopover', ['personalInformationService', function(personalInformationService) {

    var link = function(scope, element) {
        element.on('click', function(event) {
            // If not clicking on the modal (the check for ".parents('.popover.in')" checks for that),
            // destroy all popovers.
            if ($(event.target).parents('.popover.in').length === 0) {
                personalInformationService.destroyAllPopovers();
            }
        });
    };

    return {
        restrict: 'A',
        link: link
    };
}]);
