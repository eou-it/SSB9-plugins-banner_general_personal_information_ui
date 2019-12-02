/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general


import com.fasterxml.jackson.databind.ObjectMapper
import grails.gorm.transactions.Transactional
import grails.util.Holders


@Transactional
class PersonalInformationConfigService extends BasePersonConfigService {

    static final String PERSONAL_INFO_CONFIG_CACHE_NAME = 'generalPersonalInfoConfig'
    static final String PERSONAL_INFO_PROCESS_CODE = 'PERSONAL_INFORMATION_SSB'
    static final String OVERVIEW_ADDR = 'personalInfo.overviewAddressType'
    static final String OVERVIEW_PHONE = 'personalInfo.overviewPhoneType'
    static final String DISPLAY_OVERVIEW_ADDR = 'personalInfo.overview.displayOverviewAddress'
    static final String DISPLAY_OVERVIEW_PHONE = 'personalInfo.overview.displayOverviewPhone'
    static final String DISPLAY_OVERVIEW_EMAIL = 'personalInfo.overview.displayOverviewEmail'
    static final String PROFILE_PICTURE = 'personalInfo.overview.displayOverviewPicture'
    static final String PREF_EMAIL = 'personalInfo.prefEmailUpdatability'
    static final String PERS_DETAILS_MODE = 'personalInfo.personalDetailSectionMode'
    static final String EMAIL_MODE = 'personalInfo.emailSectionMode'
    static final String PHONE_MODE = 'personalInfo.phoneSectionMode'
    static final String ADDR_MODE = 'personalInfo.addressSectionMode'
    static final String EMER_MODE = 'personalInfo.emergencyContactSectionMode'
    static final String ETHN_RACE_MODE = 'personalInfo.additionalDetails.ethnicityRaceMode'
    static final String VETERANS_CLASSIFICATION = 'personalInfo.additionalDetails.veteranClassificationMode'
    static final String DISABILITY_STATUS = 'personalInfo.additionalDetails.disabilityStatusMode'
    static final String ADDITIONAL_DETAILS_MODE = 'personalInfo.additionalDetailsSectionMode'
    static final String DIRECTORY_PROFILE = 'personalInfo.enableDirectoryProfile'
    static final String SECURITY_QA_CHANGE = 'personalInfo.enableSecurityQaChange'
    static final String PASSWORD_CHANGE = 'personalInfo.enablePasswordChange'
    static final String GENDER_MODE = 'personalInfo.personalDetail.genderIdentification'
    static final String PRONOUN_MODE = 'personalInfo.personalDetail.personalPronoun'
    static final String LEGAL_SEX_MODE = 'personalInfo.personalDetail.legalSex'
    static final String MARITAL_STATUS_MODE = 'personalInfo.personalDetail.maritalStatus'
    static final String GENDER_KEY = 'gender'
    static final String PRONOUN_KEY = 'pronoun'
    static final String MARITAL_STATUS_KEY = 'maritalStatus'
    static final String LEGAL_SEX_KEY = 'sex'
    static final String NO_OF_QSTNS = 'GUBPPRF_NO_OF_QSTNS'
    static final Integer SECTION_HIDDEN = 0
    static final Integer SECTION_READONLY = 1
    static final Integer SECTION_UPDATEABLE = 2
    static final String YES = 'Y'
    static final String NO = 'N'

    HashMap<String, Integer> fieldDisplayConfigurations = createFieldDisplayConfigurations()

    private static getConfigurationFromDatabase(configurationName) {
        return Holders?.config?.get(configurationName)
    }

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

    protected getFieldDisplayConfigurationsHashMap() {
        updateFieldDisplayConfigurations()
        return fieldDisplayConfigurations
    }

    protected getFieldConfiguration(mode) {
        return fieldDisplayConfigurations.get(mode)
    }

    //If the field is not a valid value, then set the field as default (updatable).
    protected static isFieldUpdateable(field) {
        return (field == SECTION_HIDDEN || field == SECTION_READONLY || field == SECTION_UPDATEABLE) ? (field == SECTION_UPDATEABLE) : true
    }

