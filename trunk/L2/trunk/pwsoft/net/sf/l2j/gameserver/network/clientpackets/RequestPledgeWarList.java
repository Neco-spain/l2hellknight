package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PledgeReceiveWarList;

public final class RequestPledgeWarList extends L2GameClientPacket
{
  private int _unk1;
  private int _tab;

  protected void readImpl()
  {
    _unk1 = readD();
    _tab = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    if (player.getClan() == null) return;

    player.sendPacket(new PledgeReceiveWarList(player.getClan(), _tab));
  }

  public String getType()
  {
    return "C.PledgeWarList";
  }
}