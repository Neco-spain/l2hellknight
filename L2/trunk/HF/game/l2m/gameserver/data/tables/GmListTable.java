package l2m.gameserver.data.tables;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.SystemMessage;

public class GmListTable
{
  public static List<Player> getAllGMs()
  {
    List gmList = new ArrayList();
    for (Player player : GameObjectsStorage.getAllPlayersForIterate()) {
      if (player.isGM())
        gmList.add(player);
    }
    return gmList;
  }

  public static List<Player> getAllVisibleGMs()
  {
    List gmList = new ArrayList();
    for (Player player : GameObjectsStorage.getAllPlayersForIterate()) {
      if ((player.isGM()) && (!player.isInvisible()))
        gmList.add(player);
    }
    return gmList;
  }

  public static void sendListToPlayer(Player player)
  {
    List gmList = getAllVisibleGMs();
    if (gmList.isEmpty())
    {
      player.sendPacket(Msg.THERE_ARE_NOT_ANY_GMS_THAT_ARE_PROVIDING_CUSTOMER_SERVICE_CURRENTLY);
      return;
    }

    player.sendPacket(Msg._GM_LIST_);
    for (Player gm : gmList)
      player.sendPacket(new SystemMessage(704).addString(gm.getName()));
  }

  public static void broadcastToGMs(L2GameServerPacket packet)
  {
    for (Player gm : getAllGMs())
      gm.sendPacket(packet);
  }

  public static void broadcastMessageToGMs(String message)
  {
    for (Player gm : getAllGMs())
      gm.sendMessage(message);
  }
}