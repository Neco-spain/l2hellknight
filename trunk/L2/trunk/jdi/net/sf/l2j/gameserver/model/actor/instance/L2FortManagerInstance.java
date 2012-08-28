package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FortManagerInstance extends L2MerchantInstance
{
  protected static final int COND_ALL_FALSE = 0;
  protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  protected static final int COND_OWNER = 2;

  public L2FortManagerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isWarehouse()
  {
    return true;
  }

  private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
  {
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcId%", String.valueOf(getNpcId()));
    player.sendPacket(html);
    html = null;
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) {
      return;
    }
    player.setLastFolkNPC(this);

    if (this != player.getTarget())
    {
      player.setTarget(this);
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else
    {
      showMessageWindow(player);
    }

    player.sendPacket(new ActionFailed());
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    int condition = validateCondition(player);

    if (player.getLastFolkNPC().getObjectId() != getObjectId()) {
      return;
    }
    if ((condition <= 0) || (condition == 1)) {
      return;
    }
    if (condition == 2)
    {
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();

      String val = "";
      if (st.countTokens() >= 1)
      {
        val = st.nextToken();
      }

      if (actualCommand.equalsIgnoreCase("banish_foreigner"))
      {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

        if ((player.getClanPrivileges() & 0x80000) == 524288)
        {
          if (val.isEmpty())
          {
            html.setFile("data/html/fortress/foreman-expel.htm");
          }
          else
          {
            getFort().banishForeigners();
            html.setFile("data/html/fortress/foreman-expeled.htm");
          }
        }
        else
        {
          html.setFile("data/html/fortress/foreman-noprivs.htm");
        }

        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);

        html = null;
        return;
      }
      if (actualCommand.equalsIgnoreCase("manage_vault"))
      {
        if ((player.getClanPrivileges() & 0x8) == 8)
        {
          if (val.equalsIgnoreCase("deposit"))
          {
            showVaultWindowDeposit(player);
          }
          else if (val.equalsIgnoreCase("withdraw"))
          {
            showVaultWindowWithdraw(player);
          }
          else
          {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/fortress/foreman-vault.htm");
            sendHtmlMessage(player, html);

            html = null;
          }
        }
        else
        {
          NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
          html.setFile("data/html/fortress/foreman-noprivs.htm");
          html.replace("%objectId%", String.valueOf(getObjectId()));
          player.sendPacket(html);

          html = null;
          return;
        }
        return;
      }
      if (actualCommand.equalsIgnoreCase("operate_door"))
      {
        if ((player.getClanPrivileges() & 0x8000) == 32768)
        {
          if (!val.isEmpty())
          {
            boolean open = Integer.parseInt(val) == 1;
            while (st.hasMoreTokens())
            {
              getFort().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
            }
          }

          NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
          html.setFile("data/html/fortress/" + getNpcId() + "-d.htm");
          html.replace("%objectId%", String.valueOf(getObjectId()));
          html.replace("%npcname%", getName());
          player.sendPacket(html);

          html = null;
          return;
        }

        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/fortress/foreman-noprivs.htm");
        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);

        html = null;
        return;
      }

      super.onBypassFeedback(player, command);

      st = null;
      actualCommand = null;
      val = null;
    }
  }

  private void showMessageWindow(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    String filename = "data/html/fortress/foreman-no.htm";

    int condition = validateCondition(player);
    if (condition > 0)
    {
      if (condition == 1)
      {
        filename = "data/html/fortress/foreman-busy.htm";
      }
      else if (condition == 2)
      {
        filename = "data/html/fortress/foreman.htm";
      }
    }

    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);

    filename = null;
    html = null;
  }

  private void showVaultWindowDeposit(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    player.setActiveWarehouse(player.getClan().getWarehouse());
    player.sendPacket(new WareHouseDepositList(player, 2));
  }

  private void showVaultWindowWithdraw(L2PcInstance player)
  {
    if ((player.isClanLeader()) || ((player.getClanPrivileges() & 0x8) == 8))
    {
      player.sendPacket(new ActionFailed());
      player.setActiveWarehouse(player.getClan().getWarehouse());
      player.sendPacket(new WareHouseWithdrawalList(player, 2));
    }
    else
    {
      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
      html.setFile("data/html/fortress/foreman-noprivs.htm");
      html.replace("%objectId%", String.valueOf(getObjectId()));
      player.sendPacket(html);

      html = null;
      return;
    }
  }

  protected int validateCondition(L2PcInstance player)
  {
    if ((getFort() != null) && (getFort().getFortId() > 0))
    {
      if (player.getClan() != null)
      {
        if (getFort().getSiege().getIsInProgress())
          return 1;
        if (getFort().getOwnerId() == player.getClanId())
          return 2;
      }
    }
    return 0;
  }
}