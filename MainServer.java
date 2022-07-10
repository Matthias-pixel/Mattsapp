import java.util.Scanner;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public abstract class MainServer {
    public static void main(String[] args) {
        main1();   
    }
    
    public static void main1() {
        new WebServer(new Server());
    }
}
