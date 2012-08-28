package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.model.entity.Wedding;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CoupleManager
{
  private static final Log _log = LogFactory.getLog(CoupleManager.class.getName());
  private static CoupleManager _instance;
  private FastList<Couple> _couples = new FastList();
  private Map<Integer, Wedding> _wedding = new ConcurrentHashMap();

  public static final CoupleManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new CoupleManager();
    _instance.load();
  }

  public void reload()
  {
    _couples.clear();
    load();
  }

  private final void load()
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT id FROM mods_wedding ORDER BY id");
      rs = statement.executeQuery();
      rs.setFetchSize(50);

      while (rs.next())
      {
        _couples.add(new Couple(rs.getInt("id")));
      }
    }
    catch (Exception e)
    {
      _log.error("Exception: CoupleManager.load(): " + e.getMessage(), e);
    }
    finally
    {
      Close.CSR(con, statement, rs);
    }
    _log.info("CoupleManager: Loaded " + _couples.size() + " couples(s)");
  }

  public final Couple getCouple(int coupleId)
  {
    int index = getCoupleIndex(coupleId);
    if (index >= 0) return (Couple)_couples.get(index);
    return null;
  }

  public void createCouple(L2PcInstance player1, L2PcInstance player2)
  {
    if ((player1 != null) && (player2 != null))
    {
      if ((player1.getPartnerId() == 0) && (player2.getPartnerId() == 0))
      {
        int _player1id = player1.getObjectId();
        int _player2id = player2.getObjectId();

        Couple _new = new Couple(player1, player2);
        _couples.add(_new);
        player1.setPartnerId(_player2id);
        player2.setPartnerId(_player1id);
        player1.setCoupleId(_new.getId());
        player2.setCoupleId(_new.getId());
      }
    }
  }

  public void deleteCouple(int coupleId)
  {
    int index = getCoupleIndex(coupleId);
    Couple couple = (Couple)_couples.get(index);
    if (couple != null)
    {
      L2PcInstance player1 = L2World.getInstance().getPlayer(couple.getPlayer1Id());
      L2PcInstance player2 = L2World.getInstance().getPlayer(couple.getPlayer2Id());
      if (player1 != null)
      {
        player1.setPartnerId(0);
        player1.setMarried(false);
        player1.setCoupleId(0);
      }

      if (player2 != null)
      {
        player2.setPartnerId(0);
        player2.setMarried(false);
        player2.setCoupleId(0);
      }

      couple.divorce();
      _couples.remove(index);
    }
  }

  public final int getCoupleIndex(int coupleId)
  {
    int i = 0;
    FastList.Node n = _couples.head(); for (FastList.Node end = _couples.tail(); (n = n.getNext()) != end; )
    {
      Couple temp = (Couple)n.getValue();
      if (temp == null) {
        continue;
      }
      if (temp.getId() == coupleId)
        return i;
      i++;
    }
    return -1;
  }

  public final FastList<Couple> getCouples()
  {
    return _couples;
  }

  public void regWedding(int id, Wedding wed)
  {
    _wedding.put(Integer.valueOf(id), wed);
  }

  public Wedding getWedding(int id)
  {
    return (Wedding)_wedding.get(Integer.valueOf(id));
  }
}