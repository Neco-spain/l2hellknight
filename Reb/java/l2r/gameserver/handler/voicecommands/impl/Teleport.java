package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.scripts.Functions;

/**
 * @author 4ipolino
 */
public class Teleport implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "pvp", "farm", "farm_hard", "farm_low" };
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		command = command.intern();
		if(command.equalsIgnoreCase("pvp"))
			return pvp(command, activeChar, args);
		if(command.equalsIgnoreCase("farm"))
			return farm(command, activeChar, args);
		if(command.equalsIgnoreCase("farm_hard"))
			return farm_hard(command, activeChar, args);
		if(command.equalsIgnoreCase("farm_low"))
			return farm_low(command, activeChar, args);

		return false;
	}
	
	private boolean pvp(String command, Player activeChar, String args)
	{
		if (Config.COMMAND_PVP)
		{
		final int CoinCountPvP = Config.PRICE_PVP;
		if (command.equalsIgnoreCase("pvp"))
		{
			if(activeChar.isCursedWeaponEquipped() || activeChar.getReflectionId() != 0 || activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isFlying() || activeChar.isTerritoryFlagEquipped() || activeChar.isInZone(ZoneType.no_escape) || activeChar.isInZone(ZoneType.SIEGE) || activeChar.isInZone(ZoneType.epic))
			{
				activeChar.sendMessage("Телепортация невозможна");
				return false;
			}
   			if(CoinCountPvP != 0 && activeChar.getInventory().getItemByItemId(Config.PVP_TELEPORT_ITEM_ID).getCount() < CoinCountPvP)
   			{
   				activeChar.sendMessage("Вы не имеете достаточно денег");
   				activeChar.sendActionFailed();
   				return false;
   			}
			activeChar.teleToLocation(Config.PVP_X, Config.PVP_Y, Config.PVP_Z);
			Functions.removeItem(activeChar, Config.PVP_TELEPORT_ITEM_ID, CoinCountPvP);
			activeChar.sendMessage("Вы перемещены на PvP Арену");
		}
		}		
		return true;
	}
	private boolean farm(String command, Player activeChar, String args)
	{
		if (Config.COMMAND_FARM)
		{
		final int CoinCountFarm = Config.PRICE_FARM;
		if (command.equalsIgnoreCase("farm"))
		{
			if(activeChar.isCursedWeaponEquipped() || activeChar.getReflectionId() != 0 || activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isFlying() || activeChar.isTerritoryFlagEquipped() || activeChar.isInZone(ZoneType.no_escape) || activeChar.isInZone(ZoneType.SIEGE) || activeChar.isInZone(ZoneType.epic))
			{
				activeChar.sendMessage("Телепортация невозможна");
				return false;
			}
			
   			if(CoinCountFarm != 0 && activeChar.getInventory().getItemByItemId(Config.FARM_TELEPORT_ITEM_ID).getCount() < CoinCountFarm)
   			{
   				activeChar.sendMessage("Вы не имеете достаточно денег");
   				activeChar.sendActionFailed();
   				return false;
   			}
			activeChar.teleToLocation(Config.FARM_X, Config.FARM_Y, Config.FARM_Z);
			Functions.removeItem(activeChar, Config.FARM_TELEPORT_ITEM_ID, CoinCountFarm);
			activeChar.sendMessage("Вы перемещены в фарм зону");
		}
		}		
		return true;
	}
	
	private boolean farm_hard(String command, Player activeChar, String args)
	{
		if (Config.COMMAND_FARM_HARD)
		{
		final int CoinCountFarmH = Config.PRICE_FARM_HARD;
		if (command.equalsIgnoreCase("farm_hard"))
		{
			if(activeChar.isCursedWeaponEquipped() || activeChar.getReflectionId() != 0 || activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isFlying() || activeChar.isTerritoryFlagEquipped() || activeChar.isInZone(ZoneType.no_escape) || activeChar.isInZone(ZoneType.SIEGE) || activeChar.isInZone(ZoneType.epic))
			{
				activeChar.sendMessage("Телепортация невозможна");
				return false;
			}
			            
   			if(CoinCountFarmH != 0 && activeChar.getInventory().getItemByItemId(Config.FARM_HARD_TELEPORT_ITEM_ID).getCount() < CoinCountFarmH)
   			{
   				activeChar.sendMessage("Вы не имеете достаточно денег");
   				activeChar.sendActionFailed();
   				return false;
   			}
   			
			activeChar.teleToLocation(Config.FARM_HARD_X, Config.FARM_HARD_Y, Config.FARM_HARD_Z);
			Functions.removeItem(activeChar, Config.FARM_HARD_TELEPORT_ITEM_ID, CoinCountFarmH);
			activeChar.sendMessage("Вы перемещены в фарм зону");
		}
		}		
		return true;
	}
	private boolean farm_low(String command, Player activeChar, String args)
	{
		if (Config.COMMAND_FARM_LOW)
		{
		final int CoinCount = Config.PRICE_FARM_LOW;
		if (command.equalsIgnoreCase("farm_low"))
		{
			if(activeChar.isCursedWeaponEquipped() || activeChar.getReflectionId() != 0 || activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isFlying() || activeChar.isTerritoryFlagEquipped() || activeChar.isInZone(ZoneType.no_escape) || activeChar.isInZone(ZoneType.SIEGE) || activeChar.isInZone(ZoneType.epic))
			{
				activeChar.sendMessage("Телепортация невозможна");
				return false;
			}
			            
   			if(CoinCount != 0 && activeChar.getInventory().getItemByItemId(Config.FARM_LOW_TELEPORT_ITEM_ID).getCount() < CoinCount)
   			{
   				activeChar.sendMessage("Вы не имеете достаточно денег");
   				activeChar.sendActionFailed();
   				return false;
   			}
   			
			activeChar.teleToLocation(Config.FARM_LOW_X, Config.FARM_LOW_Y, Config.FARM_LOW_Z);
			Functions.removeItem(activeChar, Config.FARM_LOW_TELEPORT_ITEM_ID, CoinCount);
			activeChar.sendMessage("Вы перемещены в фарм зону");
		}
		}		
		return true;
	}
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
}