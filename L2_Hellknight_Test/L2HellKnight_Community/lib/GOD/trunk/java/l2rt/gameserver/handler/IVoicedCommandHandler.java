package l2rt.gameserver.handler;

import l2rt.gameserver.model.L2Player;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:09 $
 */
public interface IVoicedCommandHandler
{
	/**
	 * this is the worker method that is called when someone uses an admin command.
	 * @param activeChar
	 * @param command
	 * @return command success
	 */
	public boolean useVoicedCommand(String command, L2Player activeChar, String target);

	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public String[] getVoicedCommandList();
}
