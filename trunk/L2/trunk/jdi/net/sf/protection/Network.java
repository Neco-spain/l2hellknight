package net.sf.protection;

import java.net.InetAddress;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOConnection;

public class Network
{
  private static final Logger _log = Logger.getLogger(Network.class.getName());

  public static boolean checkIfHostIsLoggedOn(L2PcInstance player)
  {
    boolean loggedIn = false;

    for (L2PcInstance loggedPlayersHosts : L2World.getInstance().getAllPlayers())
    {
      String clientsLoggedOn = getHostsLoggedOn(loggedPlayersHosts);
      String newClient = getHostName(player);
      if (clientsLoggedOn.equalsIgnoreCase(newClient));
      loggedIn = true;
    }

    return loggedIn;
  }

  public static void disconnectHost(L2PcInstance player)
  {
    player.sendMessage("Administrator doesn't allows Same ip 2 Times.");
    player.sendMessage("You will be kicked Soon, Admin informed!");

    GmListTable.broadcastMessageToGMs("client " + player.getName() + " has been kicked due duplicate host!");
    try
    {
      Thread.sleep(10000L);
    }
    catch (Throwable t)
    {
      _log.severe("Error, NetworkService.disconnectHost, reason: " + t.getMessage());
    }
    try
    {
      _log.warning("kicked client: " + player.getName() + " due to duplicate Host.");
      _log.warning("Host: " + getHostName(player));
      player.closeNetConnection(true);
    }
    catch (Throwable t)
    {
      _log.severe("Error, NetworkService.disconnectHost, reason: " + t.getMessage());
    }
  }

  private static String getHostName(L2PcInstance player)
  {
    try
    {
      return player.getClient().getConnection().getSocket().getInetAddress().getHostName();
    }
    catch (Throwable t)
    {
      _log.severe("Error, Network Service.getHostName " + t.getMessage());
    }return null;
  }

  private static String getHostsLoggedOn(L2PcInstance player)
  {
    return getHostName(player).toString();
  }

  public static void handlePacketProtection(L2GameClient client)
  {
    if (client.getActiveChar() == null) {
      return;
    }
    if (client.isSendingTooManyUnknownPackets())
      punish(client);
  }

  public static void punish(L2GameClient client)
  {
    if (client.getActiveChar() == null) {
      return;
    }
    switch (Config.UNKNOWN_PACKETS_PUNISHMENT)
    {
    case 1:
      GmListTable.broadcastMessageToGMs("Player " + client.getActiveChar().toString() + " is flooding with unknown packets.");
      break;
    case 2:
      _log.warning("PacketProtection: " + client.toString() + " got kicked due flooding of unknown packets");
      GmListTable.broadcastMessageToGMs("Player " + client.getActiveChar().toString() + " flooding unknown packets and got kicked.");
      client.getActiveChar().sendMessage("You are will be kicked for unknown packet flooding, GM informed.");
      client.getActiveChar().closeNetConnection(false);
      break;
    case 3:
      _log.warning("PacketProtection: " + client.toString() + " got banned due flooding of unknown packets");
      LoginServerThread.getInstance().sendAccessLevel(client.getAccountName(), -100);
      GmListTable.broadcastMessageToGMs("Player " + client.getActiveChar().toString() + " flooding unknown packets and got banned.");
      client.getActiveChar().sendMessage("You are banned for unknown packet flooding, GM informed.");
      client.getActiveChar().closeNetConnection(false);
    }
  }
}