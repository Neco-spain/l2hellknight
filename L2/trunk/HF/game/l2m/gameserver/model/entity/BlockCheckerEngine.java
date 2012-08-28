package l2m.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.instancemanager.games.HandysBlockCheckerManager;
import l2m.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2m.gameserver.listener.actor.player.OnPlayerExitListener;
import l2m.gameserver.listener.actor.player.OnTeleportListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.SimpleSpawner;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.base.TeamType;
import l2m.gameserver.model.instances.BlockInstance;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.ExBasicActionList;
import l2m.gameserver.network.serverpackets.ExCubeGameChangePoints;
import l2m.gameserver.network.serverpackets.ExCubeGameCloseUI;
import l2m.gameserver.network.serverpackets.ExCubeGameEnd;
import l2m.gameserver.network.serverpackets.ExCubeGameExtendedChangePoints;
import l2m.gameserver.network.serverpackets.RelationChanged;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlockCheckerEngine
{
  private static final Logger _log = LoggerFactory.getLogger(BlockCheckerEngine.class);
  private HandysBlockCheckerManager.ArenaParticipantsHolder _holder;
  private Map<Player, Integer> _redTeamPoints = new ConcurrentHashMap();
  private Map<Player, Integer> _blueTeamPoints = new ConcurrentHashMap();

  private int _redPoints = 15;
  private int _bluePoints = 15;

  private int _arena = -1;

  private List<SimpleSpawner> _spawns = new CopyOnWriteArrayList();
  private boolean _isRedWinner;
  private long _startedTime;
  private static final int[][] _arenaCoordinates = { { -58368, -62745, -57751, -62131, -58053, -62417 }, { -58350, -63853, -57756, -63266, -58053, -63551 }, { -57194, -63861, -56580, -63249, -56886, -63551 }, { -57200, -62727, -56584, -62115, -56850, -62391 } };
  private static final int _zCoord = -2405;
  private NpcInstance _girlNpc;
  private List<ItemInstance> _drops = new ArrayList();
  private static final byte DEFAULT_ARENA = -1;
  private boolean _isStarted = false;
  private ScheduledFuture<?> _task;
  private boolean _abnormalEnd = false;
  private final String[] zoneNames = { "[block_checker_1]", "[block_checker_2]", "[block_checker_3]", "[block_checker_4]" };

  private OnExitPlayerListener _listener = new OnExitPlayerListener(null);

  public BlockCheckerEngine(HandysBlockCheckerManager.ArenaParticipantsHolder holder, int arena)
  {
    _holder = holder;
    if ((arena > -1) && (arena < 4)) {
      _arena = arena;
    }
    for (Player player : holder.getRedPlayers())
      _redTeamPoints.put(player, Integer.valueOf(0));
    for (Player player : holder.getBluePlayers())
      _blueTeamPoints.put(player, Integer.valueOf(0));
  }

  public void updatePlayersOnStart(HandysBlockCheckerManager.ArenaParticipantsHolder holder)
  {
    _holder = holder;
  }

  public HandysBlockCheckerManager.ArenaParticipantsHolder getHolder()
  {
    return _holder;
  }

  public int getArena()
  {
    return _arena;
  }

  public long getStarterTime()
  {
    return _startedTime;
  }

  public int getRedPoints()
  {
    synchronized (this)
    {
      return _redPoints;
    }
  }

  public int getBluePoints()
  {
    synchronized (this)
    {
      return _bluePoints;
    }
  }

  public int getPlayerPoints(Player player, boolean isRed)
  {
    if ((!_redTeamPoints.containsKey(player)) && (!_blueTeamPoints.containsKey(player))) {
      return 0;
    }
    if (isRed) {
      return ((Integer)_redTeamPoints.get(player)).intValue();
    }
    return ((Integer)_blueTeamPoints.get(player)).intValue();
  }

  public synchronized void increasePlayerPoints(Player player, int team)
  {
    if (player == null) {
      return;
    }
    if (team == 0)
    {
      int points = getPlayerPoints(player, true) + 1;
      _redTeamPoints.put(player, Integer.valueOf(points));
      _redPoints += 1;
      _bluePoints -= 1;
    }
    else
    {
      int points = getPlayerPoints(player, false) + 1;
      _blueTeamPoints.put(player, Integer.valueOf(points));
      _bluePoints += 1;
      _redPoints -= 1;
    }
  }

  public void addNewDrop(ItemInstance item)
  {
    if (item != null)
      _drops.add(item);
  }

  public boolean isStarted()
  {
    return _isStarted;
  }

  private void broadcastRelationChanged(Player plr)
  {
    for (Player p : _holder.getAllPlayers())
      p.sendPacket(RelationChanged.update(plr, p, plr));
  }

  public void endEventAbnormally()
  {
    try
    {
      synchronized (this)
      {
        _isStarted = false;

        if (_task != null) {
          _task.cancel(true);
        }
        _abnormalEnd = true;

        ThreadPoolManager.getInstance().execute(new EndEvent());
      }
    }
    catch (Exception e)
    {
      _log.error("Couldnt end Block Checker event at " + _arena + e);
    }
  }

  public void clearArena(String zoneName)
  {
    Zone zone = ReflectionUtils.getZone(zoneName);
    if (zone != null)
      for (Creature cha : zone.getObjects())
        if ((cha.isPlayer()) && (cha.getPlayer().getBlockCheckerArena() < 0))
          cha.getPlayer().teleToClosestTown();
        else if (cha.isNpc())
          cha.deleteMe();
  }

  private class OnExitPlayerListener
    implements OnTeleportListener, OnPlayerExitListener
  {
    private boolean _isExit = false;

    private OnExitPlayerListener() {
    }
    public void onTeleport(Player player, int x, int y, int z, Reflection reflection) { if (_isExit)
        return;
      onPlayerExit(player);
    }

    public void onPlayerExit(Player player)
    {
      if (player.getBlockCheckerArena() < 0)
        return;
      _isExit = true;
      player.teleToLocation(-57478, -60367, -2370);
      player.setTransformation(0);
      player.getEffectList().stopAllEffects();
      int arena = player.getBlockCheckerArena();
      int team = HandysBlockCheckerManager.getInstance().getHolder(arena).getPlayerTeam(player);
      HandysBlockCheckerManager.getInstance().removePlayer(player, arena, team);

      player.setTeam(TeamType.NONE);
      player.broadcastCharInfo();

      PcInventory inv = player.getInventory();
      inv.destroyItemByItemId(13787, inv.getCountOf(13787));
      inv.destroyItemByItemId(13788, inv.getCountOf(13788));
    }
  }

  class EndEvent extends RunnableImpl
  {
    EndEvent()
    {
    }

    private void clearMe()
    {
      HandysBlockCheckerManager.getInstance().clearPaticipantQueueByArenaId(_arena);
      for (Player player : _holder.getAllPlayers())
      {
        if (player == null) {
          continue;
        }
        player.removeListener(_listener);
      }
      _holder.clearPlayers();
      _blueTeamPoints.clear();
      _redTeamPoints.clear();
      HandysBlockCheckerManager.getInstance().setArenaFree(_arena);

      for (SimpleSpawner spawn : _spawns) {
        spawn.deleteAll();
      }
      _spawns.clear();

      for (ItemInstance item : _drops)
      {
        if ((item == null) || 
          (!item.isVisible()) || (item.getOwnerId() != 0)) {
          continue;
        }
        item.deleteMe();
      }
      _drops.clear();
    }

    private void rewardPlayers()
    {
      if (_redPoints == _bluePoints) {
        return;
      }
      BlockCheckerEngine.access$1602(BlockCheckerEngine.this, _redPoints > _bluePoints);

      if (_isRedWinner)
      {
        rewardAsWinner(true);
        rewardAsLooser(false);
        SystemMessage msg = new SystemMessage(2928).addString("Red Team");

        _holder.broadCastPacketToTeam(msg);
      }
      else if (_bluePoints > _redPoints)
      {
        rewardAsWinner(false);
        rewardAsLooser(true);
        SystemMessage msg = new SystemMessage(2928).addString("Blue Team");
        _holder.broadCastPacketToTeam(msg);
      }
      else
      {
        rewardAsLooser(true);
        rewardAsLooser(false);
      }
    }

    private void addRewardItemWithMessage(int id, long count, Player player)
    {
      player.getInventory().addItem(id, ()(count * Config.ALT_RATE_COINS_REWARD_BLOCK_CHECKER));
      player.sendPacket(new SystemMessage(29).addItemName(id).addNumber(count));
    }

    private void rewardAsWinner(boolean isRed)
    {
      Map tempPoints = isRed ? _redTeamPoints : _blueTeamPoints;

      for (Player pc : tempPoints.keySet())
      {
        if (pc == null) {
          continue;
        }
        if (((Integer)tempPoints.get(pc)).intValue() >= 10)
          addRewardItemWithMessage(13067, 2L, pc);
        else {
          tempPoints.remove(pc);
        }
      }
      int first = 0; int second = 0;
      Player winner1 = null; Player winner2 = null;
      for (Player pc : tempPoints.keySet())
      {
        int pcPoints = ((Integer)tempPoints.get(pc)).intValue();
        if (pcPoints > first)
        {
          second = first;
          winner2 = winner1;

          first = pcPoints;
          winner1 = pc;
        }
        else if (pcPoints > second)
        {
          second = pcPoints;
          winner2 = pc;
        }
      }
      if (winner1 != null)
        addRewardItemWithMessage(13067, 8L, winner1);
      if (winner2 != null)
        addRewardItemWithMessage(13067, 5L, winner2);
    }

    private void rewardAsLooser(boolean isRed)
    {
      Map tempPoints = isRed ? _redTeamPoints : _blueTeamPoints;

      for (Player player : tempPoints.keySet())
        if ((player != null) && (((Integer)tempPoints.get(player)).intValue() >= 10))
          addRewardItemWithMessage(13067, 2L, player);
    }

    private void setPlayersBack()
    {
      ExCubeGameEnd end = new ExCubeGameEnd(_isRedWinner);

      for (Player player : _holder.getAllPlayers())
      {
        if (player == null) {
          continue;
        }
        player.getEffectList().stopAllEffects();

        player.setTeam(TeamType.NONE);

        player.setBlockCheckerArena(-1);

        PcInventory inv = player.getInventory();
        inv.destroyItemByItemId(13787, inv.getCountOf(13787));
        inv.destroyItemByItemId(13788, inv.getCountOf(13788));
        BlockCheckerEngine.this.broadcastRelationChanged(player);

        player.teleToLocation(-57478, -60367, -2370);

        player.sendPacket(end);
        player.broadcastCharInfo();
      }
    }

    public void runImpl()
    {
      if (!_abnormalEnd)
        rewardPlayers();
      BlockCheckerEngine.access$1102(BlockCheckerEngine.this, false);
      setPlayersBack();
      clearMe();
      BlockCheckerEngine.access$1702(BlockCheckerEngine.this, false);
    }
  }

  class CountDown extends RunnableImpl
  {
    private int seconds = 5;

    CountDown() {
    }
    public void runImpl() throws Exception {
      switch (seconds)
      {
      case 5:
        _holder.broadCastPacketToTeam(new SystemMessage(2922));
        break;
      case 4:
        _holder.broadCastPacketToTeam(new SystemMessage(2923));
        break;
      case 3:
        _holder.broadCastPacketToTeam(new SystemMessage(2925));
        break;
      case 2:
        _holder.broadCastPacketToTeam(new SystemMessage(2926));
        break;
      case 1:
        _holder.broadCastPacketToTeam(new SystemMessage(2927));
      }

      if (--seconds > 0)
        ThreadPoolManager.getInstance().schedule(this, 1000L);
      else
        ThreadPoolManager.getInstance().execute(new BlockCheckerEngine.EndEvent(BlockCheckerEngine.this));
    }
  }

  class SpawnRound extends RunnableImpl
  {
    int _numOfBoxes;
    int _round;

    SpawnRound(int numberOfBoxes, int round)
    {
      _numOfBoxes = numberOfBoxes;
      _round = round;
    }

    public void runImpl()
    {
      if (!_isStarted) {
        return;
      }
      switch (_round)
      {
      case 1:
        BlockCheckerEngine.access$1302(BlockCheckerEngine.this, ThreadPoolManager.getInstance().schedule(new SpawnRound(BlockCheckerEngine.this, 20, 2), 60000L));
        break;
      case 2:
        BlockCheckerEngine.access$1302(BlockCheckerEngine.this, ThreadPoolManager.getInstance().schedule(new SpawnRound(BlockCheckerEngine.this, 14, 3), 60000L));
        break;
      case 3:
        BlockCheckerEngine.access$1302(BlockCheckerEngine.this, ThreadPoolManager.getInstance().schedule(new BlockCheckerEngine.CountDown(BlockCheckerEngine.this), 175000L));
      }

      byte random = 2;

      NpcTemplate template = NpcHolder.getInstance().getTemplate(18672);
      try
      {
        for (int i = 0; i < _numOfBoxes; i++)
        {
          SimpleSpawner spawn = new SimpleSpawner(template);
          spawn.setLocx(BlockCheckerEngine._arenaCoordinates[_arena][4] + Rnd.get(-400, 400));
          spawn.setLocy(BlockCheckerEngine._arenaCoordinates[_arena][5] + Rnd.get(-400, 400));
          spawn.setLocz(-2405);
          spawn.setAmount(1);
          spawn.setHeading(1);
          spawn.setRespawnDelay(1);
          BlockInstance blockInstance = (BlockInstance)spawn.doSpawn(true);
          blockInstance.setRed(random % 2 == 0);

          _spawns.add(spawn);
          random = (byte)(random + 1);
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      if ((_round == 1) || (_round == 2))
      {
        NpcTemplate girl = NpcHolder.getInstance().getTemplate(18676);
        try
        {
          SimpleSpawner girlSpawn = new SimpleSpawner(girl);
          girlSpawn.setLocx(BlockCheckerEngine._arenaCoordinates[_arena][4] + Rnd.get(-400, 400));
          girlSpawn.setLocy(BlockCheckerEngine._arenaCoordinates[_arena][5] + Rnd.get(-400, 400));
          girlSpawn.setLocz(-2405);
          girlSpawn.setAmount(1);
          girlSpawn.setHeading(1);
          girlSpawn.setRespawnDelay(1);
          girlSpawn.doSpawn(true);
          girlSpawn.init();
          BlockCheckerEngine.access$1402(BlockCheckerEngine.this, girlSpawn.getLastSpawn());

          ThreadPoolManager.getInstance().schedule(new RunnableImpl()
          {
            public void runImpl()
              throws Exception
            {
              if (_girlNpc == null)
                return;
              _girlNpc.deleteMe();
            }
          }
          , 9000L);
        }
        catch (Exception e)
        {
          _log.warn("Couldnt Spawn Block Checker NPCs! Wrong instance type at npc table?" + e);
        }
      }

      BlockCheckerEngine.access$112(BlockCheckerEngine.this, _numOfBoxes / 2);
      BlockCheckerEngine.access$312(BlockCheckerEngine.this, _numOfBoxes / 2);

      int timeLeft = (int)((getStarterTime() - System.currentTimeMillis()) / 1000L);
      ExCubeGameChangePoints changePoints = new ExCubeGameChangePoints(timeLeft, getBluePoints(), getRedPoints());
      getHolder().broadCastPacketToTeam(changePoints);
    }
  }

  public class StartEvent extends RunnableImpl
  {
    private Skill _freeze;
    private Skill _transformationRed;
    private Skill _transformationBlue;
    private final ExCubeGameCloseUI _closeUserInterface = new ExCubeGameCloseUI();

    public StartEvent()
    {
      _freeze = SkillTable.getInstance().getInfo(6034, 1);
      _transformationRed = SkillTable.getInstance().getInfo(6035, 1);
      _transformationBlue = SkillTable.getInstance().getInfo(6036, 1);
    }

    private void setUpPlayers()
    {
      HandysBlockCheckerManager.getInstance().setArenaBeingUsed(_arena);

      BlockCheckerEngine.access$102(BlockCheckerEngine.this, _spawns.size() / 2);
      BlockCheckerEngine.access$302(BlockCheckerEngine.this, _spawns.size() / 2);
      ExCubeGameChangePoints initialPoints = new ExCubeGameChangePoints(300, _bluePoints, _redPoints);

      for (Player player : _holder.getAllPlayers())
      {
        if (player == null) {
          continue;
        }
        player.addListener(_listener);

        boolean isRed = _holder.getRedPlayers().contains(player);

        ExCubeGameExtendedChangePoints clientSetUp = new ExCubeGameExtendedChangePoints(300, _bluePoints, _redPoints, isRed, player, 0);
        player.sendPacket(clientSetUp);

        player.sendActionFailed();

        int tc = _holder.getPlayerTeam(player) * 2;

        int x = BlockCheckerEngine._arenaCoordinates[_arena][tc];
        int y = BlockCheckerEngine._arenaCoordinates[_arena][(tc + 1)];
        player.teleToLocation(x, y, -2405);

        if (isRed)
        {
          _redTeamPoints.put(player, Integer.valueOf(0));
          player.setTeam(TeamType.RED);
        }
        else
        {
          _blueTeamPoints.put(player, Integer.valueOf(0));
          player.setTeam(TeamType.BLUE);
        }
        player.getEffectList().stopAllEffects();

        if (player.getPet() != null) {
          player.getPet().unSummon();
        }

        _freeze.getEffects(player, player, false, false);

        if (_holder.getPlayerTeam(player) == 0)
          _transformationRed.getEffects(player, player, false, false);
        else {
          _transformationBlue.getEffects(player, player, false, false);
        }
        player.setBlockCheckerArena((byte)_arena);

        player.sendPacket(initialPoints);
        player.sendPacket(_closeUserInterface);

        player.sendPacket(new ExBasicActionList(player));
        BlockCheckerEngine.this.broadcastRelationChanged(player);
        player.broadcastCharInfo();
      }
    }

    public void runImpl()
    {
      if (_arena == -1)
      {
        _log.error("Couldnt set up the arena Id for the Block Checker event, cancelling event...");
        return;
      }
      if (isStarted())
        return;
      clearArena(zoneNames[_arena]);
      BlockCheckerEngine.access$1102(BlockCheckerEngine.this, true);

      ThreadPoolManager.getInstance().execute(new BlockCheckerEngine.SpawnRound(BlockCheckerEngine.this, 16, 1));

      setUpPlayers();

      BlockCheckerEngine.access$1202(BlockCheckerEngine.this, System.currentTimeMillis() + 300000L);
    }
  }
}