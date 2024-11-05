package com.zemnitskiy.parser;

import com.zemnitskiy.Main;
import com.zemnitskiy.api.LeonApiClient;
import com.zemnitskiy.visitor.ResultPrinter;
import com.zemnitskiy.visitor.ResultVisitor;
import com.zemnitskiy.request.RootRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code LeonParser} class orchestrates the process of fetching and processing sports-related data
 * using the {@link LeonApiClient}. It initiates a root request for the current disciplines, processes
 * the fetched results with a {@link ResultVisitor}, and handles any exceptions that occur during processing.
 */
public class LeonParser {

    private static final Logger logger = LoggerFactory.getLogger(LeonParser.class);

    private final LeonApiClient apiClient;

    /**
     * Constructs a new {@code LeonParser} with the specified {@link LeonApiClient}.
     *
     * @param apiClient the API client used to communicate with the Leonbets API
     */
    public LeonParser(LeonApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Executes the data fetching and processing workflow. It sends a root request for the current disciplines,
     * processes the results using a {@link ResultPrinter}, and logs any errors that occur.
     */
    public void processData() {
        RootRequest rootRequest = new RootRequest(apiClient, Main.CURRENT_DISCIPLINES);
        rootRequest.fetch()
                .thenAccept(rootResults -> {
                    ResultVisitor printer = new ResultPrinter();
                    rootResults.accept(printer);
                })
                .exceptionally(e -> {
                    logger.error("Error during processing: {}", e.getMessage(), e);
                    return null;
                }).join();
    }

}