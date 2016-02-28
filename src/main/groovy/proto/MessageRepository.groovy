package proto

import org.springframework.data.jpa.repository.JpaRepository

public interface MessageRepository extends JpaRepository<Message, String> {
    public List<Message> findByDate(String date)
}
