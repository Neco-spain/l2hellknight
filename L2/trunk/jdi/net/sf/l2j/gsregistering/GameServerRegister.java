package net.sf.l2j.gsregistering;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.loginserver.GameServerTable;

public class GameServerRegister
{
  private static String _choice;
  private static boolean _choiceOk;

  public static void main(String[] args)
    throws IOException
  {
    net.sf.l2j.Server.serverMode = 2;

    Config.load();

    LineNumberReader _in = new LineNumberReader(new InputStreamReader(System.in));
    try
    {
      GameServerTable.load();
    }
    catch (Exception e)
    {
      System.out.println(new StringBuilder().append("FATAL: Failed loading GameServerTable. Reason: ").append(e.getMessage()).toString());
      e.printStackTrace();
      System.exit(1);
    }
    GameServerTable gameServerTable = GameServerTable.getInstance();
    System.out.println("***** Welcome to Eon Game Server Regitering *****");
    System.out.println("Enter The id of the server you want to register");
    System.out.println("Type 'help' to get a list of ids.");
    System.out.println("Type 'clean' to unregister all currently registered gameservers on this LoginServer.");
    while (!_choiceOk)
    {
      System.out.println("Your choice:");
      _choice = _in.readLine();
      if (_choice.equalsIgnoreCase("help"))
      {
        for (Map.Entry entry : gameServerTable.getServerNames().entrySet())
        {
          System.out.println(new StringBuilder().append("Server: ID: ").append(entry.getKey()).append("\t- ").append((String)entry.getValue()).append(" - In Use: ").append(gameServerTable.hasRegisteredGameServerOnId(((Integer)entry.getKey()).intValue()) ? "YES" : "NO").toString());
        }
        System.out.println("You can also see servername.xml"); continue;
      }
      if (_choice.equalsIgnoreCase("clean"))
      {
        System.out.print("This is going to UNREGISTER ALL servers from this LoginServer. Are you sure? (y/n) ");
        _choice = _in.readLine();
        if (_choice.equals("y"))
        {
          cleanRegisteredGameServersFromDB();
          gameServerTable.getRegisteredGameServers().clear(); continue;
        }

        System.out.println("ABORTED"); continue;
      }

      try
      {
        int id = new Integer(_choice).intValue();
        int size = gameServerTable.getServerNames().size();

        if (size == 0)
        {
          System.out.println("No server names avalible, please make sure that servername.xml is in the LoginServer directory.");
          System.exit(1);
        }

        String name = gameServerTable.getServerNameById(id);
        if (name == null)
        {
          System.out.println(new StringBuilder().append("No name for id: ").append(id).toString());
          continue;
        }

        if (gameServerTable.hasRegisteredGameServerOnId(id))
        {
          System.out.println("This id is not free");
        }
        else
        {
          byte[] hexId = LoginServerThread.generateHex(16);
          gameServerTable.registerServerOnDB(hexId, id, "");
          Config.saveHexid(id, new BigInteger(hexId).toString(16), new StringBuilder().append("hexid(server ").append(id).append(").txt").toString());
          System.out.println(new StringBuilder().append("Server Registered hexid saved to 'hexid(server ").append(id).append(").txt'").toString());
          System.out.println("Put this file in the /config folder of your gameserver and rename it to 'hexid.txt'");
          return;
        }

      }
      catch (NumberFormatException nfe)
      {
        System.out.println("Please, type a number or 'help'");
      }
    }
  }

  public static void cleanRegisteredGameServersFromDB()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM gameservers");
      statement.executeUpdate();
      statement.close();
    }
    catch (SQLException e)
    {
      System.out.println(new StringBuilder().append("SQL error while cleaning registered servers: ").append(e).toString());
    }
    finally {
      try {
        statement.close(); } catch (Exception e) {
      }try { con.close();
      }
      catch (Exception e)
      {
      }
    }
  }
}