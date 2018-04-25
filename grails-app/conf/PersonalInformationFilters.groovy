/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import net.hedtech.banner.SanitizeUtility
import org.apache.log4j.Logger

class PersonalInformationFilters {
    private static final log = Logger.getLogger(PersonalInformationFilters.class)

    def filters = {
        // Sanitize all parameter values for actions that use request params in the personalInformationDetails controller.
        sanitizePersonalInfoDetailsFilter(controller:'personalInformationDetails', action:'\\A(update|add|delete).+|\\A.+List', find:true, regex:true) {
            before = {
                def requestParams = request?.JSON ?: params

                if(!sanitize(requestParams))
                    return false
            }
        }

        sanitizePictureAndQaFilter(controller:'personalInformationPicture|personalInformationQA', action:'*') {
            before = {
                def requestParams = request?.JSON ?: params

                if(!sanitize(requestParams))
                    return false
            }
        }
    }

    private boolean sanitize(requestParams) {
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
}