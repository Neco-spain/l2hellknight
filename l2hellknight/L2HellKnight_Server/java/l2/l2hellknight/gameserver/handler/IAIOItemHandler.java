package l2.hellknight.gameserver.handler;

import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public interface IAIOItemHandler
{
	public String getBypass();
	
	public void onBypassUse(L2PcInstance player, String command);
}