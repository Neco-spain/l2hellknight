package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.PledgeReceivePowerInfo;

public class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
  private int _not_known;
  private String _target;

  protected void readImpl()
  {
    _not_known = readD();
    _target = readS(16);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    Clan clan = activeChar.getClan();
    if (clan != null)
    {
      UnitMember cm = clan.getAnyMember(_target);
      if (cm != null)
        activeChar.sendPacket(new PledgeReceivePowerInfo(cm));
    }
  }
}