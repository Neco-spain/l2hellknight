package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;
import net.sf.l2j.gameserver.network.serverpackets.ShowMiniMap;

public class Book
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5588, 6317, 7561, 7064, 7082, 7083, 7084, 7085, 7086, 7087, 7088, 7089, 7090, 7091, 7092, 7093, 7094, 7095, 7096, 7097, 7098, 7099, 7100, 7101, 7102, 7103, 7104, 7105, 7106, 7107, 7108, 7109, 7110, 7111, 7112 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance))
      return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    int itemId = item.getItemId();

    String filename = "data/html/help/" + itemId + ".htm";
    String content = HtmCache.getInstance().getHtm(filename);

    if (itemId == 7064)
    {
      activeChar.sendPacket(new ShowMiniMap(1665));
      activeChar.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
    }

    if (content == null)
    {
      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setHtml("<html><body>My Text is missing:<br>" + filename + "</body></html>");
      activeChar.sendPacket(html);
    }
    else
    {
      NpcHtmlMessage itemReply = new NpcHtmlMessage(5);
      itemReply.setHtml(content);
      activeChar.sendPacket(itemReply);
    }

    activeChar.sendPacket(new ActionFailed());
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}