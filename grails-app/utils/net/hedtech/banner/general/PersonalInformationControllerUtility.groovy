/******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.DisplayMaskingColumnRuleView
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.security.core.context.SecurityContextHolder

@Slf4j
class PersonalInformationControllerUtility {

    def static getPrincipalPidm() {
        try {
            return SecurityContextHolder?.context?.authentication?.principal?.pidm
        } catch (MissingPropertyException it) {
            log.error("principal lacks a pidm - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(it)
            throw it
        }
    }

    def static getPrincipalUsername() {
        try {
            return SecurityContextHolder?.context?.authentication?.principal?.username
        } catch (MissingPropertyException it) {
            log.error("principal lacks a username - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(it)
            throw it
        }
    }

    public static getFetchListParams(params) {
        def maxItems = params.max as int
        def map = [
                max: maxItems,
                offset: (params.offset as int) * maxItems,  // Convert the page-level offset passed as a param to an item-level offset
                searchString: params.searchString
        ]

        map
    }

    /**
     * Returns a map of masking rules for the given block name.
     * @param blockName a valid block name such as 'BWGKOADR_ALL'
     * @return map of masking rules
     */
    public static getMaskingRule(String blockName) {
        def maskingRule = [:]
        def maskingColumnRules = DisplayMaskingColumnRuleView.fetchSSBMaskByBlockName([blockName: blockName])

        maskingColumnRules.each { it ->
            if (it.columnName == '%_SURNAME_PREFIX')
                maskingRule.displaySurnamePrefix = (it.displayIndicator == "Y")
            else if (it.columnName == '%_HOUSE_NUMBER')
                maskingRule.displayHouseNumber = (it.displayIndicator == "Y")
            else if (it.columnName == '%_INTL_ACCESS')
                maskingRule.displayInternationalAccess = (it.displayIndicator == "Y")
            else if (it.columnName == '%_STREET_LINE4')
                maskingRule.displayStreetLine4 = (it.displayIndicator == "Y")
            else if (it.columnName == '%_CTRY_CODE_PHONE')
                maskingRule.displayCountryCode = (it.displayIndicator == "Y")
        }

        return maskingRule
    }

    public static  returnFailureMessage(ApplicationException e) {
        def model = [:]

        model.failure = true
        log.error(e.getMessage())

        try {
            def extractError = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) })
            model.message = extractError.message + (extractError.errors ? " " + extractError.errors : "")

            if(e.type == 'SQLException'){
                // don't expose the oracle error numbers in SQL exceptions
                model.message = model.message.replaceAll("(ORA)-[0-9]+: ","")
            }

            return model
        }
        catch (Exception ex) {
            log.error(ex)
            model.message = e.message
            return model
        }
    }

}
