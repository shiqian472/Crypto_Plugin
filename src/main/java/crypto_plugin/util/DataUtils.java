package crypto_plugin.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataUtils {
    public static final Map<String, BigDecimal> coinSupplyMap = new ConcurrentHashMap<>();
}
