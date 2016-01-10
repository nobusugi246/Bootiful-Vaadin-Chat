package proto.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import proto.Broadcaster
import proto.Message
import proto.MessageRepository
import proto.domain.GithubWebHook

@Service
class WebHookService {
    @Autowired
    private MessageRepository repository;

    def handleGithubHook(String json) {
        def slurper = new groovy.json.JsonSlurper()
        def content = slurper.parseText(json)

        def hook = new GithubWebHook(action: content.action,
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
