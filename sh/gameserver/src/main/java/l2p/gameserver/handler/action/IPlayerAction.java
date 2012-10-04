package l2p.gameserver.handler.action;

import l2p.gameserver.handler.action.PlayerAction.ActionType;
import l2p.gameserver.model.Creature;

public interface IPlayerAction
{
   /**
	* Вызывается из разного рода классов, связанных с чарами.
	*/
	public void actionHandler(Creature character, ActionType action, Object... objects) throws Exception;
}
