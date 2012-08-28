package net.sf.l2j.status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.Manager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.mysql.Connect;

public class GameStatusThread extends Thread
{
  private Socket _cSocket;
  private PrintWriter _print;
  private BufferedReader _read;
  private int _uptime;

  private void telnetOutput(int type, String text)
  {
    if (Config.DEVELOPER)
    {
      if (type == 1) System.out.println(new StringBuilder().append("TELNET | ").append(text).toString());
      else if (type == 2) System.out.print(new StringBuilder().append("TELNET | ").append(text).toString());
      else if (type == 3) System.out.print(text);
      else if (type == 4) System.out.println(text); else {
        System.out.println(new StringBuilder().append("TELNET | ").append(text).toString());
      }

    }
    else if (type == 5) System.out.println(new StringBuilder().append("TELNET | ").append(text).toString());
  }

  private boolean isValidIP(Socket client)
  {
    boolean result = false;
    InetAddress ClientIP = client.getInetAddress();

    String clientStringIP = ClientIP.getHostAddress();

    telnetOutput(1, new StringBuilder().append("Connection from: ").append(clientStringIP).toString());

    if (Config.DEVELOPER) {
      telnetOutput(2, "");
    }
    InputStream is = null;
    try
    {
      Properties telnetSettings = new Properties();
      is = new FileInputStream(new File("./config/telnet.cfg"));
      telnetSettings.load(is);
      is.close();

      String HostList = telnetSettings.getProperty("ListOfHosts", "127.0.0.1,localhost");

      if (Config.DEVELOPER) telnetOutput(3, "Comparing ip to list...");

      String ipToCompare = null;
      for (String ip : HostList.split(",")) {
        if (!result) {
          ipToCompare = InetAddress.getByName(ip).getHostAddress();
          if (clientStringIP.equals(ipToCompare)) result = true;
          if (!Config.DEVELOPER) continue; telnetOutput(3, new StringBuilder().append(clientStringIP).append(" = ").append(ipToCompare).append("(").append(ip).append(") = ").append(result).toString());
        }
      }
    }
    catch (IOException e1)
    {
      if (Config.DEVELOPER)
        telnetOutput(4, "");
      telnetOutput(1, new StringBuilder().append("Error: ").append(e).toString());
    }
    finally {
      try {
        is.close(); } catch (Exception e1) {
      }
    }
    if (Config.DEVELOPER) telnetOutput(4, new StringBuilder().append("Allow IP: ").append(result).toString());
    return result;
  }

  public GameStatusThread(Socket client, int uptime, String StatusPW) throws IOException
  {
    _cSocket = client;
    _uptime = uptime;

    _print = new PrintWriter(_cSocket.getOutputStream());
    _read = new BufferedReader(new InputStreamReader(_cSocket.getInputStream()));

    if (isValidIP(client)) {
      telnetOutput(1, new StringBuilder().append(client.getInetAddress().getHostAddress()).append(" accepted.").toString());
      _print.println("Welcome To Fatal-World Telnet Session.");
      _print.println("Please Insert Your Password!");
      _print.print("Password: ");
      _print.flush();
      String tmpLine = _read.readLine();
      if (tmpLine == null) {
        _print.println("Error.");
        _print.println("Disconnected...");
        _print.flush();
        _cSocket.close();
      }
      else if (tmpLine.compareTo(StatusPW) != 0)
      {
        _print.println("Incorrect Password!");
        _print.println("Disconnected...");
        _print.flush();
        _cSocket.close();
      }
      else
      {
        _print.println("Password Correct!");
        _print.println("[L2J]");
        _print.print("");
        _print.flush();
        start();
      }
    }
    else
    {
      telnetOutput(5, new StringBuilder().append("Connection attempt from ").append(client.getInetAddress().getHostAddress()).append(" rejected.").toString());
      _cSocket.close();
    }
  }

