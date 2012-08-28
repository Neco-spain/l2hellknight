package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private static final String _S__54_PLEDGESHOWMEMBERLISTUPDATE = "[S] 54 PledgeShowMemberListUpdate";
	private L2PcInstance _activeChar;
	private int _pledgeType;
	private int _hasSponsor;
	private String _name;
	private int _level;
	private int _classId;
	private int _objectId;
	private int _isOnline;


	public PledgeShowMemberListUpdate(L2PcInstance player)
	{
		_activeChar = player;
		_pledgeType = player.getPledgeType();
		if(_pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _activeChar.getSponsor() != 0 ? 1 : 0;
		}
		else
			_hasSponsor = 0;
		{
			if (_activeChar.isOnline() == 1)
			{
				_hasSponsor = _activeChar.isClanLeader() ? 1 : 0;
			}
			else
			{
				_hasSponsor = 0;
			}
		}
		_name = _activeChar.getName();
		_level = _activeChar.getLevel();
		_classId = _activeChar.getClassId().getId();
		_objectId = _activeChar.getObjectId();
		_isOnline = _activeChar.isOnline();
	}

	public PledgeShowMemberListUpdate(L2ClanMember player)
	{
		_activeChar = player.getPlayerInstance();
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId();
		_objectId = player.getObjectId();
		if (player.isOnline())
			_isOnline = 1;
		else
			_isOnline = 0;
		_pledgeType = player.getPledgeType();
		if(_pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _activeChar.getSponsor() != 0 ? 1 : 0;
		}
		else
			_hasSponsor = 0;
		{
			if (player.isOnline())
			{
				_hasSponsor = _activeChar.isClanLeader() ? 1 : 0;
			}
			else
			{
				_hasSponsor = 0;
			}
		}
	}


	@Override
	protected final void writeImpl()
	{
		writeC(0x54);
		writeS(_name);
		writeD(_level);
		writeD(_classId);
		writeD(0);
		writeD(_objectId);
		writeD(_isOnline); // 1=online 0=offline
		writeD(_pledgeType);
		writeD(_hasSponsor);
	}

	@Override
	public String getType()
	{
		return _S__54_PLEDGESHOWMEMBERLISTUPDATE;
	}

}
