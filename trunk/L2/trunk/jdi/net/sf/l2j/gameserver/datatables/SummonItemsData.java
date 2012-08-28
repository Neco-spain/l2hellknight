package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2SummonItem;

public class SummonItemsData
{
  private FastMap<Integer, L2SummonItem> _summonitems = new FastMap();
  private static SummonItemsData _instance;

  public static SummonItemsData getInstance()
  {
    if (_instance == null) {
      _instance = new SummonItemsData();
    }
    return _instance;
  }

  public SummonItemsData()
  {
    Scanner s;
    try
    {
      s = new Scanner(new File("./data/csv/summon_items.csv"));
    }
    catch (Exception e)
    {
      System.out.println("Summon items data: Can not find './data/summon_items.csv'");
      return;
    }

    int lineCount = 0;
    int commentLinesCount = 0;

    while (s.hasNextLine())
    {
      lineCount++;

      String line = s.nextLine();

      if (line.startsWith("#"))
      {
        commentLinesCount++;
        continue;
      }
      if (line.equals("")) {
        continue;
      }
      String[] lineSplit = line.split(";");
      boolean ok = true;
      int itemID = 0;
      int npcID = 0;
      byte summonType = 0;
      try
      {
        itemID = Integer.parseInt(lineSplit[0]);
        npcID = Integer.parseInt(lineSplit[1]);
        summonType = Byte.parseByte(lineSplit[2]);
      }
      catch (Exception e)
      {
        System.out.println("Summon items data: Error in line " + lineCount + " -> incomplete/invalid data or wrong seperator!");
        System.out.println("\t\t" + line);
        ok = false;
      }

      if (!ok) {
        continue;
      }
      L2SummonItem summonitem = new L2SummonItem(itemID, npcID, summonType);
      _summonitems.put(Integer.valueOf(itemID), summonitem);
    }

    System.out.println("Summon items data: Loaded " + _summonitems.size() + " summon items.");
  }

  public L2SummonItem getSummonItem(int itemId)
  {
    return (L2SummonItem)_summonitems.get(Integer.valueOf(itemId));
  }

  public int[] itemIDs() {
    int size = _summonitems.size();
    int[] result = new int[size];
    int i = 0;
    for (L2SummonItem si : _summonitems.values())
    {
      result[i] = si.getItemId();
      i++;
    }
    return result;
  }
}