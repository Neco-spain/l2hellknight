package zone_scripts.Hellbound.BaseTower;

import l2.brick.Config;
import l2.brick.gameserver.datatables.DoorTable;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.base.ClassId;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.skills.SkillHolder;

import java.util.Map;
import javolution.util.FastMap; 

public class BaseTower extends Quest
{
	private static final int GUZEN = 22362;
	private static final int KENDAL = 32301;
	private static final int BODY_DESTROYER = 22363;
	
	private static Map<Integer, L2PcInstance> BODY_DESTROYER_TARGET_LIST = new FastMap<Integer, L2PcInstance>();

	private static final SkillHolder DEATH_WORD = new SkillHolder(5256, 1);

	public BaseTower(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addKillId(GUZEN);
		addKillId(BODY_DESTROYER);
		addFirstTalkId(KENDAL);
		addAggroRangeEnterId(BODY_DESTROYER);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		ClassId classId = player.getClassId();
		if (classId.equalsOrChildOf(ClassId.hellKnight) || classId.equalsOrChildOf(ClassId.soultaker)) 
			return "32301-02.htm";
		else
			return "32301-01.htm";
	}

	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("close"))
		{
			DoorTable.getInstance().getDoor(20260004).closeMe();
		}
		return null;
	}

 @Override
  public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
  {
		if (!BODY_DESTROYER_TARGET_LIST.containsKey(npc.getObjectId()))
		{
			BODY_DESTROYER_TARGET_LIST.put(npc.getObjectId(), player);
			npc.setTarget(player);
			npc.doSimultaneousCast(DEATH_WORD.getSkill());
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case GUZEN:
				//Should Kendal be despawned before Guzen's spawn? Or it will be crowd of Kendal's
				addSpawn(KENDAL, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, npc.getSpawn().getRespawnDelay(), false);
				DoorTable.getInstance().getDoor(20260003).openMe();
				DoorTable.getInstance().getDoor(20260004).openMe();
				startQuestTimer("close", 60000, npc, null, false);
				break;
			case BODY_DESTROYER:
				if (BODY_DESTROYER_TARGET_LIST.containsKey(npc.getObjectId()))
				{
					L2PcInstance pl = BODY_DESTROYER_TARGET_LIST.get(npc.getObjectId());
					if (pl != null && pl.isOnline() && !pl.isDead())
					{
						L2Effect e = pl.getFirstEffect(DEATH_WORD.getSkill());
						if (e != null)
							e.exit();
					}
					
					BODY_DESTROYER_TARGET_LIST.remove(npc.getObjectId());
				}
		} 

		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args)
	{
		new BaseTower(-1, BaseTower.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Base Tower");
	}
}