package crypto_plugin.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ToolsUtil {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final String getLog(String log) {
        return String.format("%s %s", LocalDateTime.now().format(ToolsUtil.formatter), log);
    }

}
