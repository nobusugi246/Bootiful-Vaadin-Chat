package proto

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
@EnableScheduling
public class Broadcaster implements Serializable {
    static ExecutorService executorService = Executors.newSingleThreadExecutor()

    public interface BroadcastListener {
        void receiveBroadcast(Message message);
    }

    private static LinkedList<BroadcastListener> listeners = new LinkedList<BroadcastListener>()
    private static Map<BroadcastListener, String> userlist = new ConcurrentHashMap<BroadcastListener, String>()

    public static synchronized void register(BroadcastListener newlistener) {
        listeners.add(newlistener)
        LinkedList<BroadcastListener> removeList = new LinkedList<BroadcastListener>()
        for (final BroadcastListener listener: listeners) {
            if (listener != newlistener && !((VaadinFrontend)listener).getPushConnection().isConnected()) {
                removeList.add(listener)
            }
        }
        for (final BroadcastListener listener: removeList) {
            listeners.remove(listener)
            userlist.remove(listener)
            Message msg = new Message("deluser", "", listener.toString(), "")
            broadcast(msg)
        }
    }

    public static void pushUserList(BroadcastListener targetListener, String username) {
        if (userlist.get(targetListener) != null) {
            userlist.remove(targetListener)
            userlist.put(targetListener, username)
        } else {
            userlist.put(targetListener, username)
        }
        for (final BroadcastListener listener: listeners) {
            Message msg = new Message("adduser", "", listener.toString(), userlist.get(listener))
            broadcast(msg)
        }
    }

    public static synchronized void unregister(BroadcastListener listener) {
        listeners.remove(listener)
    }

    public static synchronized void broadcast(final Message message) {
        for (final BroadcastListener listener: listeners)
            executorService.execute{
                listener.receiveBroadcast(message)
            }
    }

    @Scheduled(fixedRate=1000L)
    public static void cyclicPush(){
        Message msg = new Message('time')
        broadcast(msg)
    }
}
