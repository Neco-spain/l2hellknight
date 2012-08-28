package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.templates.L2Item;

public class SellListProcure extends L2GameServerPacket
{
  private static final String _S__E9_SELLLISTPROCURE = "[S] E9 SellListProcure";
  private final L2PcInstance _activeChar;
  private int _money;
  private Map<L2ItemInstance, Integer> _sellList = new FastMap();
  private List<CastleManorManager.CropProcure> _procureList = new FastList();
  private int _castle;

  public SellListProcure(L2PcInstance player, int castleId)
  {
    _money = player.getAdena();
    _activeChar = player;
    _castle = castleId;
    _procureList = CastleManager.getInstance().getCastleById(_castle).getCropProcure(0);
    for (CastleManorManager.CropProcure c : _procureList)
    {
      L2ItemInstance item = _activeChar.getInventory().getItemByItemId(c.getId());
      if ((item != null) && (c.getAmount() > 0))
      {
        _sellList.put(item, Integer.valueOf(c.getAmount()));
      }
    }
  }

  protected final void writeImpl()
  {
    writeC(233);
    writeD(_money);
    writeD(0);
    writeH(_sellList.size());

    for (L2ItemInstance item : _sellList.keySet())
    {
      writeH(item.getItem().getType1());
      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeD(((Integer)_sellList.get(item)).intValue());
      writeH(item.getItem().getType2());
      writeH(0);
      writeD(0);
    }
  }

  public String getType()
  {
    return "[S] E9 SellListProcure";
  }
}