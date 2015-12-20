package proto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Message {

    @Id
    @GeneratedValue
    private Long id;

    private String status = "";
    private String sendto = "";
    private String text = "";
    private String username = "";

    private String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    private String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

    public Message(String status, String sendto, String text, String username) {
        this.status = status;
        this.sendto = sendto;
        this.text = text;
        this.username = username;
    }

    public Message(String status) {
        this.status = status;
    }

    public Message() {
    }

}
