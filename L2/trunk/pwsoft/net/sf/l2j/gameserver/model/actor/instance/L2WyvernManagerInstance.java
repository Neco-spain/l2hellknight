package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2CastleChamberlainInstance
{
  public L2WyvernManagerInstance(int objectId, L2NpcTemplate template)
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
      if (player.getPet() == null)
      {
        if (player.isMounted())
        {
          player.sendMessage("You Already Have a Pet or Are Mounted.");
          return;
        }

        player.sendMessage("Summon your Strider first.");
        return;
      }

      if ((player.getPet().getNpcId() == 12526) || (player.getPet().getNpcId() == 12527) || (player.getPet().getNpcId() == 12528))
      {
        if ((player.getInventory().getItemByItemId(1460) != null) && (player.getInventory().getItemByItemId(1460).getCount() >= 10))
        {
          if (player.getPet().getLevel() < 55)
          {
            player.sendMessage("Your Strider Has not reached the required level.");
            return;
          }

          if (!player.disarmWeapons()) return;
          player.getPet().unSummon(player);
          player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
          Ride mount = new Ride(player.getObjectId(), 1, 12621);
          player.sendPacket(mount);
          player.broadcastPacket(mount);
          player.setMountType(mount.getMountType());
          player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
          player.sendMessage("The Wyvern has been summoned successfully!");
          return;
        }

        player.sendMessage("You need 10 Crystals: B Grade.");
        return;
      }

      player.sendMessage("Unsummon your pet.");
      return;
    }

    super.onBypassFeedback(player, command);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

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

  private void showMessageWindow(L2PcInstance player)
  {
    player.sendActionFailed();
    String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";

    int condition = validateCondition(player);
    if (condition > 0)
    {
      if (condition == 2)
        filename = "data/html/wyvernmanager/wyvernmanager.htm";
    }
    NpcHtmlMessage html = NpcHtmlMessage.id(1);
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }
}