  public void run()
  {
    String _usrCommand = "";
    try
    {
      while ((_usrCommand.compareTo("quit") != 0) && (_usrCommand.compareTo("exit") != 0))
      {
        _usrCommand = _read.readLine();
        if (_usrCommand == null)
        {
          _cSocket.close();
          break;
        }
        if (_usrCommand.equals("help")) {
          _print.println("The following is a list of all available commands: ");
          _print.println("help                - shows this help.");
          _print.println("status              - displays basic server statistics.");
          _print.println("performance         - shows server performance statistics.");
          _print.println("purge               - removes finished threads from thread pools.");
          _print.println("announce <text>     - announces <text> in game.");
          _print.println("msg <nick> <text>   - Sends a whisper to char <nick> with <text>.");
          _print.println("gmchat <text>       - Sends a message to all GMs with <text>.");
          _print.println("gmlist              - lists all gms online.");
          _print.println("kick                - kick player <name> from server.");
          _print.println("shutdown <time>     - shuts down server in <time> seconds.");
          _print.println("restart <time>      - restarts down server in <time> seconds.");
          _print.println("abort               - aborts shutdown/restart.");
          _print.println("give <player> <itemid> <amount>");
          _print.println("extlist             - list all loaded extension classes");
          _print.println("extreload <name>    - reload and initializes the named extension or all if used without argument");
          _print.println("extinit <name>      - initilizes the named extension or all if used without argument");
          _print.println("extunload <name>    - unload the named extension or all if used without argument");
          _print.println("debug <cmd>         - executes the debug command (see 'help debug').");
          _print.println("jail <player> [time]");
          _print.println("unjail <player>");
          _print.println("quit                - closes telnet session.");
        }
        else if (_usrCommand.equals("help debug"))
        {
          _print.println("The following is a list of all available debug commands: ");
          _print.println("decay               - prints info about the DecayManager");
          _print.println("PacketTP            - prints info about the General Packet ThreadPool");
          _print.println("IOPacketTP          - prints info about the I/O Packet ThreadPool");
          _print.println("GeneralTP           - prints info about the General ThreadPool");
        }
        else if (_usrCommand.equals("performance"))
        {
          for (String line : ThreadPoolManager.getInstance().getStats())
          {
            _print.println(line);
          }
          _print.flush();
        }
        else if (_usrCommand.equals("purge"))
        {
          ThreadPoolManager.getInstance().purge();
          _print.println("STATUS OF THREAD POOLS AFTER PURGE COMMAND:");
          _print.println("");
          for (String line : ThreadPoolManager.getInstance().getStats())
          {
            _print.println(line);
          }
          _print.flush();
        }
        else if (_usrCommand.startsWith("announce"))
        {
          try
          {
            _usrCommand = _usrCommand.substring(9);
            Announcements.getInstance().announceToAll(_usrCommand);
            _print.println("Announcement Sent!");
          }
          catch (StringIndexOutOfBoundsException e)
          {
            _print.println("Please Enter Some Text To Announce!");
          }
        }
        else if (_usrCommand.startsWith("gmchat"))
        {
          try
          {
            _usrCommand = _usrCommand.substring(7);
            CreatureSay cs = new CreatureSay(0, 9, new StringBuilder().append("Telnet GM Broadcast from ").append(_cSocket.getInetAddress().getHostAddress()).toString(), _usrCommand);
            GmListTable.broadcastToGMs(cs);
            _print.println(new StringBuilder().append("Your Message Has Been Sent To ").append(getOnlineGMS()).append(" GM(s).").toString());
          }
          catch (StringIndexOutOfBoundsException e)
          {
            _print.println("Please Enter Some Text To Announce!");
          }

        }
        else if (_usrCommand.startsWith("kick"))
        {
          try
          {
            _usrCommand = _usrCommand.substring(5);
            L2PcInstance player = L2World.getInstance().getPlayer(_usrCommand);
            if (player != null)
            {
              player.sendMessage("You are kicked by gm");
              player.logout();
              _print.println("Player kicked");
            }
          }
          catch (StringIndexOutOfBoundsException e)
          {
            _print.println("Please enter player name to kick");
          }
        }
        else if (_usrCommand.startsWith("shutdown"))
        {
          try
          {
            int val = Integer.parseInt(_usrCommand.substring(9));
            Shutdown.getInstance().startTelnetShutdown(_cSocket.getInetAddress().getHostAddress(), val, false);
            _print.println(new StringBuilder().append("Server Will Shutdown In ").append(val).append(" Seconds!").toString());
            _print.println("Type \"abort\" To Abort Shutdown!");
          }
          catch (StringIndexOutOfBoundsException e)
          {
            _print.println("Please Enter * amount of seconds to shutdown!");
          }
          catch (Exception NumberFormatException) {
            _print.println("Numbers Only!");
          }
        }
        else if (_usrCommand.startsWith("restart"))
        {
          try
          {
            int val = Integer.parseInt(_usrCommand.substring(8));
            Shutdown.getInstance().startTelnetShutdown(_cSocket.getInetAddress().getHostAddress(), val, true);
            _print.println(new StringBuilder().append("Server Will Restart In ").append(val).append(" Seconds!").toString());
            _print.println("Type \"abort\" To Abort Restart!");
          }
          catch (StringIndexOutOfBoundsException e)
          {
            _print.println("Please Enter * amount of seconds to restart!");
          }
          catch (Exception NumberFormatException) {
            _print.println("Numbers Only!");
          }
        }
        else if (_usrCommand.startsWith("abort"))
        {
          Shutdown.getInstance().telnetAbort(_cSocket.getInetAddress().getHostAddress());
          _print.println("OK! - Shutdown/Restart Aborted.");
        }
        else if (!_usrCommand.equals("quit")) {
          if (_usrCommand.startsWith("give"))
          {
            try
            {
              String[] opaopa = _usrCommand.split(" ");

              String name = opaopa[1];
              int item = Integer.parseInt(opaopa[2]);
              int count = Integer.parseInt(opaopa[3]);

              L2PcInstance player = L2World.getInstance().getPlayer(name);
              if (player != null)
              {
                L2ItemInstance prize = player.getInventory().addItem("Telnet", item, count, player, null);
                InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(prize);
                player.sendPacket(iu);

                SystemMessage sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S);
                sm.addItemName(prize.getItemId());
                sm.addNumber(count);
                player.sendPacket(sm);
                _print.println("ok");
              }
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
          }
          else if (_usrCommand.startsWith("msg"))
          {
            try
            {
              String val = _usrCommand.substring(4);
              StringTokenizer st = new StringTokenizer(val);
              String name = st.nextToken();
              String message = val.substring(name.length() + 1);
              L2PcInstance reciever = L2World.getInstance().getPlayer(name);
              CreatureSay cs = new CreatureSay(0, 2, "SYS", message);
              if (reciever != null)
              {
                reciever.sendPacket(cs);
                _print.println(new StringBuilder().append("Telnet Priv->").append(name).append(": ").append(message).toString());
                _print.println("Message Sent!");
              }
              else
              {
                _print.println(new StringBuilder().append("Unable To Find Username: ").append(name).toString());
              }
            }
            catch (StringIndexOutOfBoundsException e)
            {
              _print.println("Please Enter Some Text!");
            }
          }
          else if (_usrCommand.startsWith("jail"))
          {
            StringTokenizer st = new StringTokenizer(_usrCommand.substring(5));
            try
            {
              L2PcInstance playerObj = L2World.getInstance().getPlayer(st.nextToken());
              int delay = 0;
              try
              {
                delay = Integer.parseInt(st.nextToken());
              } catch (NumberFormatException nfe) {
              }
              catch (NoSuchElementException nsee) {
              }
              if (playerObj != null)
              {
                playerObj.setInJail(true, delay);
                _print.println(new StringBuilder().append("Character ").append(playerObj.getName()).append(" jailed for ").append(delay > 0 ? new StringBuilder().append(delay).append(" minutes.").toString() : "ever!").toString());
              } else {
                jailOfflinePlayer(playerObj.getName(), delay);
              }
            } catch (NoSuchElementException nsee) {
              _print.println("Specify a character name.");
            }
            catch (Exception e) {
              if (Config.DEBUG) e.printStackTrace();
            }
          }
          else if (_usrCommand.startsWith("unjail"))
          {
            StringTokenizer st = new StringTokenizer(_usrCommand.substring(7));
            try
            {
              L2PcInstance playerObj = L2World.getInstance().getPlayer(st.nextToken());

              if (playerObj != null)
              {
                playerObj.stopJailTask(false);
                playerObj.setInJail(false, 0);
                _print.println(new StringBuilder().append("Character ").append(playerObj.getName()).append(" removed from jail").toString());
              } else {
                unjailOfflinePlayer(playerObj.getName());
              }
            } catch (NoSuchElementException nsee) {
              _print.println("Specify a character name.");
            }
            catch (Exception e) {
              if (Config.DEBUG) e.printStackTrace();
            }
          }
          else if ((_usrCommand.startsWith("debug")) && (_usrCommand.length() > 6))
          {
            StringTokenizer st = new StringTokenizer(_usrCommand.substring(6));
            try
            {
              String dbg = st.nextToken();

              if (dbg.equals("decay"))
              {
                _print.print(DecayTaskManager.getInstance().toString());
              }
              else if (!dbg.equals("ai"))
              {
                if (!dbg.equals("aiflush"));
              }

            }
            catch (Exception e)
            {
            }

          }
          else if (_usrCommand.startsWith("reload"))
          {
            StringTokenizer st = new StringTokenizer(_usrCommand.substring(7));
            try
            {
              String type = st.nextToken();

              if (type.equals("multisell"))
              {
                _print.print("Reloading multisell... ");
                L2Multisell.getInstance().reload();
                _print.print("done\n");
              }
              else if (type.equals("skill"))
              {
                _print.print("Reloading skills... ");
                SkillTable.getInstance().reload();
                _print.print("done\n");
              }
              else if (type.equals("npc"))
              {
                _print.print("Reloading npc templates... ");
                NpcTable.getInstance().reloadAllNpc();
                _print.print("done\n");
              }
              else if (type.equals("html"))
              {
                _print.print("Reloading html cache... ");
                HtmCache.getInstance().reload();
                _print.print("done\n");
              }
              else if (type.equals("item"))
              {
                _print.print("Reloading item templates... ");
                ItemTable.getInstance().reload();
                _print.print("done\n");
              }
              else if (type.equals("instancemanager"))
              {
                _print.print("Reloading instance managers... ");
                Manager.reloadAll();
                _print.print("done\n");
              }
              else if (type.equals("zone"))
              {
                _print.print("Reloading zone tables... ");

                _print.print("done\n");
              }
            }
            catch (Exception e) {
            }
          }
          else if (_usrCommand.startsWith("gamestat"))
          {
            StringTokenizer st = new StringTokenizer(_usrCommand.substring(9));
            try
            {
              String type = st.nextToken();

              if (type.equals("privatestore"))
              {
                for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                {
                  if (player.getPrivateStoreType() == 0) {
                    continue;
                  }
                  TradeList list = null;
                  String content = "";

                  if (player.getPrivateStoreType() == 1)
                  {
                    list = player.getSellList();
                    for (TradeList.TradeItem item : list.getItems())
                    {
                      content = new StringBuilder().append(content).append(item.getItem().getItemId()).append(":").append(item.getEnchant()).append(":").append(item.getPrice()).append(":").toString();
                    }
                    content = new StringBuilder().append(player.getName()).append(";").append("sell;").append(player.getX()).append(";").append(player.getY()).append(";").append(content).toString();
                    _print.println(content);
                    continue;
                  }
                  if (player.getPrivateStoreType() == 3)
                  {
                    list = player.getBuyList();
                    for (TradeList.TradeItem item : list.getItems())
                    {
                      content = new StringBuilder().append(content).append(item.getItem().getItemId()).append(":").append(item.getEnchant()).append(":").append(item.getPrice()).append(":").toString();
                    }
                    content = new StringBuilder().append(player.getName()).append(";").append("buy;").append(player.getX()).append(";").append(player.getY()).append(";").append(content).toString();
                    _print.println(content);
                    continue;
                  }
                }
              }
            }
            catch (Exception e) {
            }
          }
        }
        _print.print("");
        _print.flush();
      }
      if (!_cSocket.isClosed())
      {
        _print.println("Bye Bye!");
        _print.flush();
        _cSocket.close();
      }
      telnetOutput(1, new StringBuilder().append("Connection from ").append(_cSocket.getInetAddress().getHostAddress()).append(" was closed by client.").toString());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void jailOfflinePlayer(String name, int delay)
  {
    Connect con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
      statement.setInt(1, -114356);
      statement.setInt(2, -249645);
      statement.setInt(3, -2984);
      statement.setInt(4, 1);
      statement.setLong(5, delay * 60000L);
      statement.setString(6, name);

      statement.execute();
      int count = statement.getUpdateCount();
      statement.close();

      if (count == 0)
        _print.println("Character not found!");
      else
        _print.println(new StringBuilder().append("Character ").append(name).append(" jailed for ").append(delay > 0 ? new StringBuilder().append(delay).append(" minutes.").toString() : "ever!").toString());
    }
    catch (SQLException e) {
      _print.println("SQLException while jailing player");
      if (Config.DEBUG) se.printStackTrace(); 
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private void unjailOfflinePlayer(String name) {
    Connect con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
      statement.setInt(1, 17836);
      statement.setInt(2, 170178);
      statement.setInt(3, -3507);
      statement.setInt(4, 0);
      statement.setLong(5, 0L);
      statement.setString(6, name);

      statement.execute();
      int count = statement.getUpdateCount();
      statement.close();

      if (count == 0)
        _print.println("Character not found!");
      else
        _print.println(new StringBuilder().append("Character ").append(name).append(" set free.").toString());
    }
    catch (SQLException e) {
      _print.println("SQLException while jailing player");
      if (Config.DEBUG) se.printStackTrace(); 
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  private int getOnlineGMS() {
    return GmListTable.getInstance().getAllGms(true).size();
  }

  private String getUptime(int time)
  {
    int uptime = (int)System.currentTimeMillis() - time;
    uptime /= 1000;
    int h = uptime / 3600;
    int m = (uptime - h * 3600) / 60;
    int s = uptime - h * 3600 - m * 60;
    return new StringBuilder().append(h).append("hrs ").append(m).append("mins ").append(s).append("secs").toString();
  }

  private String gameTime()
  {
    int t = GameTimeController.getInstance().getGameTime();
    int h = t / 60;
    int m = t % 60;
    SimpleDateFormat format = new SimpleDateFormat("H:mm");
    Calendar cal = Calendar.getInstance();
    cal.set(11, h);
    cal.set(12, m);
    return format.format(cal.getTime());
  }
}