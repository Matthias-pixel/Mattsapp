import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;
import java.util.UUID;

public class Server {
    private ArrayList<User> users = new ArrayList<>(0);
    private HashMap<Integer, User> sessions = new HashMap<>(0);
    private ArrayList<Chat> chats = new ArrayList<>(0);

    public Server() {
        users.add(new User("User1", Crypto.hashPassword("User1", "password"), false, UUID.randomUUID()));
        users.add(new User("User2", Crypto.hashPassword("User2", "password22"), false, UUID.randomUUID()));
    }
    
    private int generateSessionId() {
        int id;
        do {
            id = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
        } while(sessions.keySet().contains(id));
        return id;
    }
    private boolean userExists(String username) {
        Iterator<User> it = users.iterator();
        while(it.hasNext()) {
            if(it.next().username.equals(username)) 
                return true;
        }
        return false;
    }
    private UUID getUUID(String username) {
        Iterator<User> it = users.iterator();
        while(it.hasNext()) {
            User u = it.next();
            if(u.username.equals(username)) 
                return u.uuid;
        }
        return null;
    }
    private boolean userLoggedIn(UUID uuid) {
        if(uuid == null) {
            return false;
        }
        Iterator<Integer> it = sessions.keySet().iterator();
        while(it.hasNext()) {
            if(sessions.get(it.next()).uuid.equals(uuid))
                return true;
        }
        return false;
    }
    private boolean userLoggedIn(int sessionId) {
        return sessions.keySet().contains(sessionId);
    }
    private User getUser(int sessionId) {
        if(!userLoggedIn(sessionId))
            return null;
        return sessions.get(sessionId);
    }
    private User getUser(UUID uuid) {
        if(uuid == null) {
            return null;
        }
        Iterator<User> it = users.iterator();
        while(it.hasNext()) {
            User u = it.next();
            if(u.uuid.equals(uuid))
                return u;
        }
        return null;
    }
    public UUID getUUID(int sessionId) {
        User u = getUser(sessionId);
        if(u == null)
            return null;
        return u.uuid;
    }
    private Chat getChat(UUID user1, UUID user2) {
        Iterator<Chat> it = chats.iterator();
        while(it.hasNext()) {
            Chat c = it.next();
            if(c.user1.equals(user1) && c.user2.equals(user2) ||
              (c.user1.equals(user2) && c.user2.equals(user1)))
                return c;
        }
        return null;
    }
    private Chat getChat(UUID ucid) {
        Iterator<Chat> it = chats.iterator();
        while(it.hasNext()) {
            Chat c = it.next();
            if(c.ucid.equals(ucid))
                return c;
        }
        return null;
    }
    private boolean isUser1(UUID ucid, UUID uuid) {
        Chat c = getChat(ucid);
        return c.user1.equals(uuid);
    }

    public int login(String username, String password) throws Exception {
        String passwordHash = Crypto.hashPassword(username, password);
        for(int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (!u.username.equals(username))
                continue;
            if(!u.passwordHash.equals(passwordHash)) 
                throw new Exception("Wrong username or password!");
            if(userLoggedIn(u.uuid))
                throw new Exception("User already logged in!");
            int sessionId = generateSessionId();
            sessions.put(sessionId, u);
            return sessionId;
        }
        throw new Exception("Wrong username or password!");
    }    
    public void sendMessage(int from_sessionId, String to_userName, String message) throws Exception {
        UUID to = getUUID(to_userName);
        if(to == null) {
            throw new Exception("User does not exist!");
        }
        UUID from = getUUID(from_sessionId);
        if(from == null)
            throw new Exception("User not logged in!");
        Message msg = new Message(from, to, message, System.currentTimeMillis() / 1000L);
        Chat chat = getChat(from, to);
        if(chat == null) {
            chat = new Chat(UUID.randomUUID(), from, 0, to, 0, new ArrayList<Message>(0));
            chats.add(chat);
        }
        chat.messages.add(msg);
    }
    public UUID[] getChats(int sessionId) throws Exception {
        UUID uuid = getUUID(sessionId);
        if(uuid == null)
            throw new Exception("Invalid session Id!");
        ArrayList<UUID> rchats = new ArrayList<>(0);
        Iterator<Chat> it = chats.iterator();
        while(it.hasNext()) {
            Chat c = it.next();
            if(c.user1.equals(uuid) || c.user2.equals(uuid)) {
                rchats.add(c.ucid);
            }
        }
        return (UUID[])rchats.toArray(new UUID[rchats.size()]);
    }
    public String getChatPartner(int sessionId, UUID ucid) throws Exception {
        UUID uuid = getUUID(sessionId);
        if(uuid == null)
            throw new Exception("Invalid session id!");
        Iterator<Chat> it = chats.iterator();
        while(it.hasNext()) {
            Chat c = it.next();
            if(c.ucid.equals(ucid)) {
                User u;
                if(isUser1(ucid, uuid))
                    u = getUser(c.user2);
                else
                    u = getUser(c.user1);
                if(u == null)
                    throw new Exception("User does not exist!");
                return u.username;
            }
        }
        return null;
    }
    public int getUnreadMessages(int sessionId, UUID ucid) throws Exception {
        Chat c = getChat(ucid);
        if(c == null)
            throw new Exception("Chat does not exist!");
        UUID uuid = getUUID(sessionId);
        if(uuid == null)
            throw new Exception("Invalid session id!");
        if(isUser1(ucid, uuid)) 
            return c.messages.size() - c.read1;
        else
            return c.messages.size() - c.read2;
    }
    public Message[] getMessages(int sessionId, UUID ucid) throws Exception {
        Chat c = getChat(ucid);
        if(c == null)
            throw new Exception("Chat does not exist!");
        UUID uuid = getUUID(sessionId);
        if(uuid == null)
            throw new Exception("Invalid session id!");
        c.sortMessages();
        if(isUser1(ucid, uuid)) {
            c.read1 = c.messages.size();
        } else {
            c.read2 = c.messages.size();
        }
        return (Message[])c.messages.toArray(new Message[c.messages.size()]);
    }
    public void logout(int sessionId) {
        sessions.remove(sessionId);
    }
}