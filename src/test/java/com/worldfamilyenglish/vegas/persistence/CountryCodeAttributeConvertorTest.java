package com.worldfamilyenglish.vegas.persistence;

import com.neovisionaries.i18n.CountryCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CountryCodeAttributeConvertorTest {

    private CountryCodeAttributeConvertor _testConvertor;

    @BeforeEach
    void setUp() {
        _testConvertor = new CountryCodeAttributeConvertor();
    }

    @Test
    public void GIVEN_string_of_countries_EXPECT_they_are_converted_to_list_of_objects() {

        String countries = "HK, JP, TW, GB, US";

        List<CountryCode> countryCodes = _testConvertor.convertToEntityAttribute(countries);

        assertThat(countryCodes).isNotNull();
        assertThat(countryCodes).hasSize(5);

        assertThat(countryCodes).contains(CountryCode.HK, CountryCode.JP, CountryCode.TW, CountryCode.GB, CountryCode.US);
    }

    @Test
    public void GIVEN_string_without_spacing_only_delimiter_EXPECT_string_is_parsed_normally() {

        String countries = "HK,JP,TW,GB, US";

        List<CountryCode> countryCodes = _testConvertor.convertToEntityAttribute(countries);

        assertThat(countryCodes).isNotNull();
        assertThat(countryCodes).hasSize(5);

        assertThat(countryCodes).contains(CountryCode.HK, CountryCode.JP, CountryCode.TW, CountryCode.GB, CountryCode.US);
    }

    @Test
    public void GIVEN_a_list_of_countries_EXPECT_they_are_converted_to_string() {

        List<CountryCode> countryList = List.of(CountryCode.HK, CountryCode.JP, CountryCode.TW, CountryCode.GB, CountryCode.US);

        String serializedString = _testConvertor.convertToDatabaseColumn(countryList);

        assertThat(serializedString).isEqualTo("HK, JP, TW, GB, US");
    }

    @Test
    public void GIVEN_empty_country_list_EXPECT_empty_string() {


        String countryString = _testConvertor.convertToDatabaseColumn(List.of());

        assertThat(countryString).isNotNull();
        assertThat(countryString).isBlank();
    }

    @Test
    public void GIVEN_null_country_list_EXPECT_empty_string() {

        String countryString = _testConvertor.convertToDatabaseColumn(null);

        assertThat(countryString).isBlank();
    }

    @Test
    public void GIVEN_empty_string_EXPECT_empty_AND_not_null_country_string() {

        String countries = "";

        List<CountryCode> countryCodes = _testConvertor.convertToEntityAttribute(countries);

        assertThat(countryCodes).isNotNull();
        assertThat(countryCodes).isEmpty();
    }

    @Test
    public void GIVEN_null_string_EXPECT_empty_AND_not_null_country_string() {

        List<CountryCode> countryCodes = _testConvertor.convertToEntityAttribute(null);

        assertThat(countryCodes).isNotNull();
        assertThat(countryCodes).isEmpty();
    }
}