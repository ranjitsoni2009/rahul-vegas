package com.worldfamilyenglish.vegas.fetcher;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.worldfamilyenglish.vegas.service.ParentService;
import com.worldfamilyenglish.vegas.types.ParentInfoFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ParentFetcherTest {

    final MonoGraphQLClient _monoGraphQLClient;

    private ParentService _mockParentService;
    private ParentFetcher _testFetcher;

    public ParentFetcherTest(@LocalServerPort Integer port) {
        String baseUrl = format("http://localhost:%s/graphql", port.toString());
        WebClient webClient = WebClient.create(baseUrl);

        _monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient);
    }

    @BeforeEach
    void setUp() {
        _mockParentService = mock(ParentService.class);
        _testFetcher = new ParentFetcher(_mockParentService);
    }

    @Test
    void GIVEN_filter_is_null_EXPECT_parent_service_is_not_invoked() {

        _testFetcher.parentInfo(null);

        verify(_mockParentService, times(0)).getParent(any());
    }

    @Test
    void GIVEN_valid_external_id_EXPECT_service_is_provided_with_exact_external_id_value() {
        final ArgumentCaptor<String> externalIdCaptor = ArgumentCaptor.forClass(String.class);

        when(_mockParentService.getParent(notNull())).thenReturn(Optional.empty());

        final String externalId = "P1478";

        final ParentInfoFilter filter = ParentInfoFilter
                .newBuilder()
                .externalId(externalId)
                .build();

        _testFetcher.parentInfo(filter);

        verify(_mockParentService).getParent(externalIdCaptor.capture());

        assertThat(externalIdCaptor.getValue()).isEqualTo("P1478");
    }

    @Test
    void GIVEN_valid_parentInfo_query_EXPECT_fetcher_is_able_fetch_response_successfully() {
        final String createParentMutation = """
                mutation {
                 	createParent(input:{
                    uniqueExternalId: "parent_1"
                   	displayName: "Wendy Parent"
                    children: [{
                       uniqueExternalId: "child_1"
                       capLevel: BLUE
                       displayName: "Child one"
                       dateOfBirth: "2020-04-01"
                    }]
                   })  {
                   	id
                   }
                 }
                """;

        _monoGraphQLClient.reactiveExecuteQuery(createParentMutation).block();

        final String parentInfoGraphqlQuery = """
                query {
                  parentInfo(filter : { externalId:"parent_1"}) {
                    id
                    displayName
                    children{
                      id
                      displayName
                      bookings {
                        id
                        status
                      }
                    }
                    bookings {
                      child{
                        id
                        displayName
                      }
                    }
                  }
                }
                """;

        GraphQLResponse response =
                _monoGraphQLClient.reactiveExecuteQuery(parentInfoGraphqlQuery).block();

        String displayName = response.extractValueAsObject("parentInfo.displayName", String.class);

        assertThat(displayName).isEqualTo("Wendy Parent");
    }
}