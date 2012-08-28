package l2m.gameserver.geodata;

import l2p.commons.geometry.Shape;

public abstract interface GeoCollision
{
  public abstract Shape getShape();

  public abstract byte[][] getGeoAround();

  public abstract void setGeoAround(byte[][] paramArrayOfByte);

  public abstract boolean isConcrete();
}