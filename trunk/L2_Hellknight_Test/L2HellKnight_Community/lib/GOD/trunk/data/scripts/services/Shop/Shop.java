package services.Shop;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.instancemanager.TownManager;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.instances.L2HennaInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.HennaEquipList;
import l2rt.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.HennaTreeTable;
import l2rt.util.Files;
import l2rt.util.Location;

public class Shop extends Functions implements ScriptFile
{
	public String DialogAppend_50004(Integer val)
	{
		return Files.read("data/scripts/services/Shop/LearnSkill.htm");
	}
	
		public String DialogAppend_50003(Integer val)
	{
		return Files.read("data/scripts/services/Shop/LearnSkillClan.htm");
	}
		
	public String DialogAppend_50007(Integer val)
	{
		return Files.read("data/scripts/services/Shop/info-1.htm");
	}

	public String DialogAppend_50001(Integer val)
	{
		return Files.read("data/scripts/services/Shop/ShopOne-1.htm");
	}
	
	public String DialogAppend_50002(Integer val)
	{
		return Files.read("data/scripts/services/Shop/ShopTwo-1.htm");
	}
	
	public String DialogAppend_50006(Integer val)
	{
		return Files.read("data/scripts/services/Shop/ShopDye.htm");
	}
	
	public String DialogAppend_50008(Integer val)
	{
		return Files.read("data/scripts/services/Shop/Teleport.htm");
	}
	
	public void Teleporter(String[] param)
	{
		
		L2Player player = (L2Player) getSelf();
		if(player == null)
			return;

		int Item = Integer.parseInt (param[3]);				
		long price = Long.parseLong(param[4]);

				
		if(!L2NpcInstance.canBypassCheck(player, player.getLastNpc()))
			return;
				
		if(player.getInventory().getItemByItemId(Item) == null)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}

		if(player.isTerritoryFlagEquipped() || player.isCombatFlagEquipped())
		{
			player.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return;
		}

		if(player.getMountType() == 2)
		{
			player.sendMessage("Телепортация верхом на виверне невозможна.");
			return;
		}

		int x = Integer.parseInt(param[0]);
		int y = Integer.parseInt(param[1]);
		int z = Integer.parseInt(param[2]);

		// Нельзя телепортироваться в города, где идет осада
		// Узнаем, идет ли осада в ближайшем замке к точке телепортации
		Castle castle = TownManager.getInstance().getClosestTown(x, y).getCastle();
		if(castle != null && castle.getSiege().isInProgress())
		{
			// Определяем, в город ли телепортируется чар
			boolean teleToTown = false;
			int townId = 0;
			for(L2Zone town : ZoneManager.getInstance().getZoneByType(ZoneType.Town))
				if(town.checkIfInZone(x, y))
				{
					teleToTown = true;
					townId = town.getIndex();
					break;
				}

			if(teleToTown && townId == castle.getTown())
			{
				player.sendPacket(Msg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
		}

		Location pos = GeoEngine.findPointToStay(x, y, z, 50, 100, player.getReflection().getGeoIndex());

		if(price > 0)
			removeItem(player, Item, price);
		player.teleToLocation(pos);
	}

	public void removeDye()
	{
		L2Player player = (L2Player) getSelf();
		StringBuffer html1 = new StringBuffer("<html><body>");
		html1.append("Выберете татуировку которую Вы хотите свести:<br><br>");
		boolean hasHennas = false;
		for(int i = 1; i <= 3; i++)
		{
			L2HennaInstance henna = player.getHenna(i);
			if(henna != null)
			{
				hasHennas = true;
				html1.append("<a action=\"bypass -h scripts_services.Shop.Shop:removeDye " + i + "\">" + henna.getName() + "</a><br>");
			}
		}
		if(!hasHennas)
			html1.append("У Вас нет татуировок для сведения!");
		html1.append("</body></html>");

		show(html1.toString(), player);
	}
	
	public void addDye()
	{
		L2Player player = (L2Player) getSelf();
		L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(player.getClassId(), player.getSex());
		HennaEquipList hel = new HennaEquipList(player, henna);
		player.sendPacket(hel);
	}
	
	public void showHtml(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		String page = args[0];
		show(Files.read("data/scripts/services/Shop/" + page + ".htm", player), player, npc);
	}

	public void removeDye(String[] args)
	{
		L2Player player = (L2Player) getSelf();
		int slot = Integer.parseInt(args[0]);
		player.removeHenna(slot);
		
	}
	/**
	 * Clan Reputation Trader
	 * @author IDEASERV
	 * 
	 * @param player
	 * @param command
	 */
	public void ClanRepTrader(L2Player player, String command)
	{
        if(command.startsWith("buyclanrep"))
		{

			int itemId = Integer.parseInt(command.substring(9).trim());

			int reputation = 0;
			long itemCount = 0;

			L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
			long playerItemCount = item == null ? 0 : item.getCount();

			switch(itemId)
			{
				case 21000:
					reputation = 1000;
					itemCount = 1;
					break;
					
				case 21001:
					reputation = 1000;
					itemCount = 10;
					break;
					
				case 21002:
					reputation = 1000;
					itemCount = 10;
					break;					
			}

			if(playerItemCount >= itemCount)
			{
				player.getInventory().destroyItemByItemId(itemId, itemCount, true);
				player.getClan().incReputation(reputation, false, "ClanTrader " + itemId + " from " + player.getName());
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
				player.sendPacket(new SystemMessage(SystemMessage.YOUR_CLAN_HAS_ADDED_1S_POINTS_TO_ITS_CLAN_REPUTATION_SCORE).addNumber(reputation));

			}
		}
	}
	
	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void onReload() {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void onShutdown() {
		// TODO Auto-generated method stub
		
	}
}