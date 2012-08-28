package l2p.gameserver.listener.reflection;

import l2p.commons.listener.Listener;
import l2p.gameserver.model.entity.Reflection;

public abstract interface OnReflectionCollapseListener extends Listener<Reflection>
{
  public abstract void onReflectionCollapse(Reflection paramReflection);
}