package proto.controllers

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import proto.services.WebHookService

@Slf4j
@RestController
class DefaultController {

    @Autowired
    private WebHookService webHookService;

    @RequestMapping(path="/webhook")
    def receiveGithubHook(@RequestBody String body) {
        def json = URLDecoder.decode(body, 'UTF-8')
        log.info "${json}"
        webHookService.handleGithubHook(json)
    }
}

