package net.sf.l2j.gameserver.model.entity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.EventData;

public class L2Event
{
  public static String eventName = "";
  public static int teamsNumber = 0;
  public static final HashMap<Integer, String> names = new HashMap();
  public static final LinkedList<String> participatingPlayers = new LinkedList();
  public static final HashMap<Integer, LinkedList<String>> players = new HashMap();
  public static int id = 12760;
  public static final LinkedList<String> npcs = new LinkedList();
  public static boolean active = false;
  public static final HashMap<String, EventData> connectionLossData = new HashMap();

  public static int getTeamOfPlayer(String name)
  {
    for (int i = 1; i <= players.size(); i++)
    {
      LinkedList temp = (LinkedList)players.get(Integer.valueOf(i));
      Iterator it = temp.iterator();
      while (it.hasNext())
      {
        if (it.next().equals(name)) return i;
      }
    }
    return 0;
  }

  public static String[] getTopNKillers(int N)
  {
    String[] killers = new String[N];
    String playerTemp = "";
    int kills = 0;
    LinkedList killersTemp = new LinkedList();

    for (int k = 0; k < N; k++)
    {
      kills = 0;
      for (int i = 1; i <= teamsNumber; i++)
      {
        LinkedList temp = (LinkedList)players.get(Integer.valueOf(i));
        Iterator it = temp.iterator();
        while (it.hasNext())
        {
          try
          {
            L2PcInstance player = L2World.getInstance().getPlayer((String)it.next());
            if (!killersTemp.contains(player.getName()))
            {
              if (player.kills.size() > kills)
              {
                kills = player.kills.size();
                playerTemp = player.getName();
              }
            }
          }
          catch (Exception e)
          {
          }
        }
      }
      killersTemp.add(playerTemp);
    }

    for (int i = 0; i < N; i++)
    {
      kills = 0;
      Iterator it = killersTemp.iterator();
      while (it.hasNext())
      {
        try
        {
          L2PcInstance player = L2World.getInstance().getPlayer((String)it.next());
          if (player.kills.size() > kills)
          {
            kills = player.kills.size();
            playerTemp = player.getName();
          }
        }
        catch (Exception e)
        {
        }
      }
      killers[i] = playerTemp;
      killersTemp.remove(playerTemp);
    }
    return killers;
  }

  public static void showEventHtml(L2PcInstance player, String objectid)
  {
    try
    {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

      DataInputStream in = null;
      BufferedReader inbr = null;
      try
      {
        in = new DataInputStream(new BufferedInputStream(new FileInputStream("data/events/" + eventName)));
        inbr = new BufferedReader(new InputStreamReader(in));
        TextBuilder replyMSG = new TextBuilder("<html><body>");
        replyMSG.append("<center><font color=\"LEVEL\">" + eventName + "</font><font color=\"FF0000\"> bY " + inbr.readLine() + "</font></center><br>");

        replyMSG.append("<br>" + inbr.readLine());
        if (participatingPlayers.contains(player.getName())) replyMSG.append("<br><center>You are already in the event players list !!</center></body></html>"); else {
          replyMSG.append("<br><center><button value=\"Participate !! \" action=\"bypass -h npc_" + objectid + "_event_participate\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
        }
        adminReply.setHtml(replyMSG.toString());
      }
      catch (Exception e)
      {
      }
      finally
      {
        in.close();
        inbr.close();
      }
      player.sendPacket(adminReply);
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
  }

  public static void spawn(L2PcInstance target, int npcid)
  {
    L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(npcid);
    try
    {
      L2Spawn spawn = new L2Spawn(template1);

      spawn.setLocx(target.getX() + 50);
      spawn.setLocy(target.getY() + 50);
      spawn.setLocz(target.getZ());
      spawn.setAmount(1);
      spawn.setHeading(target.getHeading());
      spawn.setRespawnDelay(1);

      SpawnTable.getInstance().addNewSpawn(spawn, false);

      spawn.init();
      spawn.getLastSpawn().setCurrentHp(999999999.0D);
      spawn.getLastSpawn().setName("event inscriptor");
      spawn.getLastSpawn().setTitle(eventName);
      spawn.getLastSpawn().isEventMob = true;
      spawn.getLastSpawn().isAggressive();
      spawn.getLastSpawn().decayMe();
      spawn.getLastSpawn().spawnMe(spawn.getLastSpawn().getX(), spawn.getLastSpawn().getY(), spawn.getLastSpawn().getZ());

      spawn.getLastSpawn().broadcastPacket(new MagicSkillUser(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));

      npcs.add(String.valueOf(spawn.getLastSpawn().getObjectId()));
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
  }

  public static void announceAllPlayers(String text)
  {
    CreatureSay cs = new CreatureSay(0, 10, "", text);

    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
    {
      player.sendPacket(cs);
    }
  }

  public static boolean isOnEvent(L2PcInstance player)
  {
    for (int k = 0; k < teamsNumber; k++)
    {
      Iterator it = ((LinkedList)players.get(Integer.valueOf(k + 1))).iterator();
      boolean temp = false;
      while (it.hasNext())
      {
        temp = player.getName().equalsIgnoreCase(it.next().toString());
        if (temp) return true;
      }
    }
    return false;
  }

  public static void inscribePlayer(L2PcInstance player)
  {
    try
    {
      participatingPlayers.add(player.getName());
      eventkarma = player.getKarma();
      eventX = player.getX();
      eventY = player.getY();
      eventZ = player.getZ();
      eventpkkills = player.getPkKills();
      eventpvpkills = player.getPvpKills();
      eventTitle = player.getTitle();
      kills.clear();
      atEvent = true;
    }
    catch (Exception e)
    {
      System.out.println("error when signing in the event:" + e);
    }
  }

  public static void restoreChar(L2PcInstance player)
  {
    try
    {
      eventX = ((EventData)connectionLossData.get(player.getName())).eventX;
      eventY = ((EventData)connectionLossData.get(player.getName())).eventY;
      eventZ = ((EventData)connectionLossData.get(player.getName())).eventZ;
      eventkarma = ((EventData)connectionLossData.get(player.getName())).eventKarma;
      eventpvpkills = ((EventData)connectionLossData.get(player.getName())).eventPvpKills;
      eventpkkills = ((EventData)connectionLossData.get(player.getName())).eventPkKills;
      eventTitle = ((EventData)connectionLossData.get(player.getName())).eventTitle;
      kills = ((EventData)connectionLossData.get(player.getName())).kills;
      eventSitForced = ((EventData)connectionLossData.get(player.getName())).eventSitForced;
      atEvent = true;
    }
    catch (Exception e)
    {
    }
  }

  public static void restoreAndTeleChar(L2PcInstance target)
  {
    try
    {
      restoreChar(target);
      target.setTitle(eventTitle);
      target.setKarma(eventkarma);
      target.setPvpKills(eventpvpkills);
      target.setPkKills(eventpkkills);
      target.teleToLocation(eventX, eventY, eventZ, true);
      kills.clear();
      eventSitForced = false;
      atEvent = false;
    }
    catch (Exception e)
    {
    }
  }
}