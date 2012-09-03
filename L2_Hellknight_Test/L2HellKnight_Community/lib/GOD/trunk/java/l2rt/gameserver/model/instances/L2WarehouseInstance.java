package l2rt.gameserver.model.instances;

import l2rt.Config;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance.ItemClass;
import l2rt.gameserver.model.items.Warehouse;
import l2rt.gameserver.model.items.Warehouse.WarehouseType;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.network.serverpackets.PackageToList;
import l2rt.gameserver.network.serverpackets.WareHouseDepositList;
import l2rt.gameserver.network.serverpackets.WareHouseWithdrawList;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Log;

public final class L2WarehouseInstance extends L2NpcInstance
{
	public L2WarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if(val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		return "data/html/warehouse/" + pom + ".htm";
	}

	private void showRetrieveWindow(L2Player player, int val)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		player.sendPacket(new WareHouseWithdrawList(player, WarehouseType.PRIVATE, ItemClass.values()[val]));
	}

	private void showDepositWindow(L2Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		player.tempInventoryDisable();
		player.sendPacket(new WareHouseDepositList(player, WarehouseType.PRIVATE));
		player.sendActionFailed();
	}

	private void showDepositWindowClan(L2Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;

		if(player.getClan() == null)
		{
			player.sendActionFailed();
			return;
		}

		if(player.getClan().getLevel() == 0)
		{
			player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
			player.sendActionFailed();
			return;
		}

		player.tempInventoryDisable();

		if(!(player.isClanLeader() // забирать может лидер
				|| Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE && (player.getClanPrivileges() & L2Clan.CP_CL_WAREHOUSE_SEARCH) == L2Clan.CP_CL_WAREHOUSE_SEARCH || player.getVarB("canWhWithdraw"))) // выданы персональные права
			player.sendPacket(Msg.ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE);

		player.sendPacket(new WareHouseDepositList(player, WarehouseType.CLAN));
	}

	private void showWithdrawWindowClan(L2Player player, int val)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;

		if(player.getClan() == null)
		{
			player.sendActionFailed();
			return;
		}

		L2Clan _clan = player.getClan();

		if(_clan.getLevel() == 0)
		{
			player.sendPacket(Msg.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE);
			player.sendActionFailed();
			return;
		}

		if(/*Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE&&*/(player.getClanPrivileges() & L2Clan.CP_CL_WAREHOUSE_SEARCH) == L2Clan.CP_CL_WAREHOUSE_SEARCH)
		{
			player.tempInventoryDisable();
			player.sendPacket(new WareHouseWithdrawList(player, WarehouseType.CLAN, ItemClass.values()[val]));
		}
		else
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
			player.sendActionFailed();
		}
	}

	private void showWithdrawWindowFreight(L2Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;

		Warehouse list = player.getFreight();

		if(list != null)
			player.sendPacket(new WareHouseWithdrawList(player, WarehouseType.FREIGHT, ItemClass.ALL));

		player.sendActionFailed();
	}

	private void showDepositWindowFreight(L2Player player)
	{
		if(!player.getPlayerAccess().UseWarehouse)
			return;
		player.sendPacket(new PackageToList(player));
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(player.getEnchantScroll() != null)
		{
			Log.add("Player " + player.getName() + " trying to use enchant exploit[Warehouse], ban this player!", "illegal-actions");
			player.setEnchantScroll(null);
			return;
		}

		if(command.startsWith("WithdrawP"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 9)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("data/html/warehouse/personal.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
				showRetrieveWindow(player, val);
		}
		else if(command.equals("DepositP"))
			showDepositWindow(player);
		else if(command.startsWith("WithdrawC"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 9)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("data/html/warehouse/clan.htm");
				html.replace("%npcname%", getName());
				player.sendPacket(html);
			}
			else
				showWithdrawWindowClan(player, val);
		}
		else if(command.equals("DepositC"))
			showDepositWindowClan(player);
		else if(command.startsWith("WithdrawF"))
		{
			if(Config.ALLOW_FREIGHT)
				showWithdrawWindowFreight(player);
		}
		else if(command.startsWith("DepositF"))
		{
			if(Config.ALLOW_FREIGHT)
				showDepositWindowFreight(player);
		}
		else
			super.onBypassFeedback(player, command);
	}
}