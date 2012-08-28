package l2p.gameserver.model.entity.residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2p.commons.dao.JdbcEntity;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.events.EventType;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.items.ClanWarehouse;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Residence
  implements JdbcEntity
{
  private static final Logger _log = LoggerFactory.getLogger(Residence.class);
  public static final long CYCLE_TIME = 3600000L;
  protected final int _id;
  protected final String _name;
  protected Clan _owner;
  protected Zone _zone;
  protected List<ResidenceFunction> _functions = new ArrayList();
  protected List<Skill> _skills = new ArrayList();
  protected SiegeEvent<?, ?> _siegeEvent;
  protected Calendar _siegeDate = Calendar.getInstance();
  protected Calendar _lastSiegeDate = Calendar.getInstance();
  protected Calendar _ownDate = Calendar.getInstance();
  protected ScheduledFuture<?> _cycleTask;
  private int _cycle;
  private int _rewardCount;
  private int _paidCycle;
  protected JdbcEntityState _jdbcEntityState = JdbcEntityState.CREATED;

  protected List<Location> _banishPoints = new ArrayList();
  protected List<Location> _ownerRestartPoints = new ArrayList();
  protected List<Location> _otherRestartPoints = new ArrayList();
  protected List<Location> _chaosRestartPoints = new ArrayList();

  public Residence(StatsSet set)
  {
    _id = set.getInteger("id");
    _name = set.getString("name");
  }

  public abstract ResidenceType getType();

  public void init() {
    initZone();
    initEvent();

    loadData();
    loadFunctions();
    rewardSkills();
    startCycleTask();
  }

  protected void initZone()
  {
    _zone = ReflectionUtils.getZone("residence_" + _id);
    _zone.setParam("residence", this);
  }

  protected void initEvent()
  {
    _siegeEvent = ((SiegeEvent)EventHolder.getInstance().getEvent(EventType.SIEGE_EVENT, _id));
  }

  public <E extends SiegeEvent> E getSiegeEvent()
  {
    return _siegeEvent;
  }

  public int getId()
  {
    return _id;
  }

  public String getName()
  {
    return _name;
  }

  public int getOwnerId()
  {
    return _owner == null ? 0 : _owner.getClanId();
  }

  public Clan getOwner()
  {
    return _owner;
  }

  public Zone getZone()
  {
    return _zone;
  }
  protected abstract void loadData();

  public abstract void changeOwner(Clan paramClan);

  public Calendar getOwnDate() {
    return _ownDate;
  }

  public Calendar getSiegeDate()
  {
    return _siegeDate;
  }

  public Calendar getLastSiegeDate()
  {
    return _lastSiegeDate;
  }

  public void addSkill(Skill skill)
  {
    _skills.add(skill);
  }

  public void addFunction(ResidenceFunction function)
  {
    _functions.add(function);
  }

  public boolean checkIfInZone(Location loc, Reflection ref)
  {
    return checkIfInZone(loc.x, loc.y, loc.z, ref);
  }

  public boolean checkIfInZone(int x, int y, int z, Reflection ref)
  {
    return (getZone() != null) && (getZone().checkIfInZone(x, y, z, ref));
  }

  public void banishForeigner()
  {
    for (Player player : _zone.getInsidePlayers())
    {
      if (player.getClanId() == getOwnerId()) {
        continue;
      }
      player.teleToLocation(getBanishPoint());
    }
  }

  public void rewardSkills()
  {
    Clan owner = getOwner();
    if (owner != null)
    {
      for (Skill skill : _skills)
      {
        owner.addSkill(skill, false);
        owner.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill) });
      }
    }
  }

  public void removeSkills()
  {
    Clan owner = getOwner();
    if (owner != null)
    {
      for (Skill skill : _skills)
        owner.removeSkill(skill.getId());
    }
  }

  protected void loadFunctions()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM residence_functions WHERE id = ?");
      statement.setInt(1, getId());
      rs = statement.executeQuery();
      while (rs.next())
      {
        ResidenceFunction function = getFunction(rs.getInt("type"));
        function.setLvl(rs.getInt("lvl"));
        function.setEndTimeInMillis(rs.getInt("endTime") * 1000L);
        function.setInDebt(rs.getBoolean("inDebt"));
        function.setActive(true);
        startAutoTaskForFunction(function);
      }
    }
    catch (Exception e)
    {
      _log.warn("Residence: loadFunctions(): " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rs);
    }
  }

  public boolean isFunctionActive(int type)
  {
    ResidenceFunction function = getFunction(type);

    return (function != null) && (function.isActive()) && (function.getLevel() > 0);
  }

  public ResidenceFunction getFunction(int type)
  {
    for (int i = 0; i < _functions.size(); i++)
      if (((ResidenceFunction)_functions.get(i)).getType() == type)
        return (ResidenceFunction)_functions.get(i);
    return null;
  }

  public boolean updateFunctions(int type, int level)
  {
    Clan clan = getOwner();
    if (clan == null) {
      return false;
    }
    long count = clan.getAdenaCount();

    ResidenceFunction function = getFunction(type);
    if (function == null) {
      return false;
    }
    if ((function.isActive()) && (function.getLevel() == level)) {
      return true;
    }
    int lease = level == 0 ? 0 : getFunction(type).getLease(level);

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      long time;
      if (!function.isActive())
      {
        if (count >= lease) {
          clan.getWarehouse().destroyItemByItemId(57, lease);
        } else {
          int i = 0;
          return i;
        }
        time = Calendar.getInstance().getTimeInMillis() + 86400000L;

        statement = con.prepareStatement("REPLACE residence_functions SET id=?, type=?, lvl=?, endTime=?");
        statement.setInt(1, getId());
        statement.setInt(2, type);
        statement.setInt(3, level);
        statement.setInt(4, (int)(time / 1000L));
        statement.execute();

        function.setLvl(level);
        function.setEndTimeInMillis(time);
        function.setActive(true);
        startAutoTaskForFunction(function);
      }
      else
      {
        if (count >= lease - getFunction(type).getLease())
        {
          if (lease > getFunction(type).getLease())
            clan.getWarehouse().destroyItemByItemId(57, lease - getFunction(type).getLease());
        }
        else {
          int j = 0;
          return j;
        }
        statement = con.prepareStatement("REPLACE residence_functions SET id=?, type=?, lvl=?");
        statement.setInt(1, getId());
        statement.setInt(2, type);
        statement.setInt(3, level);
        statement.execute();

        function.setLvl(level);
      }
    }
    catch (Exception e)
    {
      _log.warn("Exception: SiegeUnit.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
    return true;
  }

  public void removeFunction(int type)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM residence_functions WHERE id=? AND type=?");
      statement.setInt(1, getId());
      statement.setInt(2, type);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.warn("Exception: removeFunctions(int type): " + e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  private void startAutoTaskForFunction(ResidenceFunction function)
  {
    if (getOwnerId() == 0) {
      return;
    }
    Clan clan = getOwner();

    if (clan == null) {
      return;
    }
    if (function.getEndTimeInMillis() > System.currentTimeMillis()) {
      ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
    } else if ((function.isInDebt()) && (clan.getAdenaCount() >= function.getLease()))
    {
      clan.getWarehouse().destroyItemByItemId(57, function.getLease());
      function.updateRentTime(false);
      ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
    }
    else if (!function.isInDebt())
    {
      function.setInDebt(true);
      function.updateRentTime(true);
      ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
    }
    else
    {
      function.setLvl(0);
      function.setActive(false);
      removeFunction(function.getType());
    }
  }

  public void setJdbcState(JdbcEntityState state)
  {
    _jdbcEntityState = state;
  }

  public JdbcEntityState getJdbcState()
  {
    return _jdbcEntityState;
  }

  public void save()
  {
    throw new UnsupportedOperationException();
  }

  public void delete()
  {
    throw new UnsupportedOperationException();
  }

  public void cancelCycleTask()
  {
    _cycle = 0;
    _paidCycle = 0;
    _rewardCount = 0;
    if (_cycleTask != null)
    {
      _cycleTask.cancel(false);
      _cycleTask = null;
    }

    setJdbcState(JdbcEntityState.UPDATED);
  }

  public void startCycleTask()
  {
    if (_owner == null) {
      return;
    }
    long ownedTime = getOwnDate().getTimeInMillis();
    if (ownedTime == 0L)
      return;
    long diff = System.currentTimeMillis() - ownedTime;
    while (diff >= 3600000L) {
      diff -= 3600000L;
    }
    _cycleTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ResidenceCycleTask(), diff, 3600000L);
  }

  public void chanceCycle()
  {
    setCycle(getCycle() + 1);

    setJdbcState(JdbcEntityState.UPDATED);
  }

  public List<Skill> getSkills()
  {
    return _skills;
  }

  public void addBanishPoint(Location loc)
  {
    _banishPoints.add(loc);
  }

  public void addOwnerRestartPoint(Location loc)
  {
    _ownerRestartPoints.add(loc);
  }

  public void addOtherRestartPoint(Location loc)
  {
    _otherRestartPoints.add(loc);
  }

  public void addChaosRestartPoint(Location loc)
  {
    _chaosRestartPoints.add(loc);
  }

  public Location getBanishPoint()
  {
    if (_banishPoints.isEmpty())
      return null;
    return (Location)_banishPoints.get(Rnd.get(_banishPoints.size()));
  }

  public Location getOwnerRestartPoint()
  {
    if (_ownerRestartPoints.isEmpty())
      return null;
    return (Location)_ownerRestartPoints.get(Rnd.get(_ownerRestartPoints.size()));
  }

  public Location getOtherRestartPoint()
  {
    if (_otherRestartPoints.isEmpty())
      return null;
    return (Location)_otherRestartPoints.get(Rnd.get(_otherRestartPoints.size()));
  }

  public Location getChaosRestartPoint()
  {
    if (_chaosRestartPoints.isEmpty())
      return null;
    return (Location)_chaosRestartPoints.get(Rnd.get(_chaosRestartPoints.size()));
  }

  public Location getNotOwnerRestartPoint(Player player)
  {
    return player.getKarma() > 0 ? getChaosRestartPoint() : getOtherRestartPoint();
  }

  public int getCycle()
  {
    return _cycle;
  }

  public long getCycleDelay()
  {
    if (_cycleTask == null)
      return 0L;
    return _cycleTask.getDelay(TimeUnit.SECONDS);
  }

  public void setCycle(int cycle)
  {
    _cycle = cycle;
  }

  public int getPaidCycle()
  {
    return _paidCycle;
  }

  public void setPaidCycle(int paidCycle)
  {
    _paidCycle = paidCycle;
  }

  public int getRewardCount()
  {
    return _rewardCount;
  }

  public void setRewardCount(int rewardCount)
  {
    _rewardCount = rewardCount;
  }

  private class AutoTaskForFunctions extends RunnableImpl
  {
    ResidenceFunction _function;

    public AutoTaskForFunctions(ResidenceFunction function)
    {
      _function = function;
    }

    public void runImpl()
      throws Exception
    {
      Residence.this.startAutoTaskForFunction(_function);
    }
  }

  public class ResidenceCycleTask extends RunnableImpl
  {
    public ResidenceCycleTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      chanceCycle();

      update();
    }
  }
}