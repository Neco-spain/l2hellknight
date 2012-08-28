package net.sf.l2j.gameserver.loginserverpackets;

import net.sf.l2j.gameserver.TaskPriority;

public abstract class LoginServerBasePacket
{
  private byte[] _decrypt;
  private int _off;

  public LoginServerBasePacket(byte[] decrypt)
  {
    _decrypt = decrypt;
    _off = 1;
  }

  public int readD()
  {
    int result = _decrypt[(_off++)] & 0xFF;
    result |= _decrypt[(_off++)] << 8 & 0xFF00;
    result |= _decrypt[(_off++)] << 16 & 0xFF0000;
    result |= _decrypt[(_off++)] << 24 & 0xFF000000;
    return result;
  }

  public int readC()
  {
    int result = _decrypt[(_off++)] & 0xFF;
    return result;
  }

  public int readH()
  {
    int result = _decrypt[(_off++)] & 0xFF;
    result |= _decrypt[(_off++)] << 8 & 0xFF00;
    return result;
  }

  public double readF()
  {
    long result = _decrypt[(_off++)] & 0xFF;
    result |= _decrypt[(_off++)] << 8 & 0xFF00;
    result |= _decrypt[(_off++)] << 16 & 0xFF0000;
    result |= _decrypt[(_off++)] << 24 & 0xFF000000;
    result |= _decrypt[(_off++)] << 32 & 0x0;
    result |= _decrypt[(_off++)] << 40 & 0x0;
    result |= _decrypt[(_off++)] << 48 & 0x0;
    result |= _decrypt[(_off++)] << 56 & 0x0;
    return Double.longBitsToDouble(result);
  }

  public String readS()
  {
    String result = null;
    try
    {
      result = new String(_decrypt, _off, _decrypt.length - _off, "UTF-16LE");
      result = result.substring(0, result.indexOf(0));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    _off += result.length() * 2 + 2;
    return result;
  }

  public final byte[] readB(int length)
  {
    byte[] result = new byte[length];
    for (int i = 0; i < length; i++)
    {
      result[i] = _decrypt[(_off + i)];
    }
    _off += length;
    return result;
  }
  public TaskPriority getPriority() {
    return TaskPriority.PR_HIGH;
  }
}