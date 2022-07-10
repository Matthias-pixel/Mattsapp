import java.util.UUID;

public class User {
    public String username;
    public String passwordHash;
    public boolean admin;
    public UUID uuid;
    
    public User(String username, String passwordHash, boolean admin, UUID uuid) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.admin = admin;
        this.uuid = uuid;
    }
}