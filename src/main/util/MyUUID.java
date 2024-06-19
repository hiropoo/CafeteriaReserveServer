package main.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class MyUUID {

    // Base64エンコードされたUUIDを生成
    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return encodeBase64(uuid);
    }

    // UUIDをBase64エンコードする処理
    static private String encodeBase64(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        byte[] uuidBytes = byteBuffer.array();

        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes);
        return encoded;
    }
}