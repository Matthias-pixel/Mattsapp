import java.util.UUID;
import java.util.Arrays;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    int sessionId;
    Server server;
    boolean web = false;
    String ip = "localhost";
    Socket soc;
    String mmkPublic = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuvOsemJ9HNMGz8I8N4z3NoXt1el/HYe1Ycqs3o4cxDARaVnfsMUUPhvi2f9cExvHrDlTqjq71ELikF79UkYYB6CDzyObERCXVxzX1SzgZK8pgd7+T9RQx/+dNRNIWm44sYDHVHWwgA4F++bvw3jn2ptRfwVgTya4s7ZAWP0MooFU5OoY164GCpr2iaWAUKnzltL4LcMTbq+zUBizPY298hVy6oj82h9OysQ6nfg4O64+mly4br0LSK50HMHuRUhDqSNVnubPfOov3nvt8RyYgUHz40b+6NLJla30dk7HfQiF9lRm/3KuJtBSEzygtle+STCwcRlqVLBLN9+82tYN3wIDAQAB"; //Demo key. Do not use!
    int encryptionSession;
    String aesBundle;
    
    public Client(Server server, String username, String password) throws Exception {
        if(username == null || password == null) {
            throw new RuntimeException();
        }
        if(server == null) {
            this.web = true;
            keyExchange();
            sessionId = Integer.parseInt(sendToServer(new String[]{"login", username, password})[0]);
        } else {
            this.server = server;
            sessionId = server.login(username, password);
        }
    }
    public Client(Server server, String username, String password, String ip) throws Exception {
        if(username == null || password == null) {
            throw new RuntimeException();
        }
        if(ip == null) {
            ip = "localhost";
        }
        if(server == null) {
            this.web = true;
            this.ip = ip;
            keyExchange();
            sessionId = Integer.parseInt(sendToServer(new String[]{"login", username, password})[0]);
        } else {
            this.server = server;
            sessionId = server.login(username, password);
        }
    }
    private String decrypt(String input) {
        byte[] encodedKey = Util.decode64(Crypto.decodeAesBundleKey(aesBundle));
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        IvParameterSpec iv = new IvParameterSpec(Util.decode64(Crypto.decodeAesBundleIv(aesBundle)));
        return Crypto.decryptAes(input, key, iv);
    }
    private String encrypt(String input) {
        byte[] encodedKey = Util.decode64(Crypto.decodeAesBundleKey(aesBundle));
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        IvParameterSpec iv = new IvParameterSpec(Util.decode64(Crypto.decodeAesBundleIv(aesBundle)));
        return Crypto.encryptAes(input, key, iv);
    }
    private void connect() {
        try {
            soc = new Socket(this.ip, 4141);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    private void keyExchange() {
        connect();
        ObjectOutputStream os = null;
        ObjectInputStream is = null;
        try {
            os = new ObjectOutputStream(soc.getOutputStream());
            // Create AES key
            SecretKey key = Crypto.generateAesKey();
            IvParameterSpec iv = Crypto.generateIVector();
            byte[] encodedKey = key.getEncoded();
            byte[] encodedIv = iv.getIV();
            aesBundle = Crypto.encodeAesBundle(Util.encode64(encodedKey), Util.encode64(encodedIv));
            String encryptedAesBundle = Crypto.encryptRsa(aesBundle, Crypto.decodePublicKey(mmkPublic));
            os.writeObject(new String[]{Integer.toString(-1), "_keyExchange", encryptedAesBundle});
            is = new ObjectInputStream(soc.getInputStream());
            encryptionSession = Integer.parseInt(((String[]) is.readObject())[0]);
            //System.out.println(String.format("Encryption session: %d", encryptionSession));
            //System.out.println(String.format("Aes bundle: %s", aesBundle));
            os.close();
            is.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(String to, String message) throws Exception {
        if(this.web) {
            sendToServer(new String[]{"sendMessage", Integer.toString(sessionId), to, message});
        } else {
            server.sendMessage(sessionId, to, message);
        }
    }
    public UUID[] listChats() throws Exception {
        if(this.web) {
            String[] resp = sendToServer(new String[]{"listChats", Integer.toString(sessionId)});
            UUID[] res = new UUID[resp.length];
            for(int i = 0; i < resp.length; i++) {
                res[i] = UUID.fromString(resp[i]);
            }
            return res;
        } else {
            return server.getChats(sessionId);
        }
    }
    public String getChatPartner(UUID ucid) throws Exception {
        if(this.web) {
            return sendToServer(new String[]{"getChatPartner", Integer.toString(sessionId), ucid.toString()})[0];
        } else {
            return server.getChatPartner(sessionId, ucid);
        }
    }
    public int getUnreadMessages(UUID ucid) throws Exception {
        if(this.web) {
            return Integer.parseInt(sendToServer(new String[]{"getUnreadMessages", Integer.toString(sessionId), ucid.toString()})[0]);
        } else {
            return server.getUnreadMessages(sessionId, ucid);
        }
    }
    public Message[] getMessages(UUID ucid) throws Exception {
        if(this.web) {
            String[] res = sendToServer(new String[]{"getMessages", Integer.toString(sessionId), ucid.toString()});
            int count = res.length/4;
            Message[] messages = new Message[count];
            for(int i = 0; i < res.length; i+=4) {
                UUID from = UUID.fromString(res[i+0]);
                UUID to = UUID.fromString(res[i+1]);
                long creation = Long.parseLong(res[i+2]);
                String message = res[i+3];
                messages[i/4] = new Message(from, to, message, creation);
            }
            return messages;
        } else {
            return server.getMessages(sessionId, ucid);
        }
    }
    public UUID getUUID() throws Exception {
        if(this.web) {
            return UUID.fromString(sendToServer(new String[]{"getUUID", Integer.toString(sessionId)})[0]);
        } else {
            return server.getUUID(sessionId);
        }
    }
    public String[] sendToServer(String[] args) throws Exception {
        String[] output = new String[0];
        connect();
        ObjectOutputStream os = null;
        ObjectInputStream is = null;
        /*System.out.println(String.format("decrypted(args):"));
        for(int i = 0; i < args.length; i++) {
            System.out.println(String.format("  %s", args[i]));
        }
        System.out.println();*/
        String[] data = new String[args.length+1];
        data[0] = Integer.toString(encryptionSession);
        for(int i = 1; i < data.length; i++) {
            data[i] = encrypt(args[i-1]);
        }
        /*System.out.println(String.format("encrypted(args):"));
        for(int i = 1; i < data.length; i++) {
            System.out.println(String.format("  %s", data[i]));
        }
        System.out.println();*/
        try {
            os = new ObjectOutputStream(soc.getOutputStream());
            os.writeObject(data);
            os.flush();
            is = new ObjectInputStream(soc.getInputStream());
            output = (String[]) is.readObject();
        } catch(Exception e) {
            e.printStackTrace();
        }
        for(int i = 0; i < output.length; i++) {
            output[i] = decrypt(output[i]);
        }
        if(output.length > 0 && output[0].equals("Exception")) {
            is.close();
            os.close();
            soc.close();
            throw new Exception(output[1]);
        }
        is.close();
        os.close();
        soc.close();
        return output;
    }
    public void logout() throws Exception {
        if(this.web) {
            sendToServer(new String[]{"logout", Integer.toString(sessionId)});
        } else {
            server.logout(sessionId);
        }
    }
}
