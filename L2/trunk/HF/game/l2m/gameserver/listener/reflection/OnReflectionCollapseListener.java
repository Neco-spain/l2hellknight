package l2m.gameserver.listener.reflection;

import l2p.commons.listener.Listener;
import l2m.gameserver.model.entity.Reflection;

public abstract interface OnReflectionCollapseListener extends Listener<Reflection>
{
  public abstract void onReflectionCollapse(Reflection paramReflection);
}