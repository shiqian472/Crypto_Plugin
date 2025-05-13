package crypto_plugin.v1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinSupplyData {
    public Double bid;
    public Double ask;
    public String exchange;
    public String symbol;
    @JsonProperty("spot_bid")
    public Double spotBid;

    @JsonProperty("spot_ask")
    public Double spotAsk;

    @JsonProperty("funding_fee")
    public Double fundingFee;

    @JsonProperty("apr_funding")
    public Double aprFunding;

    public Double oi;

    @JsonProperty("volume_24h")
    public Double volume24h;

    @Override
    public String toString() {
        return "MarketData{" +
                "symbol='" + symbol + '\'' +
                ", exchange='" + exchange + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", fundingFee=" + fundingFee +
                ", aprFunding=" + aprFunding +
                ", oi=" + oi +
                ", volume24h=" + volume24h +
                '}';
    }
}
