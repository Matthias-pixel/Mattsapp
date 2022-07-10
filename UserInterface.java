import java.util.Scanner;
import java.util.UUID;
import java.util.Arrays;

public class UserInterface {
    private static int screenWidth = 80;
    private static int screenHeight = 50;
    Client client;
    Server server;
    String chatPartner;
    UUID[] chats;
    UUID chat;
    String state = "login";
    String error = "";
    String consoleClearSequence = "\u000C";
    String ip = "";
    
    public UserInterface(Server server) {
        this.server = server;
        cmdLoop();
    }
    public UserInterface(Server server, String consoleClearSequence) {
        this.consoleClearSequence = consoleClearSequence;
        this.server = server;
        cmdLoop();
    }
    public UserInterface(String ip) {
        this.server = null;
        this.ip = ip;
        cmdLoop();
    }
    public UserInterface(String ip, String consoleClearSequence) {
        this.consoleClearSequence = consoleClearSequence;
        this.server = null;
        this.ip = ip;
        cmdLoop();
    }
    
    private static void printLines(int n) {
        for(int i = 0; i < n; i++) {
            System.out.println("");
        }
    }
    private static void printLine(int length, char c) {
        for(int i = 0; i < length; i++) {
            System.out.print(c);
        }
        //System.out.println();
    }
    private static void printLinePlus(int length, char c, char d) {
        System.out.print(d);
        for(int i = 0; i < length-2; i++) {
            System.out.print(c);
        }
        System.out.println(d);
    }
    private void clearScreen() {
        System.out.print(consoleClearSequence); 
    }
    private void updateChats() {
        try
        {
            chats = client.listChats();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void printLeft(String line) {
        System.out.print("| ");
        System.out.print(line);
        printLine(screenWidth-3-line.length(), ' ');
        System.out.println('|');
    }
    private void printRight(String line) {
        System.out.print("| ");
        printLine(screenWidth-3-line.length(), ' ');
        System.out.print(line);
        System.out.println('|');
    }
    private void printLeftRight(String left, String right) {
        System.out.print("| ");
        System.out.print(left);
        printLine(screenWidth-3-left.length()-right.length()-1, ' ');
        System.out.print(right);
        System.out.println(" |");
    }
    private void printCenter(String line) {
        System.out.print("| ");
        printLine((screenWidth-3-line.length())/2, ' ');
        System.out.print(line);
        printLine((screenWidth-3-line.length())/2, ' ');
        if((screenWidth-3-line.length())%2 != 0)
            System.out.print(' ');
        System.out.println('|');
    }
    private void printErrors() {
        if(!error.equals("")) {
            printLeft(error);
            printLinePlus(screenWidth, '-', '+');
        }
    }

    private void printHomeScreen() {
        clearScreen();
        try {
            updateChats();
            printCenter("HOME");
            printLinePlus(screenWidth, '-', '+');
            if(chats.length == 0) {
                printCenter("You currently have no chats.");
            } else {
                for(int i = 0; i < chats.length; i++) {
                    String partner = client.getChatPartner(chats[i]);
                    int unread = client.getUnreadMessages(chats[i]);
                    if(unread == 0)
                        printLeft(partner);
                    else
                        printLeftRight(partner, String.format("(%s)", unread));
                }
            }
            printLinePlus(screenWidth, '-', '+');
            if(chats.length == 0) {
                printLeft("Use write <recipient> <message> to send your first message.");
            } else {
                printLeft("Use enter <username> to enter a chat.");
            }
            printLinePlus(screenWidth, '-', '+');
            printErrors();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void printLoginScreen() {
        clearScreen();
        printCenter("LOGIN");
        printLinePlus(screenWidth, '-', '+');
        printCenter("Log in to see yout chats.");
        printLinePlus(screenWidth, '-', '+');
        printLeft("Use login <username> <password> to login.");
        printLinePlus(screenWidth, '-', '+');
        printErrors();
    }
    private void printChatScreen() {
        try {
            clearScreen();
            printCenter(String.format("Chat with %s", chatPartner).toUpperCase());
            printLinePlus(screenWidth, '-', '+');
            UUID id = client.getUUID();
            Message[] messages = client.getMessages(chat);
            if(messages.length == 0) {
                printCenter("Empty chat. Use write <message> or w <message> to write your first message!");
            }
            for(int i = 0; i < messages.length; i++) {
                Message msg = messages[i];
                if(msg.from.equals(id)) {
                    printRight(String.format("[You] %s", msg.message));
                } else {
                    printLeft(String.format("[%s] %s", chatPartner, msg.message));
                }
            }
            printLinePlus(screenWidth, '-', '+');
            printErrors();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update() {
        switch(state) {
            case "login":
                printLoginScreen();
                break;
            case "home":
                printHomeScreen();
                break;
            case "chat":
                printChatScreen();
        }
    }
    private void drawLoop() {
        while(!state.equals("quit")) {
            update();
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }
    }
    private void cmdLoop() {
        Scanner s = new Scanner(System.in);
        while(!state.equals("quit")) {
            update();
            error = "";
            String l = s.nextLine();
            String[] args = l.split(" ");
            String cmd = args[0];
            try {
                if(state.equals("login")) {
                    switch(cmd) {
                        case "quit":
                            state = "quit";
                        case "login":
                            client = new Client(server, args[1], args[2], ip);
                            state = "home";
                            break;
                        case "":
                            break;
                        default:
                            error = "Unknown command in this context.";
                            break;
                    }
                } else if(state.equals("home")) {
                    switch(cmd) {
                        case "quit":
                            client.logout();
                            state = "quit";
                        case "w":
                        case "write":
                            client.sendMessage(args[1], String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                        case "enter":
                            updateChats();
                            int i;
                            for(i = 0; i < chats.length; i++) {
                                String partner = client.getChatPartner(chats[i]);
                                if(partner.equals(args[1])) {
                                    chat = chats[i];
                                    chatPartner = partner;
                                    state = "chat";
                                    break;
                                }
                            }
                            if(!(i < chats.length))
                                error = "This chat does not exist.";
                            break;
                        case "exit":
                        case "logout":
                            client.logout();
                            state = "login";
                            break;
                        case "":
                            break;
                        default:
                            error = "Unknown command in this context.";
                            break;
                    }
                } else if(state.equals("chat")) {
                    switch(cmd) {
                        case "quit":
                            client.logout();
                            state = "quit";
                        case "w":
                        case "write":
                            client.sendMessage(chatPartner, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                            break;
                        case "exit":
                        case "home":
                            state = "home";
                            break;
                        case "":
                            break;
                        default:
                            System.out.println("Unknown command or use enter <username> to enter a chat.");
                            break;
                    }
                }
            } catch (Exception e) {
                error = "An error occoured: \"" + e.getMessage() + "\" Please try again.";
            }
        }
        update();
        s.close();
    }
}
