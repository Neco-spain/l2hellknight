package l2rt.gameserver.network.clientpackets;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;

public class Logout extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// Dont allow leaving if player is fighting
		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_EXIT_WHILE_IN_COMBAT, Msg.ActionFail);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING, Msg.ActionFail);
			return;
		}

		if(activeChar.isBlocked() && !activeChar.isFlying()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
		{
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.Logout.OutOfControl", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.Logout.Olympiad", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.inObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.Logout.Observer", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		activeChar.logout(false, false, false, false);
	}
}