package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.NpcHtmlMessage;

public class AdminEnchant
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditChar) {
      return false;
    }
    int armorType = -1;

    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminEnchant$Commands[command.ordinal()])
    {
    case 1:
      showMainPage(activeChar);
      return true;
    case 2:
      armorType = 6;
      break;
    case 3:
      armorType = 10;
      break;
    case 4:
      armorType = 9;
      break;
    case 5:
      armorType = 12;
      break;
    case 6:
      armorType = 11;
      break;
    case 7:
      armorType = 7;
      break;
    case 8:
      armorType = 8;
      break;
    case 9:
      armorType = 2;
      break;
    case 10:
      armorType = 1;
      break;
    case 11:
      armorType = 5;
      break;
    case 12:
      armorType = 4;
      break;
    case 13:
      armorType = 3;
      break;
    case 14:
      armorType = 0;
      break;
    case 15:
      armorType = 13;
      break;
    case 16:
      armorType = 15;
      break;
    case 17:
      armorType = 15;
      break;
    case 18:
      armorType = 18;
      break;
    case 19:
      armorType = 17;
      break;
    case 20:
      armorType = 25;
    }

    if ((armorType == -1) || (wordList.length < 2))
    {
      showMainPage(activeChar);
      return true;
    }

    try
    {
      int ench = Integer.parseInt(wordList[1]);
      if ((ench < 0) || (ench > 65535))
        activeChar.sendMessage("You must set the enchant level to be between 0-65535.");
      else
        setEnchant(activeChar, ench, armorType);
    }
    catch (StringIndexOutOfBoundsException e)
    {
      activeChar.sendMessage("Please specify a new enchant value.");
    }
    catch (NumberFormatException e)
    {
      activeChar.sendMessage("Please specify a valid new enchant value.");
    }

    showMainPage(activeChar);
    return true;
  }

  private void setEnchant(Player activeChar, int ench, int armorType)
  {
    GameObject target = activeChar.getTarget();
    if (target == null)
      target = activeChar;
    if (!target.isPlayer())
    {
      activeChar.sendMessage("Wrong target type.");
      return;
    }

    Player player = (Player)target;

    int curEnchant = 0;

    ItemInstance itemInstance = player.getInventory().getPaperdollItem(armorType);

    if (itemInstance != null)
    {
      curEnchant = itemInstance.getEnchantLevel();

      player.getInventory().unEquipItem(itemInstance);
      itemInstance.setEnchantLevel(ench);
      player.getInventory().equipItem(itemInstance);

      player.sendPacket(new InventoryUpdate().addModifiedItem(itemInstance));
      player.broadcastCharInfo();

      activeChar.sendMessage("Changed enchantment of " + player.getName() + "'s " + itemInstance.getName() + " from " + curEnchant + " to " + ench + ".");
      player.sendMessage("Admin has changed the enchantment of your " + itemInstance.getName() + " from " + curEnchant + " to " + ench + ".");
    }
  }

  public void showMainPage(Player activeChar)
  {
    GameObject target = activeChar.getTarget();
    if (target == null)
      target = activeChar;
    Player player = activeChar;
    if (target.isPlayer()) {
      player = (Player)target;
    }
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

    StringBuilder replyMSG = new StringBuilder("<html><body>");
    replyMSG.append("<center><table width=260><tr><td width=40>");
    replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
    replyMSG.append("</td><td width=180>");
    replyMSG.append("<center>Enchant Equip for player: " + player.getName() + "</center>");
    replyMSG.append("</td><td width=40>");
    replyMSG.append("</td></tr></table></center><br>");
    replyMSG.append("<center><table width=270><tr><td>");
    replyMSG.append("<button value=\"Shirt\" action=\"bypass -h admin_setun $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Helmet\" action=\"bypass -h admin_seteh $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Cloak\" action=\"bypass -h admin_setba $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Mask\" action=\"bypass -h admin_setha $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Necklace\" action=\"bypass -h admin_seten $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
    replyMSG.append("</center><center><table width=270><tr><td>");
    replyMSG.append("<button value=\"Weapon\" action=\"bypass -h admin_setew $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Chest\" action=\"bypass -h admin_setec $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Shield\" action=\"bypass -h admin_setes $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Earring\" action=\"bypass -h admin_setre $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Earring\" action=\"bypass -h admin_setle $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
    replyMSG.append("</center><center><table width=270><tr><td>");
    replyMSG.append("<button value=\"Gloves\" action=\"bypass -h admin_seteg $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Leggings\" action=\"bypass -h admin_setel $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Boots\" action=\"bypass -h admin_seteb $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Ring\" action=\"bypass -h admin_setrf $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Ring\" action=\"bypass -h admin_setlf $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
    replyMSG.append("</center><center><table width=270><tr><td>");
    replyMSG.append("<button value=\"Hair\" action=\"bypass -h admin_setdha $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"R-Bracelet\" action=\"bypass -h admin_setrbr $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"L-Bracelet\" action=\"bypass -h admin_setlbr $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td><td>");
    replyMSG.append("<button value=\"Belt\" action=\"bypass -h admin_setbelt $menu_command\" width=50 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
    replyMSG.append("</center><br>");
    replyMSG.append("<center>[Enchant 0-65535]</center>");
    replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_seteh, 
    admin_setec, 
    admin_seteg, 
    admin_setel, 
    admin_seteb, 
    admin_setew, 
    admin_setes, 
    admin_setle, 
    admin_setre, 
    admin_setlf, 
    admin_setrf, 
    admin_seten, 
    admin_setun, 
    admin_setba, 
    admin_setha, 
    admin_setdha, 
    admin_setlbr, 
    admin_setrbr, 
    admin_setbelt, 
    admin_enchant;
  }
}