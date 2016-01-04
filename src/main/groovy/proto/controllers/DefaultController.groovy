package proto.controllers

import proto.Broadcaster
import proto.domain.HookRequest
import proto.Message
import proto.MessageRepository

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestBody

@Slf4j
@RestController
class DefaultController {

  @Autowired
  private MessageRepository repository;

  @RequestMapping(path="/webhook", consumes="application/x-www-form-urlencoded")
  def receiveHook(@RequestBody String body) {
    def slurper = new groovy.json.JsonSlurper()
    def json = URLDecoder.decode(body).substring(8)
    log.info "${json}"
    def content = slurper.parseText(json)

    def hook = new HookRequest(action: content.action,
                               sender: content.sender,
                               comment: content.comment,
                               ref: content.ref,
                               issue: content.issue,
                               pullRequest: content.pull_request,
                               commits: content.commits,
                               repository: content.repository)

    def msg = new Message('fix')
    msg.setUsername(hook.sender.login)
    if(content.comment != null && content.issue != null) { // Issue/Pull Request commented on.
      msg.setText("${hook.ref} - ${hook.repository.name}")
    } else if(content.issue != null && content.number != null) { // Issue opened, closed.
    } else if(content.pull_request != null) { // Pull Request opened, closed, or synchronized.
    } else if(content.issue != null && content.number != null) { // Git push to a repository. 
    }
    
    Broadcaster.broadcast(msg)
    repository.save(msg);
  }
}

