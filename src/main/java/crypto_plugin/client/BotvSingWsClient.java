package crypto_plugin.client;

import crypto_plugin.schedule.ConfigScheduler;
import crypto_plugin.util.ToolsUtil;
import crypto_plugin.v1.service.BotvSingService;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.zip.InflaterInputStream;

@ClientEndpoint
@Component
public class BotvSingWsClient {
    private final Logger log = LoggerFactory.getLogger("BotvSingWsClient_Logs");
    private Session session;
    private final String uri = "wss://botvsing.com/ws";
    @Autowired
    private BotvSingService botvSingService;

    public void connect() {
        new Thread(() -> {
            while (session == null || !session.isOpen()) {
                try {
                    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                    container.setDefaultMaxBinaryMessageBufferSize(1024 * 512);
                    container.setDefaultMaxTextMessageBufferSize(1024 * 512);
                    container.connectToServer(this, URI.create(uri));
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log.error(ToolsUtil.getLog("WebSocket 連線失敗: " + ex));
                }

            }
        }).start();

    }

    @OnOpen
    public void onOpen(Session session) {
        log.info(ToolsUtil.getLog("WebSocket 已連線."));
        this.session = session;

        // 初始訂閱
        try {
            String initMessage = """
                    {"type":"set_min_oi","value":1,"compression":true,"incremental":true}
                    """;
            session.getAsyncRemote().sendText(initMessage);
            log.info(ToolsUtil.getLog("已送出訊息: " + initMessage));
        } catch (Exception ex) {
            log.error(ToolsUtil.getLog("傳送訊息失敗: " + ex.getMessage()));
        }
    }

    @OnMessage
    public void onBinaryMessage(byte[] message) {
        try {
            InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(message));
            byte[] buffer = new byte[8192];
            int len;
            StringBuilder sb = new StringBuilder();
            while ((len = inflater.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, len));
            }
//            log.info(ToolsUtil.getLog("解壓完成."));
//            log.info(ToolsUtil.getLog(sb.toString()));
            botvSingService.processData(sb);
        } catch (Exception ex) {
            log.error(ToolsUtil.getLog("解壓失敗: " + ex.getMessage()));
        }
    }

    @OnMessage
    public void onTextMessage(String message) {

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info(ToolsUtil.getLog("WebSocket 已關閉."));
        this.session = null;
        // 自動重連
        connect();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error(ToolsUtil.getLog("WebSocket 錯誤: " + throwable.getMessage()));
    }

}
