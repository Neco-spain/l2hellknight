package handlers.aioitemhandler;

import l2.hellknight.gameserver.handler.IAIOItemHandler;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.network.serverpackets.WareHouseDepositList;
import l2.hellknight.gameserver.network.serverpackets.WareHouseWithdrawalList;
import l2.hellknight.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;

public class AIOWarehouseHandler implements IAIOItemHandler
{
	private static final String BYPASS = "ware";
	
	@Override
	public String getBypass() 
	{
		return BYPASS;
	}

	@Override
	public void onBypassUse(L2PcInstance player, String command) 
	{
		/*
		 * Handles the normal warehouse deposit
		 */
		if(command.equals("nDeposit"))
		{
			player.setIsUsingAIOItemWareHouse(true);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getWarehouse());
			player.tempInventoryDisable();
			player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
		}
		
		/*
		 * Handles the normal warehouse item withdraw
		 */
		else if(command.equals("nWithdraw"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getWarehouse());

			if (player.getActiveWarehouse().getSize() == 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
				return;
			}
			player.setIsUsingAIOItemWareHouse(true);
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
		}
		
		/*
		 * Handles the clan item deposit
		 */
		else if(command.equals("cDeposit"))
		{
			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
				return;
			}

			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
				return;
			}
			player.setIsUsingAIOItemWareHouse(true);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.tempInventoryDisable();
			player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.CLAN));
		}
		
		/*
		 * Handles the clan item withdraw
		 */
		else if(command.equals("cWithdraw"))
		{
			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
				return;
			}

			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
				return;
			}
			
			player.sendPacket(ActionFailed.STATIC_PACKET);

			if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
				return;
			}

			player.setActiveWarehouse(player.getClan().getWarehouse());

			if (player.getActiveWarehouse().getSize() == 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
				return;
			}
			player.setIsUsingAIOItemWareHouse(true);
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z));
		}
	}
}