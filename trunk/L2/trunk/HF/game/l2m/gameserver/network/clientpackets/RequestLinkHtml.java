package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestLinkHtml extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(RequestLinkHtml.class);
  private String _link;

  protected void readImpl()
  {
    _link = readS();
  }

  protected void runImpl()
  {
    Player actor = ((GameClient)getClient()).getActiveChar();
    if (actor == null) {
      return;
    }
    if ((_link.contains("..")) || (!_link.endsWith(".htm")))
    {
      _log.warn("[RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped");
      return;
    }
    try
    {
      NpcHtmlMessage msg = new NpcHtmlMessage(0);
      msg.setFile("" + _link);
      sendPacket(msg);
    }
    catch (Exception e)
    {
      _log.warn("Bad RequestLinkHtml: ", e);
    }
  }
}