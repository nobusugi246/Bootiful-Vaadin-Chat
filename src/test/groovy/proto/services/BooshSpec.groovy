package proto.services

class BooshSpec extends spock.lang.Specification {
  def "Boosh"() {
    when:
    def boosh = new Boosh()

    then:
    boosh.execute('test test'.split(' ')) == 2
  }
}
