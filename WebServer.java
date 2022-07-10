import java.net.ServerSocket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.Arrays;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class WebServer {
    Server server;
    boolean quit;
    ServerSocket serverSocket;
    String mmkPrivate = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC686x6Yn0c0wbPwjw3jPc2he3V6X8dh7VhyqzejhzEMBFpWd+wxRQ+G+LZ/1wTG8esOVOqOrvUQuKQXv1SRhgHoIPPI5sREJdXHNfVLOBkrymB3v5P1FDH/501E0habjixgMdUdbCADgX75u/DeOfam1F/BWBPJriztkBY/QyigVTk6hjXrgYKmvaJpYBQqfOW0vgtwxNur7NQGLM9jb3yFXLqiPzaH07KxDqd+Dg7rj6aXLhuvQtIrnQcwe5FSEOpI1We5s986i/ee+3xHJiBQfPjRv7o0smVrfR2Tsd9CIX2VGb/cq4m0FITPKC2V75JMLBxGWpUsEs337za1g3fAgMBAAECggEATZgPsWgpZ4pK3HZQyfmvLake0tcsFFScOu+FGoY/eEo/H8vWpSbXRQQL/tEXJTM1h8zvZcsk5G+yW5uhZsGC/b9oldPAa6/IMLbe+jK7G+77ulSUlwuXT7tPQ5UyRZ5T15CMqwiB7lRjtW7fevE2wiwPAjp4A8IdjTGKuaSl1thDOic/dX8kdZlJd0SMkvT+SCnP4JnoZiRe+yWOOObyS+aM7VRBLpMHZVDC9fDh32AKf5MTlxN1liHVPPXgqthE9LR0haTCThT3QaKIKQHq/DSWsB9MrhzK1TC0Wh0ylQGeae+8OG7VpH2ICCIVdAmBmD3IxbYu5shYFGADzpSVkQKBgQDoD5rlbcCwWpUNKTicKryzzfr3mF4TU0itNGI9GkUPmqXdXsXqCZbrCEecrxDF2cFSdH+WUM88hZZ5tROWvxZMrRj39C6f82Nj3xciQbsI6WuRDac5UzDyOkuQXxp+pQ5nklWWalGv3Z8Pr8et1PcDYDz2eFPh/TLubH9grnKedwKBgQDOPM1iJNULVJWJ5uoVWKYiXOwRKaBbtTmOXw5JDYXrVDf23HQsOh4UHfbb93hPImPKNFyD6NayRw5/UwzTncpKXEvu3+3SZNaaPkOVrWKYouZeHz/v7C8/kdyrHs2xizTJGV7un0iFQuw7J37fTbG6bq9Lv/4xZj6W8NoOsWfd2QKBgF8I3P9uRgYy4m2kXvmdiu/7v/ab4JL7fXzpQf4R/wxX4etZf6XLzZFtL56cFztfZQt2iDWv3F7Fuo0XOfF3ShOWo+4PgvSgQSjKWPf/gTAyH4ok1EHmrtnUQ9PZDsmkoNSLmh9iZRenpBdRfm3TFGG+jNjHCFs5TR1VM4kNiAUbAoGASTZabqnGm3QWddLsbFGBAJfeEaWmUIf/+6OQ7awIS2clStQtxXmILfoJJUMviWAa7rAx233voXAKHHb+Ca1tH4KuTgleYp77c9wAoATiCG9juiv7xY8hor6ECJcxzAIv5efExI707qG4PLOjnV/2ZMlgH1LzyNOUCn9L2YVu6tkCgYBdom5UDQaC+eECRvHHxFWUuIAoCrY58KowBgVEWKRClzIaGImq8bvNfwMCI6elDkbbslI46bdl2Qpu0MLmZazu8qMnuC4oegiw3jhCQoLXZs9axhERAp0C+tzPht78CjSFsSe77eHj+G/LSkCpMGDOzMPS7xr+uLKB+SGoZKU5gg=="; //Demo key. Do not use!
    HashMap<Integer, String> encryptionSessions = new HashMap<>();
    
    public WebServer(Server server) {
        this.server = server;
        main();
    }
    private String decrypt(String input, int id) {
        byte[] encodedKey = Util.decode64(Crypto.decodeAesBundleKey(encryptionSessions.get(id)));
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        IvParameterSpec iv = new IvParameterSpec(Util.decode64(Crypto.decodeAesBundleIv(encryptionSessions.get(id))));
        String res = Crypto.decryptAes(input, key, iv);
        System.out.println(String.format("Decrypting %s -> %s", input, res));
        return res;
    }
    private String encrypt(String input, int id) {
        byte[] encodedKey = Util.decode64(Crypto.decodeAesBundleKey(encryptionSessions.get(id)));
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        IvParameterSpec iv = new IvParameterSpec(Util.decode64(Crypto.decodeAesBundleIv(encryptionSessions.get(id))));
        String res = Crypto.encryptAes(input, key, iv);
        System.out.println(String.format("Encrypting %s -> %s", input, res));
        return res;
    }
    private void out(ObjectOutputStream output, int id, String[] data) throws java.io.IOException {
        String[] clone = new String[data.length];
        for(int i = 0; i < clone.length; i++) {
            clone[i] = encrypt(data[i], id);
        }
        output.writeObject(clone);
    }
    public void main() {
        boolean quit = false;
        try {
            serverSocket = new ServerSocket(4141);
            System.out.println("Server listening on port 4141! (Version 3)");
            while(!quit) {
                Socket socket = serverSocket.accept();
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                String[] args = (String[])input.readObject();
                int encryptionSession = Integer.parseInt(args[0]);
                String cmd = args[1];
                /*System.out.println(String.format("encrypted(args):"));
                for(int i = 1; i < args.length; i++) {
                    System.out.println(String.format("  %s", args[i]));
                }
                System.out.println();*/
                if(encryptionSession != -1) {
                    if(!encryptionSessions.keySet().contains(encryptionSession))
                        throw new Exception("Invalid encryption id!");
                    for(int i = 1; i < args.length; i++) {
                        args[i] = decrypt(args[i], encryptionSession);
                        if(i == 1) {
                            System.out.println(String.format("%s:", args[i]));
                        } else {
                            System.out.println(String.format("  %s", args[i]));
                        }
                    }
                    cmd = args[1];
                    args = Arrays.copyOfRange(args, 2, args.length);
                    System.out.println();
                }
                try {
                    switch(cmd) {
                        case "login":
                            out(output, encryptionSession, new String[]{Integer.toString(server.login(args[0], args[1]))});
                            break;
                        case "sendMessage":
                            server.sendMessage(Integer.parseInt(args[0]), args[1], args[2]);
                            out(output, encryptionSession, new String[0]);
                            break;
                        case "listChats":
                            UUID[] chats = server.getChats(Integer.parseInt(args[0]));
                            String[] chats_s = new String[chats.length];
                            for(int i = 0; i < chats.length; i++) {
                                chats_s[i] = chats[i].toString();
                            }
                            out(output, encryptionSession, chats_s);
                            break;
                        case "getChatPartner":
                            String chatPartner = server.getChatPartner(Integer.parseInt(args[0]), UUID.fromString(args[1]));
                            out(output, encryptionSession, new String[]{chatPartner});
                            break;
                        case "getUUID":
                            UUID uuid = server.getUUID(Integer.parseInt(args[0]));
                            out(output, encryptionSession, new String[]{uuid.toString()});
                            break;
                        case "getUnreadMessages":
                            int unreadMessages = server.getUnreadMessages(Integer.parseInt(args[0]), UUID.fromString(args[1]));
                            out(output, encryptionSession, new String[]{Integer.toString(unreadMessages)});
                            break;
                        case "getMessages":
                            Message[] messages = server.getMessages(Integer.parseInt(args[0]), UUID.fromString(args[1]));
                            String[] resp = new String[messages.length*4];
                            for(int i = 0; i < messages.length; i++) {
                                resp[4*i] = messages[i].from.toString();
                                resp[4*i+1] = messages[i].to.toString();
                                resp[4*i+2] = Long.toString(messages[i].creation);
                                resp[4*i+3] = messages[i].message;
                            }
                            out(output, encryptionSession, resp);
                            break;
                        case "logout":
                            server.logout(Integer.parseInt(args[0]));
                            out(output, encryptionSession, new String[0]);
                            break;
                        case "_keyExchange":
                            String encryptedAesBundle = args[2];
                            String encodedAesBundle = Crypto.decryptRsa(encryptedAesBundle, Crypto.decodePrivateKey(mmkPrivate));
                            int id;
                            do {
                                id = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
                            } while(encryptionSessions.keySet().contains(encryptionSession));
                            encryptionSessions.put(id, encodedAesBundle);
                            output.writeObject(new String[]{Integer.toString(id)});
                            System.out.println(String.format("Encryption session: %d", id));
                            System.out.println(String.format("Aes bundle: %s", encodedAesBundle));
                            break;
                    }
                } catch(Exception e) {
                    out(output, encryptionSession, new String[]{"Exception", e.getMessage()});
                }
            }
            serverSocket.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
