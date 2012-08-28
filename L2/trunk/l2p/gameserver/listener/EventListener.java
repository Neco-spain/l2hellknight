package l2p.gameserver.listener;

import l2p.commons.listener.Listener;
import l2p.gameserver.model.entity.events.GlobalEvent;

public abstract interface EventListener extends Listener<GlobalEvent>
{
}