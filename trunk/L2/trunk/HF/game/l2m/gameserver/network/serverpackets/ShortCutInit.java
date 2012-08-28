package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.actor.instances.player.ShortCut;

public class ShortCutInit extends ShortCutPacket
{
  private List<ShortCutPacket.ShortcutInfo> _shortCuts = Collections.emptyList();

  public ShortCutInit(Player pl)
  {
    Collection shortCuts = pl.getAllShortCuts();
    _shortCuts = new ArrayList(shortCuts.size());
    for (ShortCut shortCut : shortCuts)
      _shortCuts.add(convert(pl, shortCut));
  }

  protected final void writeImpl()
  {
    writeC(69);
    writeD(_shortCuts.size());

    for (ShortCutPacket.ShortcutInfo sc : _shortCuts)
      sc.write(this);
  }
}