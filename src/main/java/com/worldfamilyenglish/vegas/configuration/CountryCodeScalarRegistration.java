package com.worldfamilyenglish.vegas.configuration;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsRuntimeWiring;
import com.worldfamilyenglish.vegas.fetcher.CountryScalar;
import graphql.Scalars;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;

@DgsComponent
public class CountryCodeScalarRegistration {

    @DgsRuntimeWiring
    public RuntimeWiring.Builder addScalar(RuntimeWiring.Builder builder) {
        GraphQLScalarType scalarConfiguration = new GraphQLScalarType
                .Builder()
                .name("CountryCode")
                .description("Adds ISO country code scalars")
                .coercing(new CountryScalar()).build();

        return builder.scalar(scalarConfiguration);
    }
}
