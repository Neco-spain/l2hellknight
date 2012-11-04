package l2r.gameserver.model.entity.events.objects;

import java.io.Serializable;

import l2r.gameserver.model.entity.events.GlobalEvent;

public interface SpawnableObject extends Serializable
{
	void spawnObject(GlobalEvent event);

	void despawnObject(GlobalEvent event);

	void refreshObject(GlobalEvent event);
}
