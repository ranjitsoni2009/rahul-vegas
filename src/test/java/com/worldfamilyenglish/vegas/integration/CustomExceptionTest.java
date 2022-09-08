package com.worldfamilyenglish.vegas.integration;

import com.netflix.graphql.dgs.client.GraphQLError;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CustomExceptionTest {

    final MonoGraphQLClient _monoGraphQLClient;

    public CustomExceptionTest(@LocalServerPort final Integer port) {
        WebClient webClient = WebClient.create("http://localhost:" + port + "/graphql");
        _monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient);
    }

    @Test
    void GIVEN_email_already_exists_EXPECT_error_with_proper_message_is_shown() {
        String createDomainUserMutation = """
            mutation {
                createUser(input: {
                    givenName: "Peter"
                    familyName: "Borough"
                    email: "example@worldfamilyenglish"
                    canTeach: true
                    canAdmin: true
                }) {
                    id
                }
            }
            """;

        _monoGraphQLClient.reactiveExecuteQuery(createDomainUserMutation).block();

        assertThat(_monoGraphQLClient.reactiveExecuteQuery(createDomainUserMutation).block()).isNotNull();
        assertThat(_monoGraphQLClient.reactiveExecuteQuery(createDomainUserMutation).block().getErrors()).isNotNull();

        GraphQLError error = _monoGraphQLClient.reactiveExecuteQuery(createDomainUserMutation).block().getErrors().get(0);

        assertThat(error.getMessage()).isEqualTo("User already exists with same email address: 'example@worldfamilyenglish'.");
    }

}
