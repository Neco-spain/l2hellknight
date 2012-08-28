package l2p.gameserver.model.actor.listener;

import java.util.Collection;
import l2p.commons.listener.Listener;
import l2p.commons.listener.ListenerList;
import l2p.gameserver.listener.actor.npc.OnDecayListener;
import l2p.gameserver.listener.actor.npc.OnSpawnListener;
import l2p.gameserver.model.instances.NpcInstance;

public class NpcListenerList extends CharListenerList
{
  public NpcListenerList(NpcInstance actor)
  {
    super(actor);
  }

  public NpcInstance getActor()
  {
    return (NpcInstance)actor;
  }

  public void onSpawn()
  {
    if (!global.getListeners().isEmpty()) {
      for (Listener listener : global.getListeners())
        if (OnSpawnListener.class.isInstance(listener))
          ((OnSpawnListener)listener).onSpawn(getActor());
    }
    if (!getListeners().isEmpty())
      for (Listener listener : getListeners())
        if (OnSpawnListener.class.isInstance(listener))
          ((OnSpawnListener)listener).onSpawn(getActor());
  }

  public void onDecay()
  {
    if (!global.getListeners().isEmpty()) {
      for (Listener listener : global.getListeners())
        if (OnDecayListener.class.isInstance(listener))
          ((OnDecayListener)listener).onDecay(getActor());
    }
    if (!getListeners().isEmpty())
      for (Listener listener : getListeners())
        if (OnDecayListener.class.isInstance(listener))
          ((OnDecayListener)listener).onDecay(getActor());
  }
}