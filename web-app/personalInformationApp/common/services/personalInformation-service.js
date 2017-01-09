/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationApp.service('personalInformationService', ['$rootScope', '$filter', '$resource',
    function ($rootScope, $filter, $resource) {

    var calendar = $.calendars.instance();

    // CONSTANTS
    this.AUDIBLE_MSG_UPDATED = 'audible-msg-updated';


    /**
     * Get state for view which shows full profile information (as opposed to
     * just an overview (summary) view).
     * @returns {string} Name of appropriate state.
     */
    this.getFullProfileState = function() {
        return isMobile() ? 'piFullViewMobile' : 'personalInformationMain';
    };

    this.stringToDate = function (date) {
        var dateFmt = $filter('i18n')('default.date.format').toLowerCase(),
            result;
        try {
            result = calendar.parseDate(dateFmt, date).toJSDate();
            return result;
        }
        catch (exception) {
            return null;
        }
    };

    // Destroy all popovers (i.e. Bootstrap popovers)
    this.destroyAllPopovers = function (){
        // When created, the actual popover is the next sibling adjacent to the
        // AngularJS popover element.  The actual popover has the '.popover.in'
        // CSS selector.  Here's a diagram:
        //
        //     ANGULARJS ELEMENT              ACTUAL POPOVER VISIBLE TO USER
        //     <dd-pop-over ...></dd-pop-over><div class="popover in" ...> ... </div>
        //
        // Thus the previous sibling (grabbed with prev()) is the
        // AngularJS popover element that needs to have 'destroy' called on it.
        $('body').find('.popover.in').prev().popover('destroy');
    };

    /**
     * Set up an audible message for a screen reader, if any, to read.
     * @param msg The message to voice
     * @param popoverElement Element to which popover is anchored
     */
    this.setPlayAudibleMessage = function (msg, popoverElement) {
        var self = this;

        // Set up audible message to reset when Bootstrap popover closes
        popoverElement.on('hide.bs.popover', function(event) {
            // Reset message
            $rootScope.playAudibleMessage = null;

            // Broadcast event to notify controllers to update views, as changes to the value of
            // $rootScope.playAudibleMessage are not always implicitly "noticed" by views.
            $rootScope.$broadcast(self.AUDIBLE_MSG_UPDATED);
        });

        $rootScope.playAudibleMessage = msg;
        $rootScope.$broadcast(self.AUDIBLE_MSG_UPDATED);
    };

    this.checkSecurityQuestionsExist = function () {
        return $resource('../ssb/:controller/:action',
            {controller: 'PersonalInformation', action: 'checkSecurityQuestionsExist'}).get();
    };

}]);