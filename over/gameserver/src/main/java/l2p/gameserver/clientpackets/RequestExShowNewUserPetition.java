package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.ExResponseShowStepOne;

/**
 * @author VISTALL
 */
public class RequestExShowNewUserPetition extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null || !Config.EX_NEW_PETITION_SYSTEM)
			return;

		player.sendPacket(new ExResponseShowStepOne(player));
	}
}