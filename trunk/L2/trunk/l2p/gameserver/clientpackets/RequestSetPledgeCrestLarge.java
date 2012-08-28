package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.network.GameClient;

public class RequestSetPledgeCrestLarge extends L2GameClientPacket
{
  private int _length;
  private byte[] _data;

  protected void readImpl()
  {
    _length = readD();
    if ((_length == 2176) && (_length == _buf.remaining()))
    {
      _data = new byte[_length];
      readB(_data);
    }
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Clan clan = activeChar.getClan();
    if (clan == null) {
      return;
    }
    if ((activeChar.getClanPrivileges() & 0x80) == 128)
    {
      if ((clan.getCastle() == 0) && (clan.getHasHideout() == 0))
      {
        activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
        return;
      }

      int crestId = 0;

      if (_data != null)
      {
        crestId = CrestCache.getInstance().savePledgeCrestLarge(clan.getClanId(), _data);
        activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
      }
      else if (clan.hasCrestLarge()) {
        CrestCache.getInstance().removePledgeCrestLarge(clan.getClanId());
      }
      clan.setCrestLargeId(crestId);
      clan.broadcastClanStatus(false, true, false);
    }
  }
}