package l2r.gameserver.model.entity.events;

public interface EventAction
{
	void call(GlobalEvent event);
}