    //If the field is not a valid value, then set the field as default (displayable).
    protected static isFieldDisplayable(field) {
        return (field == SECTION_HIDDEN || field == SECTION_READONLY || field == SECTION_UPDATEABLE) ? (field == SECTION_READONLY || field == SECTION_UPDATEABLE) : true
    }

    //If field is not a valid value, then set the field as default (enabled).
    protected static isFieldEnabled(field) {
        return (field == SECTION_HIDDEN || field == SECTION_READONLY) ? field == SECTION_READONLY : true
    }

    //If the field is not a valid value, then set the field as default (updatable).
    protected static getMode(field) {
        return (field == SECTION_HIDDEN || field == SECTION_READONLY || field == SECTION_UPDATEABLE) ? field as Integer : 2
    }

    protected updateFieldDisplayConfigurations() {
        fieldDisplayConfigurations = createFieldDisplayConfigurations()
    }

    /**
     * Returns updated map of GUROCFG configurations.
     *
     * Some fields are always updatable if they are enabled, such as preferred email, so they can only be 0 or 1. Other fields can be disabled, read only
     * or updatable, and therefore can be a 0, 1 or a 2.
     *
     * 0: Disabled.
     * 1: Visible and not updatable OR enabled and updatable (depending on the configuration).
     * 2: Visible and updatable.
     *
     * @param model - A map of Personal Information configuration names to configuration values.
     * @return Map - Personal Information Configuration map updated with values retrieved from Holders.
     */
    protected getUpdatedPersonalInformationConfigurations(model) {
        model.isPreferredEmailUpdateable = isFieldEnabled(getConfigurationFromDatabase(PREF_EMAIL))
        model.isProfilePicDisplayable = isFieldEnabled(getConfigurationFromDatabase(PROFILE_PICTURE))
        model.isOverviewAddressDisplayable = isFieldEnabled(getConfigurationFromDatabase(DISPLAY_OVERVIEW_ADDR))
        model.isOverviewPhoneDisplayable = isFieldEnabled(getConfigurationFromDatabase(DISPLAY_OVERVIEW_PHONE))
        model.isOverviewEmailDisplayable = isFieldEnabled(getConfigurationFromDatabase(DISPLAY_OVERVIEW_EMAIL))
        model.isDirectoryProfileDisplayable = isFieldEnabled(getConfigurationFromDatabase(DIRECTORY_PROFILE))
        model.veteranClassificationMode = getMostRestrictiveAdditionalDetailsSetting(getConfigurationFromDatabase(VETERANS_CLASSIFICATION))
        model.isVetClassificationDisplayable = isFieldDisplayable(model.veteranClassificationMode)
        model.isSecurityQandADisplayable = isFieldEnabled(getConfigurationFromDatabase(SECURITY_QA_CHANGE))
        model.isPasswordChangeDisplayable = isFieldEnabled(getConfigurationFromDatabase(PASSWORD_CHANGE))
        model.disabilityStatusMode = getMostRestrictiveAdditionalDetailsSetting(getConfigurationFromDatabase(DISABILITY_STATUS))
        model.isDisabilityStatusDisplayable = isFieldDisplayable(model.disabilityStatusMode)
        model.ethnRaceMode = getMostRestrictiveAdditionalDetailsSetting(getConfigurationFromDatabase(ETHN_RACE_MODE))
        model.emailSectionMode = getMode(getConfigurationFromDatabase(EMAIL_MODE))
        model.telephoneSectionMode = getMode(getConfigurationFromDatabase(PHONE_MODE))
        model.addressSectionMode = getMode(getConfigurationFromDatabase(ADDR_MODE))
        model.emergencyContactSectionMode = getMode(getConfigurationFromDatabase(EMER_MODE))
        model.personalDetailsSectionMode = getMode(getConfigurationFromDatabase(PERS_DETAILS_MODE))
        model.additionalDetailsSectionMode = getAdditionalDetailsSectionMode(model)
        model.personalPronounMode = getFieldConfiguration(PRONOUN_KEY)
        model.maritalStatusMode = getFieldConfiguration(MARITAL_STATUS_KEY)
        model.genderIdentificationMode = getFieldConfiguration(GENDER_KEY)
        model.legalSexMode = getFieldConfiguration(LEGAL_SEX_KEY)
        model
    }

