package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExReceiveShowPostFriend;

/**
 * @author VISTALL
 * @date 22:04/22.03.2011
 */
public class RequestExShowPostFriendListForPostBox extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{

	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		player.sendPacket(new ExReceiveShowPostFriend(player));
	}
}
