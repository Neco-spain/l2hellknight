package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Clan.SubPledge;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private static final String _S__68_PLEDGESHOWMEMBERLISTALL = "[S] 53 PledgeShowMemberListAll";
	private L2Clan _clan;
	private L2PcInstance _activeChar;
	private L2ClanMember[] _members;
	private int _pledgeType;
	//private static Logger _log = Logger.getLogger(PledgeShowMemberListAll.class.getName());

	public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
		_members = _clan.getMembers();
	}

	@Override
	protected final void writeImpl()
	{

		_pledgeType = 0;
		writePledge(0);

		SubPledge[] subPledge = _clan.getAllSubPledges();
		for (int i = 0; i<subPledge.length; i++)
		{
			_activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge[i]));
		}

		for (L2ClanMember m : _members)
		{
            if (m.getPledgeType() == 0) 
            	continue;
			_activeChar.sendPacket(new PledgeShowMemberListAdd(m));
		}

		// unless this is sent sometimes, the client doesn't recognise the player as the leader
		_activeChar.sendPacket(new UserInfo(_activeChar));

	}

	void writePledge(int mainOrSubpledge)
	{
		writeC(0x53);

		writeD(mainOrSubpledge); //c5 main clan 0 or any subpledge 1?
		writeD(_clan.getClanId());
		writeD(_pledgeType); //c5 - possibly pledge type?
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());

		writeD(_clan.getCrestId()); // crest id .. is used again
		writeD(_clan.getLevel());
		writeD(_clan.getHasCastle());
		writeD(_clan.getHasHideout());
		writeD(_clan.getRank()); // not confirmed
		writeD(_clan.getReputationScore()); //was activechar lvl
		writeD(0); //0
		writeD(0); //0

		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
        writeD(_clan.isAtWar());// new c3
		writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		
		int yellow;
		for (L2ClanMember m : _members)
		{
    		if(m.getPledgeType() != _pledgeType) 
    			continue;
    		if (m.getPledgeType() == -1)
    			yellow = m.getSponsor() != 0 ? 1 : 0;
    		else if (m.getPlayerInstance() != null)
    			yellow = m.getPlayerInstance().isClanLeader() ? 1 : 0;
    		else
    			yellow = 0;	
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			writeD(0); // no visible effect
			writeD(m.getObjectId());//writeD(1);
			writeD(m.isOnline() ? 1 : 0);  // 1=online 0=offline
			writeD(yellow); //c5 makes the name yellow. member is in academy and has a sponsor
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__68_PLEDGESHOWMEMBERLISTALL;
	}

}
