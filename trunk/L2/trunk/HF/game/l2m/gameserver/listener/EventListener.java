package l2m.gameserver.listener;

import l2p.commons.listener.Listener;
import l2m.gameserver.model.entity.events.GlobalEvent;

public abstract interface EventListener extends Listener<GlobalEvent>
{
}