package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestLinkHtml extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestLinkHtml.class.getName());
  private static final String REQUESTLINKHTML__C__20 = "[C] 20 RequestLinkHtml";
  private String _link;

  protected void readImpl()
  {
    _link = readS();
  }

  public void runImpl()
  {
    L2PcInstance actor = ((L2GameClient)getClient()).getActiveChar();
    if (actor == null) {
      return;
    }
    if ((_link.contains("..")) || (!_link.contains(".htm")))
    {
      _log.warning("[RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped");
      return;
    }

    NpcHtmlMessage msg = new NpcHtmlMessage(0);
    msg.setFile(_link);

    sendPacket(msg);
  }

  public String getType()
  {
    return "[C] 20 RequestLinkHtml";
  }
}