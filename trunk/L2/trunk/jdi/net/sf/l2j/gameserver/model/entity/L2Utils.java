package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.CloseUtil;

public class L2Utils
{
  public static L2PcInstance loadPlayer(String charName)
  {
    L2PcInstance result = L2World.getInstance().getPlayer(charName);
    if (result == null) {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement("select obj_id from characters where char_name like ?");
        stm.setString(1, charName);
        ResultSet r = stm.executeQuery();
        if (r.next())
          result = L2PcInstance.load(r.getInt(1));
        r.close();
        stm.close();
      }
      catch (SQLException e)
      {
        result = null;
      } finally {
        CloseUtil.close(con);
        con = null;
      }
    }

    return result;
  }
  public static String formatUserItems(L2PcInstance player, int startItem, IItemFilter filter, String actionString) {
    String result = "<table width=300>";
    int startwith = 0;
    for (L2ItemInstance it : player.getInventory().getItems()) {
      if ((startwith++ < startItem) || (
        (filter != null) && (!filter.isCanShow(it)))) continue;
      result = result + "<tr><td>";
      if (actionString != null) {
        String s = actionString.replace("%itemid%", String.valueOf(it.getItemId()));
        s = s.replace("%objectId%", String.valueOf(it.getObjectId()));
        result = result + "<a action=\"" + s + "\">";
      }

      if (it.getEnchantLevel() > 0) result = result + "+" + it.getEnchantLevel() + " ";
      result = result + it.getItemName();
      if (actionString != null)
        result = result + "</a>";
      result = result + "</td><td>";
      if (it.getCount() > 1) result = result + it.getCount() + " \u0421\u20AC\u0421\u201A.";
      result = result + "</td></tr>";
    }
    result = result + "<table>";
    return result;
  }
  public static String loadMessage(String msg) {
    if (msg.startsWith("@")) {
      msg = msg.substring(1);
      int iPos = msg.indexOf(";");
      if (iPos != -1) {
        StringTable st = new StringTable(msg.substring(0, iPos));
        return st.Message(msg.substring(iPos + 1));
      }
    }
    return msg;
  }

  public static abstract interface IItemFilter
  {
    public abstract boolean isCanShow(L2ItemInstance paramL2ItemInstance);
  }
}