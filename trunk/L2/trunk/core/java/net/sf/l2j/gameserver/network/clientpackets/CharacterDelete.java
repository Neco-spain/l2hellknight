package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.CharDeleteFail;
import net.sf.l2j.gameserver.network.serverpackets.CharDeleteOk;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;

public final class CharacterDelete extends L2GameClientPacket
{
	private static final String _C__0C_CHARACTERDELETE = "[C] 0C CharacterDelete";
	private static Logger _log = Logger.getLogger(CharacterDelete.class.getName());

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
		if (Config.DEBUG) _log.fine("deleting slot:" + _charSlot);

		L2PcInstance character = null;
		try
		{
		    if (Config.DELETE_DAYS == 0)
				character = getClient().deleteChar(_charSlot);
		    else
				character = getClient().markToDeleteChar(_charSlot);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error:", e);
		}

		if (character == null)
		{
			sendPacket(new CharDeleteOk());
		}
		else
		{
			if (character.isClanLeader())
			{
				sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
			}
			else
			{
				sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
			}
			if (character.isChatBanned())
			{
				sendPacket(new CharDeleteFail(CharDeleteFail.REASON_DELETION_FAILED));
			}
		}

		CharSelectInfo cl = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

	@Override
	public String getType()
	{
		return _C__0C_CHARACTERDELETE;
	}
}
