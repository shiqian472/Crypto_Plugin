package crypto_plugin.v1.restcontroller;

import crypto_plugin.v1.restcontroller.payload.TestGetRs;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = "test", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController("TestController")
public class TestController {
    private final Logger log = LoggerFactory.getLogger("Crypto_Plugin_Logs");

    @Operation(summary = "測試", description = "測試")
    @GetMapping(value = "/get")
    public TestGetRs get() {
        return new TestGetRs();
    }
}
