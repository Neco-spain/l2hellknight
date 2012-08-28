package l2m.loginserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import l2m.loginserver.database.L2DatabaseFactory;

public class GameServerRegister
{
  public static void main(String[] params)
    throws Throwable
  {
    Config.load();
    while (true)
    {
      System.out.println();
      System.out.println(" ====================================== ");
      System.out.println("|  1. Show registered GameServer       |");
      System.out.println("|  2. Show all names GameServers       |");
      System.out.println("|  3. To add a new GameServer\t       |");
      System.out.println("|  4. Exit                             |");
      System.out.println(" ====================================== ");
      System.out.print("   *. Enter: ");
      try
      {
        InputStreamReader stream = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(stream);
        PreparedStatement stateman = null;
        switch (Integer.parseInt(reader.readLine()))
        {
        case 1:
          System.out.println(" ====================================== ");
          System.out.println("|        Registered GameServer         |");
          System.out.println(" ====================================== ");
          System.out.println("       ID                  Host         ");
          stateman = L2DatabaseFactory.getInstance().getConnection().prepareStatement("SELECT * FROM gameservers");
          ResultSet rs = stateman.executeQuery();
          while (rs.next())
            System.out.println("       " + rs.getInt("server_id") + "                " + rs.getString("host"));
          rs.close();
          stateman.close();
          System.out.println(" ====================================== ");
          break;
        case 2:
          System.out.println(" ====================================== ");
          System.out.println("|        All names GameServers         |");
          System.out.println(" ====================================== ");
          System.out.println("|______ID__________________Name________|");
          for (Map.Entry entry : Config.SERVER_NAMES.entrySet())
            System.out.println("       " + entry.getKey() + "               " + (String)entry.getValue() + "        ");
          System.out.println(" ====================================== ");
          break;
        case 3:
          System.out.println(" ====================================== ");
          System.out.println("|          Register GameServer         |");
          System.out.println(" ====================================== ");
          System.out.print("| Enter GameServer id: ");
          InputStreamReader stream2 = new InputStreamReader(System.in);
          BufferedReader reader2 = new BufferedReader(stream2);
          int id = Integer.parseInt(reader2.readLine());
          System.out.print("| Enter GameServer ip: ");
          stream2 = new InputStreamReader(System.in);
          reader2 = new BufferedReader(stream2);
          String ip = reader2.readLine();
          stateman = L2DatabaseFactory.getInstance().getConnection().prepareStatement("REPLACE INTO gameservers VALUES (?, ?)");
          stateman.setInt(1, id);
          stateman.setString(2, ip);
          stateman.execute();
          stateman.close();
          System.out.println("|         GameServer registered");
          System.out.println(" ====================================== ");
          break;
        case 4:
          return;
        }
      }
      catch (Exception e) {
      }
    }
  }

  public static boolean isWindows() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.indexOf("win") >= 0;
  }
}