package l2m.gameserver.model.entity.SevenSignsFestival;

import java.util.concurrent.Future;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.SimpleSpawner;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.entity.SevenSigns;
import l2m.gameserver.model.instances.FestivalMonsterInstance;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DarknessFestival extends Reflection
{
  private static final Logger _log = LoggerFactory.getLogger(DarknessFestival.class);
  public static final int FESTIVAL_LENGTH = 1080000;
  public static final int FESTIVAL_FIRST_SPAWN = 60000;
  public static final int FESTIVAL_SECOND_SPAWN = 540000;
  public static final int FESTIVAL_CHEST_SPAWN = 900000;
  private FestivalSpawn _witchSpawn;
  private FestivalSpawn _startLocation;
  private int currentState = 0;
  private boolean _challengeIncreased = false;
  private final int _levelRange;
  private final int _cabal;
  private Future<?> _spawnTimerTask;

  public DarknessFestival(Party party, int cabal, int level)
  {
    onCreate();
    setName("Darkness Festival");
    setParty(party);
    _levelRange = level;
    _cabal = cabal;
    startCollapseTimer(1140000L);

    if (cabal == 2)
    {
      _witchSpawn = new FestivalSpawn(FestivalSpawn.FESTIVAL_DAWN_WITCH_SPAWNS[_levelRange]);
      _startLocation = new FestivalSpawn(FestivalSpawn.FESTIVAL_DAWN_PLAYER_SPAWNS[_levelRange]);
    }
    else
    {
      _witchSpawn = new FestivalSpawn(FestivalSpawn.FESTIVAL_DUSK_WITCH_SPAWNS[_levelRange]);
      _startLocation = new FestivalSpawn(FestivalSpawn.FESTIVAL_DUSK_PLAYER_SPAWNS[_levelRange]);
    }

    party.setReflection(this);
    setReturnLoc(party.getPartyLeader().getLoc());
    for (Player p : party.getPartyMembers())
    {
      p.setVar("backCoords", p.getLoc().toXYZString(), -1L);
      p.getEffectList().stopAllEffects();
      p.teleToLocation(Location.findPointToStay(_startLocation.loc, 20, 100, getGeoIndex()), this);
    }

    scheduleNext();
    NpcTemplate witchTemplate = NpcHolder.getInstance().getTemplate(_witchSpawn.npcId);
    try
    {
      SimpleSpawner npcSpawn = new SimpleSpawner(witchTemplate);
      npcSpawn.setLoc(_witchSpawn.loc);
      npcSpawn.setReflection(this);
      addSpawn(npcSpawn);
      npcSpawn.doSpawn(true);
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    sendMessageToParticipants("The festival will begin in 1 minute.");
  }

  private void scheduleNext()
  {
    switch (currentState)
    {
    case 0:
      currentState = 60000;

      _spawnTimerTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
      {
        public void runImpl()
          throws Exception
        {
          spawnFestivalMonsters(60, 0);
          DarknessFestival.this.sendMessageToParticipants("Go!");
          DarknessFestival.this.scheduleNext();
        }
      }
      , 60000L);

      break;
    case 60000:
      currentState = 540000;

      _spawnTimerTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
      {
        public void runImpl()
          throws Exception
        {
          spawnFestivalMonsters(60, 2);
          DarknessFestival.this.sendMessageToParticipants("Next wave arrived!");
          DarknessFestival.this.scheduleNext();
        }
      }
      , 480000L);

      break;
    case 540000:
      currentState = 900000;

      _spawnTimerTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
      {
        public void runImpl()
          throws Exception
        {
          spawnFestivalMonsters(60, 3);
          DarknessFestival.this.sendMessageToParticipants("The chests have spawned! Be quick, the festival will end soon.");
        }
      }
      , 360000L);
    }
  }

  public void spawnFestivalMonsters(int respawnDelay, int spawnType)
  {
    int[][] spawns = (int[][])null;
    switch (spawnType)
    {
    case 0:
    case 1:
      spawns = _cabal == 2 ? FestivalSpawn.FESTIVAL_DAWN_PRIMARY_SPAWNS[_levelRange] : FestivalSpawn.FESTIVAL_DUSK_PRIMARY_SPAWNS[_levelRange];
      break;
    case 2:
      spawns = _cabal == 2 ? FestivalSpawn.FESTIVAL_DAWN_SECONDARY_SPAWNS[_levelRange] : FestivalSpawn.FESTIVAL_DUSK_SECONDARY_SPAWNS[_levelRange];
      break;
    case 3:
      spawns = _cabal == 2 ? FestivalSpawn.FESTIVAL_DAWN_CHEST_SPAWNS[_levelRange] : FestivalSpawn.FESTIVAL_DUSK_CHEST_SPAWNS[_levelRange];
    }

    if (spawns != null)
      for (int[] element : spawns)
      {
        FestivalSpawn currSpawn = new FestivalSpawn(element);
        NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(currSpawn.npcId);

        SimpleSpawner npcSpawn = new SimpleSpawner(npcTemplate);
        npcSpawn.setReflection(this);
        npcSpawn.setLoc(currSpawn.loc);
        npcSpawn.setHeading(Rnd.get(65536));
        npcSpawn.setAmount(1);
        npcSpawn.setRespawnDelay(respawnDelay);
        npcSpawn.startRespawn();
        FestivalMonsterInstance festivalMob = (FestivalMonsterInstance)npcSpawn.doSpawn(true);

        if (spawnType == 1)
          festivalMob.setOfferingBonus(2);
        else if (spawnType == 3)
          festivalMob.setOfferingBonus(5);
        addSpawn(npcSpawn);
      }
  }

  public boolean increaseChallenge()
  {
    if (_challengeIncreased) {
      return false;
    }
    _challengeIncreased = true;

    spawnFestivalMonsters(60, 1);
    return true;
  }

  public void collapse()
  {
    if (isCollapseStarted()) {
      return;
    }
    if (_spawnTimerTask != null)
    {
      _spawnTimerTask.cancel(false);
      _spawnTimerTask = null;
    }

    if ((SevenSigns.getInstance().getCurrentPeriod() == 1) && (getParty() != null))
    {
      Player player = getParty().getPartyLeader();
      ItemInstance bloodOfferings = player.getInventory().getItemByItemId(5901);
      long offeringCount = bloodOfferings == null ? 0L : bloodOfferings.getCount();

      if (player.getInventory().destroyItem(bloodOfferings))
      {
        long offeringScore = offeringCount * 1L;
        boolean isHighestScore = SevenSignsFestival.getInstance().setFinalScore(getParty(), _cabal, _levelRange, offeringScore);

        player.sendPacket(new SystemMessage(1267).addNumber(offeringScore));

        sendCustomMessageToParticipants("l2p.gameserver.model.entity.SevenSignsFestival.Ended");
        if (isHighestScore)
          sendMessageToParticipants("Your score is highest!");
      }
      else {
        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2FestivalGuideInstance.BloodOfferings", player, new Object[0]));
      }
    }
    super.collapse();
  }

  private void sendMessageToParticipants(String s)
  {
    for (Player p : getPlayers())
      p.sendMessage(s);
  }

  private void sendCustomMessageToParticipants(String s)
  {
    for (Player p : getPlayers())
      p.sendMessage(new CustomMessage(s, p, new Object[0]));
  }

  public void partyMemberExited()
  {
    if ((getParty() == null) || (getParty().getMemberCount() <= 1))
      collapse();
  }

  public boolean canChampions()
  {
    return true;
  }

  public boolean isAutolootForced()
  {
    return true;
  }
}