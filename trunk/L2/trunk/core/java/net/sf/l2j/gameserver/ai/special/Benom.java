package net.sf.l2j.gameserver.ai.special;

import java.util.Iterator;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.util.Rnd;

public final class Benom extends Quest
{
  @SuppressWarnings("unused")
private static final int CASTLE_ID = 8;
  @SuppressWarnings("unused")
private static final int BENOM = 29054;
  @SuppressWarnings("unused")
private static final int BENOM_TELEPORT = 35506;
  private static final String[] BENOM_SPEAK = { "You should have finished me when you had the chance!!!", "I will crush all of you!!!", "I am not finished here, come face me!!!", "You cowards!!! I will torture each and everyone of you!!!" };
  private static final FastMap<Integer, Location> BENON_WALK_ROUTES = new FastMap<Integer, Location>();
  private static final int[] WALK_TIMES = { 18000, 17000, 4500, 16000, 22000, 14000, 10500, 14000, 9500, 12500, 20500, 14500, 17000, 20000, 22000, 11000, 11000, 20000, 8000, 5500, 20000, 18000, 25000, 28000, 25000, 25000, 25000, 25000, 10000, 24000, 7000, 12000, 20000 };
  private L2NpcInstance _Benom;
  @SuppressWarnings("unused")
private static final byte ALIVE = 0;
  @SuppressWarnings("unused")
private static final byte DEAD = 1;
  private static byte BenomIsSpawned = 0;
  private static int BenomWalkRouteStep = 0;

