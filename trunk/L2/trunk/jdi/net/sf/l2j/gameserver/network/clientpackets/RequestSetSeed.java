package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.SeedProduction;
import net.sf.l2j.gameserver.model.entity.Castle;

public class RequestSetSeed extends L2GameClientPacket
{
  private static final String _C__D0_0A_REQUESTSETSEED = "[C] D0:0A RequestSetSeed";
  private int _size;
  private int _manorId;
  private int[] _items;

  protected void readImpl()
  {
    _manorId = readD();
    _size = readD();
    if ((_size * 12 > _buf.remaining()) || (_size > 500))
    {
      _size = 0;
      return;
    }
    _items = new int[_size * 3];
    for (int i = 0; i < _size; i++)
    {
      int itemId = readD();
      _items[(i * 3 + 0)] = itemId;
      int sales = readD();
      _items[(i * 3 + 1)] = sales;
      int price = readD();
      _items[(i * 3 + 2)] = price;
    }
  }

  protected void runImpl()
  {
    if (_size < 1) {
      return;
    }
    FastList seeds = new FastList();
    for (int i = 0; i < _size; i++)
    {
      int id = _items[(i * 3 + 0)];
      int sales = _items[(i * 3 + 1)];
      int price = _items[(i * 3 + 2)];
      if (id <= 0)
        continue;
      CastleManorManager.SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);

      seeds.add(s);
    }

    CastleManager.getInstance().getCastleById(_manorId).setSeedProduction(seeds, 1);
    if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
      CastleManager.getInstance().getCastleById(_manorId).saveSeedData(1);
  }

  public String getType()
  {
    return "[C] D0:0A RequestSetSeed";
  }
}