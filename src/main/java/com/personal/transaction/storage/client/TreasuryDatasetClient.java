package com.personal.transaction.storage.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.transaction.storage.exceptions.CurrencyExchangeNotAvailableException;
import com.personal.transaction.storage.model.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class TreasuryDatasetClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("TREASURY_CLIENT");

    private static final String TREASURY_EXTERNAL_URI =
            "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?" +
            "fields=country_currency_desc,exchange_rate,record_date&" +
            "filter=country_currency_desc:in:%s,record_date:gte:%s&" +
            "sort=-record_date";

    private static HttpClient client = HttpClient.newHttpClient();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Cacheable("rateRequests")
    public ExchangeRate getExchangeRate(String recordDate, String countryCurrency) {
        ExchangeRate obtainedRates;

        LOGGER.info("Making request to external API for country-currency={} record_date={}", countryCurrency, recordDate);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(TREASURY_EXTERNAL_URI, countryCurrency, recordDate)))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Status code from external API: " + response.statusCode());

            obtainedRates = OBJECT_MAPPER.readValue(response.body(), ExchangeRate.class);
            LOGGER.info("Response body deserialized is: " + obtainedRates);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error when sending request to external API.", e);
            throw new CurrencyExchangeNotAvailableException(502, "Error obtaining exchange currency.");
        }

        return obtainedRates;
    }
}
