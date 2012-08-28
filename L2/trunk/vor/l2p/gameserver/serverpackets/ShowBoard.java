package l2p.gameserver.serverpackets;

import java.util.List;
import l2p.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowBoard extends L2GameServerPacket
{
  private static final Logger _log = LoggerFactory.getLogger(ShowBoard.class);
  private String _htmlCode;
  private String _id;
  private List<String> _arg;
  private String _addFav = "";

  public static void separateAndSend(String html, Player player)
  {
    if (html.length() < 8180)
    {
      player.sendPacket(new ShowBoard(html, "101", player));
      player.sendPacket(new ShowBoard(null, "102", player));
      player.sendPacket(new ShowBoard(null, "103", player));
    }
    else if (html.length() < 16360)
    {
      player.sendPacket(new ShowBoard(html.substring(0, 8180), "101", player));
      player.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102", player));
      player.sendPacket(new ShowBoard(null, "103", player));
    }
    else if (html.length() < 24540)
    {
      player.sendPacket(new ShowBoard(html.substring(0, 8180), "101", player));
      player.sendPacket(new ShowBoard(html.substring(8180, 16360), "102", player));
      player.sendPacket(new ShowBoard(html.substring(16360, html.length()), "103", player));
    }
  }

  public ShowBoard(String htmlCode, String id, Player player)
  {
    if ((htmlCode != null) && (htmlCode.length() > 8192))
    {
      _log.warn("Html '" + htmlCode + "' is too long! this will crash the client!");
      _htmlCode = "<html><body>Html was too long</body></html>";
      return;
    }
    _id = id;

    if (player.getSessionVar("add_fav") != null) {
      _addFav = "bypass _bbsaddfav_List";
    }
    if (htmlCode != null)
    {
      if (id.equalsIgnoreCase("101")) {
        player.cleanBypasses(true);
      }
      _htmlCode = player.encodeBypasses(htmlCode, true);
    }
    else {
      _htmlCode = null;
    }
  }

  public ShowBoard(List<String> arg) {
    _id = "1002";
    _htmlCode = null;
    _arg = arg;
  }

  protected final void writeImpl()
  {
    writeC(123);
    writeC(1);
    writeS("bypass _bbshome");
    writeS("bypass _bbsgetfav");
    writeS("bypass _bbsloc");
    writeS("bypass _bbsclan");
    writeS("bypass _bbsmemo");
    writeS("bypass _maillist_0_1_0_");
    writeS("bypass _friendlist_0_");
    writeS(_addFav);
    String str = _id + "\b";
    if (!_id.equals("1002"))
    {
      if (_htmlCode != null)
        str = str + _htmlCode;
    }
    else
      for (String arg : _arg)
        str = str + arg + " \b";
    writeS(str);
  }
}