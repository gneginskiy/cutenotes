package util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Password protection of a note: AES-256-GCM with a key derived by PBKDF2-HMAC-SHA256 from the
 * password and a random salt. The stored text is {@code cutenotes:enc:v1:<salt>:<iv>:<data>}
 * (Base64); GCM also detects a wrong password or a damaged file.
 */
public final class NoteCrypto {

  public static final String PREFIX = "cutenotes:enc:v1:";

  static final int ITERATIONS = 210_000;
  private static final int KEY_BITS = 256;
  private static final int SALT_BYTES = 16;
  private static final int IV_BYTES = 12;
  private static final int TAG_BITS = 128;
  private static final SecureRandom RANDOM = new SecureRandom();

  /** A derived key together with the salt it was derived with. */
  public record Key(SecretKey secret, byte[] salt) {}

  private NoteCrypto() {}

  public static boolean isEncrypted(String content) {
    return content != null && content.startsWith(PREFIX);
  }

  /** A new key for a password, with a fresh random salt. */
  public static Key newKey(char[] password) {
    byte[] salt = new byte[SALT_BYTES];
    RANDOM.nextBytes(salt);
    return new Key(derive(password, salt), salt);
  }

  /** The key that decrypts {@code stored} with {@code password} (its salt is read from it). */
  public static Key keyFor(char[] password, String stored) {
    return new Key(derive(password, part(stored, 0)), part(stored, 0));
  }

  public static String encrypt(String plain, Key key) {
    byte[] iv = new byte[IV_BYTES];
    RANDOM.nextBytes(iv);
    byte[] data =
        run(Cipher.ENCRYPT_MODE, key.secret(), iv, plain.getBytes(StandardCharsets.UTF_8));
    Base64.Encoder b64 = Base64.getEncoder();
    return PREFIX
        + b64.encodeToString(key.salt())
        + ":"
        + b64.encodeToString(iv)
        + ":"
        + b64.encodeToString(data);
  }

  /** The plain text; throws {@link IllegalArgumentException} for a wrong key or damaged text. */
  public static String decrypt(String stored, Key key) {
    byte[] plain = run(Cipher.DECRYPT_MODE, key.secret(), part(stored, 1), part(stored, 2));
    return new String(plain, StandardCharsets.UTF_8);
  }

  private static byte[] run(int mode, SecretKey key, byte[] iv, byte[] input) {
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(mode, key, new GCMParameterSpec(TAG_BITS, iv));
      return cipher.doFinal(input);
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException("wrong password or damaged note", e);
    }
  }

  private static SecretKey derive(char[] password, byte[] salt) {
    try {
      PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
      byte[] raw =
          SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
      spec.clearPassword();
      return new SecretKeySpec(raw, "AES");
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  private static byte[] part(String stored, int index) {
    if (!isEncrypted(stored)) {
      throw new IllegalArgumentException("not an encrypted note");
    }
    String[] parts = stored.substring(PREFIX.length()).split(":");
    if (parts.length != 3) {
      throw new IllegalArgumentException("damaged note");
    }
    try {
      return Base64.getDecoder().decode(parts[index].trim());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("damaged note", e);
    }
  }
}
