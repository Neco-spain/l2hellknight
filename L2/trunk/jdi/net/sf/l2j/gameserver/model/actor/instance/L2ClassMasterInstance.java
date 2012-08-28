package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.Config.ClassMasterSettings;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2ClassMasterInstance extends L2FolkInstance
{
  public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (getObjectId() != player.getTargetId())
    {
      player.setTarget(this);

      player.sendPacket(new MyTargetSelected(getObjectId(), 0));

      player.sendPacket(new ValidateLocation(this));
    }
    else
    {
      if (!canInteract(player))
      {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
        return;
      }

      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
      TextBuilder sb = new TextBuilder();
      sb.append("<html><body>");
      sb.append(getName() + ":<br>");
      sb.append("<br>");

      ClassId classId = player.getClassId();
      int level = player.getLevel();
      int jobLevel = classId.level();

      int newJobLevel = jobLevel + 1;

      if (((level >= 20) && (jobLevel == 0)) || ((level >= 40) && (jobLevel == 1)) || (((level >= 76) && (jobLevel == 2) && (Config.CLASS_MASTER_SETTINGS.isAllowed(newJobLevel))) || (Config.CLASS_MASTER_STRIDER_UPDATE)))
      {
        if (((level >= 20) && (jobLevel == 0)) || ((level >= 40) && (jobLevel == 1)) || ((level >= 76) && (jobLevel == 2) && (Config.CLASS_MASTER_SETTINGS.isAllowed(newJobLevel))))
        {
          sb.append("You can change your occupation to following:<br>");

          for (ClassId child : ClassId.values()) {
            if ((child.childOf(classId)) && (child.level() == newJobLevel))
              sb.append("<br><a action=\"bypass -h npc_" + getObjectId() + "_change_class " + child.getId() + "\"> " + CharTemplateTable.getClassNameById(child.getId()) + "</a>");
          }
          if ((Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel) != null) && (Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).size() > 0))
          {
            sb.append("<br><br>Item(s) required for class change:");
            sb.append("<table width=270>");
            for (Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
            {
              int _count = ((Integer)Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId)).intValue();
              sb.append("<tr><td><font color=\"LEVEL\">" + _count + "</font></td><td>" + ItemTable.getInstance().getTemplate(_itemId.intValue()).getName() + "</td></tr>");
            }
            sb.append("</table>");
          }
        }

        if (Config.CLASS_MASTER_STRIDER_UPDATE)
        {
          sb.append("<table width=270>");
          sb.append("<tr><td><br></td></tr>");
          sb.append("<tr><td><a action=\"bypass -h npc_" + getObjectId() + "_upgrade_hatchling\">Upgrade Hatchling to Strider</a></td></tr>");
          sb.append("</table>");
        }
        sb.append("<br>");
      }
      else
      {
        switch (jobLevel)
        {
        case 0:
          if (Config.CLASS_MASTER_SETTINGS.isAllowed(1)) {
            sb.append("Come back here when you reached level 20 to change your class.<br>");
          }
          else if (Config.CLASS_MASTER_SETTINGS.isAllowed(2)) {
            sb.append("Come back after your first occupation change.<br>");
          }
          else if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
            sb.append("Come back after your second occupation change.<br>");
          else
            sb.append("I can't change your occupation.<br>");
          break;
        case 1:
          if (Config.CLASS_MASTER_SETTINGS.isAllowed(2)) {
            sb.append("Come back here when you reached level 40 to change your class.<br>");
          }
          else if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
            sb.append("Come back after your second occupation change.<br>");
          else
            sb.append("I can't change your occupation.<br>");
          break;
        case 2:
          if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
            sb.append("Come back here when you reached level 76 to change your class.<br>");
          else
            sb.append("I can't change your occupation.<br>");
          break;
        case 3:
          sb.append("There is no class change available for you anymore.<br>");
        }

        sb.append("<br>");
      }

      for (Quest q : Quest.findAllEvents())
        sb.append("Event: <a action=\"bypass -h Quest " + q.getName() + "\">" + q.getDescr() + "</a><br>");
      sb.append("</body></html>");
      html.setHtml(sb.toString());
      player.sendPacket(html);
    }

    player.sendPacket(new ActionFailed());
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("change_class"))
    {
      int val = Integer.parseInt(command.substring(13));

      ClassId classId = player.getClassId();
      ClassId newClassId = ClassId.values()[val];

      int level = player.getLevel();
      int jobLevel = classId.level();
      int newJobLevel = newClassId.level();

      if (!Config.CLASS_MASTER_SETTINGS.isAllowed(newJobLevel)) return;

      if (!newClassId.childOf(classId)) return;

      if (newJobLevel != jobLevel + 1) return;

      if ((level < 20) && (newJobLevel > 1)) return;
      if ((level < 40) && (newJobLevel > 2)) return;
      if ((level < 76) && (newJobLevel > 3)) return;

      for (Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
      {
        int _count = ((Integer)Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId)).intValue();
        if (player.getInventory().getInventoryItemCount(_itemId.intValue(), -1) < _count)
        {
          player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
          return;
        }

      }

      for (Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
      {
        int _count = ((Integer)Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId)).intValue();
        player.destroyItemByItemId("ClassMaster", _itemId.intValue(), _count, player, true);
      }

      for (Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).keySet())
      {
        int _count = ((Integer)Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).get(_itemId)).intValue();
        player.addItem("ClassMaster", _itemId.intValue(), _count, player, true);
      }

      changeClass(player, val);

      player.rewardSkills();

      if (newJobLevel == 3)
      {
        if (Config.CLASSMASTER_MSG)
        {
          CreatureSay cs = new CreatureSay(getObjectId(), 1, getName(), player.getName() + " congratulations! You got a third profession! Now you " + CharTemplateTable.getClassNameById(player.getClassId().getId()));
          broadcastPacket(cs);
        }
        player.sendPacket(new SystemMessage(SystemMessageId.THIRD_CLASS_TRANSFER));
      }
      else
      {
        if (Config.CLASSMASTER_MSG)
        {
          CreatureSay cs = new CreatureSay(getObjectId(), 1, getName(), player.getName() + " congratulations! You on obtaining a profession! Now you " + CharTemplateTable.getClassNameById(player.getClassId().getId()));
          broadcastPacket(cs);
        }
        player.sendPacket(new SystemMessage(SystemMessageId.CLASS_TRANSFER));
      }

      NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
      TextBuilder sb = new TextBuilder();
      sb.append("<html><body>");
      sb.append(getName() + ":<br>");
      sb.append("<br>");
      sb.append("You have now become a <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getClassId().getId()) + "</font>.");
      sb.append("</body></html>");
      html.setHtml(sb.toString());
      player.sendPacket(html);
      player.refreshOverloaded();
      player.refreshExpertisePenalty();
    }
    else if ((command.startsWith("upgrade_hatchling")) && (Config.CLASS_MASTER_STRIDER_UPDATE))
    {
      L2Summon summon = player.getPet();
      if (summon == null)
      {
        NpcHtmlMessage adminReply = new NpcHtmlMessage(getObjectId());
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        replyMSG.append("I want to look at your dragon, summon it");
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        player.sendPacket(adminReply);
        return;
      }
      if ((summon.getLevel() >= Config.STRIDER_LEVEL_FOR_UP) && ((player.getInventory().getItemByItemId(3500) != null) || (player.getInventory().getItemByItemId(3501) != null) || (player.getInventory().getItemByItemId(3502) != null)))
      {
        if (!player.reduceAdena("PetUpdate", Config.PRICE_FOR_STRIDER, this, true))
          return;
        if (player.getInventory().getItemByItemId(3500) != null)
        {
          player.getInventory().addItem("Admin", 4422, 1, player, null);
          player.getInventory().destroyItemByItemId("Admin", 3500, 1, player, null);
        }
        else if (player.getInventory().getItemByItemId(3501) != null)
        {
          player.getInventory().destroyItemByItemId("Admin", 3501, 1, player, null);
          player.getInventory().addItem("Admin", 4423, 1, player, null);
        }
        else if (player.getInventory().getItemByItemId(3502) != null)
        {
          player.getInventory().destroyItemByItemId("Admin", 3502, 1, player, null);
          player.getInventory().addItem("Admin", 4424, 1, player, null);
        }
        ItemList il = new ItemList(player, true);
        player.sendPacket(il);
        player.getPet().unSummon(player);
      }
      else {
        NpcHtmlMessage adminReply = new NpcHtmlMessage(getObjectId());
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        replyMSG.append("You do not have " + Config.PRICE_FOR_STRIDER + "aden or your pet not" + Config.STRIDER_LEVEL_FOR_UP + " level ...");
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        player.sendPacket(adminReply);
      }
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  private void changeClass(L2PcInstance player, int val)
  {
    player.setClassId(val);

    if (player.isSubClassActive()) ((SubClass)player.getSubClasses().get(Integer.valueOf(player.getClassIndex()))).setClassId(player.getActiveClass()); else
      player.setBaseClass(player.getActiveClass());
    player.broadcastUserInfo();
  }
}