/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationApp.service('personalInformationService', ['$filter', function ($filter) {

    var calendar = $.calendars.instance();

    /**
     * Get state for view which shows full profile information (as opposed to
     * just an overview (summary) view).
     * @returns {string} Name of appropriate state.
     */
    this.getFullProfileState = function() {
        return isDesktop() ? 'personalInformationMain' : 'piFullViewMobile';
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

}]);