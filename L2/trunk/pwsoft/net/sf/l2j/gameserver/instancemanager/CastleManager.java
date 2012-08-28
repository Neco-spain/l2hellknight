package net.sf.l2j.gameserver.instancemanager;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class CastleManager
{
  private static final Logger _log = AbstractLogger.getLogger(CastleManager.class.getName());
  private static CastleManager _instance;
  private List<Castle> _castles;
  private static final int[] _castleCirclets = { 0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183 };

  int _castleId = 1;

  public static final CastleManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new CastleManager();
    _instance.load();
  }

  public final int findNearestCastleIndex(L2Object obj)
  {
    int index = getCastleIndex(obj);
    if (index < 0)
    {
      double closestDistance = 99999999.0D;

      for (int i = 0; i < getCastles().size(); i++)
      {
        Castle castle = (Castle)getCastles().get(i);
        if (castle != null) {
          double distance = castle.getDistance(obj);
          if (closestDistance <= distance)
            continue;
          closestDistance = distance;
          index = i;
        }
      }
    }
    return index;
  }

  private final void load()
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT id FROM castle ORDER BY id");
      rs = st.executeQuery();
      rs.setFetchSize(50);
      while (rs.next())
      {
        getCastles().add(new Castle(rs.getInt("id")));
      }
    }
    catch (Exception e)
    {
      _log.warning("Exception: loadCastleData(): " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
    _log.info("CastleManager: Loaded " + getCastles().size() + " castles");
  }

  public final Castle getCastleById(int castleId)
  {
    for (Castle temp : getCastles())
    {
      if (temp.getCastleId() == castleId)
        return temp;
    }
    return null;
  }

  public final Castle getCastleByOwner(L2Clan clan)
  {
    for (Castle temp : getCastles())
    {
      if (temp.getOwnerId() == clan.getClanId())
        return temp;
    }
    return null;
  }

  public final Castle getCastle(String name)
  {
    for (Castle temp : getCastles())
    {
      if (temp.getName().equalsIgnoreCase(name.trim()))
        return temp;
    }
    return null;
  }

  public final Castle getCastle(int x, int y, int z)
  {
    for (Castle temp : getCastles())
    {
      if (temp.checkIfInZone(x, y, z))
        return temp;
    }
    return null;
  }
  public final Castle getCastle(L2Object activeObject) {
    return getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
  }

  public final int getCastleIndex(int castleId)
  {
    for (int i = 0; i < getCastles().size(); i++)
    {
      Castle castle = (Castle)getCastles().get(i);
      if ((castle != null) && (castle.getCastleId() == castleId)) return i;
    }
    return -1;
  }

  public final int getCastleIndex(L2Object activeObject)
  {
    return getCastleIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
  }

  public final int getCastleIndex(int x, int y, int z)
  {
    for (int i = 0; i < getCastles().size(); i++)
    {
      Castle castle = (Castle)getCastles().get(i);
      if ((castle != null) && (castle.checkIfInZone(x, y, z))) return i;
    }
    return -1;
  }

  public final List<Castle> getCastles()
  {
    if (_castles == null) _castles = new FastList();
    return _castles;
  }

  public final void validateTaxes(int sealStrifeOwner)
  {
    int maxTax;
    switch (sealStrifeOwner)
    {
    case 1:
      maxTax = 5;
      break;
    case 2:
      maxTax = 25;
      break;
    default:
      maxTax = 15;
    }

    for (Castle castle : _castles)
      if (castle.getTaxPercent() > maxTax)
        castle.setTaxPercent(maxTax);
  }

  public int getCirclet()
  {
    return getCircletByCastleId(_castleId);
  }

  public int getCircletByCastleId(int castleId)
  {
    if ((castleId > 0) && (castleId < 10)) {
      return _castleCirclets[castleId];
    }
    return 0;
  }

  public void removeCirclet(L2Clan clan, int castleId)
  {
    for (L2ClanMember member : clan.getMembers())
      removeCirclet(member, castleId);
  }

  public void removeCirclet(L2ClanMember member, int castleId) {
    if (member == null) return;
    L2PcInstance player = member.getPlayerInstance();
    int circletId = getCircletByCastleId(castleId);

    if (circletId != 0)
    {
      if (player != null)
      {
        try
        {
          L2ItemInstance circlet = player.getInventory().getItemByItemId(circletId);
          if (circlet != null)
          {
            if (circlet.isEquipped())
              player.getInventory().unEquipItemInSlotAndRecord(circlet.getEquipSlot());
            player.destroyItemByItemId("CastleCircletRemoval", circletId, 1, player, true);
          }
          return;
        }
        catch (NullPointerException e) {
          e.printStackTrace();
        }

      }

      Connect con = null;
      PreparedStatement st = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("DELETE FROM items WHERE owner_id = ? and item_id = ?");
        st.setInt(1, member.getObjectId());
        st.setInt(2, circletId);
        st.execute();
      }
      catch (Exception e)
      {
        System.out.println("Failed to remove castle circlets offline for player " + member.getName());
        e.printStackTrace();
      }
      finally
      {
        Close.CS(con, st);
      }
    }
  }
}