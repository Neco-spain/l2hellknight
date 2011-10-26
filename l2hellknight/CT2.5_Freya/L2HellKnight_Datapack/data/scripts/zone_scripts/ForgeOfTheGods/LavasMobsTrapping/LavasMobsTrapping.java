package zone_scripts.ForgeOfTheGods.LavasMobsTrapping;

import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

public class LavasMobsTrapping extends L2AttackableAIScript
{
    private static final int[] mobs1 = {22634,22635,22636,22637,22638,22639,22640,22641};
    private static final int[] lavas1 = {18799,18800,18801};
    private static final int[] mobs2 = {22644,22645,22646,22647,22648,22649};
    private static final int[] lavas2 = {18801,18802,18803};
    private static final L2Skill trapSkill1 = SkillTable.getInstance().getInfo(6142, 1);
    private static final L2Skill trapSkill2 = SkillTable.getInstance().getInfo(6142, 2);
    private static final L2Skill trapSkill3 = SkillTable.getInstance().getInfo(6142, 3);

    @Override
    public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (Util.contains(mobs1, npc.getNpcId() ))
        {
            if (Rnd.get(100) < 30)
            {
                L2Npc lavas = addSpawn(lavas1[Rnd.get(lavas1.length)], npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, npc.getInstanceId());
                lavas.setIsOverloaded(true);
            }
        }
        else
        {
            if (Rnd.get(100) < 30)
            {
                L2Npc lavas = addSpawn(lavas2[Rnd.get(lavas2.length)], npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, npc.getInstanceId());
                lavas.setIsOverloaded(true);
            }
        }
        return super.onKill(npc,player,isPet);
    }

    @Override
    public final String onSpawn(L2Npc npc)
    {
        npc.setIsOverloaded(true);
        npc.setIsInvul(true);
        startQuestTimer("despawn", 120000, npc, null);
        return super.onSpawn(npc);
    }

    @Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
        if (event.equals("despawn"))
        {
            npc.onDecay();
            npc.deleteMe();
        }
        return null;
    }

    @Override
	public final String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (npc.isSkillDisabled(trapSkill1))
            return super.onAggroRangeEnter(npc,player,isPet);
        L2Effect effect = null;
        for (L2Effect eff : player.getAllEffects())
        {
            if (eff.getSkill().getId() == 6142)
            {
                effect = eff;
                break;
            }
        }
        if (effect == null)
        {
            npc.disableSkill(trapSkill1, 30000);
            trapSkill1.getEffects(npc, player);
        }
        else
        {
            switch(effect.getLevel())
            {
                case 1:
                    npc.disableSkill(trapSkill1, 30000);
                    effect.exit();
                    trapSkill2.getEffects(npc, player);
                    break;
                case 2:
                    npc.disableSkill(trapSkill1, 30000);
                    effect.exit();
                    trapSkill3.getEffects(npc, player);
                    break;
            }
        }
        return super.onAggroRangeEnter(npc,player,isPet);
    }

    public LavasMobsTrapping(int id, String name, String descr)
    {
        super(id, name, descr);
        for( int id_mob : mobs1 )
        	addKillId(id_mob);
        for( int id_mob : mobs2 )
        	addKillId(id_mob);
        addSpawnId(18804);
        addAggroRangeEnterId(18804);
        for (L2Npc npc : SpawnTable.getInstance().findNpces(18804))
        {
            npc.setIsOverloaded(true);
            npc.setIsInvul(true);
            startQuestTimer("despawn", 160000, npc, null);
        }
    }

    public static void main(String[] args)
    {
        new LavasMobsTrapping(-1, "ForgeOfGods", "zone_scripts/ForgeOfTheGods");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Forge Of The Gods: Lavas Mobs Trapping");
    }
}
