package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.skills.SkillTimeStamp;
import l2rt.util.GArray;

/**
 * @author: Death
 * @date: 16/2/2007
 * @time: 21:25:14
 */
public class SkillCoolTime extends L2GameServerPacket
{
	/**
	 * Example (C4, 656)
	 * C1 01 00 00 00 6E 00 00 00 02 00 00 00 9D 05 00 00 83 05 00 00 - Ultimate Defence level 2
	 *
	 * possible structure
	 * c - packet number
	 * d - size of skills ???
	 * now cycle?????
	 * d - skill id
	 * d - skill level
	 * d - 1437, total reuse delay
	 * d - 1411, remaining reuse delay
	 */

	GArray<L2Skill> _sList;
	GArray<SkillTimeStamp> _tList;

	public SkillCoolTime(L2Player player)
	{
		_sList = new GArray<L2Skill>();
		_tList = new GArray<SkillTimeStamp>();

		SkillTimeStamp sts = null;
		for(L2Skill skill : player.getAllSkills())
		{
			if(skill == null || skill.isLikePassive())
				continue;
			if((sts = player.getSkillReuseTimeStamps().get(skill.getId())) != null && sts.hasNotPassed())
			{
				_sList.add(skill);
				_tList.add(sts);
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xc7); //packet type
		writeD(_sList.size()); //Size of list

		for(int i = 0; i < _sList.size(); i++)
		{
			writeD(_sList.get(i).getId()); //Skill Id
			writeD(_sList.get(i).getLevel()); //Skill Level
			writeD((int) _tList.get(i).getReuseBasic() / 1000); //Total reuse delay, seconds
			writeD((int) _tList.get(i).getReuseCurrent() / 1000); //Time remaining, seconds
		}
	}
}