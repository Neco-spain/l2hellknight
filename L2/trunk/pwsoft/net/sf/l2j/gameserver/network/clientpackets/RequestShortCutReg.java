package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutRegister;

public final class RequestShortCutReg extends L2GameClientPacket
{
  private int _type;
  private int _id;
  private int _slot;
  private int _page;
  private int _unk;

  protected void readImpl()
  {
    _type = readD();
    int slot = readD();
    _id = readD();
    _unk = readD();

    _slot = (slot % 12);
    _page = (slot / 12);
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    switch (_type)
    {
    case 1:
    case 3:
    case 4:
    case 5:
      L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, -1, _unk);
      sendPacket(new ShortCutRegister(sc));
      player.registerShortCut(sc);
      break;
    case 2:
      int level = player.getSkillLevel(_id);
      if (level <= 0)
        break;
      L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, level, _unk);
      sendPacket(new ShortCutRegister(sc));
      player.registerShortCut(sc);
      break;
    }
  }
}