/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2005/07/11 15:29:30 $
 */
public final class RequestAutoSoulShot extends L2GameClientPacket
{
    private static final String _C__CF_REQUESTAUTOSOULSHOT = "[C] CF RequestAutoSoulShot";
    // format  cd
    private int _itemId;
    private int _type; // 1 = on : 0 = off;

    @Override
	protected void readImpl()
    {
        _itemId = readD();
        _type = readD();
    }

    @Override
	protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();

        if (activeChar == null)
            return;
        
        if (activeChar.getActiveTradeList() != null)
            return;
        
        if (activeChar.getPrivateStoreType() != 0 &&
            activeChar.getActiveRequester() != null &&
            activeChar.isDead())
        	return;
        
            L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);

            if (item != null)
            {
            	
            	if (_itemId>=3947 && _itemId<=3952 && activeChar.isInOlympiadMode())
            	{
        			SystemMessage sm = new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
        			sm.addString(item.getItemName());
        			activeChar.sendPacket(sm);
        			sm = null;
        			return;
        		}
            	
                if (_type == 1)
                {
                	//Fishingshots are not automatic on retail
                	if (_itemId < 6535 || _itemId > 6540) 
                	{
                		activeChar.addAutoSoulShot(_itemId);
                		ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
                		activeChar.sendPacket(atk);
                		SystemMessage sm = new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
                		sm.addString(item.getItemName());
                		activeChar.sendPacket(sm);
                		sm = null;
                	
                	
	                    // Attempt to charge first shot on activation
	                    if (_itemId == 6645 || _itemId == 6646 || _itemId == 6647)
	                    {
	                    	activeChar.rechargeAutoSoulShot(true, true, true);
	                    }
                	
	                
                	if (activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem() && 
                		item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getCrystalType())
	                    {
                		activeChar.rechargeAutoSoulShot(true, true, false);
	                    }
                	}
                }
                else if (_type == 0)
                {
                	 activeChar.removeAutoSoulShot(_itemId);
                     ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
                     activeChar.sendPacket(atk);
                     SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
                     sm.addString(item.getItemName());
                     activeChar.sendPacket(sm);
                     sm = null;
                }
        }
    }
    

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
	public String getType()
    {
        return _C__CF_REQUESTAUTOSOULSHOT;
    }
}