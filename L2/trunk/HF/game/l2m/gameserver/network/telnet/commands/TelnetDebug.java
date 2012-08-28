package l2m.gameserver.network.telnet.commands;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import l2m.gameserver.loginservercon.LoginServerCommunication;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.network.telnet.TelnetCommand;
import l2m.gameserver.network.telnet.TelnetCommandHolder;
import org.apache.commons.io.FileUtils;

public class TelnetDebug
  implements TelnetCommandHolder
{
  private Set<TelnetCommand> _commands = new LinkedHashSet();

  public TelnetDebug()
  {
    _commands.add(new TelnetCommand("dumpnpc", new String[] { "dnpc" })
    {
      public String getUsage()
      {
        return "dumpnpc";
      }

      public String handle(String[] args)
      {
        StringBuilder sb = new StringBuilder();

        int total = 0;
        int maxId = 0; int maxCount = 0;

        TIntObjectHashMap npcStats = new TIntObjectHashMap();

        for (GameObject obj : GameObjectsStorage.getAllObjects()) {
          if ((obj.isCreature()) && 
            (obj.isNpc()))
          {
            NpcInstance npc = (NpcInstance)obj;
            int id = npc.getNpcId();
            List list;
            if ((list = (List)npcStats.get(id)) == null) {
              npcStats.put(id, list = new ArrayList());
            }
            list.add(npc);

            if (list.size() > maxCount)
            {
              maxId = id;
              maxCount = list.size();
            }

            total++;
          }
        }
        sb.append("Total NPCs: ").append(total).append("\n");
        sb.append("Maximum NPC ID: ").append(maxId).append(" count : ").append(maxCount).append("\n");

        TIntObjectIterator itr = npcStats.iterator();

        while (itr.hasNext())
        {
          itr.advance();
          int id = itr.key();
          List list = (List)itr.value();
          sb.append("=== ID: ").append(id).append(" ").append(" Count: ").append(list.size()).append(" ===").append("\n");

          for (NpcInstance npc : list) {
            try
            {
              sb.append("AI: ");

              if (npc.hasAI())
                sb.append(npc.getAI().getClass().getName());
              else {
                sb.append("none");
              }
              sb.append(", ");

              if (npc.getReflectionId() > 0)
              {
                sb.append("ref: ").append(npc.getReflectionId());
                sb.append(" - ").append(npc.getReflection().getName());
              }

              sb.append("loc: ").append(npc.getLoc());
              sb.append(", ");
              sb.append("spawned: ");
              sb.append(npc.isVisible());
              sb.append("\n");
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
          }
        }
        try
        {
          new File("stats").mkdir();
          FileUtils.writeStringToFile(new File(new StringBuilder().append("stats/NpcStats-").append(new SimpleDateFormat("MMddHHmmss").format(Long.valueOf(System.currentTimeMillis()))).append(".txt").toString()), sb.toString());
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }

        return "NPC stats saved.\n";
      }
    });
    _commands.add(new TelnetCommand("asrestart")
    {
      public String getUsage()
      {
        return "asrestart";
      }

      public String handle(String[] args)
      {
        LoginServerCommunication.getInstance().restart();

        return "Restarted.\n";
      }
    });
  }

  public Set<TelnetCommand> getCommands()
  {
    return _commands;
  }
}