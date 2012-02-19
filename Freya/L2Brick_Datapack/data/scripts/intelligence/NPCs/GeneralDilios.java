package intelligence.NPCs;

import java.util.ArrayList;
import java.util.List;

import l2.brick.Config;
import l2.brick.bflmpsvz.a.L2AttackableAIScript;

import l2.brick.gameserver.datatables.SpawnTable;
import l2.brick.gameserver.model.L2Spawn;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.serverpackets.NpcSay;
import l2.brick.gameserver.network.serverpackets.SocialAction;
import l2.brick.util.Rnd;

public class GeneralDilios extends L2AttackableAIScript
{
	private static final int generalId = 32549;
	private static final int guardId = 32619;
	
	private L2Npc _general;
	private List<L2Npc> _guards = new ArrayList<L2Npc>();
	
	private static final int[] diliosText =
	{
		1800695, // Messenger, inform the patrons of the Keucereus Alliance Base! We're gathering brave adventurers to attack Tiat's Mounted Troop that's rooted in the Seed of Destruction.
	    //1800696,  Messenger, inform the patrons of the Keucereus Alliance Base! The Seed of Destruction is currently secured under the flag of the Keucereus Alliance!
		//1800697,  Messenger, inform the patrons of the Keucereus Alliance Base! Tiat's Mounted Troop is currently trying to retake Seed of Destruction! Commit all the available reinforcements into Seed of Destruction!
		1800698, // Messenger, inform the brothers in Kucereus' clan outpost! Brave adventurers who have challenged the Seed of Infinity are currently infiltrating the Hall of Erosion through the defensively weak Hall of Suffering!
		//1800699,  Messenger, inform the brothers in Kucereus' clan outpost! Sweeping the Seed of Infinity is currently complete to the Heart of the Seed. Ekimus is being directly attacked, and the Undead remaining in the Hall of Suffering are being eradicated!
		1800700  // Messenger, inform the patrons of the Keucereus Alliance Base! The Seed of Infinity is currently secured under the flag of the Keucereus Alliance!
		//1800702   Messenger, inform the patrons of the Keucereus Alliance Base! The resurrected Undead in the Seed of Infinity are pouring into the Hall of Suffering and the Hall of Erosion! 
		//1800703   Messenger, inform the brothers in Kucereus' clan outpost! Ekimus is about to be revived by the resurrected Undead in Seed of Infinity. Send all reinforcements to the Heart and the Hall of Suffering!
	};
	
	public GeneralDilios(int questId, String name, String descr)
	{
		super(questId, name, descr);
		findNpcs();
		if (_general == null || _guards.isEmpty())
			throw new NullPointerException("Cannot find npcs!");
		startQuestTimer("command_0", 60000, null, null);
	}
	
	public void findNpcs()
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
			if (spawn != null)
				if (spawn.getNpcid() == generalId)
					_general = spawn.getLastSpawn();
				else if (spawn.getNpcid() == guardId)
					_guards.add(spawn.getLastSpawn());
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.startsWith("command_"))
		{
			int value = Integer.parseInt(event.substring(8));
			if (value < 6)
			{
				_general.broadcastPacket(new NpcSay(_general.getObjectId(), 0, generalId, 1800704)); // Stabbing three times!
				startQuestTimer("guard_animation_0", 3400, null, null);
			}
			else
			{
				value = -1;
				_general.broadcastPacket(new NpcSay(_general.getObjectId(), 1, generalId, diliosText[Rnd.get(diliosText.length)]));
			}
			startQuestTimer("command_"+(value+1), 60000, null, null);
		}
		else if (event.startsWith("guard_animation_"))
		{
			int value = Integer.parseInt(event.substring(16));
			for (L2Npc guard : _guards)
			{
				guard.broadcastPacket(new SocialAction(guard, 4));
			}
			if (value < 2)
				startQuestTimer("guard_animation_"+(value+1), 1500, null, null);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new GeneralDilios(-1, "GeneralDilios", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: General Dilios");
	}
}