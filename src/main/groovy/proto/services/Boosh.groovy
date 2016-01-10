package proto.services

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.crsh.plugin.PluginLifeCycle
import org.crsh.shell.Shell
import org.crsh.shell.ShellFactory

@Slf4j
class Boosh {
    @Autowired
    final PluginLifeCycle crshBootstrapBean

    Shell shell
  
    def execute(String[] args) {
        log.info "args: ${args}"
        return args.size()
    }

}

