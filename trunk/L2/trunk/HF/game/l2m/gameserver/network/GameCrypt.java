package l2m.gameserver.network;

public class GameCrypt
{
  private final byte[] _inKey = new byte[16]; private final byte[] _outKey = new byte[16];
  private boolean _isEnabled = false;

  public void setKey(byte[] key)
  {
    System.arraycopy(key, 0, _inKey, 0, 16);
    System.arraycopy(key, 0, _outKey, 0, 16);
  }

  public void setKey(byte[] key, boolean value)
  {
    setKey(key);
  }

  public boolean decrypt(byte[] raw, int offset, int size)
  {
    if (!_isEnabled) {
      return true;
    }
    int temp = 0;
    for (int i = 0; i < size; i++)
    {
      int temp2 = raw[(offset + i)] & 0xFF;
      raw[(offset + i)] = (byte)(temp2 ^ _inKey[(i & 0xF)] ^ temp);
      temp = temp2;
    }

    int old = _inKey[8] & 0xFF;
    old |= _inKey[9] << 8 & 0xFF00;
    old |= _inKey[10] << 16 & 0xFF0000;
    old |= _inKey[11] << 24 & 0xFF000000;

    old += size;

    _inKey[8] = (byte)(old & 0xFF);
    _inKey[9] = (byte)(old >> 8 & 0xFF);
    _inKey[10] = (byte)(old >> 16 & 0xFF);
    _inKey[11] = (byte)(old >> 24 & 0xFF);
    return true;
  }

  public void encrypt(byte[] raw, int offset, int size)
  {
    if (!_isEnabled)
    {
      _isEnabled = true;
      return;
    }

    int temp = 0;
    for (int i = 0; i < size; i++)
    {
      int temp2 = raw[(offset + i)] & 0xFF;
      temp = temp2 ^ _outKey[(i & 0xF)] ^ temp;
      raw[(offset + i)] = (byte)temp;
    }

    int old = _outKey[8] & 0xFF;
    old |= _outKey[9] << 8 & 0xFF00;
    old |= _outKey[10] << 16 & 0xFF0000;
    old |= _outKey[11] << 24 & 0xFF000000;

    old += size;

    _outKey[8] = (byte)(old & 0xFF);
    _outKey[9] = (byte)(old >> 8 & 0xFF);
    _outKey[10] = (byte)(old >> 16 & 0xFF);
    _outKey[11] = (byte)(old >> 24 & 0xFF);
  }
}