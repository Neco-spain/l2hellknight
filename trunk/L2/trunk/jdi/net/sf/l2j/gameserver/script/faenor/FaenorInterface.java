package net.sf.l2j.gameserver.script.faenor;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.datatables.EventDroplist;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2DropCategory;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2PetData;
import net.sf.l2j.gameserver.script.DateRange;
import net.sf.l2j.gameserver.script.EngineInterface;
import net.sf.l2j.gameserver.script.Expression;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class FaenorInterface
  implements EngineInterface
{
  protected static final Logger _log = Logger.getLogger(FaenorInterface.class.getName());
  private static FaenorInterface _instance;

  public static FaenorInterface getInstance()
  {
    if (_instance == null)
    {
      _instance = new FaenorInterface();
    }
    return _instance;
  }

  public List<?> getAllPlayers()
  {
    return null;
  }

  public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states)
  {
    L2NpcTemplate npc = npcTable.getTemplate(npcID);
    if (npc == null)
    {
      throw new NullPointerException();
    }
    L2DropData drop = new L2DropData();
    drop.setItemId(itemID);
    drop.setMinDrop(min);
    drop.setMaxDrop(max);
    drop.setChance(chance);
    drop.setQuestID(questID);
    drop.addStates(states);
    addDrop(npc, drop, false);
  }

  public void addDrop(int npcID, int itemID, int min, int max, boolean sweep, int chance)
    throws NullPointerException
  {
    L2NpcTemplate npc = npcTable.getTemplate(npcID);
    if (npc == null)
    {
      if (Config.DEBUG)
        _log.warning("Npc doesnt Exist");
      throw new NullPointerException();
    }
    L2DropData drop = new L2DropData();
    drop.setItemId(itemID);
    drop.setMinDrop(min);
    drop.setMaxDrop(max);
    drop.setChance(chance);

    addDrop(npc, drop, sweep);
  }

  public void addDrop(L2NpcTemplate npc, L2DropData drop, boolean sweep)
  {
    if (sweep) {
      addDrop(npc, drop, -1);
    }
    else {
      int maxCategory = -1;

      if (npc.getDropData() != null)
        for (L2DropCategory cat : npc.getDropData())
        {
          if (maxCategory < cat.getCategoryType())
            maxCategory = cat.getCategoryType();
        }
      maxCategory++;
      npc.addDropData(drop, maxCategory);
    }
  }

  public void addDrop(L2NpcTemplate npc, L2DropData drop, int category)
  {
    npc.addDropData(drop, category);
  }

  public List<L2DropData> getQuestDrops(int npcID)
  {
    L2NpcTemplate npc = npcTable.getTemplate(npcID);
    if (npc == null)
    {
      return null;
    }
    List questDrops = new FastList();
    if (npc.getDropData() != null)
      for (L2DropCategory cat : npc.getDropData())
        for (L2DropData drop : cat.getAllDrops())
        {
          if (drop.getQuestID() != null)
          {
            questDrops.add(drop);
          }
        }
    return questDrops;
  }

  public void addEventDrop(int[] items, int[] count, double chance, DateRange range)
  {
    EventDroplist.getInstance().addGlobalDrop(items, count, (int)(chance * 1000000.0D), range);
  }

  public void onPlayerLogin(String[] message, DateRange validDateRange)
  {
    Announcements.getInstance().addEventAnnouncement(validDateRange, message);
  }

  public void addPetData(ScriptContext context, int petID, int levelStart, int levelEnd, Map<String, String> stats)
    throws ScriptException
  {
    L2PetData[] petData = new L2PetData[levelEnd - levelStart + 1];
    int value = 0;
    for (int level = levelStart; level <= levelEnd; level++)
    {
      petData[(level - 1)] = new L2PetData();
      petData[(level - 1)].setPetID(petID);
      petData[(level - 1)].setPetLevel(level);

      context.setAttribute("level", new Double(level), 100);
      for (String stat : stats.keySet())
      {
        value = ((Number)Expression.eval(context, "beanshell", (String)stats.get(stat))).intValue();
        petData[(level - 1)].setStat(stat, value);
      }
      context.removeAttribute("level", 100);
    }
  }
}