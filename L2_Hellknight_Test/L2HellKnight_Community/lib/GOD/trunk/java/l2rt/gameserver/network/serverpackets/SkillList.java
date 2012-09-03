package l2rt.gameserver.network.serverpackets;

import javolution.util.FastTable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.util.GArray;

/** format   d (dddc) */
public class SkillList extends L2GameServerPacket
{
	private GArray<L2Skill> _skills;
	private boolean canEnchant;

	public SkillList(L2Player p)
	{
		_skills = new GArray<L2Skill>();
		for (L2Skill s : p.getAllSkills())
		{
			_skills.add(s);
		}
		canEnchant = p.getTransformation() == 0;
		p.sendPacket(new ExAcquirableSkillListByClass(p));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x5f);
		writeD(_skills.size());
		for(L2Skill temp : _skills)
		{
			writeD(temp.isActive() || temp.isToggle() ? 0 : 1); // deprecated? клиентом игнорируется
			writeD(temp.getDisplayLevel());
			writeD(temp.getDisplayId());
			writeD(-1); // god
			writeC(0x00); // иконка скилла серая если не 0
			writeC(canEnchant ? SkillTreeTable.isEnchantable(temp) : 0); // для заточки: если 1 скилл можно точить
		}
	}
}