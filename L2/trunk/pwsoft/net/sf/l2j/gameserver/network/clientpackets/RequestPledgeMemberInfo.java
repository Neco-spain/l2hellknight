package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

public final class RequestPledgeMemberInfo extends L2GameClientPacket
{
  private int _unk1;
  private String _player;

  protected void readImpl()
  {
    _unk1 = readD();
    _player = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    L2Clan clan = player.getClan();
    if (clan == null)
      return;
    L2ClanMember member = clan.getClanMember(_player);
    if (member == null)
      return;
    player.sendPacket(new PledgeReceiveMemberInfo(member));
  }

  public String getType()
  {
    return "C.PledgeMemberInfo";
  }
}