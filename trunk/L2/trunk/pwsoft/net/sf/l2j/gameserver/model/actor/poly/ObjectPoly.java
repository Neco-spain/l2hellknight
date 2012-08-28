package net.sf.l2j.gameserver.model.actor.poly;

import net.sf.l2j.gameserver.model.L2Object;

public class ObjectPoly
{
  private L2Object _activeObject;
  private int _polyId;
  private String _polyType;

  public ObjectPoly(L2Object activeObject)
  {
    _activeObject = activeObject;
  }

  public void setPolyInfo(String polyType, String polyId)
  {
    setPolyId(Integer.parseInt(polyId));
    setPolyType(polyType);
  }

  public final L2Object getActiveObject()
  {
    return _activeObject;
  }
  public final boolean isMorphed() {
    return getPolyType() != null;
  }
  public final int getPolyId() { return _polyId; } 
  public final void setPolyId(int value) { _polyId = value; } 
  public final String getPolyType() {
    return _polyType; } 
  public final void setPolyType(String value) { _polyType = value;
  }
}