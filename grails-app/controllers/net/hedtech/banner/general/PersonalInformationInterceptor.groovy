/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import net.hedtech.banner.SanitizeUtility


class PersonalInformationInterceptor {

    PersonalInformationInterceptor() {
        match controller:'personalInformationDetails', action:'\\A(update|add|delete).+|\\A.+List', find:true, regex:true
        match controller:'personalInformationPicture|personalInformationQA', action:'*'
    }

    boolean before() {
        def requestParams = request?.JSON ?: params

        if (requestParams in List) {
            requestParams.each { item -> SanitizeUtility.sanitizeMap(item) }
        } else if (requestParams in Map) {
            SanitizeUtility.sanitizeMap(requestParams)
        } else {
            log.error(new Exception('Unknown request parameter type. Expected Map or Array.'))
            return false
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
