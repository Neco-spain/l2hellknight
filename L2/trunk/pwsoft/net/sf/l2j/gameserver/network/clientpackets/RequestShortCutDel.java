package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestShortCutDel extends L2GameClientPacket
{
  private int _slot;
  private int _page;

  protected void readImpl()
  {
    int id = readD();
    _slot = (id % 12);
    _page = (id / 12);
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    player.deleteShortCut(_slot, _page);
  }

  public String getType()
  {
    return "C.ShortCutDel";
  }
}