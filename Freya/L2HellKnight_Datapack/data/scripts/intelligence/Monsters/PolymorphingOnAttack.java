package intelligence.Monsters;

import gnu.trove.TIntObjectHashMap;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.util.Rnd;

import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

public class PolymorphingOnAttack extends L2AttackableAIScript
{
	private static final TIntObjectHashMap<Integer[]> MOBSPAWNS = new TIntObjectHashMap<Integer[]>();
	static
	{
		MOBSPAWNS.put(21258,new Integer[]{21259,100,100,-1}); //Fallen Orc Shaman -> Sharp Talon Tiger (always polymorphs)
		MOBSPAWNS.put(21261,new Integer[]{21262,100,20 ,0}); //Ol Mahum Transcender 1st stage
		MOBSPAWNS.put(21262,new Integer[]{21263,100,10 ,1}); //Ol Mahum Transcender 2nd stage
		MOBSPAWNS.put(21263,new Integer[]{21264,100,5  ,2}); //Ol Mahum Transcender 3rd stage
		MOBSPAWNS.put(21265,new Integer[]{21271,100,33 ,0}); //Cave Ant Larva -> Cave Ant
		MOBSPAWNS.put(21266,new Integer[]{21269,100,100,-1}); //Cave Ant Larva -> Cave Ant (always polymorphs)
		MOBSPAWNS.put(21267,new Integer[]{21270,100,100,-1}); //Cave Ant Larva -> Cave Ant Soldier (always polymorphs)
		MOBSPAWNS.put(21271,new Integer[]{21272,66,10 ,1}); //Cave Ant -> Cave Ant Soldier
		MOBSPAWNS.put(21272,new Integer[]{21273,33 ,5  ,2}); //Cave Ant Soldier -> Cave Noble Ant
		MOBSPAWNS.put(21521,new Integer[]{21522,100,30 ,-1}); //Claws of Splendor
		MOBSPAWNS.put(21527,new Integer[]{21528,100,30 ,-1}); //Anger of Splendor
		MOBSPAWNS.put(21533,new Integer[]{21534,100,30 ,-1}); //Alliance of Splendor
		MOBSPAWNS.put(21537,new Integer[]{21538,100,30 ,-1}); //Fang of Splendor
	}
	protected static final String[][] MOBTEXTS =
	{
		new String[]{"Enough fooling around. Get ready to die!", "You idiot! I've just been toying with you!", "Now the fun starts!"},
		new String[]{"I must admit, no one makes my blood boil quite like you do!", "Now the battle begins!", "Witness my true power!"},
		new String[]{"Prepare to die!", "I'll double my strength!", "You have more skill than I thought"}
	};
	
	public PolymorphingOnAttack(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : MOBSPAWNS.keys())
			super.addAttackId(id);
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.isVisible() && !npc.isDead())
		{
			final Integer[] tmp = MOBSPAWNS.get(npc.getNpcId());
			if (tmp != null)
			{
				if (npc.getCurrentHp() <= (npc.getMaxHp() * tmp[1]/100.0)&& Rnd.get(100) < tmp[2])
				{
					if (tmp[3] >= 0)
					{
						String text = MOBTEXTS[tmp[3]][Rnd.get(MOBTEXTS[tmp[3]].length)];
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(),Say2.ALL,npc.getName(),text));
					}
					npc.deleteMe();
					L2Attackable newNpc = (L2Attackable) addSpawn(tmp[0], npc.getX(), npc.getY(), npc.getZ()+10, npc.getHeading(), false, 0, true);
					L2Character originalAttacker = isPet? attacker.getPet(): attacker;
					newNpc.setRunning();
					newNpc.addDamageHate(originalAttacker,0,500);
					newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
				}
			}
		}
		return super.onAttack (npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new PolymorphingOnAttack(-1,"polymorphing_on_attack","ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Polymorphing On Attack");
	}
}