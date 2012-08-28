package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class SiegeManager
{
  private static final Logger _log = Logger.getLogger(SiegeManager.class.getName());
  private static SiegeManager _instance;
  private int _attackerMaxClans = 500;
  private int _attackerRespawnDelay = 20000;
  private int _defenderMaxClans = 500;
  private int _defenderRespawnDelay = 10000;
  private FastMap<Integer, FastList<SiegeSpawn>> _artefactSpawnList;
  private FastMap<Integer, FastList<SiegeSpawn>> _controlTowerSpawnList;
  private int _controlTowerLosePenalty = 20000;
  private int _flagMaxCount = 1;
  private int _siegeClanMinLevel = 4;
  private int _siegeLength = 120;
  private List<Siege> _sieges;

  public static final SiegeManager getInstance()
  {
    if (_instance == null)
    {
      System.out.println("Initializing SiegeManager");
      _instance = new SiegeManager();
      _instance.load();
    }
    return _instance;
  }

  public final void addSiegeSkills(L2PcInstance character)
  {
    character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
    character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
  }

  public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
  {
    if ((activeChar == null) || (!(activeChar instanceof L2PcInstance))) return false;

    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
    L2PcInstance player = (L2PcInstance)activeChar;
    Castle castle = CastleManager.getInstance().getCastle(player);

    if ((castle == null) || (castle.getCastleId() <= 0))
      sm.addString("You must be on castle ground to summon this");
    else if (!castle.getSiege().getIsInProgress())
      sm.addString("You can only summon this during a siege.");
    else if ((player.getClanId() != 0) && (castle.getSiege().getAttackerClan(player.getClanId()) == null))
      sm.addString("You can only summon this as a registered attacker.");
    else {
      return true;
    }
    if (!isCheckOnly) player.sendPacket(sm);
    return false;
  }

  public final boolean checkIsRegistered(L2Clan clan, int castleid)
  {
    if (clan == null) return false;

    if (clan.getHasCastle() > 0) return true;

    Connection con = null;
    boolean register = false;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");
      statement.setInt(1, clan.getClanId());
      statement.setInt(2, castleid);
      ResultSet rs = statement.executeQuery();

      if (rs.next())
      {
        register = true;
      }

      rs.close();
      statement.close();
    }
    catch (Exception e)
    {
      System.out.println("Exception: checkIsRegistered(): " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return register;
  }

  public final boolean checkIsRegisteredType(L2Clan clan, int type)
  {
    if (clan == null) return false;

    if (clan.getHasCastle() > 0) return true;

    Connection con = null;
    boolean register = false;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and type=?");
      statement.setInt(1, clan.getClanId());
      statement.setInt(2, type);
      ResultSet rs = statement.executeQuery();

      if (rs.next())
      {
        register = true;
      }

      rs.close();
      statement.close();
    }
    catch (Exception e)
    {
      System.out.println("Exception: checkIsRegistered(): " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return register;
  }

  public final void removeSiegeSkills(L2PcInstance character)
  {
    character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
    character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
  }

  private final void load()
  {
    try
    {
      InputStream is = new FileInputStream(new File("./config/siege.ini"));
      siegeSettings = new Properties();
      siegeSettings.load(is);
      is.close();

      _attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500")).intValue();
      _attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "30000")).intValue();
      _controlTowerLosePenalty = Integer.decode(siegeSettings.getProperty("CTLossPenalty", "20000")).intValue();
      _defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500")).intValue();
      _defenderRespawnDelay = Integer.decode(siegeSettings.getProperty("DefenderRespawn", "20000")).intValue();
      _flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1")).intValue();
      _siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4")).intValue();
      _siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120")).intValue();

      _controlTowerSpawnList = new FastMap();
      _artefactSpawnList = new FastMap();

      for (Castle castle : CastleManager.getInstance().getCastles())
      {
        FastList _controlTowersSpawns = new FastList();

        for (int i = 1; i < 255; i++)
        {
          String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + Integer.toString(i), "");

          if (_spawnParams.length() == 0)
            break;
          StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
          try
          {
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int z = Integer.parseInt(st.nextToken());
            int npc_id = Integer.parseInt(st.nextToken());
            int hp = Integer.parseInt(st.nextToken());

            _controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, 0, npc_id, hp));
          }
          catch (Exception e)
          {
            _log.warning("Error while loading control tower(s) for " + castle.getName() + " castle.");
          }
        }

        FastList _artefactSpawns = new FastList();

        for (int i = 1; i < 255; i++)
        {
          String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + Integer.toString(i), "");

          if (_spawnParams.length() == 0)
            break;
          StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
          try
          {
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int z = Integer.parseInt(st.nextToken());
            int heading = Integer.parseInt(st.nextToken());
            int npc_id = Integer.parseInt(st.nextToken());

            _artefactSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, heading, npc_id));
          }
          catch (Exception e)
          {
            _log.warning("Error while loading artefact(s) for " + castle.getName() + " castle.");
          }
        }

        _controlTowerSpawnList.put(Integer.valueOf(castle.getCastleId()), _controlTowersSpawns);
        _artefactSpawnList.put(Integer.valueOf(castle.getCastleId()), _artefactSpawns);
      }
    }
    catch (Exception e)
    {
      Properties siegeSettings;
      System.err.println("Error while loading siege data.");
      e.printStackTrace();
    }
  }

  public final FastList<SiegeSpawn> getArtefactSpawnList(int _castleId)
  {
    if (_artefactSpawnList.containsKey(Integer.valueOf(_castleId))) {
      return (FastList)_artefactSpawnList.get(Integer.valueOf(_castleId));
    }
    return null;
  }

  public final FastList<SiegeSpawn> getControlTowerSpawnList(int _castleId)
  {
    if (_controlTowerSpawnList.containsKey(Integer.valueOf(_castleId))) {
      return (FastList)_controlTowerSpawnList.get(Integer.valueOf(_castleId));
    }
    return null;
  }
  public final int getAttackerMaxClans() {
    return _attackerMaxClans;
  }
  public final int getAttackerRespawnDelay() { return _attackerRespawnDelay; } 
  public final int getControlTowerLosePenalty() {
    return _controlTowerLosePenalty;
  }
  public final int getDefenderMaxClans() { return _defenderMaxClans; } 
  public final int getDefenderRespawnDelay() {
    return _defenderRespawnDelay;
  }
  public final int getFlagMaxCount() { return _flagMaxCount; } 
  public final Siege getSiege(L2Object activeObject) {
    return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
  }

  public final Siege getSiege(int x, int y, int z) {
    for (Castle castle : CastleManager.getInstance().getCastles())
      if (castle.getSiege().checkIfInZone(x, y, z)) return castle.getSiege();
    return null;
  }
  public final int getSiegeClanMinLevel() {
    return _siegeClanMinLevel;
  }
  public final int getSiegeLength() { return _siegeLength; }

  public final List<Siege> getSieges()
  {
    if (_sieges == null)
    {
      _sieges = new FastList();
    }
    return _sieges;
  }

  public final void addSiege(Siege siege)
  {
    if (_sieges == null) {
      _sieges = new FastList();
    }
    _sieges.add(siege);
  }

  public final void removeSiege(Siege siege)
  {
    if (_sieges == null) {
      _sieges = new FastList();
    }
    _sieges.remove(siege);
  }
  public class SiegeSpawn { Location _location;
    private int _npcId;
    private int _heading;
    private int _castleId;
    private int _hp;

    public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id) { _castleId = castle_id;
      _location = new Location(x, y, z, heading);
      _heading = heading;
      _npcId = npc_id;
    }

    public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
    {
      _castleId = castle_id;
      _location = new Location(x, y, z, heading);
      _heading = heading;
      _npcId = npc_id;
      _hp = hp;
    }

    public int getCastleId()
    {
      return _castleId;
    }

    public int getNpcId()
    {
      return _npcId;
    }

    public int getHeading()
    {
      return _heading;
    }

    public int getHp()
    {
      return _hp;
    }

    public Location getLocation()
    {
      return _location;
    }
  }
}