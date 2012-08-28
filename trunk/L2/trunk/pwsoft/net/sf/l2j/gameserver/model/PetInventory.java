package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javolution.util.FastTable;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public class PetInventory extends Inventory
{
  private final L2PetInstance _owner;

  public FastTable<L2ItemInstance> listItems()
  {
    FastTable items = new FastTable();
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      String loc = "PET";

      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=?) ORDER BY object_id DESC");
      statement.setInt(1, getOwnerId());
      statement.setString(2, loc);
      rset = statement.executeQuery();

      while (rset.next())
      {
        int objectId = rset.getInt(1);
        L2ItemInstance item = L2ItemInstance.restoreFromDb(objectId);
        if (item == null)
          continue;
        items.add(item);
      }

    }
    catch (Exception e)
    {
    }
    finally
    {
      Close.CSR(con, statement, rset);
    }
    return items;
  }

  public PetInventory(L2PetInstance owner)
  {
    _owner = owner;
  }

  public L2PetInstance getOwner()
  {
    return _owner;
  }

  protected L2ItemInstance.ItemLocation getBaseLocation()
  {
    return L2ItemInstance.ItemLocation.PET;
  }

  protected L2ItemInstance.ItemLocation getEquipLocation()
  {
    return L2ItemInstance.ItemLocation.PET_EQUIP;
  }
}