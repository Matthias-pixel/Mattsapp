import java.util.Scanner;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public abstract class MainClient {
    public static void main(String[] args) {
        main1();   
    }
    public static void main1() {
        try {
            new UserInterface("localhost", "\033[H\033[2J");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
