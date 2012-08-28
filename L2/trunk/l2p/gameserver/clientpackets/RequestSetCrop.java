package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.templates.manor.CropProcure;

public class RequestSetCrop extends L2GameClientPacket
{
  private int _count;
  private int _manorId;
  private long[] _items;

  protected void readImpl()
  {
    _manorId = readD();
    _count = readD();
    if ((_count * 21 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _items = new long[_count * 4];
    for (int i = 0; i < _count; i++)
    {
      _items[(i * 4 + 0)] = readD();
      _items[(i * 4 + 1)] = readQ();
      _items[(i * 4 + 2)] = readQ();
      _items[(i * 4 + 3)] = readC();
      if ((_items[(i * 4 + 0)] >= 1L) && (_items[(i * 4 + 1)] >= 0L) && (_items[(i * 4 + 2)] >= 0L))
        continue;
      _count = 0;
      return;
    }
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_count == 0)) {
      return;
    }
    if (activeChar.getClan() == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    Castle caslte = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
    if ((caslte.getOwnerId() != activeChar.getClanId()) || ((activeChar.getClanPrivileges() & 0x20000) != 131072))
    {
      activeChar.sendActionFailed();
      return;
    }

    List crops = new ArrayList(_count);
    for (int i = 0; i < _count; i++)
    {
      int id = (int)_items[(i * 4 + 0)];
      long sales = _items[(i * 4 + 1)];
      long price = _items[(i * 4 + 2)];
      int type = (int)_items[(i * 4 + 3)];
      if (id <= 0)
        continue;
      CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
      crops.add(s);
    }

    caslte.setCropProcure(crops, 1);
    caslte.saveCropData(1);
  }
}