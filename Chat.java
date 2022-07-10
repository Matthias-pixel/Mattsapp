import java.util.ArrayList;
import java.util.UUID;
import java.util.Collections;
import java.util.Comparator;


public class Chat {
    private class MessageComparator implements Comparator<Message> {
        public int compare(Message o1, Message o2) {
            return (int)(o1.creation - o2.creation);
        }
    }
    public ArrayList<Message> messages;
    public UUID ucid;
    public UUID user1;
    public int read1;
    public UUID user2;
    public int read2;
    
    public Chat(UUID ucid, UUID user1, int read1, UUID user2, int read2, ArrayList<Message> messages) {
        this.ucid = ucid;
        this.user1 = user1;
        this.read1 = read1;
        this.user2 = user2;
        this.read2 = read2;
        this.messages = messages;
    }
    
    public void sortMessages() {
        Collections.sort(messages, new MessageComparator());
    }

}
