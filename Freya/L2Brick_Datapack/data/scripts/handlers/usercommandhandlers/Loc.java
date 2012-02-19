package handlers.usercommandhandlers;

import l2.brick.gameserver.handler.IUserCommandHandler;
import l2.brick.gameserver.instancemanager.MapRegionManager;
import l2.brick.gameserver.instancemanager.ZoneManager;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.base.Race;
import l2.brick.gameserver.model.zone.type.L2RespawnZone;
import l2.brick.gameserver.network.serverpackets.SystemMessage;

public class Loc implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		0
	};
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IUserCommandHandler#useUserCommand(int, l2.brick.gameserver.model.actor.instance.L2PcInstance)
	 */
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		int region;
		L2RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, L2RespawnZone.class);
		
		if (zone != null)
			region = MapRegionManager.getInstance().getRestartRegion(activeChar, zone.getAllRespawnPoints().get(Race.Human)).getLocId();
		else
			region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
		
		SystemMessage sm = SystemMessage.getSystemMessage(region);
		if(sm.getSystemMessageId().getParamCount() == 3)
		{
			sm.addNumber(activeChar.getX());
			sm.addNumber(activeChar.getY());
			sm.addNumber(activeChar.getZ());
		}
		activeChar.sendPacket(sm);
		return true;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
