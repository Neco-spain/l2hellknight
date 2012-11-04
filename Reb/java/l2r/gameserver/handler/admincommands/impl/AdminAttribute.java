package l2r.gameserver.handler.admincommands.impl;

//import l2r.extensions.scripts.ScriptFile;
//import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminAttribute implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_setatreh, // 6
		admin_setatrec, // 10
		admin_setatreg, // 9
		admin_setatrel, // 11
		admin_setatreb, // 12
		admin_setatrew, // 7
		admin_setatres, // 8
		admin_setatrle, // 1
		admin_setatrre, // 2
		admin_setatrlf, // 4
		admin_setatrrf, // 5
		admin_setatren, // 3
		admin_setatrun, // 0
		admin_setatrbl, // 24
		admin_attribute
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditChar)
			return false;

		int armorType = -1;

		switch(command)
		{
			case admin_attribute:
				showMainPage(activeChar);
				return true;
			case admin_setatreh:
				armorType = Inventory.PAPERDOLL_HEAD;
				break;
			case admin_setatrec:
				armorType = Inventory.PAPERDOLL_CHEST;
				break;
			case admin_setatreg:
				armorType = Inventory.PAPERDOLL_GLOVES;
				break;
			case admin_setatreb:
				armorType = Inventory.PAPERDOLL_FEET;
				break;
			case admin_setatrel:
				armorType = Inventory.PAPERDOLL_LEGS;
				break;
			case admin_setatrew:
				armorType = Inventory.PAPERDOLL_RHAND;
				break;
			case admin_setatres:
				armorType = Inventory.PAPERDOLL_LHAND;
				break;
			case admin_setatrle:
				armorType = Inventory.PAPERDOLL_LEAR;
				break;
			case admin_setatrre:
				armorType = Inventory.PAPERDOLL_REAR;
				break;
			case admin_setatrlf:
				armorType = Inventory.PAPERDOLL_LFINGER;
				break;
			case admin_setatrrf:
				armorType = Inventory.PAPERDOLL_RFINGER;
				break;
			case admin_setatren:
				armorType = Inventory.PAPERDOLL_NECK;
				break;
			case admin_setatrun:
				armorType = Inventory.PAPERDOLL_UNDER;
				break;
			case admin_setatrbl:
				armorType = Inventory.PAPERDOLL_BELT;
				break;
		}

		if(armorType == -1 || wordList.length < 2 || activeChar.getInventory().getPaperdollItem(armorType) == null)
		{
			showMainPage(activeChar);
			return true;
		}

		try
		{

			int ench = Integer.parseInt(wordList[1]);
			byte element = -2;

			if(wordList[2].equals("Fire"))
				element = 0;
			if(wordList[2].equals("Water"))
				element = 1;
			if(wordList[2].equals("Wind"))
				element = 2;
			if(wordList[2].equals("Earth"))
				element = 3;
			if(wordList[2].equals("Holy"))
				element = 4;
			if(wordList[2].equals("Dark"))
				element = 5;

			if(ench < 0 || ench > 600)
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Допустимое значение для заточки атрибутом является значение от 0 до 600.");
				else
					activeChar.sendMessage("You must set the enchant level for ARMOR to be between 0-600.");
			}
			else
				setEnchant(activeChar, ench, element, armorType);
		}
		catch(StringIndexOutOfBoundsException e)
		{
			if(activeChar.isLangRus())
				activeChar.sendMessage("Пожалуйста, укажите новое значение для заточки.");
			else
				activeChar.sendMessage("Please specify a new enchant value.");
		}
		catch(NumberFormatException e)
		{
			if(activeChar.isLangRus())
				activeChar.sendMessage("Пожалуйста, правильное значение для заточки.");
			else
				activeChar.sendMessage("Please specify a valid new enchant value.");
		}

		// show the enchant menu after an action
		showMainPage(activeChar);
		return true;
	}

	private void setEnchant(Player activeChar, int value, byte element, int armorType)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
			target = activeChar;
		if(!target.isPlayer())
		{
			if(activeChar.isLangRus())
				activeChar.sendMessage("Неверный тип цели.");
			else
				activeChar.sendMessage("Wrong target type.");
			return;
		}
                
                Element El = Element.NONE;
		switch(element)
		{
			case 0:
				El = Element.FIRE;
				break;
			case 1:
				El = Element.WATER;
				break;
			case 2:
				El = Element.WIND;
				break;
			case 3:
				El = Element.EARTH;
				break;
			case 4:
				El = Element.HOLY;
				break;
			case 5:
				El = Element.UNHOLY;
				break;
		}             

		Player player = (Player) target;

		int curEnchant = 0;

		ItemInstance item = player.getInventory().getPaperdollItem(armorType);
		curEnchant = item.getEnchantLevel();
		if(item != null)
		{
			if(item.isWeapon())
			{
				item.setAttributeElement(El, value);
				activeChar.getInventory().equipItem(item);
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item));
				activeChar.broadcastUserInfo(true);
			}
			if(item.isArmor())
			{
				if(!canEnchantArmorAttribute(element, item))
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Невозможно вставить аттрибут в броню, не соблюдены условия.");
					else
						activeChar.sendMessage("Unable to insert an attribute in the armor, not the conditions.");
					return;
				}

				activeChar.getInventory().unEquipItem(item);
				item.setAttributeElement(El, value);
				activeChar.getInventory().equipItem(item);
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item));
				activeChar.broadcastUserInfo(true);
			}
			String ElementName = "";
			if(activeChar.isLangRus())
			{
				switch(element)
				{
					case 0:
						ElementName = "Огня";
						break;
					case 1:
						ElementName = "Воды";
						break;
					case 2:
						ElementName = "Ветра";
						break;
					case 3:
						ElementName = "Земли";
						break;
					case 4:
						ElementName = "Святости";
						break;
					case 5:
						ElementName = "Тьмы";
						break;
				}
				activeChar.sendMessage("Вы изменили Атрибут " + ElementName + " на " + value + " в " + item.getName() + " +" + curEnchant + ".");
				player.sendMessage("Админ изменил значение Атрибута " + ElementName + " на " + value + " в " + item.getName() + " +" + curEnchant + ".");
			}
			else
			{
				switch(element)
				{
					case 0:
						ElementName = "Fire";
						break;
					case 1:
						ElementName = "Water";
						break;
					case 2:
						ElementName = "Wind";
						break;
					case 3:
						ElementName = "Earth";
						break;
					case 4:
						ElementName = "Holy";
						break;
					case 5:
						ElementName = "Dark";
						break;
				}
				activeChar.sendMessage("You have changed attribute " + ElementName + " on " + value + " in " + item.getName() + " +" + curEnchant + ".");
				player.sendMessage("Admin has changed the value of the attribute " + ElementName + " on " + value + " in " + item.getName() + " +" + curEnchant + ".");

			}
		}
	}

	private boolean canEnchantArmorAttribute(int attr, ItemInstance item)
	{
		switch(attr)
		{
			case 0:
				if(item.getDefenceWater() != 0)
					return false;
				break;
			case 1:
				if(item.getDefenceFire() != 0)
					return false;
				break;
			case 2:
				if(item.getDefenceEarth() != 0)
					return false;
				break;
			case 3:
				if(item.getDefenceWind() != 0)
					return false;
				break;
			case 4:
				if(item.getDefenceUnholy() != 0)
					return false;
				break;
			case 5:
				if(item.getDefenceHoly() != 0)
					return false;
				break;
		}
		return true;
	}

	private void showMainPage(Player activeChar)
	{
		if(activeChar.isLangRus())
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html-ru/admin/attribute.htm"));
		else
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html-en/admin/attribute.htm"));
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}