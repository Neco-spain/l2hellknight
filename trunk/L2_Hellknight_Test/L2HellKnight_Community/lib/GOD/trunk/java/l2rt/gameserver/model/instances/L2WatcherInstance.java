package l2rt.gameserver.model.instances;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2World;
import l2rt.util.Rnd;

public class L2WatcherInstance extends L2MonsterInstance
{
	public L2WatcherInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		if(getNpcId() == 18601)
			ThreadPoolManager.getInstance().scheduleGeneral(new Debuff(this), 3000);
	}

	private class Debuff implements Runnable
	{
		private L2WatcherInstance _watcher;
		private int _skillsId[] =
		{
			1064,
			1160,
			1170,
			1169,
			1164,
			1165,
			1167,
			1168
		};

		private int _skillsLvl[] =
		{
			14,
			15,
			13,
			14,
			19,
			3,
			6,
			7
		};

		public Debuff(L2WatcherInstance par)
		{
			_watcher = par;
		}

		public void run()
		{
			for(L2Character ch : L2World.getAroundPlayers(_watcher))
			{
				if(ch instanceof L2Player)
				{
					int skillRnd = Rnd.get(0, 7);
					L2Skill skill = SkillTable.getInstance().getInfo(_skillsId[skillRnd], _skillsLvl[skillRnd]);
					if(skill != null)
						skill.getEffects(ch, ch, false, false);
				}
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new Debuff(_watcher), 3000);
		}
	}
}