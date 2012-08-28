package scripts.commands.admincommandhandlers;

import java.io.PrintStream;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2Item;
import scripts.commands.IAdminCommandHandler;

public class AdminEnchant
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_seteh", "admin_setec", "admin_seteg", "admin_setel", "admin_seteb", "admin_setew", "admin_setes", "admin_setle", "admin_setre", "admin_setlf", "admin_setrf", "admin_seten", "admin_setun", "admin_setba", "admin_enchant", "admin_augment" };

  private static final int REQUIRED_LEVEL = Config.GM_ENCHANT;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }

    if (command.equals("admin_enchant")) {
      showMainPage(activeChar);
    } else {
      int armorType = -1;

      if (command.startsWith("admin_seteh"))
        armorType = 6;
      else if (command.startsWith("admin_setec"))
        armorType = 10;
      else if (command.startsWith("admin_seteg"))
        armorType = 9;
      else if (command.startsWith("admin_seteb"))
        armorType = 12;
      else if (command.startsWith("admin_setel"))
        armorType = 11;
      else if (command.startsWith("admin_setew"))
        armorType = 7;
      else if (command.startsWith("admin_setes"))
        armorType = 8;
      else if (command.startsWith("admin_setle"))
        armorType = 1;
      else if (command.startsWith("admin_setre"))
        armorType = 2;
      else if (command.startsWith("admin_setlf"))
        armorType = 4;
      else if (command.startsWith("admin_setrf"))
        armorType = 5;
      else if (command.startsWith("admin_seten"))
        armorType = 3;
      else if (command.startsWith("admin_setun"))
        armorType = 0;
      else if (command.startsWith("admin_setba"))
        armorType = 13;
      else if (command.startsWith("admin_augment")) {
        armorType = 777;
      }

      if (armorType == 777)
      {
        L2Object target = activeChar.getTarget();
        if (target == null) {
          target = activeChar;
        }
        L2PcInstance player = null;
        if (target.isPlayer()) {
          player = target.getPlayer();
        } else {
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
          return true;
        }

        String[] augm = command.split(" ");
        int aug_skill = Integer.parseInt(augm[1]);
        int aug_lvl = Integer.parseInt(augm[2]);

        L2Skill augment = SkillTable.getInstance().getInfo(aug_skill, aug_lvl);
        if ((augment == null) || (!augment.isAugment())) {
          activeChar.sendAdmResultMessage("\u0410\u0443\u0433\u043C\u0435\u043D\u0442 " + augment.getName() + " (" + aug_skill + ":" + aug_lvl + ") \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D.");
          return true;
        }

        int type = 0;
        if (augment.isActive())
          type = 2;
        else if (augment.isPassive())
          type = 3;
        else {
          type = 1;
        }

        L2ItemInstance weapon = player.getActiveWeaponInstance();
        weapon.setAugmentation(AugmentationData.getInstance().generateAugmentation(weapon, aug_skill, aug_lvl, type));
        player.sendItems(false);
        player.broadcastUserInfo();

        if (player.equals(activeChar))
        {
          player.sendAdmResultMessage("\u0410\u0443\u0433\u043C\u0435\u043D\u0442 \u0438\u0437\u043C\u0435\u043D\u0435\u043D \u043D\u0430 " + augment.getName() + " (" + aug_skill + ":" + aug_lvl + ")");
        }
        else
        {
          activeChar.sendAdmResultMessage("\u0410\u0443\u0433\u043C\u0435\u043D\u0442 " + augment.getName() + " (" + aug_skill + ":" + aug_lvl + ") \u0432\u044B\u0434\u0430\u043D \u0438\u0433\u0440\u043E\u043A\u0443 " + player.getName() + "");
          player.sendAdmResultMessage("\u0410\u0443\u0433\u043C\u0435\u043D\u0442 \u0438\u0437\u043C\u0435\u043D\u0435\u043D \u043D\u0430 " + augment.getName() + " (" + aug_skill + ":" + aug_lvl + ")");
        }

        showMainPage(activeChar);
        return true;
      }

      if (armorType != -1) {
        try {
          int ench = Integer.parseInt(command.substring(12));

          if ((ench < 0) || (ench > 65535))
            activeChar.sendAdmResultMessage("You must set the enchant level to be between 0-65535.");
          else
            setEnchant(activeChar, ench, armorType);
        }
        catch (StringIndexOutOfBoundsException e) {
          if (Config.DEVELOPER) {
            System.out.println("Set enchant error: " + e);
          }
          activeChar.sendAdmResultMessage("Please specify a new enchant value.");
        } catch (NumberFormatException e) {
          if (Config.DEVELOPER) {
            System.out.println("Set enchant error: " + e);
          }
          activeChar.sendAdmResultMessage("Please specify a valid new enchant value.");
        }

      }

      showMainPage(activeChar);
    }

    return true;
  }

  private void setEnchant(L2PcInstance activeChar, int ench, int armorType)
  {
    L2Object target = activeChar.getTarget();
    if (target == null) {
      target = activeChar;
    }
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = target.getPlayer();
    } else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      return;
    }

    int curEnchant = 0;
    L2ItemInstance itemInstance = null;

    L2ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
    if ((parmorInstance != null) && (parmorInstance.getEquipSlot() == armorType)) {
      itemInstance = parmorInstance;
    }
    else {
      parmorInstance = player.getInventory().getPaperdollItem(14);
      if ((parmorInstance != null) && (parmorInstance.getEquipSlot() == 14)) {
        itemInstance = parmorInstance;
      }
    }

    if (itemInstance != null) {
      curEnchant = itemInstance.getEnchantLevel();

      player.getInventory().unEquipItemInSlotAndRecord(armorType);
      itemInstance.setEnchantLevel(ench, false);
      player.getInventory().equipItemAndRecord(itemInstance);

      InventoryUpdate iu = new InventoryUpdate();
      iu.addModifiedItem(itemInstance);
      player.sendPacket(iu);
      player.broadcastPacket(new CharInfo(player));
      player.sendPacket(new UserInfo(player));

      activeChar.sendAdmResultMessage("Changed enchantment of " + player.getName() + "'s " + itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");
      player.sendAdmResultMessage("Admin has changed the enchantment of your " + itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");

      GMAudit.auditGMAction(activeChar.getName(), "enchant", player.getName(), itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench);
    }
  }

  private void showMainPage(L2PcInstance activeChar) {
    AdminHelpPage.showHelpPage(activeChar, "enchant.htm");
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}