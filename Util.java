import java.util.Base64;

public abstract class Util {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String encode16(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static String encode64(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }
    public static byte[] decode64(String input) {
        return Base64.getDecoder().decode(input);
    }    
}
