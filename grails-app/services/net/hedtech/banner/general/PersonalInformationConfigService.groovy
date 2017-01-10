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

    static final String OVERVIEW_ADDR = 'OVERVIEW.ADDRESS'
    static final String OVERVIEW_PHONE = 'OVERVIEW.PHONE'
    static final String DISPLAY_OVERVIEW_ADDR = 'DISPLAY.OVERVIEW.ADDRESS'
    static final String DISPLAY_OVERVIEW_PHONE = 'DISPLAY.OVERVIEW.PHONE'
    static final String DISPLAY_OVERVIEW_EMAIL = 'DISPLAY.OVERVIEW.EMAIL'
    static final String PROFILE_PICTURE = 'DISPLAY.PROFILE.PICTURE'

    static final String MARITAL_STATUS = 'MARITAL.STATUS.UPDATABILITY'
    static final String PREF_EMAIL = 'PREFERRED.EMAIL.UPDATABILITY'

    static final String PERS_DETAILS_MODE = 'PERSONAL.DETAIL.SECTION.MODE'
    static final String EMAIL_MODE = 'EMAIL.SECTION.MODE'
    static final String PHONE_MODE = 'PHONE.SECTION.MODE'
    static final String ADDR_MODE = 'ADDRESS.SECTION.MODE'
    static final String EMER_MODE = 'EMERGENCY.CONTACT.SECTION.MODE'

    static final String ETHN_RACE_MODE = 'ETHNICITY.RACE.MODE'
    static final String VETERANS_CLASSIFICATION = 'ENABLE.VETERANS.CLASSIFICATION'
    static final String DISABILITY_STATUS = 'ENABLE.DISABILITY.STATUS'

    static final String DIRECTORY_PROFILE = 'DISPLAY.DIRECTORY.PROFILE'
    static final String SECURITY_QA_CHANGE = 'ENABLE.SECURITY.QA.CHANGE'
    static final String PASSWORD_CHANGE = 'ENABLE.PASSWORD.CHANGE'

    static final String SECTION_HIDDEN = '0'
    static final String SECTION_READONLY = '1'
    static final String SECTION_UPDATEABLE = '2'

    static final String YES = 'Y'
    static final String NO = 'N'

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
        def EXCLUDED_PROPERTIES = [OVERVIEW_ADDR, OVERVIEW_PHONE]

        configFromGoriccr.each {it ->
            if (!EXCLUDED_PROPERTIES.contains(it.settingName)) {
                config[it.settingName] = it.value
            }
        }

        personConfigInSession[PERSONAL_INFO_CONFIG_CACHE_NAME] = config

        personConfigInSession
    }
}