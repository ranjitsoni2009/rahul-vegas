package com.worldfamilyenglish.vegas.handler;

import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
        final DataFetcherExceptionHandlerParameters handlerParameters) {

        Throwable exception = handlerParameters.getException();

        if (exception instanceof IllegalArgumentException) {
            return handleDataFetcherException(handlerParameters, exception);
        }

        if (exception instanceof Exception) {
            return handleDataFetcherException(handlerParameters, exception);
        }

        return DataFetcherExceptionHandler.super.handleException(handlerParameters);
    }

    @NotNull
    private static CompletableFuture<DataFetcherExceptionHandlerResult> handleDataFetcherException(DataFetcherExceptionHandlerParameters handlerParameters, Throwable exception) {
        TypedGraphQLError graphError = TypedGraphQLError.newBuilder()
            .message(exception.getMessage())
            .path(handlerParameters.getPath())
            .errorType(ErrorType.BAD_REQUEST)
            .build();

        return CompletableFuture.completedFuture(
                DataFetcherExceptionHandlerResult.newResult()
                        .error(graphError)
                        .build()
        );
    }
}
