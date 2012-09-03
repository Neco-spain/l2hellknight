package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Summon;
import l2rt.util.GArray;

import java.util.Collection;

public class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PARTY1 = 0x00001; // party member
	public static final int RELATION_PARTY2 = 0x00002; // party member
	public static final int RELATION_PARTY3 = 0x00004; // party member
	public static final int RELATION_PARTY4 = 0x00008; // party member (for information, see L2PcInstance.getRelation())
	public static final int RELATION_PARTYLEADER = 0x00010; // true if is party leader
	public static final int RELATION_HAS_PARTY = 0x00020; // true if is in party
	public static final int RELATION_CLAN_MEMBER = 0x00040; // true if is in clan
	public static final int RELATION_LEADER = 0x00080; // true if is clan leader
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x04000; // double fist
	public static final int RELATION_1SIDED_WAR = 0x08000; // single fist
	public static final int RELATION_ISINTERRITORYWARS = 0x80000; // Territory Wars

	protected static final Collection<L2GameServerPacket> empty = new GArray<L2GameServerPacket>(0);

	protected final GArray<RelationChangedData> _data = new GArray<RelationChangedData>(2);

	protected RelationChanged(RelationChangedData data)
	{
		add(data);
	}

	protected void add(RelationChangedData data)
	{
		_data.add(data);
	}

	protected void writeRelationChanged(RelationChangedData data)
	{
		writeD(data.charObjId);
		writeD(data.relation);
		writeD(data.isAutoAttackable ? 1 : 0);
		writeD(data.karma);
		writeD(data.pvpFlag);
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xCE);
		writeD(_data.size());
		for(RelationChangedData d : _data)
			writeRelationChanged(d);
	}

	static class RelationChangedData
	{
		public final int charObjId;
		public final boolean isAutoAttackable;
		public final int relation, karma, pvpFlag;

		public RelationChangedData(L2Playable cha, boolean _isAutoAttackable, int _relation)
		{
			isAutoAttackable = _isAutoAttackable;
			relation = _relation;
			charObjId = cha.getObjectId();
			karma = cha.getKarma();
			pvpFlag = cha.getPvpFlag();
		}
	}

	/**
	 * @param targetChar игрок, отношение к которому изменилось
	 * @param activeChar игрок, которому будет отослан пакет с результатом
	 */
	public static Collection<L2GameServerPacket> update(L2Player sendTo, L2Player targetChar, L2Player activeChar)
	{
		if(sendTo == null || targetChar == null || activeChar == null || targetChar.isInOfflineMode())
			return empty;

		Collection<L2GameServerPacket> ret = new GArray<L2GameServerPacket>(2);
		boolean newVer = sendTo.getRevision() >= 152;
		L2Summon pet = targetChar.getPet();
		int relation = targetChar.getRelation(activeChar);

		RelationChangedData d1 = new RelationChangedData(targetChar, targetChar.isAutoAttackable(activeChar), relation);
		if(newVer)
		{
			RelationChanged pkt = new RelationChanged(d1);
			if(pet != null)
				pkt.add(new RelationChangedData(pet, pet.isAutoAttackable(activeChar), relation));
			ret.add(pkt);
		}
		else
		{
			ret.add(new RelationChangedOld(d1));
			if(pet != null)
				ret.add(new RelationChangedOld(new RelationChangedData(pet, pet.isAutoAttackable(activeChar), relation)));
		}
		return ret;
	}
}