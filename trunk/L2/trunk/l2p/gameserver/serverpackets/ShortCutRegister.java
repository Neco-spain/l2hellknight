package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.ShortCut;

public class ShortCutRegister extends ShortCutPacket
{
  private ShortCutPacket.ShortcutInfo _shortcutInfo;

  public ShortCutRegister(Player player, ShortCut sc)
  {
    _shortcutInfo = convert(player, sc);
  }

  protected final void writeImpl()
  {
    writeC(68);

    _shortcutInfo.write(this);
  }
}