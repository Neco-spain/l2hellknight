package net.sf.l2j.gameserver;

import java.nio.ByteBuffer;

public class Crypt
{
  private final byte[] _key = new byte[16];
  private boolean _isEnabled;

  public void setKey(byte[] key)
  {
    System.arraycopy(key, 0, _key, 0, key.length);
    _isEnabled = true;
  }

  public void decrypt(ByteBuffer buf)
  {
    if (!_isEnabled) {
      return;
    }
    int sz = buf.remaining();
    int temp = 0;
    for (int i = 0; i < sz; i++)
    {
      int temp2 = buf.get(i);
      buf.put(i, (byte)(temp2 ^ _key[(i & 0xF)] ^ temp));
      temp = temp2;
    }

    int old = _key[8] & 0xFF;
    old |= _key[9] << 8 & 0xFF00;
    old |= _key[10] << 16 & 0xFF0000;
    old |= _key[11] << 24 & 0xFF000000;

    old += sz;

    _key[8] = (byte)(old & 0xFF);
    _key[9] = (byte)(old >> 8 & 0xFF);
    _key[10] = (byte)(old >> 16 & 0xFF);
    _key[11] = (byte)(old >> 24 & 0xFF);
  }

  public void encrypt(ByteBuffer buf)
  {
    if (!_isEnabled) {
      return;
    }
    int temp = 0;
    int sz = buf.remaining();
    for (int i = 0; i < sz; i++)
    {
      int temp2 = buf.get(i);
      temp = temp2 ^ _key[(i & 0xF)] ^ temp;
      buf.put(i, (byte)temp);
    }

    int old = _key[8] & 0xFF;
    old |= _key[9] << 8 & 0xFF00;
    old |= _key[10] << 16 & 0xFF0000;
    old |= _key[11] << 24 & 0xFF000000;

    old += sz;

    _key[8] = (byte)(old & 0xFF);
    _key[9] = (byte)(old >> 8 & 0xFF);
    _key[10] = (byte)(old >> 16 & 0xFF);
    _key[11] = (byte)(old >> 24 & 0xFF);
  }
}