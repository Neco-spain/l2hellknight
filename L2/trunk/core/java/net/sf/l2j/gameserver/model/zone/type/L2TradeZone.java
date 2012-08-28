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
  @Override
  public void onEnter(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      character.setInsideZone(L2Character.ZONE_TRADE, true);
      if (Config.USE_TRADE_ZONE)
	    {
      ((L2PcInstance) character).sendMessage("Вы вошли в трейд зону");
	    }
    }
  }
  @Override
  public void onExit(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      character.setInsideZone(L2Character.ZONE_TRADE, false);
      if (Config.USE_TRADE_ZONE)
	    {
      ((L2PcInstance) character).sendMessage("Вы покинули трейд зону");
	    }
    }
  }
  @Override
  public void onDieInside(L2Character character)
  {
    onExit(character);
  }
  @Override
  public void onReviveInside(L2Character character)
  {
    onEnter(character);
  }
}