package l2p.gameserver.cache;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrestCache
{
  public static final int ALLY_CREST_SIZE = 192;
  public static final int CREST_SIZE = 256;
  public static final int LARGE_CREST_SIZE = 2176;
  private static final Logger _log = LoggerFactory.getLogger(CrestCache.class);

  private static final CrestCache _instance = new CrestCache();

  private final TIntIntHashMap _pledgeCrestId = new TIntIntHashMap();
  private final TIntIntHashMap _pledgeCrestLargeId = new TIntIntHashMap();
  private final TIntIntHashMap _allyCrestId = new TIntIntHashMap();

  private final TIntObjectHashMap<byte[]> _pledgeCrest = new TIntObjectHashMap();
  private final TIntObjectHashMap<byte[]> _pledgeCrestLarge = new TIntObjectHashMap();
  private final TIntObjectHashMap<byte[]> _allyCrest = new TIntObjectHashMap();

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock readLock = lock.readLock();
  private final Lock writeLock = lock.writeLock();

  public static final CrestCache getInstance()
  {
    return _instance;
  }

  private CrestCache()
  {
    load();
  }

  public void load()
  {
    int count = 0;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT clan_id, crest FROM clan_data WHERE crest IS NOT NULL");
      rset = statement.executeQuery();
      while (rset.next())
      {
        count++;

        int pledgeId = rset.getInt("clan_id");
        byte[] crest = rset.getBytes("crest");

        int crestId = getCrestId(pledgeId, crest);

        _pledgeCrestId.put(pledgeId, crestId);
        _pledgeCrest.put(crestId, crest);
      }

      DbUtils.close(statement, rset);

      statement = con.prepareStatement("SELECT clan_id, largecrest FROM clan_data WHERE largecrest IS NOT NULL");
      rset = statement.executeQuery();
      while (rset.next())
      {
        count++;

        int pledgeId = rset.getInt("clan_id");
        byte[] crest = rset.getBytes("largecrest");

        int crestId = getCrestId(pledgeId, crest);

        _pledgeCrestLargeId.put(pledgeId, crestId);
        _pledgeCrestLarge.put(crestId, crest);
      }

      DbUtils.close(statement, rset);

      statement = con.prepareStatement("SELECT ally_id, crest FROM ally_data WHERE crest IS NOT NULL");
      rset = statement.executeQuery();
      while (rset.next())
      {
        count++;
        int pledgeId = rset.getInt("ally_id");
        byte[] crest = rset.getBytes("crest");

        int crestId = getCrestId(pledgeId, crest);

        _allyCrestId.put(pledgeId, crestId);
        _allyCrest.put(crestId, crest);
      }
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
    _log.info("CrestCache: Loaded " + count + " crests");
  }

  private static int getCrestId(int pledgeId, byte[] crest)
  {
    return Math.abs(new HashCodeBuilder(15, 87).append(pledgeId).append(crest).toHashCode());
  }

  public byte[] getPledgeCrest(int crestId)
  {
    byte[] crest = null;

    readLock.lock();
    try
    {
      crest = (byte[])_pledgeCrest.get(crestId);
    }
    finally
    {
      readLock.unlock();
    }

    return crest;
  }

  public byte[] getPledgeCrestLarge(int crestId)
  {
    byte[] crest = null;

    readLock.lock();
    try
    {
      crest = (byte[])_pledgeCrestLarge.get(crestId);
    }
    finally
    {
      readLock.unlock();
    }

    return crest;
  }

  public byte[] getAllyCrest(int crestId)
  {
    byte[] crest = null;

    readLock.lock();
    try
    {
      crest = (byte[])_allyCrest.get(crestId);
    }
    finally
    {
      readLock.unlock();
    }

    return crest;
  }

  public int getPledgeCrestId(int pledgeId)
  {
    int crestId = 0;

    readLock.lock();
    try
    {
      crestId = _pledgeCrestId.get(pledgeId);
    }
    finally
    {
      readLock.unlock();
    }

    return crestId;
  }

  public int getPledgeCrestLargeId(int pledgeId)
  {
    int crestId = 0;

    readLock.lock();
    try
    {
      crestId = _pledgeCrestLargeId.get(pledgeId);
    }
    finally
    {
      readLock.unlock();
    }

    return crestId;
  }

  public int getAllyCrestId(int pledgeId)
  {
    int crestId = 0;
    readLock.lock();
    try
    {
      crestId = _allyCrestId.get(pledgeId);
    }
    finally
    {
      readLock.unlock();
    }

    return crestId;
  }

  public void removePledgeCrest(int pledgeId)
  {
    writeLock.lock();
    try
    {
      _pledgeCrest.remove(_pledgeCrestId.remove(pledgeId));
    }
    finally
    {
      writeLock.unlock();
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
      statement.setNull(1, -3);
      statement.setInt(2, pledgeId);
      statement.execute();
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

  public void removePledgeCrestLarge(int pledgeId)
  {
    writeLock.lock();
    try
    {
      _pledgeCrestLarge.remove(_pledgeCrestLargeId.remove(pledgeId));
    }
    finally
    {
      writeLock.unlock();
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clan_data SET largecrest=? WHERE clan_id=?");
      statement.setNull(1, -3);
      statement.setInt(2, pledgeId);
      statement.execute();
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

  public void removeAllyCrest(int pledgeId)
  {
    writeLock.lock();
    try
    {
      _allyCrest.remove(_allyCrestId.remove(pledgeId));
    }
    finally
    {
      writeLock.unlock();
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
      statement.setNull(1, -3);
      statement.setInt(2, pledgeId);
      statement.execute();
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

  public int savePledgeCrest(int pledgeId, byte[] crest)
  {
    int crestId = getCrestId(pledgeId, crest);

    writeLock.lock();
    try
    {
      _pledgeCrestId.put(pledgeId, crestId);
      _pledgeCrest.put(crestId, crest);
    }
    finally
    {
      writeLock.unlock();
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clan_data SET crest=? WHERE clan_id=?");
      statement.setBytes(1, crest);
      statement.setInt(2, pledgeId);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    return crestId;
  }

  public int savePledgeCrestLarge(int pledgeId, byte[] crest)
  {
    int crestId = getCrestId(pledgeId, crest);

    writeLock.lock();
    try
    {
      _pledgeCrestLargeId.put(pledgeId, crestId);
      _pledgeCrestLarge.put(crestId, crest);
    }
    finally
    {
      writeLock.unlock();
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE clan_data SET largecrest=? WHERE clan_id=?");
      statement.setBytes(1, crest);
      statement.setInt(2, pledgeId);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    return crestId;
  }

  public int saveAllyCrest(int pledgeId, byte[] crest)
  {
    int crestId = getCrestId(pledgeId, crest);

    writeLock.lock();
    try
    {
      _allyCrestId.put(pledgeId, crestId);
      _allyCrest.put(crestId, crest);
    }
    finally
    {
      writeLock.unlock();
    }

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE ally_data SET crest=? WHERE ally_id=?");
      statement.setBytes(1, crest);
      statement.setInt(2, pledgeId);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    return crestId;
  }
}