package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

public class RequestPledgeMemberInfo extends L2GameClientPacket
{
  private int _pledgeType;
  private String _target;

  protected void readImpl()
  {
    _pledgeType = readD();
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
        activeChar.sendPacket(new PledgeReceiveMemberInfo(cm));
    }
  }
}