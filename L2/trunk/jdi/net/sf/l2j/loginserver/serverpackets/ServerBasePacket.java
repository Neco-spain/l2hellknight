package net.sf.l2j.loginserver.serverpackets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class ServerBasePacket
{
  ByteArrayOutputStream _bao;

  protected ServerBasePacket()
  {
    _bao = new ByteArrayOutputStream();
  }

  protected void writeD(int value)
  {
    _bao.write(value & 0xFF);
    _bao.write(value >> 8 & 0xFF);
    _bao.write(value >> 16 & 0xFF);
    _bao.write(value >> 24 & 0xFF);
  }

  protected void writeH(int value)
  {
    _bao.write(value & 0xFF);
    _bao.write(value >> 8 & 0xFF);
  }

  protected void writeC(int value)
  {
    _bao.write(value & 0xFF);
  }

  protected void writeF(double org)
  {
    long value = Double.doubleToRawLongBits(org);
    _bao.write((int)(value & 0xFF));
    _bao.write((int)(value >> 8 & 0xFF));
    _bao.write((int)(value >> 16 & 0xFF));
    _bao.write((int)(value >> 24 & 0xFF));
    _bao.write((int)(value >> 32 & 0xFF));
    _bao.write((int)(value >> 40 & 0xFF));
    _bao.write((int)(value >> 48 & 0xFF));
    _bao.write((int)(value >> 56 & 0xFF));
  }

  protected void writeS(String text)
  {
    try
    {
      if (text != null)
      {
        _bao.write(text.getBytes("UTF-16LE"));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    _bao.write(0);
    _bao.write(0);
  }

  protected void writeB(byte[] array)
  {
    try
    {
      _bao.write(array);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public int getLength()
  {
    return _bao.size() + 2;
  }

  public byte[] getBytes()
  {
    writeD(0);

    int padding = _bao.size() % 8;
    if (padding != 0)
    {
      for (int i = padding; i < 8; i++)
      {
        writeC(0);
      }
    }

    return _bao.toByteArray();
  }

  public abstract byte[] getContent()
    throws IOException;
}