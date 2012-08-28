package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;

public abstract class ClanHallSiege
{
  protected static Logger _log = Logger.getLogger(ClanHallSiege.class.getName());
  private Calendar _siegeDate;
  public Calendar _siegeEndDate;
  private boolean _isInProgress = false;

  public long restoreSiegeDate(int ClanHallId)
  {
    long res = 0L;
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT siege_data FROM clanhall_siege WHERE id=?");
      statement.setInt(1, ClanHallId);
      ResultSet rs = statement.executeQuery();
      if (rs.next())
        res = rs.getLong("siege_data");
      rs.close();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("Exception: can't get clanhall siege date: " + e);
    }
    finally
    {
      try
      {
        if (con != null)
          con.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return res;
  }

  public void setNewSiegeDate(long siegeDate, int ClanHallId, int hour)
  {
    Calendar tmpDate = Calendar.getInstance();
    if (siegeDate <= System.currentTimeMillis())
    {
      tmpDate.setTimeInMillis(System.currentTimeMillis());
      tmpDate.add(5, 3);
      tmpDate.set(7, 6);
      tmpDate.set(11, hour);
      tmpDate.set(12, 0);
      tmpDate.set(13, 0);
      setSiegeDate(tmpDate);
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("UPDATE clanhall_siege SET siege_data=? WHERE id = ?");
        statement.setLong(1, getSiegeDate().getTimeInMillis());
        statement.setInt(2, ClanHallId);
        statement.execute();
        statement.close();
      }
      catch (SQLException e)
      {
        _log.warning("Exception: can't save clanhall siege date: " + e);
        e.getMessage();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      finally
      {
        try
        {
          if (con != null)
            con.close();
        }
        catch (Exception e)
        {
        }
      }
    }
  }

  public final Calendar getSiegeDate()
  {
    return _siegeDate;
  }

  public final void setSiegeDate(Calendar par)
  {
    _siegeDate = par;
  }

  public final boolean getIsInProgress()
  {
    return _isInProgress;
  }

  public final void setIsInProgress(boolean par)
  {
    _isInProgress = par;
  }
}