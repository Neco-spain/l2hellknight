package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2TradeZone extends L2ZoneType
{
  public L2TradeZone(int id)
  {
    super(id);
  }

  public void onEnter(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      character.setInsideZone(32768, true);
      if (Config.USE_TRADE_ZONE)
      {
        ((L2PcInstance)character).sendMessage("\u0412\u044B \u0432\u043E\u0448\u043B\u0438 \u0432 \u0442\u0440\u0435\u0439\u0434 \u0437\u043E\u043D\u0443");
      }
    }
  }

  public void onExit(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      character.setInsideZone(32768, false);
      if (Config.USE_TRADE_ZONE)
      {
        ((L2PcInstance)character).sendMessage("\u0412\u044B \u043F\u043E\u043A\u0438\u043D\u0443\u043B\u0438 \u0442\u0440\u0435\u0439\u0434 \u0437\u043E\u043D\u0443");
      }
    }
  }

  public void onDieInside(L2Character character)
  {
    onExit(character);
  }

  public void onReviveInside(L2Character character)
  {
    onEnter(character);
  }
}