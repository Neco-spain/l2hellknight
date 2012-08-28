package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.PartyMemberPosition;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

public class MoveBackwardToLocation extends L2GameClientPacket
{
    //private static Logger _log = Logger.getLogger(MoveBackwardToLocation.class.getName());
    // cdddddd
    private       int _targetX;
    private       int _targetY;
    private       int _targetZ;
    @SuppressWarnings("unused")
    private int _originX;
    @SuppressWarnings("unused")
    private int _originY;
    @SuppressWarnings("unused")
    private int _originZ;
    private       int _moveMovement;
    
    //For geodata
    private       int _curX;
    private       int _curY;
    @SuppressWarnings("unused")
    private       int _curZ;
    
    public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }

    private static final String _C__01_MOVEBACKWARDTOLOC = "[C] 01 MoveBackwardToLoc";
    /**
     * packet type id 0x01
     * @param decrypt
     */
    @Override
    protected void readImpl()
    {
        _targetX  = readD();
        _targetY  = readD();
        _targetZ  = readD();
        _originX  = readD();
        _originY  = readD();
        _originZ  = readD();
		try
		{
			_moveMovement = readD(); // is 0 if cursor keys are used  1 if mouse is used
		}
		catch (BufferUnderflowException e)
		{
			// ignore for now
			if(Config.KICK_L2WALKER)
				{
					L2PcInstance activeChar = getClient().getActiveChar();
					activeChar.sendPacket(SystemMessageId.HACKING_TOOL);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " trying to use l2walker!", IllegalPlayerAction.PUNISH_KICK);
				}
		}
    }

    
    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        _targetZ += activeChar.getTemplate().getCollisionHeight();
        _curX = activeChar.getX();
        _curY = activeChar.getY();
        _curZ = activeChar.getZ();
        
        if(activeChar.isInBoat())
        {
            activeChar.setInBoat(false);
        }

        	if(activeChar.getPrivateStoreType() != 0)
        	{
        		activeChar.sendPacket(new ActionFailed());
        		return;
        	}
        	if(activeChar.getActiveEnchantItem() != null)
        	{
                    activeChar.sendPacket(new EnchantResult(1));
                    activeChar.setActiveEnchantItem(null);
        	}
        	
        if (activeChar.getTeleMode() > 0)
        {
            if (activeChar.getTeleMode() == 1)
                activeChar.setTeleMode(0);
            activeChar.sendPacket(new ActionFailed());
            activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
            return;
        }
        
        if (activeChar.isFakeDeath())
        	return;

        if (activeChar.isDead())
        {
        	activeChar.sendPacket(new ActionFailed());
        	return;
        }
        
        if (_moveMovement == 0 && !Config.GEODATA) // cursor movement without geodata movement check is disabled
        {
            activeChar.sendPacket(new ActionFailed());
        }
        else 
        {
            double dx = _targetX-_curX;
            double dy = _targetY-_curY;

            // Can't move if character is confused, or trying to move a huge distance
            if (activeChar.isOutOfControl() || ((dx*dx+dy*dy) > 98010000)) // 9900*9900
            {
                activeChar.sendPacket(new ActionFailed());
                return;
            }
            activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
                    new L2CharPosition(_targetX, _targetY, _targetZ, 0));
            
            if(activeChar.getParty() != null)
                activeChar.getParty().broadcastToPartyMembers(activeChar,new PartyMemberPosition(activeChar));
        }       
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__01_MOVEBACKWARDTOLOC;
    }
}
