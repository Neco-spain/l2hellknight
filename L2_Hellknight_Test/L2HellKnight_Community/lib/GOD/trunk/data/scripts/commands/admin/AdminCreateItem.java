package commands.admin;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ItemList;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Log;
import l2rt.util.Rnd;

public class AdminCreateItem implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_itemcreate,
		admin_create_item,
		admin_create_item_target,
		admin_spreaditem
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().UseGMShop)
			return false;

		switch(command)
		{
			case admin_itemcreate:
				AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				break;
			case admin_create_item:
				try
				{
					if(wordList.length == 3)
						createItem(activeChar, Integer.parseInt(wordList[1]), Long.parseLong(wordList[2]));
					else if(wordList.length == 2)
						createItem(activeChar, Integer.parseInt(wordList[1]), 1);					
					else if(wordList.length == 4)
					{
						for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())				
							createItem(player, Integer.parseInt(wordList[1]), Long.parseLong(wordList[2]));
					}
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("Specify a valid number.");
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Can't create this item.");
				}
				AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				break;
			case admin_create_item_target:
				try
				{
					if(wordList.length == 3)
						createItem((L2Player)activeChar.getTarget(), Integer.parseInt(wordList[1]), Long.parseLong(wordList[2]));
					else if(wordList.length == 2)
						createItem((L2Player)activeChar.getTarget(), Integer.parseInt(wordList[1]), 1);
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("Specify a valid number.");
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Can't create this item.");
				}
				AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				break;
			case admin_spreaditem:
				try
				{
					int id = Integer.parseInt(wordList[1]);
					int num = wordList.length > 2 ? Integer.parseInt(wordList[2]) : 1;
					long count = wordList.length > 3 ? Long.parseLong(wordList[3]) : 1;
					for(int i = 0; i < num; i++)
					{
						L2ItemInstance createditem = ItemTemplates.getInstance().createItem(id);
						createditem.setCount(count);
						createditem.dropToTheGround(activeChar, Rnd.coordsRandomize(activeChar, 100));
					}
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("Specify a valid number.");
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Can't create this item.");
				}
				break;
		}

		return true;
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void createItem(L2Player activeChar, int id, long num)
	{
		L2ItemInstance createditem = ItemTemplates.getInstance().createItem(id);
		createditem.setCount(num);
		activeChar.getInventory().addItem(createditem);
		Log.LogItem(activeChar, Log.Adm_AddItem, createditem);
		if(!createditem.isStackable())
			for(long i = 0; i < num - 1; i++)
			{
				createditem = ItemTemplates.getInstance().createItem(id);
				activeChar.getInventory().addItem(createditem);
				Log.LogItem(activeChar, Log.Adm_AddItem, createditem);
			}
		activeChar.sendPacket(new ItemList(activeChar, true), SystemMessage.obtainItems(id, num, 0));
	}

	public void onLoad()
	{
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}