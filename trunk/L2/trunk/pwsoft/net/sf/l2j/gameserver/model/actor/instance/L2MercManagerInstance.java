package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2MercManagerInstance extends L2FolkInstance
{
  private static final int COND_ALL_FALSE = 0;
  private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  private static final int COND_OWNER = 2;

  public L2MercManagerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;
    player.setLastFolkNPC(this);

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);

      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else
    {
      showMessageWindow(player);
    }

    player.sendActionFailed();
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    int condition = validateCondition(player);
    if (condition <= 0) return;

    if (condition == 1) return;
    if (condition == 2)
    {
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();

      String val = "";
      if (st.countTokens() >= 1)
      {
        val = st.nextToken();
      }

      if (actualCommand.equalsIgnoreCase("hire"))
      {
        if (val == "") return;

        showBuyWindow(player, Integer.parseInt(val));
        return;
      }
    }

    super.onBypassFeedback(player, command);
  }

  private void showBuyWindow(L2PcInstance player, int val)
  {
    player.tempInvetoryDisable();

    L2TradeList list = TradeController.getInstance().getBuyList(val);
    if ((list != null) && (list.getNpcId().equals(String.valueOf(getNpcId()))))
    {
      BuyList bl = new BuyList(list, player.getAdena(), 0.0D);
      player.sendPacket(bl);
    }
    else
    {
      _log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");

      _log.warning("buylist id:" + val);
    }
  }

  public void showMessageWindow(L2PcInstance player)
  {
    String filename = "data/html/mercmanager/mercmanager-no.htm";

    int condition = validateCondition(player);
    if (condition == 1) filename = "data/html/mercmanager/mercmanager-busy.htm";
    else if (condition == 2) {
      filename = "data/html/mercmanager/mercmanager.htm";
    }
    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  private int validateCondition(L2PcInstance player)
  {
    if ((getCastle() != null) && (getCastle().getCastleId() > 0))
    {
      if (player.getClan() != null)
      {
        if (getCastle().getSiege().getIsInProgress()) return 1;
        if (getCastle().getOwnerId() == player.getClanId())
        {
          if ((player.getClanPrivileges() & 0x200000) == 2097152) return 2;
        }
      }
    }

    return 0;
  }
}