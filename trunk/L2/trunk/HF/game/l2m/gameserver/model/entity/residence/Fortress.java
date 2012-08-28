package l2m.gameserver.model.entity.residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.data.dao.ClanDataDAO;
import l2m.gameserver.data.dao.FortressDAO;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.model.items.ClanWarehouse;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fortress extends Residence
{
  private static final long serialVersionUID = 1L;
  private static final Logger _log = LoggerFactory.getLogger(Fortress.class);
  private static final long REMOVE_CYCLE = 168L;
  private static final long REWARD_CYCLE = 6L;
  public static final long CASTLE_FEE = 25000L;
  public static final int DOMAIN = 0;
  public static final int BOUNDARY = 1;
  public static final int NOT_DECIDED = 0;
  public static final int INDEPENDENT = 1;
  public static final int CONTRACT_WITH_CASTLE = 2;
  public static final int REINFORCE = 0;
  public static final int GUARD_BUFF = 1;
  public static final int DOOR_UPGRADE = 2;
  public static final int DWARVENS = 3;
  public static final int SCOUT = 4;
  public static final int FACILITY_MAX = 5;
  private int[] _facilities = new int[5];
  private int _state;
  private int _castleId;
  private int _supplyCount;
  private final List<Castle> _relatedCastles = new ArrayList(5);

  public Fortress(StatsSet set)
  {
    super(set);
  }

  public ResidenceType getType()
  {
    return ResidenceType.Fortress;
  }

  public void changeOwner(Clan clan)
  {
    if (clan != null)
    {
      if (clan.getHasFortress() != 0)
      {
        Fortress oldFortress = (Fortress)ResidenceHolder.getInstance().getResidence(Fortress.class, clan.getHasFortress());
        if (oldFortress != null)
          oldFortress.changeOwner(null);
      }
      if (clan.getCastle() != 0)
      {
        Castle oldCastle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, clan.getCastle());
        if (oldCastle != null) {
          oldCastle.changeOwner(null);
        }
      }
    }

    if ((getOwnerId() > 0) && ((clan == null) || (clan.getClanId() != getOwnerId())))
    {
      removeSkills();
      Clan oldOwner = getOwner();
      if (oldOwner != null) {
        oldOwner.setHasFortress(0);
      }
      cancelCycleTask();
      clearFacility();
    }

    if (clan != null) {
      clan.setHasFortress(getId());
    }

    updateOwnerInDB(clan);

    rewardSkills();

    setFortState(0, 0);
    setJdbcState(JdbcEntityState.UPDATED);

    update();
  }

  protected void loadData()
  {
    _owner = ClanDataDAO.getInstance().getOwner(this);
    FortressDAO.getInstance().select(this);
  }

  private void updateOwnerInDB(Clan clan)
  {
    _owner = clan;

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clan_data SET hasFortress=0 WHERE hasFortress=? LIMIT 1");
      statement.setInt(1, getId());
      statement.execute();
      DbUtils.close(statement);

      if (clan != null)
      {
        statement = con.prepareStatement("UPDATE clan_data SET hasFortress=? WHERE clan_id=? LIMIT 1");
        statement.setInt(1, getId());
        statement.setInt(2, getOwnerId());
        statement.execute();

        clan.broadcastClanStatus(true, false, false);
      }
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void setFortState(int state, int castleId)
  {
    _state = state;
    _castleId = castleId;
  }

  public int getCastleId()
  {
    return _castleId;
  }

  public int getContractState()
  {
    return _state;
  }

  public void chanceCycle()
  {
    super.chanceCycle();
    if (getCycle() >= 168L)
    {
      getOwner().broadcastToOnlineMembers(new IStaticPacket[] { SystemMsg.ENEMY_BLOOD_PLEDGES_HAVE_INTRUDED_INTO_THE_FORTRESS });
      changeOwner(null);
      return;
    }

    setPaidCycle(getPaidCycle() + 1);

    if (getPaidCycle() % 6L == 0L)
    {
      setPaidCycle(0);
      setRewardCount(getRewardCount() + 1);

      if (getContractState() == 2)
      {
        Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _castleId);
        if ((castle.getOwner() == null) || (castle.getOwner().getReputationScore() < 2) || (_owner.getWarehouse().getCountOf(57) > 25000L))
        {
          setSupplyCount(0);
          setFortState(1, 0);
          clearFacility();
        }
        else if (_supplyCount < 6)
        {
          castle.getOwner().incReputation(-2, false, "Fortress:chanceCycle():" + getId());
          _owner.getWarehouse().destroyItemByItemId(57, 25000L);
          _supplyCount += 1;
        }
      }
    }
  }

  public void update()
  {
    FortressDAO.getInstance().update(this);
  }

  public int getSupplyCount()
  {
    return _supplyCount;
  }

  public void setSupplyCount(int c)
  {
    _supplyCount = c;
  }

  public int getFacilityLevel(int type)
  {
    return _facilities[type];
  }

  public void setFacilityLevel(int type, int val)
  {
    _facilities[type] = val;
  }

  public void clearFacility()
  {
    for (int i = 0; i < _facilities.length; i++)
      _facilities[i] = 0;
  }

  public int[] getFacilities()
  {
    return _facilities;
  }

  public void addRelatedCastle(Castle castle)
  {
    _relatedCastles.add(castle);
  }

  public List<Castle> getRelatedCastles()
  {
    return _relatedCastles;
  }
}