import java.util.UUID;

public class Message {
    public String message;
    public long creation;
    public UUID from;
    public UUID to;
    
    public Message(UUID from, UUID to, String message, long creation) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.creation = creation;
    }
}