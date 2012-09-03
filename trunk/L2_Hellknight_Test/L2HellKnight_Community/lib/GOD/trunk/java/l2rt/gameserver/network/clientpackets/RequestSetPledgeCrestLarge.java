package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.CrestCache;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;

import java.util.logging.Logger;

public class RequestSetPledgeCrestLarge extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestSetPledgeCrestLarge.class.getName());
	private int _size;
	private byte[] _data;

	/**
	 * format: chd(b)
	 */
	@Override
	public void readImpl()
	{
		_size = readD();
		if(_size > _buf.remaining() || _size > Short.MAX_VALUE || _size <= 0)
			return;
		_data = new byte[_size];
		readB(_data);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_EDIT_CREST) == L2Clan.CP_CL_EDIT_CREST)
		{
			if(clan.getHasCastle() == 0 && clan.getHasHideout() == 0)
			{
				activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
				return;
			}

			if(clan.hasCrestLarge())
				CrestCache.removePledgeCrestLarge(clan);

			System.out.println("PCL size: " + _size);
			if(_data != null && _data.length <= 2176)
			{
				CrestCache.savePledgeCrestLarge(clan, _data);
				activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
			}

			clan.broadcastClanStatus(false, true, false);
		}
	}
}