package net.sf.l2j.loginserver.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import net.sf.l2j.loginserver.clientpackets.ClientBasePacket;

public class BlowFishKey extends ClientBasePacket
{
  byte[] _key;
  protected static final Logger _log = Logger.getLogger(BlowFishKey.class.getName());

  public BlowFishKey(byte[] decrypt, RSAPrivateKey privateKey)
  {
    super(decrypt);
    int size = readD();
    byte[] tempKey = readB(size);
    try
    {
      Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
      rsaCipher.init(2, privateKey);
      byte[] tempDecryptKey = rsaCipher.doFinal(tempKey);

      int i = 0;
      int len = tempDecryptKey.length;
      for (; i < len; i++)
      {
        if (tempDecryptKey[i] != 0)
          break;
      }
      _key = new byte[len - i];
      System.arraycopy(tempDecryptKey, i, _key, 0, len - i);
    }
    catch (GeneralSecurityException e)
    {
      _log.severe("Error While decrypting blowfish key (RSA)");
      e.printStackTrace();
    }
  }

  public byte[] getKey()
  {
    return _key;
  }
}