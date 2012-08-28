package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class Action extends L2GameClientPacket
{
	private static final String ACTION__C__04 = "[C] 04 Action";
	private static Logger _log = Logger.getLogger(Action.class.getName());

	// cddddc
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX;
	@SuppressWarnings("unused")
	private int _originY;
	@SuppressWarnings("unused")
	private int _originZ;
	private int _actionId;

	@Override
	protected void readImpl()
	{
		_objectId  = readD();   // Target object Identifier
		_originX   = readD();
		_originY   = readD();
		_originZ   = readD();
		_actionId  = readC();   // Action identifier : 0-Simple click, 1-Shift click
	}

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG) _log.fine("Action:" + _actionId);
		if (Config.DEBUG) _log.fine("oid:" + _objectId);

		// Get the current L2PcInstance of the player
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (activeChar.inObserverMode())
        {
                getClient().sendPacket(new ActionFailed());
                return;
        }
		
		L2Object obj;

		if (activeChar.getTargetId() == _objectId)
			obj = activeChar.getTarget();
		else
			obj = L2World.getInstance().findObject(_objectId);

		// If object requested does not exist, add warn msg into logs
		if (obj == null)
		{
			// pressing e.g. pickup many times quickly would get you here
			// _log.warning("Character: " + activeChar.getName() + " request action with non existent ObjectID:" + _objectId);
			getClient().sendPacket(new ActionFailed());
			return;
		}
		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if (activeChar.getPrivateStoreType()==0 && activeChar.getActiveRequester()==null)
		{
			switch (_actionId)
			{
				case 0:
					obj.onAction(activeChar);
					break;
				case 1:
					if (obj instanceof L2Character && ((L2Character)obj).isAlikeDead())
						obj.onAction(activeChar);
					else
						obj.onActionShift(getClient());
					break;
				default:
					// Ivalid action detected (probably client cheating), log this
					_log.warning("Character: " + activeChar.getName() + " requested invalid action: " + _actionId);
					getClient().sendPacket(new ActionFailed());
					break;
			}
		}
		else
			// Actions prohibited when in trade
			getClient().sendPacket(new ActionFailed());
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return ACTION__C__04;
	}
}
