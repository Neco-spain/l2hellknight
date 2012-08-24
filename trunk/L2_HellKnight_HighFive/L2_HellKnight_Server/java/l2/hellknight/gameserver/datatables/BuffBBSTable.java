package l2.hellknight.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2.hellknight.L2DatabaseFactory;


public class BuffBBSTable
{
  private static Logger _log = Logger.getLogger(BuffBBSTable.class.getName());

  private Map<Integer, BBSGroupBuffStat> _groupBuff = new FastMap<Integer, BBSGroupBuffStat>();
  @SuppressWarnings("rawtypes")
 private Map<Integer, Map> _skill = new FastMap<Integer, Map>();

  private BuffBBSTable()
  {
    restoreGroupBuff();
    restoreBBSBuff();
  }

  public static BuffBBSTable getInstance()
  {
    return SingletonHolder._instance;
  }

  private void restoreGroupBuff()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM bbs_group_buff");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int idGroup = rset.getInt("id");
        String nameGroup = rset.getString("name");
        int priceGroup = rset.getInt("price");
        if (!this._groupBuff.containsKey(Integer.valueOf(idGroup)))
        {
          this._groupBuff.put(Integer.valueOf(idGroup), new BBSGroupBuffStat(priceGroup, nameGroup));
          this._skill.put(Integer.valueOf(idGroup), new FastMap<Object, Object>());
        }
      }
      _log.info("BuffBBSTable: Loaded " + this._groupBuff.size() + " group buff.");
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "BuffBBSTable: Error reading group_bbs_buff table: " + e.getMessage(), e);
    }
    finally
    {
      L2DatabaseFactory.close(con);
    }
  }


@SuppressWarnings("unchecked")
private void restoreBBSBuff()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM bbs_buff");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int idSkill = rset.getInt("id_skill");
        int lvlSkill = rset.getInt("lvl_skill");
        int idGroup = rset.getInt("id_group");
        if (this._skill.containsKey(Integer.valueOf(idGroup)))
          ((Map<Integer, Integer>)this._skill.get(Integer.valueOf(idGroup))).put(Integer.valueOf(idSkill), Integer.valueOf(lvlSkill));
        else
          _log.warning("BuffBBSTable: no search group id " + idGroup + " for skill id " + idSkill);
      }
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "BuffBBSTable: Error reading buff_bbs table: " + e.getMessage(), e);
    }
    finally
    {
      L2DatabaseFactory.close(con);
    }
  }

  public int getPriceGroup(int id)
  {
    if (this._groupBuff.containsKey(Integer.valueOf(id)))
      return this._groupBuff.get(Integer.valueOf(id)).getPrice();
    return -1;
  }

  public String getNameGroup(int id)
  {
    if (this._groupBuff.containsKey(Integer.valueOf(id)))
      return this._groupBuff.get(Integer.valueOf(id)).getName();
    return null;
  }

  @SuppressWarnings("rawtypes")
public int getBBSGroupFotBuf(int id, int lvl)
  {
    for (Map.Entry entry : this._skill.entrySet()) {
      int key = ((Integer)entry.getKey()).intValue();
      Map<?, ?> skills = (Map<?, ?>)entry.getValue();
      if ((skills.containsKey(Integer.valueOf(id))) && 
        (((Integer)skills.get(Integer.valueOf(id))).intValue() == lvl))
        return key;
    }
    return 0;
  }

  public boolean isBBSSaveBuf(int id, int lvl)
  {
    return getBBSGroupFotBuf(id, lvl) > 0;
  }

  public Map<Integer, BBSGroupBuffStat> getBBSGroups()
  {
    return this._groupBuff;
  }

  @SuppressWarnings("unchecked")
public Map<Integer, Integer> getBBSBuffsForGoup(int id)
  {
    return this._skill.get(Integer.valueOf(id));
  }

  private static class SingletonHolder
  {
    protected static final BuffBBSTable _instance = new BuffBBSTable();
  }

  public class BBSGroupBuffStat
  {
    private int _prise;
    private String _nameGroup;

    private BBSGroupBuffStat(int prise, String nameGroup)
    {
      this._prise = prise;
      this._nameGroup = nameGroup;
    }
    public int getPrice() {
      return this._prise; } 
    public String getName() { return this._nameGroup;
    }
  }
}