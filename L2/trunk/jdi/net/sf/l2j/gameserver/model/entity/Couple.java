package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Couple
{
  private static final Logger _log = Logger.getLogger(Couple.class.getName());

  private int _Id = 0;
  private int _player1Id = 0;
  private int _player2Id = 0;
  private boolean _maried = false;
  private Calendar _affiancedDate;
  private Calendar _weddingDate;

  public Couple(int coupleId)
  {
    _Id = coupleId;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("Select * from mods_wedding where id = ?");
      statement.setInt(1, _Id);
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
        _player1Id = rs.getInt("player1Id");
        _player2Id = rs.getInt("player2Id");
        _maried = rs.getBoolean("married");

        _affiancedDate = Calendar.getInstance();
        _affiancedDate.setTimeInMillis(rs.getLong("affianceDate"));

        _weddingDate = Calendar.getInstance();
        _weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
      }
      statement.close();
    }
    catch (Exception e)
    {
      _log.severe("Exception: Couple.load(): " + e.getMessage()); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public Couple(L2PcInstance player1, L2PcInstance player2) {
    int _tempPlayer1Id = player1.getObjectId();
    int _tempPlayer2Id = player2.getObjectId();

    _player1Id = _tempPlayer1Id;
    _player2Id = _tempPlayer2Id;

    _affiancedDate = Calendar.getInstance();
    _affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

    _weddingDate = Calendar.getInstance();
    _weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      _Id = IdFactory.getInstance().getNextId();
      PreparedStatement statement = con.prepareStatement("INSERT INTO mods_wedding (id, player1Id, player2Id, married, affianceDate, weddingDate) VALUES (?, ?, ?, ?, ?, ?)");
      statement.setInt(1, _Id);
      statement.setInt(2, _player1Id);
      statement.setInt(3, _player2Id);
      statement.setBoolean(4, false);
      statement.setLong(5, _affiancedDate.getTimeInMillis());
      statement.setLong(6, _weddingDate.getTimeInMillis());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.severe("");
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void marry() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE mods_wedding set married = ?, weddingDate = ? where id = ?");
      statement.setBoolean(1, true);
      _weddingDate = Calendar.getInstance();
      statement.setLong(2, _weddingDate.getTimeInMillis());
      statement.setInt(3, _Id);
      statement.execute();
      statement.close();
      _maried = true;
    }
    catch (Exception e)
    {
      _log.severe("");
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public void divorce() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("DELETE FROM mods_wedding WHERE id=?");
      statement.setInt(1, _Id);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.severe("Exception: Couple.divorce(): " + e.getMessage());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public final int getId() {
    return _Id;
  }
  public final int getPlayer1Id() { return _player1Id; } 
  public final int getPlayer2Id() { return _player2Id; } 
  public final boolean getMaried() {
    return _maried;
  }
  public final Calendar getAffiancedDate() { return _affiancedDate; } 
  public final Calendar getWeddingDate() { return _weddingDate;
  }
}