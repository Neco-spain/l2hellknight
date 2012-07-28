package handlers.itemhandlers;

import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.SystemMessage;

public class NevitHourglass extends ItemSkills {
        
        @Override
        public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
        {
                
                if (!(playable instanceof L2PcInstance))
                {
                        playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
                        return;
                }
                
                L2PcInstance activeChar = (L2PcInstance)playable;
                if( activeChar.RecoBonusActive() )
                {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                        sm.addItemName(item.getItemId());
                        activeChar.sendPacket(sm);
                        return;
                }
                
                super.useItem(playable, item, forceUse);
        }
        

}