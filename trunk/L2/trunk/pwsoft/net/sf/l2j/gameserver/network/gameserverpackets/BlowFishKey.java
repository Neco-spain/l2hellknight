package net.sf.l2j.gameserver.network.gameserverpackets;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;
import javax.crypto.Cipher;

public class BlowFishKey extends GameServerBasePacket
{
  private static Logger _log = Logger.getLogger(BlowFishKey.class.getName());

  public BlowFishKey(byte[] blowfishKey, RSAPublicKey publicKey)
  {
    writeC(174);
    byte[] encrypted = null;
    try
    {
      Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
      rsaCipher.init(1, publicKey);
      encrypted = rsaCipher.doFinal(blowfishKey);
    }
    catch (GeneralSecurityException e)
    {
      _log.severe("Error While encrypting blowfish key for transmision (Crypt error)");
      e.printStackTrace();
    }
    writeD(encrypted.length);
    writeB(encrypted);
  }

  public byte[] getContent()
    throws IOException
  {
    return getBytes();
  }
}