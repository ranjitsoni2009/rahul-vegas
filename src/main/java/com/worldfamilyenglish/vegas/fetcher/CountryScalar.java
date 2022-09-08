package com.worldfamilyenglish.vegas.fetcher;

import com.neovisionaries.i18n.CountryCode;
import com.netflix.graphql.dgs.DgsScalar;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.jetbrains.annotations.NotNull;

@DgsScalar(name = "CountryCode")
public class CountryScalar implements Coercing<CountryCode, String> {

    @Override
    public String serialize(@NotNull final Object dataFetcherResult) throws CoercingSerializeException {
        if (!(dataFetcherResult instanceof CountryCode)) {
            throw new CoercingSerializeException("Object '%s' is not a type of CountryCode".formatted(dataFetcherResult));
        }

        return ((CountryCode) dataFetcherResult).getAlpha2();
    }

    @Override
    public @NotNull CountryCode parseValue(@NotNull final Object input) throws CoercingParseValueException {
        return CountryCode.getByAlpha2Code(input.toString());
    }

    @Override
    public @NotNull CountryCode parseLiteral(@NotNull final Object input) throws CoercingParseLiteralException {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException();
        }

        String countryCodeAsString = ((StringValue) input).getValue();
        return CountryCode.getByAlpha2Code(countryCodeAsString);
    }
}
