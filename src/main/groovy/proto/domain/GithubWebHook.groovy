package proto.domain

import groovy.transform.ToString

@ToString
class GithubWebHook {
    def action
    def sender
    def ref
    def commits
    def comment
    def issue
    def pullRequest
    def repository
}

