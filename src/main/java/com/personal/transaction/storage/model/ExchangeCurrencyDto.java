package com.personal.transaction.storage.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExchangeCurrencyDto {
    @JsonProperty("record_date")
    private String recordDate;

    @JsonProperty("country")
    private String country;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("country_currency_desc")
    private String countryCurrencyDescription;

    @JsonProperty("exchange_rate")
    private String exchangeRate;
}