  public Benom(int questId, String name, String descr)
  {
    super(questId, name, descr);

    addEventId(29054, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
    addEventId(29054, Quest.QuestEventType.ON_KILL);
    addEventId(35506, Quest.QuestEventType.QUEST_START);
    addEventId(35506, Quest.QuestEventType.ON_TALK);

    int castleOwner = CastleManager.getInstance().getCastleById(8).getOwnerId();

    long siegeDate = CastleManager.getInstance().getCastleById(8).getSiegeDate().getTimeInMillis();

    long currentTime = System.currentTimeMillis();
    long benomTeleporterSpawn = siegeDate - currentTime - 86400000;
    long benomRaidRoomSpawn = siegeDate - currentTime - 86400000;
    long benomRaidSiegeSpawn = siegeDate - currentTime;

    if (benomTeleporterSpawn > 0L) 
    {
      benomTeleporterSpawn = 1L;
    }

    if (benomRaidSiegeSpawn > 0L) 
    {
      benomRaidSiegeSpawn = 1L;
    }

    if (castleOwner > 0) 
    {
      if (!(benomTeleporterSpawn > 1L)) 
      {
        startQuestTimer("BenomTeleSpawn", benomTeleporterSpawn, null, null);
      }

      if (siegeDate - currentTime > 0L) 
      {
        startQuestTimer("BenomRaidRoomSpawn", benomRaidRoomSpawn, null, null);
      }

      startQuestTimer("BenomRaidSiegeSpawn", benomRaidSiegeSpawn, null, null);
    }
  }

  public final String onTalk(L2NpcInstance npc, L2PcInstance player)
  {
    String htmltext = null;
    int castleOwner = CastleManager.getInstance().getCastleById(8).getOwnerId();

    int clanId = player.getClanId();

    if ((castleOwner != 0) && (clanId != 0)) {
      if (castleOwner == clanId) {
        int X = 12558 + Rnd.get(200) - 100;
        int Y = -49279 + Rnd.get(200) - 100;
        player.teleToLocation(X, Y, -3007);
        return htmltext;
      }
      htmltext = "<html><body>Benom's Avatar:<br>Your clan does not own this castle. Only members of this Castle's owning clan can challenge Benom.</body></html>";
    }
    else {
      htmltext = "<html><body>Benom's Avatar:<br>Your clan does not own this castle. Only members of this Castle's owning clan can challenge Benom.</body></html>";
    }

    return htmltext;
  }

  public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
  {
    int statusBoss = GrandBossManager.getInstance().getBossStatus(29054);

    if (event.equalsIgnoreCase("BenomTeleSpawn")) 
    {
      addSpawn(35506, 11013, -49629, -547, 13400, false, 0);
    }
    else if (event.equalsIgnoreCase("BenomRaidRoomSpawn")) 
    {
      if ((BenomIsSpawned == 0) && (statusBoss == 0)) 
      {
        this._Benom = addSpawn(29054, 12047, -49211, -3009, 0, false, 0);
      }

      BenomIsSpawned = 1;
    } 
    else if (event.equalsIgnoreCase("BenomRaidSiegeSpawn"))
      if (statusBoss == 0) 
      {
        switch (BenomIsSpawned)
        {
        case 0:
          this._Benom = addSpawn(29054, 11025, -49152, -537, 0, false, 0);
          BenomIsSpawned = 1;
          System.out.println("Benom Spawn");
          break;
        case 1:
          this._Benom.teleToLocation(11025, -49152, -537);
          _Benom.isAggressive();
          System.out.println(_Benom.getAggroRange());
          System.out.println("Benom Teleport in castle");
        }

        startQuestTimer("BenomSpawnEffect", 100, this._Benom, null);
        startQuestTimer("BenomBossDespawn", 5400000, this._Benom, null);
        cancelQuestTimer("BenomSpawn", this._Benom, null);
        unSpawnNpc(35506);
      }
    else if (event.equalsIgnoreCase("BenomSpawnEffect")) 
    {
      npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
      npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, 0, 150, 0, 5000));

      npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
      startQuestTimer("BenomWalk", 5000, npc, null);
      BenomWalkRouteStep = 0;
    } 
    else if (event.equalsIgnoreCase("Attacking")) 
    {
      FastList<L2PcInstance> NumPlayers = new FastList<L2PcInstance>();
      for (Iterator<?> i$ = npc.getKnownList().getKnownPlayers().values().iterator(); i$.hasNext(); ) { L2PcInstance plr = (L2PcInstance)i$.next();
        NumPlayers.add(plr);
      }

      if (NumPlayers.size() > 0) {
        L2PcInstance target = (L2PcInstance)NumPlayers.get(Rnd.get(NumPlayers.size()));
        ((L2Attackable)npc).addDamageHate(target, 0, 999);
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

        startQuestTimer("Attacking", 2000, npc, player);
      } else if (NumPlayers.size() == 0)
        startQuestTimer("BenomWalkFinish", 2000, npc, null);
    }
    else if (event.equalsIgnoreCase("BenomWalkFinish")) 
    {
      if (npc.getCastle().getSiege().getIsInProgress()) 
      {
        cancelQuestTimer("Attacking", npc, player);
      }

      npc.teleToLocation((Location)BENON_WALK_ROUTES.get(Integer.valueOf(BenomWalkRouteStep)), false);
      npc.setWalking();
      BenomWalkRouteStep = 0;
      startQuestTimer("BenomWalk", 2200, npc, null);
    } 
    else if (event.equalsIgnoreCase("BenomWalk"))
      if (BenomWalkRouteStep == 33) 
      {
        BenomWalkRouteStep = 0;
        startQuestTimer("BenomWalk", 100, npc, null);
      } 
      else 
      {
        startQuestTimer("Talk", 100, npc, null);
        switch (BenomWalkRouteStep)
        {
        case 14:
          startQuestTimer("DoorOpen", 15000, null, null);
          startQuestTimer("DoorClose", 23000, null, null);
          break;
        case 32:
          startQuestTimer("DoorOpen", 500, null, null);
          startQuestTimer("DoorClose", 4000, null, null);
        }

        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((Location)BENON_WALK_ROUTES.get(Integer.valueOf(BenomWalkRouteStep))));

