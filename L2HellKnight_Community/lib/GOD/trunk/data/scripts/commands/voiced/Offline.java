package commands.voiced;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IVoicedCommandHandler;
import l2rt.gameserver.handler.VoicedCommandHandler;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2TradeList;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.entity.olympiad.Olympiad;

public class Offline extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { "offline", "ghost" };

	public void onLoad()
	{
		if(Config.SERVICES_OFFLINE_TRADE_ALLOW)
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public boolean useVoicedCommand(String command, L2Player activeChar, String args)
	{
		if(activeChar.getOlympiadObserveId() != -1 || activeChar.getOlympiadGameId() != -1 || Olympiad.isRegisteredInComp(activeChar) || activeChar.getKarma() > 0)
		{
			activeChar.sendActionFailed();
			return false;
		}

		if(activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL)
		{
			show(new CustomMessage("scripts.commands.user.offline.LowLevel", activeChar).addNumber(Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL), activeChar);
			return false;
		}

		if(!activeChar.isInStoreMode())
		{
			show(new CustomMessage("scripts.commands.user.offline.IncorrectUse", activeChar), activeChar);
			return false;
		}

		if(activeChar.getNoChannelRemained() > 0)
		{
			show(new CustomMessage("scripts.commands.user.offline.BanChat", activeChar), activeChar);
			return false;
		}

		if(activeChar.isActionBlocked(L2Zone.BLOCKED_ACTION_PRIVATE_STORE))
		{
			activeChar.sendMessage(new CustomMessage("trade.OfflineNoTradeZone", activeChar));
			return false;
		}

		if(Config.SERVICES_OFFLINE_TRADE_PRICE > 0 && Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0)
		{
			if(getItemCount(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM) < Config.SERVICES_OFFLINE_TRADE_PRICE)
			{
				show(new CustomMessage("scripts.commands.user.offline.NotEnough", activeChar).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE), activeChar);
				return false;
			}
			removeItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE);
		}

		L2TradeList.validateList(activeChar);

		if(activeChar.getPet() != null)
			activeChar.getPet().unSummon();

		activeChar.offline();
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}