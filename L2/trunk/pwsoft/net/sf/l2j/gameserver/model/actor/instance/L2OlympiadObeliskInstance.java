package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExHeroList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2OlympiadObeliskInstance extends L2NpcInstance
{
  private static final int DestinyCirclet = 6842;

  public L2OlympiadObeliskInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    String filename = "data/html/olympiad/";
    if (command.isEmpty()) {
      if ((player.isNoble()) || (player.isHero()))
        filename = filename + "obelisk001.htm";
      else {
        filename = filename + "obelisk001a.htm";
      }
      showPage(player, filename);
    } else if (command.startsWith("Obelisk")) {
      String val = command.substring(8);

      if (val.equals("010")) {
        if (player.isHero())
          filename = filename + "obelisk010b.htm";
        else if (Hero.getInstance().isInactiveHero(player.getObjectId()))
          filename = filename + "obelisk010.htm";
        else
          filename = filename + "obelisk010a.htm";
      }
      else if (val.equals("herolist"))
        player.sendPacket(new ExHeroList());
      else if (val.equals("020c")) {
        if (!player.isHero()) {
          filename = filename + "obelisk020d.htm";
        } else if (checkCanGiveDestinyCircletItems(player))
        {
          player.getInventory().addItem("Olympiad", 6842, 1, player, null);

          player.sendItems(true);
          player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(6842));
        } else {
          filename = filename + "obelisk020c.htm";
        }
      } else if (val.equals("Hero")) {
        if (Hero.getInstance().isInactiveHero(player.getObjectId()))
          Hero.getInstance().activateHero(player);
        else
          filename = filename + "obelisk010a.htm";
      }
      else if (val.equals("Back")) {
        if ((player.isNoble()) || (player.isHero()))
          filename = filename + "obelisk001.htm";
        else {
          filename = filename + "obelisk001a.htm";
        }
      }
      if ((!filename.equals("")) && (!filename.equals("data/html/olympiad/")))
        showPage(player, filename);
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  private boolean checkCanGiveDestinyCircletItems(L2PcInstance player)
  {
    return player.getInventory().getItemByItemId(6842) == null;
  }

  private void showPage(L2PcInstance player, String filename)
  {
    String page = HtmCache.getInstance().getHtmForce(filename);
    NpcHtmlMessage nhm = NpcHtmlMessage.id(getObjectId());
    nhm.setHtml(page);
    nhm.replace("%objectId%", String.valueOf(getObjectId()));
    player.sendPacket(nhm);
  }
}