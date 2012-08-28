package l2m.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.entity.residence.ClanHall;
import l2m.gameserver.model.entity.residence.Fortress;
import l2m.gameserver.model.entity.residence.Residence;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.data.tables.ClanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClanDataDAO
{
  private static final Logger _log = LoggerFactory.getLogger(ClanDataDAO.class);
  private static final ClanDataDAO _instance = new ClanDataDAO();
  public static final String SELECT_CASTLE_OWNER = "SELECT clan_id FROM clan_data WHERE hasCastle = ? LIMIT 1";
  public static final String SELECT_FORTRESS_OWNER = "SELECT clan_id FROM clan_data WHERE hasFortress = ? LIMIT 1";
  public static final String SELECT_CLANHALL_OWNER = "SELECT clan_id FROM clan_data WHERE hasHideout = ? LIMIT 1";

  public static ClanDataDAO getInstance()
  {
    return _instance;
  }

  public Clan getOwner(Castle c)
  {
    return getOwner(c, "SELECT clan_id FROM clan_data WHERE hasCastle = ? LIMIT 1");
  }

  public Clan getOwner(Fortress f)
  {
    return getOwner(f, "SELECT clan_id FROM clan_data WHERE hasFortress = ? LIMIT 1");
  }

  public Clan getOwner(ClanHall c)
  {
    return getOwner(c, "SELECT clan_id FROM clan_data WHERE hasHideout = ? LIMIT 1");
  }

  private Clan getOwner(Residence residence, String sql)
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement(sql);
      statement.setInt(1, residence.getId());
      rset = statement.executeQuery();
      if (rset.next()) {
        Clan localClan = ClanTable.getInstance().getClan(rset.getInt("clan_id"));
        return localClan;
      }
    }
    catch (Exception e)
    {
      _log.error("ClanDataDAO.getOwner(Residence, String)", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
    return null;
  }
}