package net.sf.l2j.gameserver.geodata.pathfind;

import java.util.List;
import net.sf.l2j.Config;

public abstract class PathFinding
{
  public static PathFinding getInstance()
  {
    if (!Config.GEO_PATH_FINDING)
    {
      return GeoPathFinding.getInstance();
    }

    return CellPathFinding.getInstance();
  }
  public abstract boolean pathNodesExist(short paramShort);

  public abstract List<AbstractNodeLoc> findPath(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean);

  public short getNodePos(int geo_pos) {
    return (short)(geo_pos >> 3);
  }

  public short getNodeBlock(int node_pos)
  {
    return (short)(node_pos % 256);
  }

  public byte getRegionX(int node_pos)
  {
    return (byte)((node_pos >> 8) + 16);
  }

  public byte getRegionY(int node_pos)
  {
    return (byte)((node_pos >> 8) + 10);
  }

  public short getRegionOffset(byte rx, byte ry)
  {
    return (short)((rx << 5) + ry);
  }

  public int calculateWorldX(short node_x)
  {
    return -131072 + node_x * 128 + 48;
  }

  public int calculateWorldY(short node_y)
  {
    return -262144 + node_y * 128 + 48;
  }

  public String[] getStat()
  {
    return null;
  }
}