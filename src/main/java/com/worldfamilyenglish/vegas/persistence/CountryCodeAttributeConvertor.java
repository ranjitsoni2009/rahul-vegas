package com.worldfamilyenglish.vegas.persistence;

import com.neovisionaries.i18n.CountryCode;
import org.apache.logging.log4j.util.Strings;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CountryCodeAttributeConvertor implements AttributeConverter<List<CountryCode>, String> {

    public static final String SPLIT_DELIMITER = ",";
    public static final String JOIN_DELIMITER = ", ";

    @Override
    public String convertToDatabaseColumn(final List<CountryCode> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }

        return attribute
                .stream()
                .map(CountryCode::getAlpha2)
                .collect(Collectors.joining(JOIN_DELIMITER));

    }

    @Override
    public List<CountryCode> convertToEntityAttribute(final String dbData) {
        if (Strings.isEmpty(dbData)) {
            return List.of();
        }

        return Arrays
                .stream(dbData.split(SPLIT_DELIMITER))
                .map(String::trim)
                .map(CountryCode::getByCode)
                .toList();
    }
}
