package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.actor.instances.player.ShortCut;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ShortCutRegister;

public class RequestShortCutReg extends L2GameClientPacket
{
  private int _type;
  private int _id;
  private int _slot;
  private int _page;
  private int _lvl;
  private int _characterType;

  protected void readImpl()
  {
    _type = readD();
    int slot = readD();
    _id = readD();
    _lvl = readD();
    _characterType = readD();

    _slot = (slot % 12);
    _page = (slot / 12);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((_page < 0) || (_page > 11))
    {
      activeChar.sendActionFailed();
      return;
    }

    ShortCut shortCut = new ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
    activeChar.sendPacket(new ShortCutRegister(activeChar, shortCut));
    activeChar.registerShortCut(shortCut);
  }
}