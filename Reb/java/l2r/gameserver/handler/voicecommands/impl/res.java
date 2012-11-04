package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.scripts.Functions;
/**
 * @author 4ipolino
 */
public class res extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = new String[] { "res" };

	
	@Override	
	public boolean useVoicedCommand(String command, Player activeChar, String target)
    {    
		
        if (command.equalsIgnoreCase("res"))
        {
           
           final int CoinCount = Config.PRICE_RESS;
           final ItemInstance Coin = activeChar.getInventory().getItemByItemId(Config.ITEM_ID_RESS);
          
           if (!activeChar.isAlikeDead() | activeChar.isFakeDeath())
           {
              activeChar.sendMessage("Вы не можете быть оживлены при жизни!");
              return false;
           }
           

        if(activeChar.isInOlympiadMode())
        {
           activeChar.sendMessage("Вы не можете использовать эту функцию во время олимпиады.");
          return false;
        }

   			if(Coin == null)
   				activeChar.sendMessage("Вы не имеете достаточно денег");

   			if(CoinCount != 0 && activeChar.getInventory().getItemByItemId(Config.ITEM_ID_RESS).getCount() < CoinCount)
   			{
   				activeChar.sendMessage("Вы не имеете достаточно денег");
   				activeChar.sendActionFailed();
   				return false;
   			}
			
              if (Config.COMMAND_RES)
              {
            	  Functions.removeItem(activeChar, Config.ITEM_ID_RESS, CoinCount);
                  activeChar.restoreExp();
                  activeChar.setCurrentCp(activeChar.getMaxCp());
                  activeChar.setCurrentHp(activeChar.getMaxHp(), true);
                  activeChar.setCurrentMp(activeChar.getMaxMp());
                  activeChar.broadcastPacket(new Revive(activeChar));
                  activeChar.sendMessage("Ты воскрес!");
                  activeChar.sendMessage("Вы успешно оплатили услуги сервиса. Спасибо!");  
              }

        }
       return true;
    }
	
	
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
