package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.text.TextBuilder;

public class ShowBoard extends L2GameServerPacket
{
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

    TextBuilder str = new TextBuilder(_id + "\b");
    if (!_id.equals("1002"))
      str.append(_htmlCode);
    else
      for (String arg : _arg)
        str.append(arg + " \b");
    writeS(str.toString());
    str.clear();
    str = null;
  }

  public String getType()
  {
    return "S.ShowBoard";
  }
}