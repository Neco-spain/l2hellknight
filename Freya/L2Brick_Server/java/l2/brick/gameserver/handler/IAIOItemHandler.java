package l2.brick.gameserver.handler;

import l2.brick.gameserver.model.actor.instance.L2PcInstance;

public interface IAIOItemHandler
{
	public String getBypass();
	
	public void onBypassUse(L2PcInstance player, String command);
}