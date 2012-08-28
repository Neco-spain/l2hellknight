package net.sf.l2j.util.crypt;

import java.io.IOException;
import java.util.logging.Logger;
import net.sf.l2j.util.log.AbstractLogger;

public class NewCrypt
{
  protected static Logger _log = AbstractLogger.getLogger(NewCrypt.class.getName());
  BlowfishEngine _crypt;
  BlowfishEngine _decrypt;

  public NewCrypt(byte[] blowfishKey)
  {
    _crypt = new BlowfishEngine();
    _crypt.init(true, blowfishKey);
    _decrypt = new BlowfishEngine();
    _decrypt.init(false, blowfishKey);
  }

  public NewCrypt(String key)
  {
    this(key.getBytes());
  }

  public static boolean verifyChecksum(byte[] raw)
  {
    return verifyChecksum(raw, 0, raw.length);
  }

  public static boolean verifyChecksum(byte[] raw, int offset, int size)
  {
    if (((size & 0x3) != 0) || (size <= 4))
    {
      return false;
    }

    long chksum = 0L;
    int count = size - 4;
    long check = -1L;

    for (int i = offset; i < count; i += 4)
    {
      check = raw[i] & 0xFF;
      check |= raw[(i + 1)] << 8 & 0xFF00;
      check |= raw[(i + 2)] << 16 & 0xFF0000;
      check |= raw[(i + 3)] << 24 & 0xFF000000;

      chksum ^= check;
    }

    check = raw[i] & 0xFF;
    check |= raw[(i + 1)] << 8 & 0xFF00;
    check |= raw[(i + 2)] << 16 & 0xFF0000;
    check |= raw[(i + 3)] << 24 & 0xFF000000;

    return check == chksum;
  }

  public static void appendChecksum(byte[] raw)
  {
    appendChecksum(raw, 0, raw.length);
  }

  public static void appendChecksum(byte[] raw, int offset, int size)
  {
    long chksum = 0L;
    int count = size - 4;

    for (int i = offset; i < count; i += 4)
    {
      long ecx = raw[i] & 0xFF;
      ecx |= raw[(i + 1)] << 8 & 0xFF00;
      ecx |= raw[(i + 2)] << 16 & 0xFF0000;
      ecx |= raw[(i + 3)] << 24 & 0xFF000000;

      chksum ^= ecx;
    }

    long ecx = raw[i] & 0xFF;
    ecx |= raw[(i + 1)] << 8 & 0xFF00;
    ecx |= raw[(i + 2)] << 16 & 0xFF0000;
    ecx |= raw[(i + 3)] << 24 & 0xFF000000;

    raw[i] = (byte)(int)(chksum & 0xFF);
    raw[(i + 1)] = (byte)(int)(chksum >> 8 & 0xFF);
    raw[(i + 2)] = (byte)(int)(chksum >> 16 & 0xFF);
    raw[(i + 3)] = (byte)(int)(chksum >> 24 & 0xFF);
  }

  public static void encXORPass(byte[] raw, int key)
  {
    encXORPass(raw, 0, raw.length, key);
  }

  public static void encXORPass(byte[] raw, int offset, int size, int key)
  {
    int stop = size - 8;
    int pos = 4 + offset;

    int ecx = key;

    while (pos < stop)
    {
      int edx = raw[pos] & 0xFF;
      edx |= (raw[(pos + 1)] & 0xFF) << 8;
      edx |= (raw[(pos + 2)] & 0xFF) << 16;
      edx |= (raw[(pos + 3)] & 0xFF) << 24;

      ecx += edx;

      edx ^= ecx;

      raw[(pos++)] = (byte)(edx & 0xFF);
      raw[(pos++)] = (byte)(edx >> 8 & 0xFF);
      raw[(pos++)] = (byte)(edx >> 16 & 0xFF);
      raw[(pos++)] = (byte)(edx >> 24 & 0xFF);
    }

    raw[(pos++)] = (byte)(ecx & 0xFF);
    raw[(pos++)] = (byte)(ecx >> 8 & 0xFF);
    raw[(pos++)] = (byte)(ecx >> 16 & 0xFF);
    raw[(pos++)] = (byte)(ecx >> 24 & 0xFF);
  }

  public byte[] decrypt(byte[] raw)
    throws IOException
  {
    byte[] result = new byte[raw.length];
    int count = raw.length / 8;

    for (int i = 0; i < count; i++)
    {
      _decrypt.processBlock(raw, i * 8, result, i * 8);
    }

    return result;
  }

  public void decrypt(byte[] raw, int offset, int size) throws IOException
  {
    byte[] result = new byte[size];
    int count = size / 8;

    for (int i = 0; i < count; i++)
    {
      _decrypt.processBlock(raw, offset + i * 8, result, i * 8);
    }

    System.arraycopy(result, 0, raw, offset, size);
  }

  public byte[] crypt(byte[] raw) throws IOException
  {
    int count = raw.length / 8;
    byte[] result = new byte[raw.length];

    for (int i = 0; i < count; i++)
    {
      _crypt.processBlock(raw, i * 8, result, i * 8);
    }

    return result;
  }

  public void crypt(byte[] raw, int offset, int size) throws IOException
  {
    int count = size / 8;
    byte[] result = new byte[size];

    for (int i = 0; i < count; i++)
    {
      _crypt.processBlock(raw, offset + i * 8, result, i * 8);
    }

    System.arraycopy(result, 0, raw, offset, size);
  }
}