package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.geoeditorcon.GeoEditorListener;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.PartyMemberPosition;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocationInVehicle;

public class ValidatePosition extends L2GameClientPacket
{
    private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";

    public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }

    private int _x;
    private int _y;
    private int _z;
    private int _heading;
    @SuppressWarnings("unused")
    private int _data;

    @Override
    protected void readImpl()
    {
		_x  = readD();
		_y  = readD();
		_z  = readD();
		_heading  = readD();
		_data  = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null || activeChar.isTeleporting()) return;

        if (Config.COORD_SYNCHRONIZE > 0)
        {
            activeChar.setClientX(_x);
            activeChar.setClientY(_y);
            activeChar.setClientZ(_z);
            activeChar.setClientHeading(_heading);
            int realX = activeChar.getX();
            int realY = activeChar.getY();

            double dx = _x - realX;
            double dy = _y - realY;
            double diffSq = (dx*dx + dy*dy);

            if (diffSq > 0 && diffSq < 250000)
            {
                if ((Config.COORD_SYNCHRONIZE & 1) == 1
                    && (!activeChar.isMoving()
                    || !activeChar.validateMovementHeading(_heading)))
                {
                    if (diffSq < 2500)
                    	activeChar.setXYZ(realX, realY, _z);
                    else
                    	activeChar.setXYZ(_x, _y, _z);
                    activeChar.setHeading(_heading);
                }
                else if ((Config.COORD_SYNCHRONIZE & 2) == 2
                        && diffSq > 10000)
                {
                    if (activeChar.isInBoat())
                    {
                        sendPacket(new ValidateLocationInVehicle(activeChar));
                    }
                    else
                    {
                    	activeChar.sendPacket(new ValidateLocation(activeChar));
                    }
                }
            }
            activeChar.setLastClientPosition(_x, _y, _z);
            activeChar.setLastServerPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ());
        }
        else if (Config.COORD_SYNCHRONIZE == -1)
        {
            activeChar.setClientX(_x);
            activeChar.setClientY(_y);
            activeChar.setClientZ(_z);
            activeChar.setClientHeading(_heading);
            int realX = activeChar.getX();
            int realY = activeChar.getY();
            @SuppressWarnings("unused")
			int realZ = activeChar.getZ();

            double dx = _x - realX;
            double dy = _y - realY;
            double diffSq = (dx*dx + dy*dy);
            if (diffSq < 250000)
                 activeChar.setXYZ(realX,realY,_z);

            if (Config.DEVELOPER)
            {
                if (diffSq > 1000000) 
				{
                    if (activeChar.isInBoat())
                    {
                        sendPacket(new ValidateLocationInVehicle(activeChar));
                    }
                    else
                    {
                    	activeChar.sendPacket(new ValidateLocation(activeChar));
                    }
                }
            }
        }
		if(activeChar.getParty() != null)
			activeChar.getParty().broadcastToPartyMembers(activeChar,new PartyMemberPosition(activeChar));

		if (!activeChar.isInWater() && !activeChar.isFlying())
			activeChar.isFalling(true,0);

		if (Config.ALLOW_WATER)
			activeChar.checkWaterState();

		activeChar.checkOlyState();

		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			if (GeoEditorListener.getInstance().getThread() != null && GeoEditorListener.getInstance().getThread().isWorking() && GeoEditorListener.getInstance().getThread().isSend(activeChar))
			{
				GeoEditorListener.getInstance().getThread().sendGmPosition(_x,_y,(short)_z);
			}
		}
    }

    @Override
    public String getType()
    {
        return _C__48_VALIDATEPOSITION;
    }

    @Deprecated
    public boolean equal(ValidatePosition pos)
    {
        return _x == pos._x && _y == pos._y && _z == pos._z && _heading == pos._heading;
    }
}