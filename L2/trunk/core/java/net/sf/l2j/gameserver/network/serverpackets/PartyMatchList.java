package net.sf.l2j.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class PartyMatchList extends L2GameServerPacket
{
  private static final String _S__AF_PARTYMATCHLIST = "[S] 96 PartyMatchList";
  private static Logger _log = Logger.getLogger(PartyMatchList.class.getName());
  private int _nomber;
  private String _Title;
  private int _zone;
  private int _level_min;
  private int _level_max;
  private int _in_party;
  private int _size_party;
  private String _name_lider;
  private int _sort_zone;
  private int _size;

  public PartyMatchList(int sort_zone, int sort_lvl)
  {
    _sort_zone = sort_zone;
  }

  protected final void writeImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    writeC(150);
    writeD(1);
    writeD(getSize(_sort_zone));

    Connection con = null;
	PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT nomber, title , zone , level_min , level_max, in_party , size_party , name_lider FROM party_match WHERE status=?");
      statement.setInt(1, 1);

      rs = statement.executeQuery();

      while (rs.next())
      {
        _nomber = rs.getInt("nomber");
        _Title = rs.getString("title");
        _zone = rs.getInt("zone");
        _level_min = rs.getInt("level_min");
        _level_max = rs.getInt("level_max");
        _in_party = rs.getInt("in_party");
        _size_party = rs.getInt("size_party");
        _name_lider = rs.getString("name_lider");

        if ((_sort_zone != -1) && (_sort_zone != _zone))
          continue;
        writeD(_nomber);
        writeS(_Title);
        writeD(_zone);
        writeD(_level_min);
        writeD(_level_max);
        writeD(_in_party);
        writeD(_size_party);
        writeS(_name_lider);
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

  public int getSize(int Zone)
  {
	Connection con = null;
	PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      if (Zone == -1)
      {
        statement = con.prepareStatement("SELECT COUNT(*) FROM party_match WHERE status=? ");
        statement.setInt(1, 1);
      }
      else
      {
        statement = con.prepareStatement("SELECT COUNT(*) FROM party_match WHERE status=? AND zone=? ");
        statement.setInt(1, 1);
        statement.setInt(2, Zone);
      }
      rs = statement.executeQuery();

      while (rs.next())
        _size += rs.getInt(1);
    }
    catch (Exception e)
    {
      _log.warning("Exception: getSize : " + e);
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

    return _size;
  }

  public String getType()
  {
    return _S__AF_PARTYMATCHLIST;
  }
}