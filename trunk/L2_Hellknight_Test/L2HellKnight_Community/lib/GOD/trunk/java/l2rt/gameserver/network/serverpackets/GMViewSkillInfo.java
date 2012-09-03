package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private String _charName;
	private GArray<L2Skill> _skills = new GArray<L2Skill>();
	private boolean _isClanSkillsDisabled;

	public GMViewSkillInfo(L2Player cha)
	{
		_charName = cha.getName();
		for(L2Skill s : cha.getAllSkills())
			_skills.add(s);
		_isClanSkillsDisabled = cha.getClan() != null && cha.getClan().getReputationScore() < 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x97);
		writeS(_charName);
		writeD(_skills.size());
		for(L2Skill skill : _skills)
		{
			// Сомнительное условие
			if(skill.getId() > 9000)
				continue; // fake skills to change base stats

			writeD(skill.isLikePassive() ? 1 : 0);
			writeD(skill.getDisplayLevel());
			writeD(skill.getId());
			writeD(-1);
			writeC(_isClanSkillsDisabled && skill.isClanSkill() ? 1 : 0);
			writeC(SkillTable.getInstance().getMaxLevel(skill.getId()) > 100 ? 1 : 0);
		}
	}
}