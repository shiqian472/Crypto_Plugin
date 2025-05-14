package crypto_plugin.v1.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import crypto_plugin.util.DataUtils;
import crypto_plugin.util.ToolsUtil;
import crypto_plugin.util.UrlUtils;
import crypto_plugin.v1.model.CoinSupplyData;
import crypto_plugin.v1.service.BotvSingService;
import crypto_plugin.v1.service.LineBotHandlerService;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class BotvSingServiceImpl implements BotvSingService {
    private final Logger log = LoggerFactory.getLogger("Crypto_Plugin_Logs");

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LineBotHandlerService lineBotHandlerService;

    @Override
    public void getCoinSupply() {
        try {
            String response = restTemplate.getForObject(UrlUtils.BOTVSING_BASE_URL, String.class);
            Map<String, BigDecimal> result = objectMapper.readValue(
                    response, new TypeReference<Map<String, BigDecimal>>() {
                    }
            );
            DataUtils.coinSupplyMap.clear();
            DataUtils.coinSupplyMap.putAll(result);
//            log.info(ToolsUtil.getLog("coinSupplyMap: " + DataUtils.coinSupplyMap));
        } catch (Exception ex) {
            log.error(ToolsUtil.getLog("getCoinSupply 發生錯誤: " + ex));
        }
    }

    @Override
    public void processData(StringBuilder dataSB) {
        try {

            JsonNode root = objectMapper.readTree(dataSB.toString());
            JsonNode dataArray = root.get("data");
            log.info(ToolsUtil.getLog("接收資料清單: " + dataArray.size()));
            List<CoinSupplyData> result = new ArrayList<>();
            for (JsonNode node : dataArray) {
                CoinSupplyData data = objectMapper.treeToValue(node, CoinSupplyData.class);

                if (data.aprFunding != null &&
                        data.fundingFee != null &&
                        data.oi != null &&
                        data.volume24h != null &&
                        data.bid != null) {
                    result.add(data);
                }
            }
            log.info(ToolsUtil.getLog("過濾null後清單: " + result.size()));
            List<CoinSupplyData> bearishList = new ArrayList<>();
            List<CoinSupplyData> bullishList = new ArrayList<>();
            result.forEach(x -> {
                String symbol = x.symbol;
                BigDecimal coinSupply = DataUtils.coinSupplyMap.get(symbol);
                if (ObjectUtils.isEmpty(coinSupply) || coinSupply.compareTo(BigDecimal.ZERO) == 0) return;
                BigDecimal marketCap = coinSupply.multiply(x.bid);
                BigDecimal oi = x.oi;
                BigDecimal volume24h = x.volume24h;
                BigDecimal aprFunding = x.aprFunding;
                BigDecimal fundingFee = x.fundingFee;

                BigDecimal oiRatio = oi.divide(marketCap, 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                BigDecimal volumeRatio = volume24h.divide(marketCap, 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

                boolean isBearish = aprFunding.compareTo(BigDecimal.ZERO) < 0 &&
                        fundingFee.compareTo(BigDecimal.ZERO) < 0 &&
                        oiRatio.compareTo(BigDecimal.valueOf(10)) > 0 &&
                        volumeRatio.compareTo(BigDecimal.valueOf(50)) > 0;

                boolean isBullish = aprFunding.compareTo(BigDecimal.ZERO) > 0 &&
                        fundingFee.compareTo(BigDecimal.ZERO) > 0 &&
                        oiRatio.compareTo(BigDecimal.valueOf(10)) > 0;

                if (isBearish) bearishList.add(x);
                if (isBullish) bullishList.add(x);
            });
            List<String> bearishSysbolList = bearishList.stream().map(CoinSupplyData::getSymbol).toList();
            List<String> bullishSysbolList = bullishList.stream().map(CoinSupplyData::getSymbol).toList();
            StringBuilder sendSB = new StringBuilder();
            if (CollectionUtils.isEmpty(bearishList) && CollectionUtils.isEmpty(bullishList)) {
                log.info(ToolsUtil.getLog("沒有多空頭."));
                return;
            }
            sendSB.append(String.format("""
                    %s
                    """, ToolsUtil.getLog("")));

            if (!CollectionUtils.isEmpty(bearishList)) {
                sendSB.append(
                        String.format("""
                                空頭:
                                """));
                bearishList.forEach(x -> {
                    sendSB.append(
                            String.format("""
                                    %s: %s
                                    """, x.symbol, x.bid.toPlainString()));
                });
                log.info(ToolsUtil.getLog("空頭清單: " + bearishSysbolList));
            }
            if (!CollectionUtils.isEmpty(bullishList)) {
                sendSB.append(
                        String.format("""
                                多頭:
                                """));
                bullishList.forEach(x -> {
                    sendSB.append(
                            String.format("""
                                    %s: %s
                                    """, x.symbol, x.bid.toPlainString()));
                });
                log.info(ToolsUtil.getLog("多頭清單: " + bullishSysbolList));
            }
            lineBotHandlerService.send("U6593a8a53cd4e27e1c4ac112f474f18c", sendSB.toString());
            lineBotHandlerService.send("U6ec22835ef1519b371e9002619438116", sendSB.toString());
//            result.forEach(System.out::println);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
