package intelligence.RaidBosses;
import l2.hellknight.Config;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.network.serverpackets.PlaySound;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.network.serverpackets.SpecialCamera;


public class DrChaos extends Quest
{
	
	private static final int DOCTER_CHAOS = 32033;
	private static final int STRANGE_MACHINE = 32032;
	private static final int CHAOS_GOLEM = 25512;
	private static boolean _IsGolemSpawned;
	
	public DrChaos(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addFirstTalkId(32033);
		_IsGolemSpawned = false;
	}
	
	public L2Npc findTemplate(int npcId)
	{
		for(L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn != null && spawn.getNpcid() == npcId)
			{
				return spawn.getLastSpawn();
			}
		}
		return null;
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("1"))
		{
			L2Npc machine_instance = findTemplate(STRANGE_MACHINE);
			if (machine_instance != null)
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, machine_instance);
				machine_instance.broadcastPacket(new SpecialCamera(machine_instance.getObjectId(),1,-200,15,10000,20000,0,0,1,0));
			}
			else
				//print "Dr Chaos AI: problem finding Strange Machine (npcid = "+STRANGE_MACHINE+"). Error: not spawned!"
				startQuestTimer("2",2000,npc,player);
			startQuestTimer("3",10000,npc,player);
		}
		else if (event.equalsIgnoreCase("2"))
			npc.broadcastPacket(new SocialAction(npc,3));
		else if (event.equalsIgnoreCase("3"))
		{
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(),1,-150,10,3000,20000,0,0,1,0));
			startQuestTimer("4",2500,npc,player);
		}
		else if (event.equalsIgnoreCase("4"))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(96055,-110759,-3312,0));
			startQuestTimer("5",2000,npc,player);
		}
		else if (event.equalsIgnoreCase("5"))
		{
			player.teleToLocation(94832,-112624,-3304);
			npc.teleToLocation(-113091,-243942,-15536);
			if (!_IsGolemSpawned)
			{
				L2Npc golem = addSpawn(CHAOS_GOLEM,94640,-112496,-3336,0,false,0);
				_IsGolemSpawned = true;
				startQuestTimer("6",1000,golem,player);
				player.sendPacket(new PlaySound(1,"Rm03_A",0,0,0,0,0));
			}
		}
		else if (event.equalsIgnoreCase("6"))
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(),30,-200,20,6000,8000,0,0,1,0));
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk (L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == DOCTER_CHAOS)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(96323,-110914,-3328,0));
			this.startQuestTimer("1",3000,npc,player);
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new DrChaos(-1,"Doctor Chaos","ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded RaidBoss: Dr. Chaos");
	}
}