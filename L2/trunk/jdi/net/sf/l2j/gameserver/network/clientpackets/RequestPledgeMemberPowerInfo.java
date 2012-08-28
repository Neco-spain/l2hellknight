package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PledgeReceivePowerInfo;

public final class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
  private static final String _C__D0_1B_REQUESTPLEDGEMEMBERPOWERINFO = "[C] D0:1B RequestPledgeMemberPowerInfo";
  private int _unk1;
  private String _player;

  protected void readImpl()
  {
    _unk1 = readD();
    _player = readS();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    L2Clan clan = activeChar.getClan();
    if (clan == null)
      return;
    L2ClanMember member = clan.getClanMember(_player);
    if (member == null)
      return;
    activeChar.sendPacket(new PledgeReceivePowerInfo(member));
  }

  public String getType()
  {
    return "[C] D0:1B RequestPledgeMemberPowerInfo";
  }
}