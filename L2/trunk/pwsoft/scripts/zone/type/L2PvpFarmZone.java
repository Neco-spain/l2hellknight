package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2PvpFarmZone extends L2ZoneType
{
  public L2PvpFarmZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    character.setInPvpFarmZone(true);
    character.sendMessage("\u0412\u044B \u0432\u043E\u0448\u043B\u0438 \u0432 PvP-\u0444\u0430\u0440\u043C \u0437\u043E\u043D\u0443.");
  }

  protected void onExit(L2Character character)
  {
    character.setInPvpFarmZone(false);
    character.sendMessage("\u0412\u044B \u0432\u044B\u0448\u043B\u0438 \u0438\u0437 PvP-\u0444\u0430\u0440\u043C \u0437\u043E\u043D\u044B.");
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}