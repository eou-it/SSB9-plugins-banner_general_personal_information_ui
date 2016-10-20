/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationApp.service('personalInformationService', [function () {

    /**
     * Get state for view which shows full profile information (as opposed to
     * just an overview (summary) view).
     * @returns {string} Name of appropriate state.
     */
    this.getFullProfileState = function() {
        return isDesktop() ? 'personalInformationMain' : 'piFullViewMobile';
    };

}]);