package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

public class ShowBoard extends L2GameServerPacket
{
  private static final String _S__6E_SHOWBOARD = "[S] 6e ShowBoard";
  private String _htmlCode;
  private String _id;
  private List<String> _arg;

  public ShowBoard(String htmlCode, String id)
  {
    _id = id;
    _htmlCode = htmlCode;
  }

  public ShowBoard(List<String> arg)
  {
    _id = "1002";
    _htmlCode = null;
    _arg = arg;
  }

  private byte[] get1002()
  {
    int len = _id.getBytes().length * 2 + 2;
    for (String arg : _arg)
    {
      len += (arg.getBytes().length + 4) * 2;
    }
    byte[] data = new byte[len];
    int i = 0;
    for (int j = 0; j < _id.getBytes().length; i += 2)
    {
      data[i] = _id.getBytes()[j];
      data[(i + 1)] = 0;

      j++;
    }

    data[i] = 8;
    i++;
    data[i] = 0;
    i++;
    for (String arg : _arg)
    {
      for (int j = 0; j < arg.getBytes().length; i += 2)
      {
        data[i] = arg.getBytes()[j];
        data[(i + 1)] = 0;

        j++;
      }

      data[i] = 32;
      i++;
      data[i] = 0;
      i++;
      data[i] = 8;
      i++;
      data[i] = 0;
      i++;
    }
    return data;
  }

  protected final void writeImpl()
  {
    writeC(110);
    writeC(1);
    writeS("bypass _bbshome");
    writeS("bypass _bbsgetfav");
    writeS("bypass _bbsloc");
    writeS("bypass _bbsclan");
    writeS("bypass _bbsmemo");
    writeS("bypass _bbsmail");
    writeS("bypass _bbsfriends");
    writeS("bypass bbs_add_fav");
    if (!_id.equals("1002"))
    {
      byte[] htmlBytes = null;
      if (_htmlCode != null)
        htmlBytes = _htmlCode.getBytes();
      byte[] data = new byte[6 + _id.getBytes().length * 2 + 2 * (_htmlCode != null ? htmlBytes.length : 0)];

      int i = 0;
      for (int j = 0; j < _id.getBytes().length; i += 2)
      {
        data[i] = _id.getBytes()[j];
        data[(i + 1)] = 0;

        j++;
      }

      data[i] = 8;
      i++;
      data[i] = 0;
      i++;
      if (_htmlCode != null)
      {
        for (int j = 0; j < htmlBytes.length; j++)
        {
          data[i] = htmlBytes[j];
          data[(i + 1)] = 0;

          i += 2;
        }

      }

      data[i] = 0;
      i++;
      data[i] = 0;

      writeB(data);
    }
    else
    {
      writeB(get1002());
    }
  }

  public String getType()
  {
    return "[S] 6e ShowBoard";
  }
}