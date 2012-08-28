package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FortWyvernManagerInstance extends L2NpcInstance
{
  protected static final int COND_ALL_FALSE = 0;
  protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  protected static final int COND_OWNER = 2;

  public L2FortWyvernManagerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("RideWyvern"))
    {
      if (!player.isClanLeader())
      {
        player.sendMessage("Only clan leaders are allowed.");
        return;
      }

      int petItemId = 0;
      L2ItemInstance petItem = null;

      if (player.getPet() == null)
      {
        if (player.isMounted())
        {
          petItem = player.getInventory().getItemByObjectId(player.getMountObjectID());
          if (petItem != null)
          {
            petItemId = petItem.getItemId();
          }
        }
      }
      else
      {
        petItemId = player.getPet().getControlItemId();
      }

      if ((petItemId == 0) || (!player.isMounted()))
      {
        player.sendMessage("Ride your strider first...");
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/fortress/wyvernmanager-explain.htm");
        html.replace("%count%", String.valueOf(10));
        player.sendPacket(html);
        html = null;
        return;
      }
      if ((player.isMounted()) && (petItem != null) && (petItem.getEnchantLevel() < 55))
      {
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/fortress/wyvernmanager-explain.htm");
        html.replace("%count%", String.valueOf(10));
        player.sendPacket(html);
        html = null;
        return;
      }

      if ((player.getInventory().getItemByItemId(1460) != null) && (player.getInventory().getItemByItemId(1460).getCount() >= 10))
      {
        if (!player.disarmWeapons()) {
          return;
        }
        if (player.isMounted())
        {
          if (player.setMountType(0))
          {
            if (player.isFlying()) player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
            Ride dismount = new Ride(player.getObjectId(), 0, 0);
            broadcastPacket(dismount);
            dismount = null;
            player.setMountObjectID(0);

            player.broadcastUserInfo();
          }
        }

        if (player.getPet() != null)
        {
          player.getPet().unSummon(player);
        }

        player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());

        Ride mount = new Ride(player.getObjectId(), 1, 12621);
        player.sendPacket(mount);
        player.broadcastPacket(mount);
        player.setMountType(mount.getMountType());

        player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
        player.sendMessage("The Wyvern has been summoned successfully!");
      }
      else
      {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/fortress/wyvernmanager-explain.htm");
        html.replace("%count%", String.valueOf(10));
        player.sendPacket(html);
        html = null;
        player.sendMessage("You need 10 Crystals: B Grade.");
      }

      petItem = null;
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) {
      return;
    }

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

  private void showMessageWindow(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
    String filename = "data/html/fortress/wyvernmanager-no.htm";

    int condition = validateCondition(player);

    if (condition > 0)
    {
      if (condition == 2)
      {
        filename = "data/html/fortress/wyvernmanager.htm";
      }
    }

    NpcHtmlMessage html = new NpcHtmlMessage(1);
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%count%", String.valueOf(10));
    player.sendPacket(html);
    filename = null;
    html = null;
  }

  protected int validateCondition(L2PcInstance player)
  {
    if ((getFort() != null) && (getFort().getFortId() > 0))
    {
      if (player.getClan() != null)
      {
        if (getFort().getSiege().getIsInProgress())
          return 1;
        if ((getFort().getOwnerId() == player.getClanId()) && (player.isClanLeader()))
          return 2;
      }
    }
    return 0;
  }
}