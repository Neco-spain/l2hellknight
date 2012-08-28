package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastSet;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2BoxInstance extends L2NpcInstance
{
  private static final int MAX_ITEMS_PER_PAGE = 25;
  private static final String INSERT_GRANT = "INSERT INTO boxaccess (charname,spawn) VALUES(?,?)";
  private static final String DELETE_GRANT = "DELETE FROM boxaccess WHERE charname=? AND spawn=?";
  private static final String LIST_GRANT = "SELECT charname FROM boxaccess WHERE spawn=?";
  private static final String VARIABLE_PREFIX = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  public L2BoxInstance(int objectId, L2NpcTemplate _template)
  {
    super(objectId, _template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    String playerName = player.getName();
    boolean access = hasAccess(playerName);

    if (command.startsWith("Withdraw"))
    {
      if (access)
        showWithdrawWindow(player, command.substring(9));
    }
    else if (command.startsWith("Deposit"))
    {
      if (access)
        showDepositWindow(player, command.substring(8));
    }
    else if (command.startsWith("InBox"))
    {
      if (access)
        putInBox(player, command.substring(6));
    }
    else if (command.startsWith("OutBox"))
    {
      if (access)
        takeOutBox(player, command.substring(7));
    }
    else super.onBypassFeedback(player, command);
  }

  public boolean hasRandomAnimation()
  {
    return false;
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0)
    {
      pom = "" + npcId;
    }
    else
    {
      pom = npcId + "-" + val;
    }
    return "data/html/custom/" + pom + ".htm";
  }

  public boolean hasAccess(String player)
  {
    Connection con = null;
    boolean result = false;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement st = con.prepareStatement("SELECT spawn, charname FROM boxaccess WHERE charname=? AND spawn=?");
      st.setString(1, player);
      st.setInt(2, getSpawn().getId());
      ResultSet rs = st.executeQuery();
      if (rs.next())
        result = true;
      rs.close();
      st.close();
    }
    catch (Exception e)
    {
      _log.info("hasAccess failed: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return result;
  }

  public List getAccess()
  {
    Connection con = null;
    List acl = new FastList();
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement st = con.prepareStatement("SELECT charname FROM boxaccess WHERE spawn=?");
      st.setInt(1, getSpawn().getId());
      ResultSet rs = st.executeQuery();
      while (rs.next())
      {
        acl.add(rs.getString("charname"));
      }
      rs.close();
      st.close();
    }
    catch (Exception e)
    {
      _log.info("getAccess failed: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return acl;
  }

  public boolean grantAccess(String player, boolean what)
  {
    Connection con = null;
    boolean result = false;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      String _query;
      String _query;
      if (what)
        _query = "INSERT INTO boxaccess (charname,spawn) VALUES(?,?)";
      else {
        _query = "DELETE FROM boxaccess WHERE charname=? AND spawn=?";
      }
      PreparedStatement st = con.prepareStatement(_query);
      st.setString(1, player);
      st.setInt(2, getSpawn().getId());
      st.execute();
      st.close();
    }
    catch (Exception e)
    {
      result = false;
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return result;
  }

  private void showWithdrawWindow(L2PcInstance player, String command)
  {
    String drawername = "trash";
    if (command == null)
      return;
    String[] cmd = command.split(" ");
    int startPos = 0;
    if (cmd != null)
      drawername = cmd[0];
    if (cmd.length > 1) {
      startPos = Integer.parseInt(cmd[1]);
    }
    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    int nitems = 0;
    Set _items = getItems(drawername);
    if (startPos >= _items.size())
      startPos = 0;
    String button = "<button value=\"Withdraw\" width=80 height=15 action=\"bypass -h npc_" + getObjectId() + "_OutBox " + drawername;
    String next = "<button value=\"next\" width=50 height=15 action=\"bypass -h npc_" + getObjectId() + "_Withdraw " + drawername + " " + (startPos + 25) + "\">";
    String back = "<button value=\"back\" width=50 height=15 action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">";
    String content = "<html><body>Drawer " + drawername + ":<br>" + next + " " + back + "<table width=\"100%\">";
    content = content + "<tr><td>Item</td><td>Count</td><td>Withdraw</td></tr>";
    for (L2BoxItem i : _items)
    {
      nitems++;
      if (nitems < startPos)
        continue;
      String varname = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(nitems - startPos) + String.valueOf(i.itemid);
      content = content + "<tr><td>" + i.name + "</td><td align=\"right\">" + i.count + "</td>";
      content = content + "<td><edit var=\"" + varname + "\" width=30></td></tr>";
      button = button + " ," + varname + " $" + varname;
      if (nitems - startPos >= 25)
        break;
    }
    button = button + "\">";
    content = content + "</table><br>" + button + "</body></html>";
    _log.fine("setHtml(" + content + "); items=" + nitems);
    html.setHtml(content);
    player.sendPacket(html);

    player.sendPacket(new ActionFailed());
  }

  private void showDepositWindow(L2PcInstance player, String command)
  {
    String drawername = "trash";
    if (command == null)
      return;
    String[] cmd = command.split(" ");
    int startPos = 0;
    if (cmd != null)
      drawername = cmd[0];
    if (cmd.length > 1) {
      startPos = Integer.parseInt(cmd[1]);
    }
    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    int nitems = 0;
    Set _items = new FastSet();
    for (L2ItemInstance i : player.getInventory().getItems())
    {
      if ((i.getItemId() == 57) || (i.isEquipped()))
        continue;
      L2BoxItem bi = new L2BoxItem(i.getItemId(), i.getCount(), i.getItem().getName(), i.getObjectId(), i.getEnchantLevel());
      _items.add(bi);
    }
    if (startPos >= _items.size())
      startPos = 0;
    String button = "<button value=\"Deposit\" width=80 height=15 action=\"bypass -h npc_" + getObjectId() + "_InBox " + drawername;
    String next = "<button value=\"next\" width=50 height=15 action=\"bypass -h npc_" + getObjectId() + "_Deposit " + drawername + " " + (startPos + 25) + "\">";
    String back = "<button value=\"back\" width=50 height=15 action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">";
    String content = "<html><body>Drawer " + drawername + ":<br>" + next + " " + back + "<table width=\"100%\">";
    content = content + "<tr><td>Item</td><td>Count</td><td>Deposit</td></tr>";
    for (L2BoxItem i : _items)
    {
      nitems++;
      if (nitems < startPos)
        continue;
      String varname = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(nitems - startPos) + String.valueOf(i.itemid);
      content = content + "<tr><td>" + i.name + "</td><td align=\"right\">" + i.count + "</td>";
      content = content + "<td><edit var=\"" + varname + "\" width=30></td></tr>";
      button = button + " ," + varname + " $" + varname;
      if (nitems - startPos >= 25)
        break;
    }
    button = button + "\">";
    content = content + "</table><br>" + button + "</body></html>";
    _log.fine("setHtml(" + content + "); items=" + nitems);
    html.setHtml(content);
    player.sendPacket(html);

    player.sendPacket(new ActionFailed());
  }

  private Set<L2BoxItem> getItems(String drawer)
  {
    Set it = new FastSet();
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, spawn, npcid, drawer, itemid, name, count, enchant FROM boxes where spawn=? and npcid=? and drawer=?");
      statement.setInt(1, getSpawn().getId());
      statement.setInt(2, getNpcId());
      statement.setString(3, drawer);
      ResultSet rs = statement.executeQuery();
      while (rs.next())
      {
        _log.fine("found: itemid=" + rs.getInt("itemid") + ", count=" + rs.getInt("count"));
        it.add(new L2BoxItem(rs.getInt("itemid"), rs.getInt("count"), rs.getString("name"), rs.getInt("id"), rs.getInt("enchant")));
      }
      rs.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.info("getItems failed: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return it;
  }

  private void putInBox(L2PcInstance player, String command)
  {
  }

  private void takeOutBox(L2PcInstance player, String command)
  {
  }

  private class L2BoxItem
    implements Comparable
  {
    public int itemid;
    public int id;
    public int count;
    public int enchant;
    public String name;

    public L2BoxItem()
    {
    }

    public L2BoxItem(int _itemid, int _count, String _name, int _id, int _enchant)
    {
      itemid = _itemid;
      count = _count;
      name = _name;
      id = _id;
      enchant = _enchant;
    }

    public int compareTo(Object o) {
      int r = name.compareToIgnoreCase(((L2BoxItem)o).name);
      if (r != 0)
        return r;
      if (id < ((L2BoxItem)o).id)
        return -1;
      return 1;
    }
  }
}