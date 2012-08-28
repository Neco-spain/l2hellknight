/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.ai.special;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.Calendar;
import java.util.Collection;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.quest.QuestTimer;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

public class Baium extends L2AttackableAIScript
{
	
	private static Logger _log = Logger.getLogger(Baium.class.getName());
	Calendar time = Calendar.getInstance();
	
	private L2Character _target;
	private L2Skill _skill;
	private static final int STONE_BAIUM = 29025;
	private static final int ANGELIC_VORTEX = 31862;
	private static final int LIVE_BAIUM = 29020;
	private static final int ARCHANGEL = 29021;
	private static FastList<L2Attackable> _Minions = new FastList<L2Attackable>();
	private static final int[][] ANGEL_LOCATION = { { 113004, 16209, 10076, 60242 }, { 114053, 16642, 10076, 4411 }, { 114563, 17184, 10076, 49241 }, { 116356, 16402, 10076, 31109 }, { 115015, 16393, 10076, 32760 }, { 115481, 15335, 10076, 16241 }, { 114680, 15407, 10051, 32485 }, { 114886, 14437, 10076, 16868 }, { 115391, 17593, 10076, 55346 }, { 115245, 17558, 10076, 35536 } };
	  
	//Baium status tracking
	private static final byte ASLEEP = 0;  // baium is in the stone version, waiting to be woken up.  Entry is unlocked
	private static final byte AWAKE = 1;   // baium is awake and fighting.  Entry is locked.
	private static final byte DEAD = 2;    // baium has been killed and has not yet spawned.  Entry is locked

	private static long _LastAttackVsBaiumTime = 0;
	private static L2BossZone _Zone;
	
