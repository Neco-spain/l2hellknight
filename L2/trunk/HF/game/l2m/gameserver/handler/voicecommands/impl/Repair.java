package l2m.gameserver.handler.voicecommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.data.dao.ItemsDAO;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.ItemInstance.ItemLocation;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Repair extends Functions
  implements IVoicedCommandHandler
{
  private static final Logger _log = LoggerFactory.getLogger(Repair.class);

  private final String[] _commandList = { "repair" };

  public String[] getVoicedCommandList()
  {
    return _commandList;
  }

  public boolean useVoicedCommand(String command, Player activeChar, String target)
  {
    if (!target.isEmpty())
    {
      if (activeChar.getName().equalsIgnoreCase(target))
      {
        sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCantRepairYourself", activeChar, new Object[0]), activeChar);
        return false;
      }

      int objId = 0;

      for (Map.Entry e : activeChar.getAccountChars().entrySet())
      {
        if (((String)e.getValue()).equalsIgnoreCase(target))
        {
          objId = ((Integer)e.getKey()).intValue();
          break;
        }
      }

      if (objId == 0)
      {
        sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCanRepairOnlyOnSameAccount", activeChar, new Object[0]), activeChar);
        return false;
      }
      if (World.getPlayer(objId) != null)
      {
        sendMessage(new CustomMessage("voicedcommandhandlers.Repair.CharIsOnline", activeChar, new Object[0]), activeChar);
        return false;
      }

      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rs = null;
      try
      {
        con = DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("SELECT karma FROM characters WHERE obj_Id=?");
        statement.setInt(1, objId);
        statement.execute();
        rs = statement.getResultSet();

        int karma = 0;

        rs.next();

        karma = rs.getInt("karma");

        DbUtils.close(statement, rs);

        if (karma > 0)
        {
          statement = con.prepareStatement("UPDATE characters SET x=17144, y=170156, z=-3502 WHERE obj_Id=?");
          statement.setInt(1, objId);
          statement.execute();
          DbUtils.close(statement);
        }
        else
        {
          statement = con.prepareStatement("UPDATE characters SET x=0, y=0, z=0 WHERE obj_Id=?");
          statement.setInt(1, objId);
          statement.execute();
          DbUtils.close(statement);

          items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(objId, ItemInstance.ItemLocation.PAPERDOLL);
          for (ItemInstance item : items)
          {
            item.setEquipped(false);
            item.setLocData(0);
            item.setLocation(ItemInstance.ItemLocation.WAREHOUSE);
            item.setJdbcState(JdbcEntityState.UPDATED);
            item.update();
          }
        }

        statement = con.prepareStatement("DELETE FROM character_variables WHERE obj_id=? AND type='user-var' AND name='reflection'");
        statement.setInt(1, objId);
        statement.execute();
        DbUtils.close(statement);

        sendMessage(new CustomMessage("voicedcommandhandlers.Repair.RepairDone", activeChar, new Object[0]), activeChar);
        items = 1;
        return items;
      }
      catch (Exception e)
      {
        _log.error("", e);
        Collection items = 0;
        return items; } finally { DbUtils.closeQuietly(con, statement, rs);
      }
    }

    activeChar.sendMessage(".repair <name>");

    return false;
  }
}