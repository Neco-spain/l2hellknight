package net.sf.l2j.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;

public class PartyMatchDetail extends L2GameServerPacket
{
  private static final String _S__B0_PARTYMATCHDETAIL = "[S] 97 PartyMatchDetail";
  private static Logger _log = Logger.getLogger(PartyMatchDetail.class.getName());
  private int _number;
  private String _Title;
  private int _zone;
  private int _level_min;
  private int _level_max;
  private int _size_party;

  public PartyMatchDetail(int number)
  {
    _number = number;
  }

  protected final void writeImpl()
  {
    writeC(151);

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT title , zone , level_min , level_max, in_party , size_party , name_lider ,status FROM party_match WHERE nomber=?");
      statement.setInt(1, _number);

      rs = statement.executeQuery();

      while (rs.next())
      {
        _Title = rs.getString("title");
        _zone = rs.getInt("zone");
        _level_min = rs.getInt("level_min");
        _level_max = rs.getInt("level_max");
        rs.getInt("in_party");
        _size_party = rs.getInt("size_party");
        rs.getString("name_lider");
        rs.getInt("status");

        writeD(_number);
        writeD(_size_party);
        writeD(_level_min);
        writeD(_level_max);
        writeD(0);
        writeD(_zone);
        writeS(_Title);
      }
    }
    catch (Exception e)
    {
      _log.warning("Exception:  PartyMatchList: " + e);
    }
    finally
    {
      try
      {
        con.close();
        statement.close();
        rs.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public String getType()
  {
    return "[S] 97 PartyMatchDetail";
  }
}