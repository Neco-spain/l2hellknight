package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2ExtractableItem;
import net.sf.l2j.gameserver.model.L2ExtractableProductItem;
import net.sf.l2j.util.log.AbstractLogger;

public class ExtractableItemsData
{
  private static final Logger _log = AbstractLogger.getLogger(ExtractableItemsData.class.getName());
  private static FastMap<Integer, L2ExtractableItem> _items = new FastMap().shared("ExtractableItemsData._items");

  private static ExtractableItemsData _instance = null;

  public static ExtractableItemsData getInstance()
  {
    if (_instance == null) {
      _instance = new ExtractableItemsData();
    }
    return _instance;
  }

  public ExtractableItemsData()
  {
    Scanner s;
    try
    {
      s = new Scanner(new File("./data/extractable_items.csv"));
    }
    catch (Exception e)
    {
      _log.warning("Extractable items data: Can not find './data/extractable_items.csv'");
      return;
    }

    int lineCount = 0;

    while (s.hasNextLine())
    {
      lineCount++;

      String line = s.nextLine();

      if ((line.startsWith("#")) || 
        (line.equals(""))) {
        continue;
      }
      String[] lineSplit = line.split(";");
      boolean ok = true;
      int itemID = 0;
      try
      {
        itemID = Integer.parseInt(lineSplit[0]);
      }
      catch (Exception e)
      {
        _log.warning("Extractable items data: Error in line " + lineCount + " -> invalid item id or wrong seperator after item id!");
        _log.warning("\t\t" + line);
        ok = false;
      }

      if (!ok) {
        continue;
      }
      FastList product_temp = new FastList();

      for (int i = 0; i < lineSplit.length - 1; i++)
      {
        ok = true;

        String[] lineSplit2 = lineSplit[(i + 1)].split(",");

        if (lineSplit2.length != 3)
        {
          _log.warning("Extractable items data: Error in line " + lineCount + " -> wrong seperator!");
          _log.warning("\t\t" + line);
          ok = false;
        }

        if (!ok) {
          continue;
        }
        int production = 0;
        int amount = 0;
        int chance = 0;
        try
        {
          production = Integer.parseInt(lineSplit2[0]);
          amount = Integer.parseInt(lineSplit2[1]);
          chance = Integer.parseInt(lineSplit2[2]);
        }
        catch (Exception e)
        {
          _log.warning("Extractable items data: Error in line " + lineCount + " -> incomplete/invalid production data or wrong seperator!");
          _log.warning("\t\t" + line);
          ok = false;
        }

        if (!ok) {
          continue;
        }
        L2ExtractableProductItem product = new L2ExtractableProductItem(production, amount, chance);
        product_temp.add(product);
      }

      int fullChances = 0;

      for (L2ExtractableProductItem Pi : product_temp) {
        fullChances += Pi.getChance();
      }
      if (fullChances > 100)
      {
        _log.warning("Extractable items data: Error in line " + lineCount + " -> all chances together are more then 100!");
        _log.warning("\t\t" + line);
        continue;
      }
      L2ExtractableItem product = new L2ExtractableItem(itemID, product_temp);
      _items.put(Integer.valueOf(itemID), product);
    }

    s.close();
    _log.info("Loading Extractable items... total " + _items.size() + " items!");
  }

  public L2ExtractableItem getExtractableItem(int itemID)
  {
    return (L2ExtractableItem)_items.get(Integer.valueOf(itemID));
  }

  public int[] itemIDs() {
    int size = _items.size();
    int[] result = new int[size];
    int i = 0;
    for (L2ExtractableItem ei : _items.values())
    {
      result[i] = ei.getItemId();
      i++;
    }
    return result;
  }
}