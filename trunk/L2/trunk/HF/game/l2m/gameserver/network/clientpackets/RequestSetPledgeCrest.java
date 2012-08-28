package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2m.gameserver.cache.CrestCache;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;

public class RequestSetPledgeCrest extends L2GameClientPacket
{
  private int _length;
  private byte[] _data;

  protected void readImpl()
  {
    _length = readD();
    if ((_length == 256) && (_length == _buf.remaining()))
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
    if ((activeChar.getClanPrivileges() & 0x80) == 128)
    {
      if (clan.getLevel() < 3)
      {
        activeChar.sendPacket(Msg.CLAN_CREST_REGISTRATION_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
        return;
      }

      int crestId = 0;

      if (_data != null)
        crestId = CrestCache.getInstance().savePledgeCrest(clan.getClanId(), _data);
      else if (clan.hasCrest()) {
        CrestCache.getInstance().removePledgeCrest(clan.getClanId());
      }
      clan.setCrestId(crestId);
      clan.broadcastClanStatus(false, true, false);
    }
  }
}