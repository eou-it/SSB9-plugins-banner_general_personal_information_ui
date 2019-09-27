/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import grails.gorm.transactions.Transactional


@Transactional
class PersonalInformationConfigService extends BasePersonConfigService {

    static final String PERSONAL_INFO_CONFIG_CACHE_NAME = 'generalPersonalInfoConfig'
    static final String PERSONAL_INFO_PROCESS_CODE = 'PERSONAL_INFORMATION_SSB'
    static final String OVERVIEW_ADDR = 'OVERVIEW.ADDRESS.TYPE'
    static final String OVERVIEW_PHONE = 'OVERVIEW.PHONE.TYPE'
    static final String DISPLAY_OVERVIEW_ADDR = 'DISPLAY.OVERVIEW.ADDRESS'
    static final String DISPLAY_OVERVIEW_PHONE = 'DISPLAY.OVERVIEW.PHONE'
    static final String DISPLAY_OVERVIEW_EMAIL = 'DISPLAY.OVERVIEW.EMAIL'
    static final String PROFILE_PICTURE = 'DISPLAY.PROFILE.PICTURE'
    static final String PREF_EMAIL = 'PREFERRED.EMAIL.UPDATABILITY'
    static final String PERS_DETAILS_MODE = 'PERSONAL.DETAIL.SECTION.MODE'
    static final String EMAIL_MODE = 'EMAIL.SECTION.MODE'
    static final String PHONE_MODE = 'PHONE.SECTION.MODE'
    static final String ADDR_MODE = 'ADDRESS.SECTION.MODE'
    static final String EMER_MODE = 'EMERGENCY.CONTACT.SECTION.MODE'
    static final String ETHN_RACE_MODE = 'ETHNICITY.RACE.MODE'
    static final String VETERANS_CLASSIFICATION = 'ENABLE.VETERAN.CLASSIFICATION'
    static final String DISABILITY_STATUS = 'ENABLE.DISABILITY.STATUS'
    static final String DIRECTORY_PROFILE = 'ENABLE.DIRECTORY.PROFILE'
    static final String SECURITY_QA_CHANGE = 'ENABLE.SECURITY.QA.CHANGE'
    static final String PASSWORD_CHANGE = 'ENABLE.PASSWORD.CHANGE'
    static final String NO_OF_QSTNS = 'GUBPPRF_NO_OF_QSTNS'
    static final String SECTION_HIDDEN = '0'
    static final String SECTION_READONLY = '1'
    static final String SECTION_UPDATEABLE = '2'
    static final String YES = 'Y'
    static final String NO = 'N'

    @Override
    protected String getCacheName() {
        return PERSONAL_INFO_CONFIG_CACHE_NAME
    }

    @Override
    protected String getProcessCode() {
        return PERSONAL_INFO_PROCESS_CODE
    }

    @Override
    protected List getExcludedProperties() {
        // These are sequences, not simple key-value pairs, and are not a part of this particular configuration
        return [OVERVIEW_ADDR, OVERVIEW_PHONE]
    }
}
