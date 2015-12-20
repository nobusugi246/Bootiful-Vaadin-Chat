package proto;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Item;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Component
@Push
@Theme("valo")
@Title("Bootiful Vaadin Chat")
@SpringUI
public class VaadinFrontend extends UI implements Broadcaster.BroadcastListener {

    @Autowired
    private MessageRepository repository;

    final String userNameErrorMessage = "Please set your name here.";

    InlineDateField idf = new InlineDateField();

    Table table = new Table("Connected Users");

    TextArea upper = new TextArea("Chat Area 1");
    TextArea lower = new TextArea("Chat Area 2");

    TextField serverTime = new TextField("TIME", "00:00:00");

    ComboBox stcb = new ComboBox("Send to");
    TextField username = new TextField("Your Name", "");
    TextField chatMessage = new TextField("Chat Message");

    @Override
    protected void init(VaadinRequest request) {
        HorizontalLayout rootLayout = new HorizontalLayout();
        setContent(rootLayout);
        rootLayout.setSizeFull();

        HorizontalSplitPanel hsp = new HorizontalSplitPanel();
        hsp.setSplitPosition(32, Unit.PERCENTAGE);
        rootLayout.addComponent(hsp);

        VerticalLayout leftLayout = new VerticalLayout();
        VerticalLayout rightLayout = new VerticalLayout();
        leftLayout.setSpacing(true);
        leftLayout.setMargin(true);
        rightLayout.setSizeFull();
        rightLayout.setSpacing(true);

        hsp.setFirstComponent(leftLayout);
        hsp.setSecondComponent(rightLayout);

        idf.setImmediate(true);
        idf.setTimeZone(TimeZone.getTimeZone("JPN"));
        idf.setLocale(Locale.JAPAN);
        idf.setSizeFull();
        leftLayout.addComponent(idf);

        FormLayout fl = new FormLayout();
        fl.addStyleName("outlined");
        fl.setSizeFull();
        fl.setSpacing(true);
        fl.setMargin(false);

        serverTime.setIcon(FontAwesome.CLOCK_O);
        serverTime.setWidth(70.0f, Unit.PERCENTAGE);
        serverTime.setEnabled(false);
        fl.addComponent(serverTime);

        username.setIcon(FontAwesome.USER);
        username.setWidth(100.0f, Unit.PERCENTAGE);
        username.setEnabled(true);
        username.setInputPrompt(userNameErrorMessage);
        username.setComponentError(new UserError(userNameErrorMessage));
        fl.addComponent(username);

        stcb.setIcon(FontAwesome.MAIL_FORWARD);
        stcb.addItem(0);
        stcb.setItemCaption(0, "Chat Area 1");
        stcb.addItem(1);
        stcb.setItemCaption(1, "Chat Area 2");
        stcb.setNullSelectionAllowed(false);
        stcb.setValue(0);
        stcb.setWidth(100.0f, Unit.PERCENTAGE);
        stcb.setNewItemsAllowed(false);
        stcb.setImmediate(false);
        fl.addComponent(stcb);

        leftLayout.addComponent(fl);

        chatMessage.setImmediate(true);
        chatMessage.setIcon(FontAwesome.PENCIL);
        chatMessage.setInputPrompt("Please enter chat message...");
        chatMessage.setSizeFull();
        leftLayout.addComponent(chatMessage);

        table.setIcon(FontAwesome.USERS);
        table.setHeight("200px");
        table.setWidth(100.0f, Unit.PERCENTAGE);
        table.addContainerProperty("User",  String.class, null);
        leftLayout.addComponent(table);

        upper.setIcon(FontAwesome.COMMENT);
        upper.setSizeFull();
        rightLayout.addComponent(upper);

        lower.setIcon(FontAwesome.COMMENT_O);
        lower.setSizeFull();
        rightLayout.addComponent(lower);

        setListeners();

        String user = username.getValue();
        if (user.equals("")) {
            user = this.getCurrent().toString();
        }
        Broadcaster.register(this);

        for (final Message msg : repository.findByDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))){
            appendMessage(msg);
        }
    }

    private void setListeners() {
    	idf.addValueChangeListener(
    		event -> {
    			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    			String date = df.format(((Date)event.getProperty().getValue()));
    			upper.setValue("");
    			lower.setValue("");
    	        for (final Message msg : repository.findByDate(date)){
    	            appendMessage(msg);
    	        }
    		}
    	);
    	
        username.addTextChangeListener(
                event -> {
                    String value = event.getText();
                    if (!value.equals("")) {
                        username.setComponentError(null);
                        Broadcaster.pushUserList(this, value);
                    } else {
                        username.setComponentError(new UserError(userNameErrorMessage));
                    }
                }
        );

        chatMessage.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
        chatMessage.addTextChangeListener(
                event -> {
                    String user = username.getValue();
                    if (user.equals("")) {
                        user = this.getCurrent().toString();
                    }
                    Message msg = new Message("temp", stcb.getValue().toString(), event.getText(), user);
                    Broadcaster.broadcast(msg);
                }
        );

        chatMessage.addValueChangeListener(
                event -> {
                    if (event.getProperty().getValue().toString().equals("")) return;
                    String user = username.getValue();
                    if (user.equals("")) {
                        user = this.getCurrent().toString();
                    }
                    Message msg = new Message("fix", stcb.getValue().toString(), event.getProperty().getValue().toString(), user);
                    Broadcaster.broadcast(msg);
                    chatMessage.setValue("");

                    repository.save(msg);
                }
        );
    }

    private void appendMessage(Message msg){
        if (msg.getText().equals("")) return;
        TextArea target = upper;
        if (msg.getSendto().equals("1")) {
            target = lower;
        }
        String tmp = target.getValue();
        target.setValue(msg.getTime() + " [" + msg.getUsername() + "] " + msg.getText() + "\n" + tmp);
    }

    @Override
    public void receiveBroadcast(Message msg) {
        access(() -> {
            switch (msg.getStatus()) {
                case "time":
                    serverTime.setValue(msg.getTime());
                    break;
                case "temp": {
                    if (msg.getText().equals("")) break;
                    String target = "Chat Area 1";
                    if (msg.getSendto().equals("1")) {
                        target = "Chat Area 2";
                    }
                    Notification.show(msg.getUsername() + " / " + target,
                            msg.getText(), Notification.Type.HUMANIZED_MESSAGE);
                    break;
                }
                case "fix": {
                    appendMessage(msg);
                    break;
                }
                case "adduser": {
                    Item item = table.getItem(msg.getText());
                    if (item == null) {
                        item = table.addItem(msg.getText());
                    }
                    item.getItemProperty("User").setValue(msg.getUsername());
                    break;
                }
                case "deluser": {
                    Item item = table.getItem(msg.getText());
                    if (item != null) {
                        table.removeItem(msg.getText());
                    }
                    break;
                }
            }
        });
    }

    @PreDestroy
    private void destroy(){
        Broadcaster.unregister(this);
    }
}