	public Baium (int questId, String name, String descr)
	{
		super(questId, name, descr);
		
        int[] mob = {LIVE_BAIUM};
        this.registerMobs(mob);
        
        // Quest NPC starter initialization
        addStartNpc(STONE_BAIUM);
        addStartNpc(ANGELIC_VORTEX);
        addTalkId(STONE_BAIUM);
        addTalkId(ANGELIC_VORTEX);
        _Zone = GrandBossManager.getInstance().getZone(113100,14500,10077);
        StatsSet info = GrandBossManager.getInstance().getStatsSet(LIVE_BAIUM);
        int status = GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM);
        if (status == DEAD)
        {
            // load the unlock date and time for baium from DB
            long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
            if (temp > 0)
            {
                // the unlock time has not yet expired.  Mark Baium as currently locked (dead).  Setup a timer
                // to fire at the correct time (calculate the time between now and the unlock time,
                // setup a timer to fire after that many msec)
                startQuestTimer("baium_unlock", temp, null, null);
            }
            else
            {
                // the time has already expired while the server was offline.  Delete the saved time and
                // immediately spawn the stone-baium.  Also the state need not be changed from ASLEEP
                addSpawn(STONE_BAIUM,116033,17447,10104,40188,false,0);
                GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM,ASLEEP);
            }
        }
        else if (status == AWAKE)
        {
            int loc_x = info.getInteger("loc_x");
            int loc_y = info.getInteger("loc_y");
            int loc_z = info.getInteger("loc_z");
            int heading = info.getInteger("heading");
            final int hp = info.getInteger("currentHP");
            final int mp = info.getInteger("currentMP");
            L2GrandBossInstance baium = (L2GrandBossInstance) addSpawn(LIVE_BAIUM,loc_x,loc_y,loc_z,heading,false,0);
            GrandBossManager.getInstance().addBoss(baium);
            final L2NpcInstance _baium = baium;
            ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
				public void run()
				{
					try
		            {
						_baium.setCurrentHpMp(hp,mp);
						_baium.setIsInvul(true);
						_baium.setIsImobilised(true);
		                _baium.setRunning();
						_baium.broadcastPacket(new SocialAction(_baium.getObjectId(),2));
			            startQuestTimer("baium_wakeup",15000, _baium, null);
		            }
		            catch (Exception e)
		            {
		            	e.printStackTrace();
		            }
				}
			},100L);
        }
        else
        {
            addSpawn(STONE_BAIUM,116033,17447,10104,40188,false,0);
        }
	}

	public String onAdvEvent (String event, L2NpcInstance npc, L2PcInstance player)
	{
        if (event.equalsIgnoreCase("baium_unlock"))
        {
            GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM,ASLEEP);
            addSpawn(STONE_BAIUM,116033,17447,10104,40188,false,0);
        }
        else if (event.equalsIgnoreCase("skill_range") && npc != null)
        {
        	callSkillAI(npc);
        }
        else if (event.equalsIgnoreCase("clean_player"))
        {
        	_target = getRandomTarget(npc);
        }
        else if (event.equalsIgnoreCase("baium_wakeup") && npc != null)
        {
            if (npc.getNpcId() == LIVE_BAIUM)
            {
                npc.broadcastPacket(new SocialAction(npc.getObjectId(),1));
                npc.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(),40,5));
                // start monitoring baium's inactivity
                _LastAttackVsBaiumTime = System.currentTimeMillis();
                startQuestTimer("baium_despawn", 60000, npc, null, true);
                startQuestTimer("skill_range", 500, npc, null, true);
                final L2NpcInstance baium = npc;
                ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
    				public void run()
    				{
    					try
    		            {
    						baium.setIsInvul(false);
    						baium.setIsImobilised(false);
    		            }
    		            catch (Exception e)
    		            {
    		            	e.printStackTrace();
    		            }
    				}
    			},11100L);
                // TODO: the person who woke baium up should be knocked across the room, onto a wall, and
                // lose massive amounts of HP.
                for (int i = 0; i < 5; i++) {
                    _Minions.add((L2Attackable)addSpawn(ARCHANGEL, ANGEL_LOCATION[i][0], ANGEL_LOCATION[i][1], ANGEL_LOCATION[i][2], ANGEL_LOCATION[i][3], false, 0));
                  }
            }
        // despawn the live baium after 30 minutes of inactivity
        // also check if the players are cheating, having pulled Baium outside his zone...
        }
        else if (event.equalsIgnoreCase("baium_despawn") && npc != null)
        {
            if (npc.getNpcId() == LIVE_BAIUM)
            {
                // just in case the zone reference has been lost (somehow...), restore the reference
                if (_Zone == null)
                    _Zone = GrandBossManager.getInstance().getZone(113100,14500,10077);
                if (_LastAttackVsBaiumTime + 1800000 < System.currentTimeMillis())
                {
                    npc.deleteMe();   // despawn the live-baium
                    addSpawn(STONE_BAIUM,116033,17447,10104,40188,false,0);  // spawn stone-baium
                    GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM,ASLEEP);    // mark that Baium is not awake any more
                    _Zone.oustAllPlayers();
                    cancelQuestTimer("baium_despawn", npc, null);
                    for (L2Attackable minion : _Minions)
                        if (minion != null)
                          minion.deleteMe();
                      _Minions.clear();
                }
                else if (!_Zone.isInsideZone(npc))
                	npc.teleToLocation(116033,17447,10104);
            }
        }
        return super.onAdvEvent(event, npc, player);
	}

    public String onTalk(L2NpcInstance npc,L2PcInstance player)
    {
        int npcId = npc.getNpcId();
        String htmltext = "";
        if (_Zone == null)
            _Zone = GrandBossManager.getInstance().getZone(113100,14500,10077);
        if (_Zone == null)
        	return "<html><body>Angelic Vortex:<br>You may not enter while admin disabled this zone</body></html>";
        if (npcId == STONE_BAIUM && GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) == ASLEEP)
        {
            if (_Zone.isPlayerAllowed(player))
            {
                // once Baium is awaken, no more people may enter until he dies, the server reboots, or 
                // 30 minutes pass with no attacks made against Baium.
                GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM,AWAKE);
                npc.deleteMe();
                L2GrandBossInstance baium = (L2GrandBossInstance) addSpawn(LIVE_BAIUM,npc);
                GrandBossManager.getInstance().addBoss(baium);
                final L2NpcInstance _baium = baium;
                ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
    				public void run()
    				{
    					try
    		            {
    						_baium.setIsInvul(true);
    		                _baium.setRunning();
    						_baium.broadcastPacket(new SocialAction(_baium.getObjectId(),2));
    						startQuestTimer("baium_wakeup",15000, _baium, null);
    		            }
    		            catch (Throwable e)
    		            {
    		            }
    				}
    			},100L);
            }
            else
                htmltext = "Conditions are not right to wake up Baium";
        }
        else if (npcId == ANGELIC_VORTEX)
        {
            if (player.isFlying())
            {
                //print "Player "+player.getName()+" attempted to enter Baium's lair while flying!";
                return "<html><body>Angelic Vortex:<br>You may not enter while flying a wyvern</body></html>";
            }
            
            if (GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) == ASLEEP
            		&& player.getQuestState("baium").getQuestItemsCount(4295) > 0) // bloody fabric
            {
                player.getQuestState("baium").takeItems(4295,1);
                // allow entry for the player for the next 30 secs (more than enough time for the TP to happen)
                // Note: this just means 30secs to get in, no limits on how long it takes before we get out.
                _Zone.allowPlayerEntry(player,30);
                player.teleToLocation(113100,14500,10077);
            }
            else
            	npc.showChatWindow(player, 1);
        }
        return htmltext;
    }
    
    public String onSpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
    {
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		else if (npc.getNpcId() == LIVE_BAIUM && !npc.isInvul())
    	{
    		callSkillAI(npc);
    	}
    	return super.onSpellFinished(npc, player, skill);
    }
    public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
    {
    	if (!_Zone.isInsideZone(attacker))
    	{
    		attacker.reduceCurrentHp(attacker.getCurrentHp(),attacker);
    		return null;
    	}
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		else if (npc.getNpcId() == LIVE_BAIUM && !npc.isInvul())
    	{
    		if (attacker.getMountType() == 1)
        	{
    			int sk_4258 = 0;
    			L2Effect[] effects = attacker.getAllEffects();
    			if (effects.length != 0 || effects != null)
    			{
    				for (L2Effect e : effects)
    				{
    					if (e.getSkill().getId() == 4258)
    						sk_4258 = 1;
    				}
    	        }
    			if (sk_4258 == 0)
    			{
    				npc.setTarget(attacker);
    				npc.doCast(SkillTable.getInstance().getInfo(4258,1));
    			}
        	}
    		// update a variable with the last action against baium
    		_LastAttackVsBaiumTime = System.currentTimeMillis();
    		callSkillAI(npc);
    	}
		return super.onAttack(npc, attacker, damage, isPet);
    }
    
    public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
    { 
        cancelQuestTimer("baium_despawn", npc, null);
        npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
        // spawn the "Teleportation Cubic" for 15 minutes (to allow players to exit the lair)
        addSpawn(29055,115203,16620,10078,0,false,900000); ////should we teleport everyone out if the cubic despawns??
        // "lock" baium for 5 days and 1 to 8 hours [i.e. 432,000,000 +  1*3,600,000 + random-less-than(8*3,600,000) millisecs]
        long respawnTime = (long)Config.Interval_Of_Baium_Spawn + Rnd.get(Config.Random_Of_Baium_Spawn);
        GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM,DEAD);
        startQuestTimer("baium_unlock", respawnTime, null, null);
        _log.warning(" - Epic: Baium killed: " + time.getTime());
        // also save the respawn time so that the info is maintained past reboots
        StatsSet info = GrandBossManager.getInstance().getStatsSet(LIVE_BAIUM);
        info.set("respawn_time",(System.currentTimeMillis()) + respawnTime);
        GrandBossManager.getInstance().setStatsSet(LIVE_BAIUM,info);
		if (getQuestTimer("skill_range", npc, null) != null)
			getQuestTimer("skill_range", npc, null).cancel();
		for (L2Attackable minion : _Minions)
		      if (minion != null)
		        minion.deleteMe();
		    _Minions.clear();
        return super.onKill(npc,killer,isPet);
    }

	public L2Character getRandomTarget(L2NpcInstance npc)
	{
		FastList<L2Character> result = new FastList<L2Character>();
		Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
		{
			for (L2Object obj : objs)
			{
				if (obj instanceof L2Character)
				{
					if (((L2Character) obj).getZ() < ( npc.getZ() - 100 ) && ((L2Character) obj).getZ() > ( npc.getZ() + 100 )
							|| !(GeoData.getInstance().canSeeTarget(((L2Character) obj).getX(), ((L2Character) obj).getY(), ((L2Character) obj).getZ(), npc.getX(), npc.getY(), npc.getZ())))
						continue;
				}
				if (obj instanceof L2PcInstance || obj instanceof L2SummonInstance)
				{
					if (Util.checkIfInRange(9000, npc, obj, true) && !((L2Character) obj).isDead())
						result.add((L2Character) obj);
				}
			}
		}
		if (!result.isEmpty() && result.size() != 0)
		{
			Object[] characters = result.toArray();
			QuestTimer timer = getQuestTimer("clean_player", npc, null);
			if (timer != null)
				timer.cancel();
			startQuestTimer("clean_player", 20000, npc, null);
			return (L2Character) characters[Rnd.get(characters.length)];
		}
		return null;
	}

	public synchronized void callSkillAI(L2NpcInstance npc)
	{
		if (npc.isInvul() || npc.isCastingNow()) return;

		if (_target == null || _target.isDead() || !(_Zone.isInsideZone(_target)))
		{
			_target = getRandomTarget(npc);
			if (_target != null)
				_skill = SkillTable.getInstance().getInfo(getRandomSkill(npc),1);
		}

		L2Character target = _target;
		L2Skill skill = _skill;
		if (skill == null)
			skill = SkillTable.getInstance().getInfo(getRandomSkill(npc),1);
		if (target == null || target.isDead() || !(_Zone.isInsideZone(target)))
		{
			return;
		}

		if (Util.checkIfInRange(skill.getCastRange(), npc, target, true))
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			npc.setTarget(target);
			_target = null;
			npc.doCast(skill);
		}
		else
		{
			npc.getAI().setIntention(AI_INTENTION_FOLLOW, target, null);
		}
	}

	public int getRandomSkill(L2NpcInstance npc)
	{
		int skill;
		if( npc.getCurrentHp() > ( ( npc.getMaxHp() * 3 ) / 4.0 ) )
		{
			if( Rnd.get(100) < 10 )
				skill = 4128;
			else if( Rnd.get(100) < 10 )
				skill = 4129;
			else
				skill = 4127;
		}
		else if( npc.getCurrentHp() > ( ( npc.getMaxHp() * 2 ) / 4.0) )
		{
			if( Rnd.get(100) < 10 )
				skill = 4131;
			else if( Rnd.get(100) < 10 )
				skill = 4128;
			else if( Rnd.get(100) < 10 )
				skill = 4129;
			else
				skill = 4127;
		}
		else if( npc.getCurrentHp() > ( ( npc.getMaxHp() * 1 ) / 4.0 ) )
		{
			if( Rnd.get(100) < 10 )
				skill = 4130;
			else if( Rnd.get(100) < 10 )
				skill = 4131;
			else if( Rnd.get(100) < 10 )
				skill = 4128;
			else if( Rnd.get(100) < 10 )
				skill = 4129;
			else
				skill = 4127;
		}
		else if( Rnd.get(100) < 10 )
			skill = 4130;
		else if( Rnd.get(100) < 10 )
			skill = 4131;
		else if( Rnd.get(100) < 10 )
			skill = 4128;
		else if( Rnd.get(100) < 10 )
			skill = 4129;
		else
			skill = 4127;
		return skill;
	}

	public String onSkillSee (L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		npc.setTarget(caster);
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	public int getDist(int range)
	{
		int dist = 0;
		switch(range)
		{
			case -1:
				break;
			case 100:
				dist = 85;
				break;
			default:
				dist = range-85;
				break;
		}
		return dist;
	}

}