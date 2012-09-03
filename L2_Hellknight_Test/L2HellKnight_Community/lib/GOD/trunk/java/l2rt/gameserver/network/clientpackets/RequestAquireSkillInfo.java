package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.AcquireSkillInfo;
import l2rt.gameserver.network.serverpackets.AcquireSkillList;
import l2rt.gameserver.network.serverpackets.ExAcquireSkillInfo;
import l2rt.gameserver.tables.SkillTable;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
	// format: cddd
	private int _id;
	private byte _level;
	private int _skillType;

	@Override
	public void readImpl()
	{
		_id = readD();
		_level = (byte) readD();
		_skillType = readD();// normal(0) learn or fisherman(1) clan(2) ? (3) transformation (4)
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getTransformation() != 0 || SkillTable.getInstance().getInfo(_id, _level) == null)
			return;
		L2NpcInstance trainer = activeChar.getLastNpc();
		if((trainer == null || activeChar.getDistance(trainer.getX(), trainer.getY()) > L2Character.INTERACTION_DISTANCE) && !activeChar.isGM())
			return;
		switch (_skillType)
		{
			case AcquireSkillList.USUAL:
				if (activeChar.isAwaking())
					sendPacket(new ExAcquireSkillInfo(_id, _level, activeChar.getAwakingClass(), 10));
				else 
					sendPacket(new ExAcquireSkillInfo(_id, _level, activeChar.getClassId(), 0));
				break;
			case AcquireSkillList.CLAN: 
				sendPacket(new AcquireSkillInfo(_id, _level, activeChar.getClassId(), activeChar.getClan(), _skillType));
				break;
			default:
				sendPacket(new AcquireSkillInfo(_id, _level, activeChar.getClassId(), null, _skillType));
		}
	}
}