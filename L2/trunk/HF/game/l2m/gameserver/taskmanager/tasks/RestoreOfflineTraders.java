package l2m.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestoreOfflineTraders extends RunnableImpl
{
  private static final Logger _log = LoggerFactory.getLogger(RestoreOfflineTraders.class);

  public void runImpl()
    throws Exception
  {
    int count = 0;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      if (Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L)
      {
        int expireTimeSecs = (int)(System.currentTimeMillis() / 1000L - Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);

        statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND value < ?");
        statement.setLong(1, expireTimeSecs);
        statement.executeUpdate();

        DbUtils.close(statement);
      }

      statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND obj_id IN (SELECT obj_id FROM characters WHERE accessLevel < 0)");
      statement.executeUpdate();

      DbUtils.close(statement);

      statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offline'");
      rset = statement.executeQuery();

      while (rset.next())
      {
        int objectId = rset.getInt("obj_id");
        int expireTimeSecs = rset.getInt("value");

        Player p = Player.restore(objectId);
        if (p == null) {
          continue;
        }
        if (p.isDead())
        {
          p.kick();
          continue;
        }

        p.setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR);
        p.setOfflineMode(true);
        p.setIsOnline(true);

        p.spawnMe();

        if ((p.getClan() != null) && (p.getClan().getAnyMember(p.getObjectId()) != null)) {
          p.getClan().getAnyMember(p.getObjectId()).setPlayerInstance(p, false);
        }
        if (Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L) {
          p.startKickTask((Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK + expireTimeSecs - System.currentTimeMillis() / 1000L) * 1000L);
        }

        if (Config.SERVICES_TRADE_ONLY_FAR) {
          for (Player player : World.getAroundPlayers(p, Config.SERVICES_TRADE_RADIUS, 200))
            if (player.isInStoreMode())
              if (player.isInOfflineMode())
              {
                player.setOfflineMode(false);
                player.kick();
                _log.warn("Offline trader: " + player + " kicked.");
              }
              else
              {
                player.setPrivateStoreType(0);
                player.standUp();
                player.broadcastCharInfo();
              }
        }
        count++;
      }
    }
    catch (Exception e)
    {
      _log.error("Error while restoring offline traders!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    _log.info("Restored " + count + " offline traders");
  }
}