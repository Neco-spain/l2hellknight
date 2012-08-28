package l2p.gameserver.handler.voicecommands.impl;

import l2p.gameserver.Config;
import l2p.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.components.CustomMessage;

public class Offline extends Functions
  implements IVoicedCommandHandler
{
  private String[] _commandList = { "offline" };

  public boolean useVoicedCommand(String command, Player activeChar, String args)
  {
    if (!Config.SERVICES_OFFLINE_TRADE_ALLOW) {
      return false;
    }
    if ((activeChar.getOlympiadObserveGame() != null) || (activeChar.getOlympiadGame() != null) || (Olympiad.isRegisteredInComp(activeChar)) || (activeChar.getKarma() > 0))
    {
      activeChar.sendActionFailed();
      return false;
    }

    if (activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
    {
      show(new CustomMessage("voicedcommandhandlers.Offline.LowLevel", activeChar, new Object[0]).addNumber(Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL), activeChar);
      return false;
    }

    if ((!activeChar.isInZone(Zone.ZoneType.offshore)) && (Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE))
    {
      show(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar, new Object[0]), activeChar);
      return false;
    }

    if (!activeChar.isInStoreMode())
    {
      show(new CustomMessage("voicedcommandhandlers.Offline.IncorrectUse", activeChar, new Object[0]), activeChar);
      return false;
    }

    if (activeChar.getNoChannelRemained() > 0L)
    {
      show(new CustomMessage("voicedcommandhandlers.Offline.BanChat", activeChar, new Object[0]), activeChar);
      return false;
    }

    if (activeChar.isActionBlocked("open_private_store"))
    {
      show(new CustomMessage("trade.OfflineNoTradeZone", activeChar, new Object[0]), activeChar);
      return false;
    }

    if ((Config.SERVICES_OFFLINE_TRADE_PRICE > 0) && (Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0))
    {
      if (getItemCount(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM) < Config.SERVICES_OFFLINE_TRADE_PRICE)
      {
        show(new CustomMessage("voicedcommandhandlers.Offline.NotEnough", activeChar, new Object[0]).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE), activeChar);
        return false;
      }
      removeItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE);
    }

    activeChar.offline();
    return true;
  }

  public String[] getVoicedCommandList()
  {
    return _commandList;
  }
}