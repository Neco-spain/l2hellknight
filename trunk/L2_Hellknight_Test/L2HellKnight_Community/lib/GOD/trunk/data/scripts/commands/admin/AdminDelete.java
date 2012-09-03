package commands.admin;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.handler.IAdminCommandHandler;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SpawnTable;

public class AdminDelete implements IAdminCommandHandler, ScriptFile
{
	private static enum Commands
	{
		admin_delete
	}

	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, L2Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().CanEditNPC)
			return false;

		switch(command)
		{
			case admin_delete:
				L2Object obj = activeChar.getTarget();
				if(obj != null && obj.isNpc())
				{
					L2NpcInstance target = (L2NpcInstance) obj;
					target.decayMe();
					
					L2Spawn spawn = target.getSpawn();
					if(spawn != null)
						spawn.stopRespawn();	
					
						if (Config.ALT_SAVE_SPAWN)
						{
							SpawnTable.getInstance().deleteSpawn(spawn, true);	
						}
				}
				else
					activeChar.sendPacket(Msg.INVALID_TARGET);
				break;
		}

		return true;
	}

	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
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