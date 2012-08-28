package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.CastleManorManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.templates.manor.SeedProduction;

public class RequestSetSeed extends L2GameClientPacket
{
  private int _count;
  private int _manorId;
  private long[] _items;

  protected void readImpl()
  {
    _manorId = readD();
    _count = readD();
    if ((_count * 20 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _items = new long[_count * 3];
    for (int i = 0; i < _count; i++)
    {
      _items[(i * 3 + 0)] = readD();
      _items[(i * 3 + 1)] = readQ();
      _items[(i * 3 + 2)] = readQ();
      if ((_items[(i * 3 + 0)] >= 1L) && (_items[(i * 3 + 1)] >= 0L) && (_items[(i * 3 + 2)] >= 0L))
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

    List seeds = new ArrayList(_count);
    for (int i = 0; i < _count; i++)
    {
      int id = (int)_items[(i * 3 + 0)];
      long sales = _items[(i * 3 + 1)];
      long price = _items[(i * 3 + 2)];
      if (id <= 0)
        continue;
      SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
      seeds.add(s);
    }

    caslte.setSeedProduction(seeds, 1);
    caslte.saveSeedData(1);
  }
}