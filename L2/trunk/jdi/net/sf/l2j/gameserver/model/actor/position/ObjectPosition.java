package net.sf.l2j.gameserver.model.actor.position;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Point3D;

public class ObjectPosition
{
  private static final Logger _log;
  private L2Object _activeObject;
  private int _heading = 0;
  private Point3D _worldPosition;
  private L2WorldRegion _worldRegion;

  public ObjectPosition(L2Object activeObject)
  {
    _activeObject = activeObject;
    setWorldRegion(L2World.getInstance().getRegion(getWorldPosition()));
  }

  public final void setXYZ(int x, int y, int z)
  {
    if ((Config.ASSERT) && (!$assertionsDisabled) && (getWorldRegion() == null)) throw new AssertionError();

    setWorldPosition(x, y, z);
    try
    {
      if (L2World.getInstance().getRegion(getWorldPosition()) != getWorldRegion())
      {
        updateWorldRegion();
      }
    }
    catch (Exception e)
    {
      _log.warning("Object Id at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
      _log.warning("Exception: Character " + getActiveObject().getName() + " in Bad coordinates: x: " + getActiveObject().getX() + " y: " + getActiveObject().getY() + " z: " + getActiveObject().getZ());
      if ((getActiveObject() instanceof L2PcInstance))
      {
        ((L2PcInstance)getActiveObject()).teleToLocation(0, 0, 0, false);
        ((L2PcInstance)getActiveObject()).sendMessage("Error with your coords, Please ask a GM for help!");
      }
      else if ((getActiveObject() instanceof L2Character)) {
        getActiveObject().decayMe();
      }
    }
  }

  public final void setXYZInvisible(int x, int y, int z) {
    if ((Config.ASSERT) && (!$assertionsDisabled) && (getWorldRegion() != null)) throw new AssertionError();
    if (x > 228608) x = 223608;
    if (x < -131072) x = -126072;
    if (y > 262144) y = 257144;
    if (y < -262144) y = -257144;

    setWorldPosition(x, y, z);
    getActiveObject().setIsVisible(false);
  }

  public void updateWorldRegion()
  {
    if (!getActiveObject().isVisible()) return;

    L2WorldRegion newRegion = L2World.getInstance().getRegion(getWorldPosition());
    if (newRegion != getWorldRegion())
    {
      getWorldRegion().removeVisibleObject(getActiveObject());

      setWorldRegion(newRegion);

      getWorldRegion().addVisibleObject(getActiveObject());
    }
  }

  public final L2Object getActiveObject()
  {
    return _activeObject;
  }

  public final int getHeading()
  {
    return _heading;
  }

  public final void setHeading(int value)
  {
    _heading = value;
  }

  public final int getX()
  {
    return getWorldPosition().getX();
  }

  public final void setX(int value)
  {
    getWorldPosition().setX(value);
  }

  public final int getY()
  {
    return getWorldPosition().getY();
  }

  public final void setY(int value)
  {
    getWorldPosition().setY(value);
  }

  public final int getZ()
  {
    return getWorldPosition().getZ();
  }

  public final void setZ(int value)
  {
    getWorldPosition().setZ(value);
  }

  public final Point3D getWorldPosition()
  {
    if (_worldPosition == null) _worldPosition = new Point3D(0, 0, 0);
    return _worldPosition;
  }

  public final void setWorldPosition(int x, int y, int z)
  {
    getWorldPosition().setXYZ(x, y, z);
  }

  public final void setWorldPosition(Point3D newPosition)
  {
    setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
  }

  public final L2WorldRegion getWorldRegion()
  {
    return _worldRegion;
  }

  public final void setWorldRegion(L2WorldRegion value)
  {
    if ((_worldRegion != null) && ((getActiveObject() instanceof L2Character)))
    {
      if (value != null)
        _worldRegion.revalidateZones((L2Character)getActiveObject());
      else
        _worldRegion.removeFromZones((L2Character)getActiveObject());
    }
    _worldRegion = value;
  }

  static
  {
    _log = Logger.getLogger(ObjectPosition.class.getName());
  }
}