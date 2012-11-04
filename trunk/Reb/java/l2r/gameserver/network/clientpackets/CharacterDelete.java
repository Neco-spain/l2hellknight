package l2r.gameserver.network.clientpackets;


import l2r.gameserver.Config;
import l2r.gameserver.database.mysql;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.serverpackets.CharacterDeleteFail;
import l2r.gameserver.network.serverpackets.CharacterDeleteSuccess;
import l2r.gameserver.network.serverpackets.CharacterSelectionInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterDelete extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterDelete.class);

	// cd
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		int clan = clanStatus();
		int online = onlineStatus();
		if(clan > 0 || online > 0)
		{
			if(clan == 2)
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
			else if(clan == 1)
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
			else if(online > 0)
				sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_DELETION_FAILED));
			return;
		}

		GameClient client = getClient();
		try
		{
			if(Config.DELETE_DAYS == 0)
				client.deleteChar(_charSlot);
			else
				client.markToDeleteChar(_charSlot);
		}
		catch(Exception e)
		{
			_log.error("Error:", e);
		}

		sendPacket(new CharacterDeleteSuccess());

		CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}

	private int clanStatus()
	{
		int obj = getClient().getObjectIdForSlot(_charSlot);
		if(obj == -1)
			return 0;
		if(mysql.simple_get_int("clanid", "characters", "obj_Id=" + obj) > 0)
		{
			if(mysql.simple_get_int("leader_id", "clan_subpledges", "leader_id=" + obj + " AND type = " + Clan.SUBUNIT_MAIN_CLAN) > 0)
				return 2;
			return 1;
		}
		return 0;
	}

	private int onlineStatus()
	{
		int obj = getClient().getObjectIdForSlot(_charSlot);
		if(obj == -1)
			return 0;
		if(mysql.simple_get_int("online", "characters", "obj_Id=" + obj) > 0)
			return 1;
		return 0;
	}
}