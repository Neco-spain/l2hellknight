package net.sf.l2j.gameserver.datatables;

import java.util.List;
import javolution.util.FastList;

public class CrownTable
{
  private static List<Integer> _crownList = new FastList();

  public static List<Integer> getCrownList()
  {
    if (_crownList.isEmpty())
    {
      _crownList.add(Integer.valueOf(6841));
      _crownList.add(Integer.valueOf(6834));
      _crownList.add(Integer.valueOf(6835));
      _crownList.add(Integer.valueOf(6836));
      _crownList.add(Integer.valueOf(6837));
      _crownList.add(Integer.valueOf(6838));
      _crownList.add(Integer.valueOf(6839));
      _crownList.add(Integer.valueOf(6840));
      _crownList.add(Integer.valueOf(8182));
      _crownList.add(Integer.valueOf(8183));
    }
    return _crownList;
  }

  public static int getCrownId(int CastleId)
  {
    int CrownId = 0;
    switch (CastleId)
    {
    case 1:
      CrownId = 6838;
      break;
    case 2:
      CrownId = 6835;
      break;
    case 3:
      CrownId = 6839;
      break;
    case 4:
      CrownId = 6837;
      break;
    case 5:
      CrownId = 6840;
      break;
    case 6:
      CrownId = 6834;
      break;
    case 7:
      CrownId = 6836;
      break;
    case 8:
      CrownId = 8182;
      break;
    case 9:
      CrownId = 8183;
      break;
    default:
      CrownId = 0;
    }

    return CrownId;
  }
}