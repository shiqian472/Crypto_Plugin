package crypto_plugin.v1.service.impl;

import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.PushMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import crypto_plugin.util.ToolsUtil;
import crypto_plugin.v1.service.LineBotHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class LineBotHandlerServiceImpl implements LineBotHandlerService {
    private final Logger log = LoggerFactory.getLogger("LineBot_Logs");

    @Autowired
    MessagingApiClient messagingApiClient;

    @Override
    public void send(String userId, String message) {
        TextMessage textMessage = new TextMessage(message);
        PushMessageRequest pushMessage = new PushMessageRequest(userId, List.of(textMessage), null, null);

        try {
            UUID uuid = UUID.randomUUID();
            messagingApiClient.pushMessage(uuid, pushMessage).get();
            log.info(ToolsUtil.getLog("Message sent successfully!"));
        } catch (InterruptedException | ExecutionException e) {
            log.info(ToolsUtil.getLog("Error while sending message: " + e));
        }
    }
}
