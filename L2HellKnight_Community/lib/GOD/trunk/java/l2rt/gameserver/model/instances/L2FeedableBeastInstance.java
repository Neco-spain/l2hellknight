package l2rt.gameserver.model.instances;

import javolution.util.FastMap;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class L2FeedableBeastInstance extends L2MonsterInstance
{
	public FastMap<Integer, growthInfo> growthCapableMobs = new FastMap<Integer, growthInfo>().setShared(true);

	GArray<Integer> tamedBeasts = new GArray<Integer>();
	GArray<Integer> feedableBeasts = new GArray<Integer>();

	public static FastMap<Integer, Integer> feedInfo = new FastMap<Integer, Integer>().setShared(true);

	private static int GOLDEN_SPICE = 0;
	private static int CRYSTAL_SPICE = 1;
	private static int SKILL_GOLDEN_SPICE = 2188;
	private static int SKILL_CRYSTAL_SPICE = 2189;

	private static String[][] text = new String[][] {
			{ "l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.1",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.2",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.3",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.4",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.5",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.6",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.7",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.8",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.9",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.1.10" },
			{ "l2rt.gameserver.model.instances.L2FeedableBeastInstance.2.1",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.2.2",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.2.3",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.2.4",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.2.5" },
			{ "l2rt.gameserver.model.instances.L2FeedableBeastInstance.3.1",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.3.2",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.3.3",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.3.4",
					"l2rt.gameserver.model.instances.L2FeedableBeastInstance.3.5" } };

	private static String[] mytext = new String[] { "l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.1",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.2",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.3",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.4",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.5",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.6",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.7",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.8",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.9",
			"l2rt.gameserver.model.instances.L2FeedableBeastInstance.5.10" };

	public L2FeedableBeastInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		// Alpen Kookabura

		// x0.5
		growthCapableMobs.put(18873, new growthInfo(0, new int[][][] { { { 18874 } }, { { 18875 } } }, 100));

		// x1
		growthCapableMobs.put(18874, new growthInfo(1, new int[][][] { { { 18876} }, {} }, 40));

		growthCapableMobs.put(18875, new growthInfo(1, new int[][][] { {}, { { 18877 } } }, 40));

		// x2
		growthCapableMobs.put(18876, new growthInfo(2, new int[][][] { { { 18878 }, { 16017 } }, {} }, 25));

		growthCapableMobs.put(18877, new growthInfo(2, new int[][][] { {}, { { 18879 }, { 16018 } } }, 25));

		// Alpen Cougar

		// x0.5
		growthCapableMobs.put(18880, new growthInfo(0, new int[][][] { { { 18881 } }, { { 18882 } } }, 100));

		// x1
		growthCapableMobs.put(18881, new growthInfo(1, new int[][][] { { { 18883 } }, {} }, 40));

		growthCapableMobs.put(18882, new growthInfo(1, new int[][][] { {}, { { 18884 } } }, 40));

		// x2
		growthCapableMobs.put(18883, new growthInfo(2, new int[][][] { { { 18885 }, { 16015 } }, {} }, 25));

		growthCapableMobs.put(18884, new growthInfo(2, new int[][][] { {}, { { 18886 }, { 16016 } } }, 25));

		// Alpen Buffalo

		// x0.5
		growthCapableMobs.put(18887, new growthInfo(0, new int[][][] { { { 18888 } }, { { 18889 } } }, 100));

		// x1
		growthCapableMobs.put(18888, new growthInfo(1, new int[][][] { { { 18890 } }, {} }, 40));

		growthCapableMobs.put(18889, new growthInfo(1, new int[][][] { {}, { { 18891 } } }, 40));

		// x2
		growthCapableMobs.put(18890, new growthInfo(2, new int[][][] { { { 18892 }, { 16013 } }, {} }, 25));

		growthCapableMobs.put(18891, new growthInfo(2, new int[][][] { {}, { { 18893 }, { 16014 } } }, 25));

		// Alpen Grendel

		// x0.5
		growthCapableMobs.put(18894, new growthInfo(0, new int[][][] { { { 18895 } }, { { 18896 } } }, 100));

		// x1
		growthCapableMobs.put(18895, new growthInfo(1, new int[][][] { { { 18897 } }, {} }, 40));

		growthCapableMobs.put(18886, new growthInfo(1, new int[][][] { {}, { { 18898 } } }, 40));

		// x2
		growthCapableMobs.put(18897, new growthInfo(2, new int[][][] { { { 18899 }, { 18900 } }, {} }, 25)); 

		growthCapableMobs.put(18898, new growthInfo(2, new int[][][] { {}, { { 18900 }, { 18900 } } }, 25));

		for(Integer i = 16013; i <= 16018; i++)
			tamedBeasts.add(i);
		for(Integer i = 16013; i <= 16019; i++)
			feedableBeasts.add(i); // Временно, для того, что бы прирученные звери появлялись.
		for(Integer i = 18873; i <= 18879; i++)
			feedableBeasts.add(i);
		for(Integer i = 18880; i <= 18886; i++)
			feedableBeasts.add(i);
		for(Integer i = 18887; i <= 18893; i++)
			feedableBeasts.add(i);
		for(Integer i = 18894; i <= 18900; i++)
			feedableBeasts.add(i);
	}

	private void spawnNext(L2Player player, int growthLevel, int food)
	{
		int npcId = getNpcId();
		int nextNpcId = 0;

		nextNpcId = growthCapableMobs.get(npcId).spice[food][0][Rnd.get(growthCapableMobs.get(npcId).spice[food][0].length)];

		// remove the feedinfo of the mob that got despawned, if any
		feedInfo.remove(getObjectId());

		// despawn the old mob
		if(growthCapableMobs.get(npcId).growth_level == 0)
			onDecay();
		else
			deleteMe();

		// if this is finally a trained mob, then despawn any other trained mobs that the player might have and initialize the Tamed Beast.
		if(tamedBeasts.contains(nextNpcId))
		{
			L2TamedBeastInstance oldTrained = player.getTrainedBeast();
			if(oldTrained != null)
				oldTrained.doDespawn();

			L2NpcTemplate template = NpcTable.getTemplate(nextNpcId);
			L2TamedBeastInstance nextNpc = new L2TamedBeastInstance(IdFactory.getInstance().getNextId(), template, player, food == 0 ? SKILL_GOLDEN_SPICE : SKILL_CRYSTAL_SPICE, getLoc());

			QuestState st = player.getQuestState("_020_BringUpWithLove");
			if(st != null && Rnd.chance(5) && st.getQuestItemsCount(7185) == 0)
			{
				st.giveItems(7185, 1);
				st.set("cond", "2");
			}

			// also, perform a rare random chat
			int rand = Rnd.get(10);
			if(rand <= 4)
				Functions.npcSayCustomMessage(nextNpc, "l2rt.gameserver.model.instances.L2FeedableBeastInstance.4." + (rand + 1), player.getName());
		}
		// if not trained, the newly spawned mob will automatically be agro against its feeder (what happened to "never bite the hand that feeds you" anyway?!)
		else
		{
			// spawn the new mob
			L2MonsterInstance nextNpc = spawn(nextNpcId, getX(), getY(), getZ());
			feedInfo.put(nextNpc.getObjectId(), player.getObjectId()); // register the player in the feedinfo for the mob that just spawned
			Functions.npcSayCustomMessage(nextNpc, text[growthLevel][Rnd.get(text[growthLevel].length)]);
			nextNpc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 99999);
		}
	}

	@Override
	public void doDie(L2Character killer)
	{
		feedInfo.remove(getObjectId());
		super.doDie(killer);
	}

	private class growthInfo
	{
		public int growth_level;
		public int growth_chance;
		public int[][][] spice;

		public growthInfo(int level, int[][][] sp, int chance)
		{
			growth_level = level;
			spice = sp;
			growth_chance = chance;
		}
	}

	public L2MonsterInstance spawn(int npcId, int x, int y, int z)
	{
		try
		{
			L2MonsterInstance monster = (L2MonsterInstance) NpcTable.getTemplate(npcId).getInstanceConstructor().newInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(npcId));
			monster.setSpawnedLoc(new Location(x, y, z));
			monster.onSpawn();
			monster.spawnMe(monster.getSpawnedLoc());
			return monster;
		}
		catch(Exception e)
		{
			System.out.println("Could not spawn Npc " + npcId);
			e.printStackTrace();
		}
		return null;
	}

	public void onSkillUse(L2Player player, int skill_id)
	{
		// gather some values on local variables
		int npcId = getNpcId();
		// check if the npc and skills used are valid
		if(!feedableBeasts.contains(npcId))
			return;
		if(skill_id != SKILL_GOLDEN_SPICE && skill_id != SKILL_CRYSTAL_SPICE)
			return;

		int food = GOLDEN_SPICE;
		if(skill_id == SKILL_CRYSTAL_SPICE)
			food = CRYSTAL_SPICE;

		int objectId = getObjectId();
		// display the social action of the beast eating the food.
		broadcastPacket(new SocialAction(objectId, 2));

		// if this pet can't grow, it's all done.
		if(growthCapableMobs.containsKey(npcId))
		{
			// do nothing if this mob doesn't eat the specified food (food gets consumed but has no effect).
			if(growthCapableMobs.get(npcId).spice[food].length == 0)
				return;

			// more value gathering on local variables
			int growthLevel = growthCapableMobs.get(npcId).growth_level;

			if(growthLevel > 0)
				// check if this is the same player as the one who raised it from growth 0.
				// if no, then do not allow a chance to raise the pet (food gets consumed but has no effect).
				if(feedInfo.get(objectId) != null && feedInfo.get(objectId) != player.getObjectId())
					return;

			// Polymorph the mob, with a certain chance, given its current growth level
			if(Rnd.chance(growthCapableMobs.get(npcId).growth_chance))
				spawnNext(player, growthLevel, food);
		}
		else if(tamedBeasts.contains(npcId))
			if(skill_id == ((L2TamedBeastInstance) this).getFoodType())
			{
				((L2TamedBeastInstance) this).onReceiveFood();
				Functions.npcSayCustomMessage(this, mytext[Rnd.get(mytext.length)]);
			}
	}
}