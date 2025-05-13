package crypto_plugin.v1.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import crypto_plugin.util.DataUtils;
import crypto_plugin.util.ToolsUtil;
import crypto_plugin.util.UrlUtils;
import crypto_plugin.v1.model.CoinSupplyData;
import crypto_plugin.v1.service.BotvSingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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
            log.info(ToolsUtil.getLog("coinSupplyMap: " + DataUtils.coinSupplyMap));
        } catch (Exception ex) {
            log.error(ToolsUtil.getLog("getCoinSupply 發生錯誤: " + ex));
        }
    }

    @Override
    public void processData(StringBuilder dataSB) {
        try {

            JsonNode root = objectMapper.readTree(dataSB.toString());
            JsonNode dataArray = root.get("data");
            List<CoinSupplyData> result = new ArrayList<>();
            for (JsonNode node : dataArray) {
                CoinSupplyData data = objectMapper.treeToValue(node, CoinSupplyData.class);

                if (data.aprFunding != null &&
                        data.fundingFee != null &&
                        data.oi != null &&
                        data.volume24h != null) {
                    result.add(data);
                }
            }
            result.forEach(System.out::println);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
