package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.SkillCoolTime;

public class RequestSkillCoolTime extends L2GameClientPacket
{
	L2GameClient _client;

	@Override
	public void readImpl()
	{
		_client = getClient();
	}

	@Override
	public void runImpl()
	{
		L2Player pl = _client.getActiveChar();
		if(pl != null)
			pl.sendPacket(new SkillCoolTime(pl));
	}
}