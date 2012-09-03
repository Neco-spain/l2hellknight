package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;

public class PartySmallWindowUpdate extends L2GameServerPacket
{
	private int obj_id, class_id, level, vitality;
	private int curCp, maxCp, curHp, maxHp, curMp, maxMp;
	private String obj_name;

	public PartySmallWindowUpdate(L2Player member)
	{
		obj_id = member.getObjectId();
		obj_name = member.getName();
		curCp = (int) member.getCurrentCp();
		maxCp = member.getMaxCp();
		curHp = (int) member.getCurrentHp();
		maxHp = member.getMaxHp();
		curMp = (int) member.getCurrentMp();
		maxMp = member.getMaxMp();
		level = member.getLevel();
		class_id = member.getClassId().getId();
		if (member.isAwaking())
			class_id = member.getAwakingId();
		vitality = (int) member.getVitality() * 2;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x52);
		writeD(obj_id);
		writeS(obj_name);
		writeD(curCp);
		writeD(maxCp);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
		writeD(class_id);
		writeD(vitality); //vitality
		writeD(0x00); // Идет ли поиск замены 0x00 нет, 0х01 - да

	}
}