    protected getOtherSectionConfigurations(model, numberOfSecurityQuestions) {
        model.isSecurityQandADisplayable = model.isSecurityQandADisplayable && numberOfSecurityQuestions > 0
        model.otherSectionMode = (model.isDirectoryProfileDisplayable) || (model.isSecurityQandADisplayable) || (model.isPasswordChangeDisplayable)
        model
    }

    /**
     * If the additional details section is more restrictive than the items
     * within the section, the section's configuration is returned. If the individual item is
     * more restrictive, then the item's configuration is returned.
     *
     * @param setting - The configuration value for a field within the Additional Details section.
     * @return Integer
     */
    protected  static getMostRestrictiveAdditionalDetailsSetting(setting) {
        def additionalDetailsSectionMode = getMode(getConfigurationFromDatabase(ADDITIONAL_DETAILS_MODE))
        setting = getMode(setting)
        return setting < additionalDetailsSectionMode ? setting : additionalDetailsSectionMode
    }

    /**
     * If all of the sections within additional details are disabled, the entire section should be hidden regardless of its configuration,
     * because there is nothing to display in the section.
     *
     * If any of the sections within additional details are enabled, the additional details section should get its configuration
     * from GUROCFG.
     *
     * @param model - A map which should contain the following keys; ethnRaceMode, isVetClassificationDisplayable and isDisabilityStatusDisplayable.
     */
    protected static getAdditionalDetailsSectionMode(model) {
        return ((!isFieldDisplayable(model.ethnRaceMode) && !model.isVetClassificationDisplayable && !model.isDisabilityStatusDisplayable) ?
                SECTION_HIDDEN :
                getConfigurationFromDatabase(ADDITIONAL_DETAILS_MODE))
    }

    /**
     * Each phone type or address type contains an identifying code and a priority in which
     * it should appear depending on which of the types the student has listed in personal information.
     *
     * In GUROCFG, these codes and priorities are entered as JSON. After retrieving this JSON from
     * Holders, it will be mapped to a LinkedHashMap.
     *
     * @param String configurationName The configuration name as listed in GUROCFG.
     * @return LinkedHashMap <String, Integer> which contains key to value pairs of a cross-reference code
     * to the priority which these items should appear depending on what information the user has.
     */
    protected LinkedHashMap getSequenceConfiguration(String configurationName) {
        try {
            def mapper = new ObjectMapper()
            return mapper.readValue(getConfigurationFromDatabase(configurationName).toUpperCase(), LinkedHashMap.class) as LinkedHashMap
        }
        catch (JsonMappingException) {
            log.error("The JSON structure for " + configurationName + " was incorrectly entered in the database.")
            return null
        }
    }

    /**
     * @return HashMap<String, Integer>  with updated values from Holders for Personal Information Personal Details configurations.
     */
    private createFieldDisplayConfigurations() {
        return new HashMap<String, Integer>() {
            {
                put(GENDER_KEY, getMode(getConfigurationFromDatabase(GENDER_MODE)))
                put(PRONOUN_KEY, getMode(getConfigurationFromDatabase(PRONOUN_MODE)))
                put(LEGAL_SEX_KEY, getLegalSexMode(getConfigurationFromDatabase(LEGAL_SEX_MODE)))
                put(MARITAL_STATUS_KEY, getMode(getConfigurationFromDatabase(MARITAL_STATUS_MODE)))
            }
        }
    }

    //Can only be set to 0 or 1 and returns an integer
    private getLegalSexMode(legalSexConfiguration) {
        return (legalSexConfiguration == SECTION_HIDDEN || legalSexConfiguration == SECTION_READONLY) ? legalSexConfiguration as Integer : 1
    }
}