        BenomWalkRouteStep = BenomWalkRouteStep + 1;
        startQuestTimer("BenomWalk", WALK_TIMES[BenomWalkRouteStep], npc, null);
      }

    else if (event.equalsIgnoreCase("DoorOpen"))
      DoorTable.getInstance().getDoor(Integer.valueOf(20160005)).openMe();
    else if (event.equalsIgnoreCase("DoorClose"))
      DoorTable.getInstance().getDoor(Integer.valueOf(20160005)).closeMe();
    else if (event.equalsIgnoreCase("Talk")) {
      if (Rnd.get(100) < 40)
        npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), BENOM_SPEAK[Rnd.get(BENOM_SPEAK.length)]));

    }
    else if (event.equalsIgnoreCase("BenomBossDespawn")) {
      GrandBossManager.getInstance().setBossStatus(29054, 0);
      BenomIsSpawned = 0;
      unSpawnNpc(29054);
    }

    return super.onAdvEvent(event, npc, player);
  }

  public String onAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
  {
    cancelQuestTimer("BenomWalk", npc, null);
    cancelQuestTimer("BenomWalkFinish", npc, null);
    startQuestTimer("Attacking", 100, npc, player);
    return super.onAggroRangeEnter(npc, player, isPet);
  }

  public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
  {
    GrandBossManager.getInstance().setBossStatus(29054, 1);
    cancelQuestTimer("BenomWalk", npc, null);
    cancelQuestTimer("BenomWalkFinish", npc, null);
    cancelQuestTimer("BenomBossDespawn", npc, null);
    cancelQuestTimer("Talk", npc, null);
    cancelQuestTimer("Attacking", npc, null);
    return super.onKill(npc, killer, isPet);
  }

  private void unSpawnNpc(int npcId) {
    for (Iterator<?> i$ = SpawnTable.getInstance().getSpawnTable().values().iterator(); i$.hasNext(); ) { L2Spawn spawn = (L2Spawn)i$.next();
      if (spawn.getId() == npcId) {
        SpawnTable.getInstance().deleteSpawn(spawn, false);
        L2NpcInstance npc = spawn.getLastSpawn();
        npc.deleteMe();
      }
    }
  }

  static
  {
    BENON_WALK_ROUTES.put(Integer.valueOf(0), new Location(12565, -49739, -547));
    BENON_WALK_ROUTES.put(Integer.valueOf(1), new Location(11242, -49689, -33));
    BENON_WALK_ROUTES.put(Integer.valueOf(2), new Location(10751, -49702, 83));
    BENON_WALK_ROUTES.put(Integer.valueOf(3), new Location(10824, -50808, 316));
    BENON_WALK_ROUTES.put(Integer.valueOf(4), new Location(9084, -50786, 972));
    BENON_WALK_ROUTES.put(Integer.valueOf(5), new Location(9095, -49787, 1252));
    BENON_WALK_ROUTES.put(Integer.valueOf(6), new Location(8371, -49711, 1252));
    BENON_WALK_ROUTES.put(Integer.valueOf(7), new Location(8423, -48545, 1252));
    BENON_WALK_ROUTES.put(Integer.valueOf(8), new Location(9105, -48474, 1252));
    BENON_WALK_ROUTES.put(Integer.valueOf(9), new Location(9085, -47488, 972));
    BENON_WALK_ROUTES.put(Integer.valueOf(10), new Location(10858, -47527, 316));
    BENON_WALK_ROUTES.put(Integer.valueOf(11), new Location(10842, -48626, 75));
    BENON_WALK_ROUTES.put(Integer.valueOf(12), new Location(12171, -48464, -547));
    BENON_WALK_ROUTES.put(Integer.valueOf(13), new Location(13565, -49145, -535));
    BENON_WALK_ROUTES.put(Integer.valueOf(14), new Location(15653, -49159, -1059));
    BENON_WALK_ROUTES.put(Integer.valueOf(15), new Location(15423, -48402, -839));
    BENON_WALK_ROUTES.put(Integer.valueOf(16), new Location(15066, -47438, -419));
    BENON_WALK_ROUTES.put(Integer.valueOf(17), new Location(13990, -46843, -292));
    BENON_WALK_ROUTES.put(Integer.valueOf(18), new Location(13685, -47371, -163));
    BENON_WALK_ROUTES.put(Integer.valueOf(19), new Location(13384, -47470, -163));
    BENON_WALK_ROUTES.put(Integer.valueOf(20), new Location(14609, -48608, 346));
    BENON_WALK_ROUTES.put(Integer.valueOf(21), new Location(13878, -47449, 747));
    BENON_WALK_ROUTES.put(Integer.valueOf(22), new Location(12894, -49109, 980));
    BENON_WALK_ROUTES.put(Integer.valueOf(23), new Location(10135, -49150, 996));
    BENON_WALK_ROUTES.put(Integer.valueOf(24), new Location(12894, -49109, 980));
    BENON_WALK_ROUTES.put(Integer.valueOf(25), new Location(13738, -50894, 747));
    BENON_WALK_ROUTES.put(Integer.valueOf(26), new Location(14579, -49698, 347));
    BENON_WALK_ROUTES.put(Integer.valueOf(27), new Location(12896, -51135, -166));
    BENON_WALK_ROUTES.put(Integer.valueOf(28), new Location(12971, -52046, -292));
    BENON_WALK_ROUTES.put(Integer.valueOf(29), new Location(15140, -50781, -442));
    BENON_WALK_ROUTES.put(Integer.valueOf(30), new Location(15328, -50406, -603));
    BENON_WALK_ROUTES.put(Integer.valueOf(31), new Location(15594, -49192, -1059));
    BENON_WALK_ROUTES.put(Integer.valueOf(32), new Location(13175, -49153, -537));
  }
}