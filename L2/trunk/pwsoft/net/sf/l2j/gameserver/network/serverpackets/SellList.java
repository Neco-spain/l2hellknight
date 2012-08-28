package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class SellList extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(SellList.class.getName());
  private final L2PcInstance _activeChar;
  private final L2MerchantInstance _lease;
  private int _money;
  private List<L2ItemInstance> _selllist = new FastList();

  public SellList(L2PcInstance player)
  {
    _activeChar = player;
    _lease = null;
    _money = _activeChar.getAdena();
    doLease();
  }

  public SellList(L2PcInstance player, L2MerchantInstance lease)
  {
    _activeChar = player;
    _lease = lease;
    _money = _activeChar.getAdena();
    doLease();
  }

  private void doLease()
  {
    if (_lease == null)
    {
      for (L2ItemInstance item : _activeChar.getInventory().getItems())
      {
        if ((item.isEquipped()) || (!item.getItem().isSellable()) || ((_activeChar.getPet() != null) && (item.getObjectId() == _activeChar.getPet().getControlItemId())))
        {
          continue;
        }

        _selllist.add(item);
      }
    }
  }

  protected final void writeImpl()
  {
    writeC(16);
    writeD(_money);
    writeD(_lease == null ? 0 : 1000000 + _lease.getTemplate().npcId);

    writeH(_selllist.size());

    for (L2ItemInstance item : _selllist)
    {
      writeH(item.getItem().getType1());
      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeD(item.getCount());
      writeH(item.getItem().getType2());
      writeH(0);
      writeD(item.getItem().getBodyPart());
      writeH(item.getEnchantLevel());
      writeH(0);
      writeH(0);

      if (_lease == null)
        writeD(item.getItem().getReferencePrice() / 2);
    }
  }

  public void gc()
  {
    _selllist.clear();
    _selllist = null;
  }

  public String getType()
  {
    return "S.SellList";
  }
}