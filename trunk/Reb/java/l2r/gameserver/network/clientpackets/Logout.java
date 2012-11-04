package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone;
import l2r.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class Logout extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// Dont allow leaving if player is fighting
		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_EXIT_THE_GAME_WHILE_IN_COMBAT);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isBlocked() && !activeChar.isFlying()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.Logout.OutOfControl", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFestivalParticipant())
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				activeChar.sendMessage("You cannot log out while you are a participant in a festival.");
				activeChar.sendActionFailed();
				return;
			}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.Logout.Olympiad", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getVar("isPvPevents") != null)
		{
			activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можите выйти во время участия в ивенте!" : "You can follow any responses did not leave while participating in the event!");
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode() && !activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE)
		{
			activeChar.sendMessage(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInObserverMode())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.clientpackets.Logout.Observer", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		activeChar.logout();
	}
}