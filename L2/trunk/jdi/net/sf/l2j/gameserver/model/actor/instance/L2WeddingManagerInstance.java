package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WeddingManagerInstance extends L2NpcInstance
{
  public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (this != player.getTarget())
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), 0));

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

    player.sendPacket(new ActionFailed());
  }

  private void showMessageWindow(L2PcInstance player)
  {
    String filename = "data/html/mods/Wedding_start.htm";
    String replace = String.valueOf(Config.L2JMOD_WEDDING_PRICE);

    NpcHtmlMessage html = new NpcHtmlMessage(1);
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%replace%", replace);
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    String filename = "data/html/mods/Wedding_start.htm";
    String replace = "";

    if (player.getPartnerId() == 0)
    {
      filename = "data/html/mods/Wedding_nopartner.htm";
      sendHtmlMessage(player, filename, replace);
      return;
    }

    L2PcInstance ptarget = (L2PcInstance)L2World.getInstance().findObject(player.getPartnerId());

    if ((ptarget == null) || (ptarget.isOnline() == 0))
    {
      filename = "data/html/mods/Wedding_notfound.htm";
      sendHtmlMessage(player, filename, replace);
      return;
    }

    if (player.isMarried())
    {
      filename = "data/html/mods/Wedding_already.htm";
      sendHtmlMessage(player, filename, replace);
      return;
    }
    if (player.isMarryAccepted())
    {
      filename = "data/html/mods/Wedding_waitforpartner.htm";
      sendHtmlMessage(player, filename, replace);
      return;
    }
    if (command.startsWith("AcceptWedding"))
    {
      player.setMarryAccepted(true);
      Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
      couple.marry();

      player.sendMessage("Congratulations you are married!");
      player.setMarried(true);
      player.setMaryRequest(false);
      ptarget.sendMessage("Congratulations you are married!");
      ptarget.setMarried(true);
      ptarget.setMaryRequest(false);

      player.addItem("Cupids Bow", 9140, 1, player, true);
      player.getInventory().updateDatabase();
      ptarget.addItem("Cupids Bow", 9140, 1, ptarget, true);
      ptarget.getInventory().updateDatabase();
      player.sendSkillList();
      ptarget.sendSkillList();

      MagicSkillUser MSU = new MagicSkillUser(player, player, 2230, 1, 1, 0);
      player.broadcastPacket(MSU);
      MSU = new MagicSkillUser(ptarget, ptarget, 2230, 1, 1, 0);
      ptarget.broadcastPacket(MSU);

      L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
      if (skill != null)
      {
        MSU = new MagicSkillUser(player, player, 2025, 1, 1, 0);
        player.sendPacket(MSU);
        player.broadcastPacket(MSU);
        player.useMagic(skill, false, false);

        MSU = new MagicSkillUser(ptarget, ptarget, 2025, 1, 1, 0);
        ptarget.sendPacket(MSU);
        ptarget.broadcastPacket(MSU);
        ptarget.useMagic(skill, false, false);
      }

      CoupleManager.getInstance().checkCouple(player);
      CoupleManager.getInstance().checkCouple(ptarget);
      Announcements.getInstance().announceToAll("Congratulations to " + player.getName() + " and " + ptarget.getName() + "! They have been married.");

      MSU = null;

      filename = "data/html/mods/Wedding_accepted.htm";
      replace = ptarget.getName();
      sendHtmlMessage(ptarget, filename, replace);
      return;
    }
    if (command.startsWith("DeclineWedding"))
    {
      player.setMaryRequest(false);
      ptarget.setMaryRequest(false);
      player.setMarryAccepted(false);
      ptarget.setMarryAccepted(false);
      player.sendMessage("You declined");
      ptarget.sendMessage("Your partner declined");
      replace = ptarget.getName();
      filename = "data/html/mods/Wedding_declined.htm";
      sendHtmlMessage(ptarget, filename, replace);
      return;
    }
    if (player.isMaryRequest())
    {
      if (Config.L2JMOD_WEDDING_FORMALWEAR)
      {
        Inventory inv3 = player.getInventory();
        L2ItemInstance item3 = inv3.getPaperdollItem(10);
        if (null == item3)
        {
          player.setIsWearingFormalWear(false);
        }
        else
        {
          String strItem = Integer.toString(item3.getItemId());
          String frmWear = Integer.toString(6408);
          player.sendMessage(strItem);
          if (strItem.equals(frmWear))
          {
            player.setIsWearingFormalWear(true);
          }
          else player.setIsWearingFormalWear(false);
        }
      }

      if ((Config.L2JMOD_WEDDING_FORMALWEAR) && (!player.isWearingFormalWear()))
      {
        filename = "data/html/mods/Wedding_noformal.htm";
        sendHtmlMessage(player, filename, replace);
        return;
      }
      filename = "data/html/mods/Wedding_ask.htm";
      player.setMaryRequest(false);
      ptarget.setMaryRequest(false);
      replace = ptarget.getName();
      sendHtmlMessage(player, filename, replace);
      return;
    }
    if (command.startsWith("AskWedding"))
    {
      if (Config.L2JMOD_WEDDING_FORMALWEAR)
      {
        Inventory inv3 = player.getInventory();
        L2ItemInstance item3 = inv3.getPaperdollItem(10);

        if (null == item3)
        {
          player.setIsWearingFormalWear(false);
        }
        else
        {
          String frmWear = Integer.toString(6408);
          String strItem = null;
          strItem = Integer.toString(item3.getItemId());

          if ((null != strItem) && (strItem.equals(frmWear)))
          {
            player.setIsWearingFormalWear(true);
          }
          else player.setIsWearingFormalWear(false);
        }
      }

      if ((Config.L2JMOD_WEDDING_FORMALWEAR) && (!player.isWearingFormalWear()))
      {
        filename = "data/html/mods/Wedding_noformal.htm";
        sendHtmlMessage(player, filename, replace);
        return;
      }
      if (player.getAdena() < Config.L2JMOD_WEDDING_PRICE)
      {
        filename = "data/html/mods/Wedding_adena.htm";
        replace = String.valueOf(Config.L2JMOD_WEDDING_PRICE);
        sendHtmlMessage(player, filename, replace);
        return;
      }

      player.setMarryAccepted(true);
      ptarget.setMaryRequest(true);
      replace = ptarget.getName();
      filename = "data/html/mods/Wedding_requested.htm";
      player.getInventory().reduceAdena("Wedding", Config.L2JMOD_WEDDING_PRICE, player, player.getLastFolkNPC());
      sendHtmlMessage(player, filename, replace);
      return;
    }

    sendHtmlMessage(player, filename, replace);
  }

  public boolean isMovementDisabled()
  {
    return true;
  }

  private void sendHtmlMessage(L2PcInstance player, String filename, String replace)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(1);
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%replace%", replace);
    html.replace("%npcname%", getName());
    player.sendPacket(html);
  }
}