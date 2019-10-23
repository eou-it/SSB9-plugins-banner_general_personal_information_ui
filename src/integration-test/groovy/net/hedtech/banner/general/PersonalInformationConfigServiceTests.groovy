/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general


import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Rollback
class PersonalInformationConfigServiceTests extends BaseIntegrationTestCase {

    def personalInformationConfigService

    @Before
    public void setUp() {
        setHoldersConfig()
        personalInformationConfigService = new PersonalInformationConfigService()
        formContext = ['GUAGMNU', 'SELFSERVICE']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testGetUpdatedPersonalInformationConfigurations() {
        def personalInfoConfig = personalInformationConfigService.getUpdatedPersonalInformationConfigurations([:])
        checkThatConfigurationsAreDefaultSettings(personalInfoConfig)
    }

    //When configurations are invalid, configs should be the least restrictive settings to provide the full form as provided.
    @Test
    void testGetUpdatedPersonalInformationConfigurationsWithNullConfigurationSettings() {
        setConfigurationsInHoldersToNull()
        def personalInfoConfig = personalInformationConfigService.getUpdatedPersonalInformationConfigurations([:])
        checkThatConfigurationsAreDefaultSettings(personalInfoConfig)
    }

    //When configurations are missing, configs should be the least restrictive settings to provide the full form as provided.
    @Test
    void testGetUpdatedPersonalInformationConfigurationsWithNoConfigurationsInHolders() {
        def backupConfig = Holders.config
        Holders.config = null
        assertNull Holders?.config?.'personalInfo.prefEmailUpdatability'
        def personalInfoConfig = personalInformationConfigService.getUpdatedPersonalInformationConfigurations([:])
        checkThatConfigurationsAreDefaultSettings(personalInfoConfig)
        Holders.config = backupConfig
    }

    private def checkThatConfigurationsAreDefaultSettings(personalInfoConfig) {
        assertTrue personalInfoConfig.isPreferredEmailUpdateable
        assertTrue personalInfoConfig.isProfilePicDisplayable
        assertTrue personalInfoConfig.isOverviewAddressDisplayable
        assertTrue personalInfoConfig.isOverviewPhoneDisplayable
        assertTrue personalInfoConfig.isOverviewEmailDisplayable
        assertTrue personalInfoConfig.isDirectoryProfileDisplayable
        assertTrue personalInfoConfig.isVetClassificationDisplayable
        assertTrue personalInfoConfig.isSecurityQandADisplayable
        assertTrue personalInfoConfig.isPasswordChangeDisplayable
        assertTrue personalInfoConfig.isDisabilityStatusDisplayable
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.veteranClassificationMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.disabilityStatusMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.ethnRaceMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.emailSectionMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.telephoneSectionMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.addressSectionMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.emergencyContactSectionMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.personalDetailsSectionMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.maritalStatusMode
        assertEquals personalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig.genderIdentificationMode
        assertEquals personalInformationConfigService.SECTION_READONLY, personalInfoConfig.legalSexMode
        assertNull personalInfoConfig.get(personalInformationConfigService.OVERVIEW_PHONE)
        assertNull personalInfoConfig.get(personalInformationConfigService.OVERVIEW_ADDR)
    }

    @Test
    void testGetOtherSectionConfigurations(){
        def configurations = personalInformationConfigService.getUpdatedPersonalInformationConfigurations([:])
        configurations = personalInformationConfigService.getOtherSectionConfigurations(configurations, 1)
        assertTrue configurations.isSecurityQandADisplayable
        assertTrue configurations.otherSectionMode
    }

    @Test
    void testOtherSectionAppearsWithOnlyOneOtherConfigurationEnabled(){
        Holders?.config?.'personalInfo.enableDirectoryProfile' = 0
        Holders?.config?.'personalInfo.enableSecurityQaChange' = 1
        Holders?.config?.'personalInfo.enablePasswordChange' = 0
        def configurations = personalInformationConfigService.getUpdatedPersonalInformationConfigurations([:])
        configurations = personalInformationConfigService.getOtherSectionConfigurations(configurations, 1)
        assertTrue configurations.isSecurityQandADisplayable
        assertTrue configurations.otherSectionMode
    }

    @Test
    void testOtherSectionHiddenWhenFieldsAreDisabled(){
        Holders?.config?.'personalInfo.enableDirectoryProfile' = 0
        Holders?.config?.'personalInfo.enableSecurityQaChange' = 0
        Holders?.config?.'personalInfo.enablePasswordChange' = 0
        def configurations = personalInformationConfigService.getUpdatedPersonalInformationConfigurations([:])
        configurations = personalInformationConfigService.getOtherSectionConfigurations(configurations, 0)
        assertFalse configurations.isSecurityQandADisplayable
        assertFalse configurations.otherSectionMode
    }

    @Test
    void testGetParamFromSessionWithNoPreexistingConfig() {
        def val = personalInformationConfigService.getParamFromSession('personalInfo.prefEmailUpdatability', 'dummy_default_value')

        assertEquals PersonalInformationConfigService.SECTION_READONLY, val
    }

    @Test
    void testGetMostRestrictiveAdditionalDetailsSetting() {
        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = 2
        Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode' = 2

        assertEquals 2, personalInformationConfigService.getMostRestrictiveAdditionalDetailsSetting(Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode')

        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = 2
        Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode' = 1

        assertEquals 1, personalInformationConfigService.getMostRestrictiveAdditionalDetailsSetting(Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode')

        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = 1
        Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode' = 2

        assertEquals 1, personalInformationConfigService.getMostRestrictiveAdditionalDetailsSetting(Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode')

        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = 1
        Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode' = 0

        assertEquals 0, personalInformationConfigService.getMostRestrictiveAdditionalDetailsSetting(Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode')

        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = null
        Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode' = 0

        assertEquals 0, personalInformationConfigService.getMostRestrictiveAdditionalDetailsSetting(Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode')
    }

    @Test
    void testGetAdditionalDetailsSectionMode() {
        def model = [:]

        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = 2
        model.isVetClassificationDisplayable = 0
        model.isDisabilityStatusDisplayable = 0
        model.ethnRaceMode = 0

        assertEquals 0, personalInformationConfigService.getAdditionalDetailsSectionMode(model)

        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = 2
        model.isVetClassificationDisplayable = 1
        model.isDisabilityStatusDisplayable = 1
        model.ethnRaceMode = 1

        assertEquals 2, personalInformationConfigService.getAdditionalDetailsSectionMode(model)
    }

    @Test
    void testGetSequenceConfiguration() {
        def validJson = "{\"CO\": 2, \"MA\": 1}"
        def invalidJson = "\"{]\""
        def nullJson = null

        Holders?.config?.'personalInfo.overviewAddressType' = validJson

        def validJsonHashMap = personalInformationConfigService.getSequenceConfiguration('personalInfo.overviewAddressType')
        assertEquals 1, validJsonHashMap.get("MA")

        Holders?.config?.'personalInfo.overviewAddressType' = invalidJson

        assertNull personalInformationConfigService.getSequenceConfiguration('personalInfo.overviewAddressType')

        Holders?.config?.'personalInfo.overviewAddressType' = nullJson

        assertNull personalInformationConfigService.getSequenceConfiguration('personalInfo.overviewAddressType')
    }

    private static def setHoldersConfig() {
        Holders?.config?.'personalInfo.prefEmailUpdatability' = 1
        Holders?.config?.'personalInfo.overview.displayOverviewPicture' = 1
        Holders?.config?.'personalInfo.overview.displayOverviewAddress' = 1
        Holders?.config?.'personalInfo.overview.displayOverviewPhone' = 1
        Holders?.config?.'personalInfo.overview.displayOverviewEmail' = 1
        Holders?.config?.'personalInfo.enableDirectoryProfile' = 1
        Holders?.config?.'personalInfo.additionalDetails.veteranClassificationMode' = 2
        Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode' = 2
        Holders?.config?.'personalInfo.additionalDetails.ethnicityRaceMode' = 2
        Holders?.config?.'personalInfo.enableSecurityQaChange' = 1
        Holders?.config?.'personalInfo.enablePasswordChange' = 1
        Holders?.config?.'personalInfo.emailSectionMode' = 2
        Holders?.config?.'personalInfo.phoneSectionMode' = 2
        Holders?.config?.'personalInfo.addressSectionMode' = 2
        Holders?.config?.'personalInfo.emergencyContactSectionMode' = 2
        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = 2
        Holders?.config?.'personalInfo.personalDetailSectionMode' = 2
        Holders?.config?.'personalInfo.personalDetail.genderIdentification' = 2
        Holders?.config?.'personalInfo.personalDetail.personalPronoun' = 2
        Holders?.config?.'personalInfo.personalDetail.legalSex' = 1
        Holders?.config?.'personalInfo.personalDetail.maritalStatus' = 2
    }

    private static def setConfigurationsInHoldersToNull() {
        Holders?.config?.'personalInfo.prefEmailUpdatability' = null
        Holders?.config?.'personalInfo.overview.displayOverviewPicture' = null
        Holders?.config?.'personalInfo.overview.displayOverviewAddress' = null
        Holders?.config?.'personalInfo.overview.displayOverviewPhone' = null
        Holders?.config?.'personalInfo.overview.displayOverviewEmail' = null
        Holders?.config?.'personalInfo.enableDirectoryProfile' = null
        Holders?.config?.'personalInfo.additionalDetails.veteranClassificationMode' = null
        Holders?.config?.'personalInfo.additionalDetails.disabilityStatusMode' = null
        Holders?.config?.'personalInfo.additionalDetails.ethnicityRaceMode' = null
        Holders?.config?.'personalInfo.enableSecurityQaChange' = null
        Holders?.config?.'personalInfo.enablePasswordChange' = null
        Holders?.config?.'personalInfo.emailSectionMode' = null
        Holders?.config?.'personalInfo.phoneSectionMode' = null
        Holders?.config?.'personalInfo.addressSectionMode' = null
        Holders?.config?.'personalInfo.emergencyContactSectionMode' = null
        Holders?.config?.'personalInfo.additionalDetailsSectionMode' = null
        Holders?.config?.'personalInfo.personalDetailSectionMode' = null
        Holders?.config?.'personalInfo.personalDetail.genderIdentification' = null
        Holders?.config?.'personalInfo.personalDetail.personalPronoun' = null
        Holders?.config?.'personalInfo.personalDetail.legalSex' = null
        Holders?.config?.'personalInfo.personalDetail.maritalStatus' = null
        Holders?.config?.'personalInfo.overviewAddressType' = null
        Holders?.config?.'personalInfo.overviewAddressType' = null
    }
}
