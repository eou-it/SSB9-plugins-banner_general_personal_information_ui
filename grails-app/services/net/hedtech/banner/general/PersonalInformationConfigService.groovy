/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.person.PersonUtility
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
class PersonalInformationConfigService {

    static final String PERSONAL_INFO_CONFIG_CACHE_NAME = 'generalPersonalInfoConfig'

    static final String EMAIL_MODE = 'EMAIL.SECTION.MODE'
    static final String PHONE_MODE = 'PHONE.SECTION.MODE'
    static final String ADDR_MODE = 'ADDRESS.SECTION.MODE'
    static final String EMER_MODE = 'EMERGENCY.CONTACT.SECTION.MODE'
    static final String PERS_DETAILS_MODE = 'PERSONAL.DETAIL.SECTION.MODE'
    static final String ADDL_DETAILS_MODE = 'ADDITIONAL.DETAIL.SECTION.MODE'

    static final String SECTION_HIDDEN = '0'
    static final String SECTION_READONLY = '1'
    static final String SECTION_UPDATEABLE = '2'

    def getParamFromSession(param, defaultVal) {
        def personalInfoConfig = getPersonalInfoConfigFromSession()

        def paramVal = personalInfoConfig[param]

        if (!paramVal) {
            log.error("No value found for integration configuration setting \"" + param + "\". " +
                      "This should be configured in GORICCR. Using default value of \"" + defaultVal + "\".")

            paramVal = defaultVal
        }

        paramVal
    }

    private static getPersonalInfoConfigFromSession() {
        def personConfigInSession = PersonUtility.getPersonConfigFromSession()

        if (personConfigInSession) {
            if (!personConfigInSession.containsKey(PERSONAL_INFO_CONFIG_CACHE_NAME)) {
                createPersonalInfoConfig(personConfigInSession)
            }
        } else {
            personConfigInSession = [:]
            createPersonalInfoConfig(personConfigInSession)
            PersonUtility.setPersonConfigInSession(personConfigInSession)
        }

        personConfigInSession[PERSONAL_INFO_CONFIG_CACHE_NAME]
    }

    private static createPersonalInfoConfig(personConfigInSession) {
        def configFromGoriccr = IntegrationConfiguration.fetchAllByProcessCode('PERSONAL_INFORMATION_SSB')
        def config = [:]

        // These are sequences, not simple key-value pairs, and are not a part of this particular configuration
        def EXCLUDED_PROPERTIES = ['OVERVIEW.ADDRESS', 'OVERVIEW.PHONE']

        configFromGoriccr.each {it ->
            if (!EXCLUDED_PROPERTIES.contains(it.settingName)) {
                config[it.settingName] = it.value
            }
        }

        personConfigInSession[PERSONAL_INFO_CONFIG_CACHE_NAME] = config

        personConfigInSession
    }
}