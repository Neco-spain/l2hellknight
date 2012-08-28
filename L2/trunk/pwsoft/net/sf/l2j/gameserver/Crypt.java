package net.sf.l2j.gameserver;

import java.nio.ByteBuffer;

public class Crypt
{
  private final byte[] _k = new byte[16];
  private boolean _f;

  public void setKey(byte[] k)
  {
    System.arraycopy(k, 0, _k, 0, k.length);
    _f = true;
  }

  public void decrypt(ByteBuffer b)
  {
    if (!_f) {
      return;
    }
    int sz = b.remaining();
    int temp = 0;
    for (int i = 0; i < sz; i++)
    {
      int temp2 = b.get(i);
      b.put(i, (byte)(temp2 ^ _k[(i & 0xF)] ^ temp));
      temp = temp2;
    }

    int old = _k[8] & 0xFF;
    old |= _k[9] << 8 & 0xFF00;
    old |= _k[10] << 16 & 0xFF0000;
    old |= _k[11] << 24 & 0xFF000000;

    old += sz;

    _k[8] = (byte)(old & 0xFF);
    _k[9] = (byte)(old >> 8 & 0xFF);
    _k[10] = (byte)(old >> 16 & 0xFF);
    _k[11] = (byte)(old >> 24 & 0xFF);
  }

  public void encrypt(ByteBuffer b)
  {
    if (!_f) {
      return;
    }
    int temp = 0;
    int sz = b.remaining();
    for (int i = 0; i < sz; i++)
    {
      int temp2 = b.get(i);
      temp = temp2 ^ _k[(i & 0xF)] ^ temp;
      b.put(i, (byte)temp);
    }

    int old = _k[8] & 0xFF;
    old |= _k[9] << 8 & 0xFF00;
    old |= _k[10] << 16 & 0xFF0000;
    old |= _k[11] << 24 & 0xFF000000;

    old += sz;

    _k[8] = (byte)(old & 0xFF);
    _k[9] = (byte)(old >> 8 & 0xFF);
    _k[10] = (byte)(old >> 16 & 0xFF);
    _k[11] = (byte)(old >> 24 & 0xFF);
  }
}