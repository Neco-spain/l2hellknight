package net.sf.l2j.gameserver.geodata.pathfind;

import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastList;
import net.sf.l2j.Config;

public class CellNodeBuffer
{
  @SuppressWarnings("unused")
private static final byte EAST = 1;
  @SuppressWarnings("unused")
private static final byte WEST = 2;
  @SuppressWarnings("unused")
private static final byte SOUTH = 4;
  @SuppressWarnings("unused")
private static final byte NORTH = 8;
  @SuppressWarnings("unused")
private static final byte NSWE_ALL = 15;
  @SuppressWarnings("unused")
private static final byte NSWE_NONE = 0;
  @SuppressWarnings("unused")
private static final int MAX_ITERATIONS = 3500;
  private final ReentrantLock _lock = new ReentrantLock();
  private final int _mapSize;
  private final CellNode[][] _buffer;
  private int _baseX = 0;
  private int _baseY = 0;

  private int _targetX = 0;
  private int _targetY = 0;
  private short _targetZ = 0;

  private long _timeStamp = 0L;
  private long _lastElapsedTime = 0L;

  private CellNode _current = null;

  public CellNodeBuffer(int size)
  {
    this._mapSize = size;
    this._buffer = new CellNode[this._mapSize][this._mapSize];
  }

  public final boolean lock()
  {
    return this._lock.tryLock();
  }

  public final CellNode findPath(int x, int y, short z, int tx, int ty, short tz)
  {
    this._timeStamp = System.currentTimeMillis();
    this._baseX = (x + (tx - x - this._mapSize) / 2);
    this._baseY = (y + (ty - y - this._mapSize) / 2);
    this._targetX = tx;
    this._targetY = ty;
    this._targetZ = tz;
    this._current = getNode(x, y, z);
    this._current.setCost(getCost(x, y, z, Config.HIGH_WEIGHT));

    for (int count = 0; count < 3500; count++)
    {
      if ((this._current.getLoc().getNodeX() == this._targetX) && (this._current.getLoc().getNodeY() == this._targetY) && (Math.abs(this._current.getLoc().getZ() - this._targetZ) < 64)) {
        return this._current;
      }
      getNeighbors();
      if (this._current.getNext() == null) return null;

      this._current = this._current.getNext();
    }
    return null;
  }

  public final void free()
  {
    this._current = null;

    for (int i = 0; i < this._mapSize; i++) {
      for (int j = 0; j < this._mapSize; j++)
      {
        CellNode node = this._buffer[i][j];
        if (node == null) continue; node.free();
      }
    }
    this._lock.unlock();
    this._lastElapsedTime = (System.currentTimeMillis() - this._timeStamp);
  }

  public final long getElapsedTime()
  {
    return this._lastElapsedTime;
  }

  public final FastList<CellNode> debugPath()
  {
    FastList<CellNode> result = new FastList<CellNode>();

    for (CellNode n = this._current; n.getParent() != null; n = (CellNode)n.getParent())
    {
      result.add(n);
      n.setCost(-n.getCost());
    }

    for (int i = 0; i < this._mapSize; i++) {
      for (int j = 0; j < this._mapSize; j++)
      {
        CellNode n = this._buffer[i][j];
        if ((n == null) || (!n.isInUse()) || (n.getCost() <= 0.0F))
          continue;
        result.add(n);
      }
    }
    return result;
  }

  private void getNeighbors()
  {
    short NSWE = ((NodeLoc)this._current.getLoc()).getNSWE();
    if (NSWE == 0) return;

    int x = this._current.getLoc().getNodeX();
    int y = this._current.getLoc().getNodeY();
    short z = this._current.getLoc().getZ();

    CellNode nodeE = null;
    CellNode nodeS = null;
    CellNode nodeW = null;
    CellNode nodeN = null;

    if ((NSWE & 0x1) != 0) nodeE = addNode(x + 1, y, z, false);

    if ((NSWE & 0x4) != 0) nodeS = addNode(x, y + 1, z, false);

    if ((NSWE & 0x2) != 0) nodeW = addNode(x - 1, y, z, false);

    if ((NSWE & 0x8) != 0) nodeN = addNode(x, y - 1, z, false);

    if (Config.ADVANCED_DIAGONAL_STRATEGY)
    {
      if ((nodeE != null) && (nodeS != null))
      {
        if (((((NodeLoc)nodeE.getLoc()).getNSWE() & 0x4) != 0) && ((((NodeLoc)nodeS.getLoc()).getNSWE() & 0x1) != 0)) {
          addNode(x + 1, y + 1, z, true);
        }
      }

      if ((nodeS != null) && (nodeW != null))
      {
        if (((((NodeLoc)nodeW.getLoc()).getNSWE() & 0x4) != 0) && ((((NodeLoc)nodeS.getLoc()).getNSWE() & 0x2) != 0)) {
          addNode(x - 1, y + 1, z, true);
        }
      }

      if ((nodeN != null) && (nodeE != null))
      {
        if (((((NodeLoc)nodeE.getLoc()).getNSWE() & 0x8) != 0) && ((((NodeLoc)nodeN.getLoc()).getNSWE() & 0x1) != 0)) {
          addNode(x + 1, y - 1, z, true);
        }
      }

      if ((nodeN != null) && (nodeW != null))
      {
        if (((((NodeLoc)nodeW.getLoc()).getNSWE() & 0x8) != 0) && ((((NodeLoc)nodeN.getLoc()).getNSWE() & 0x2) != 0))
          addNode(x - 1, y - 1, z, true);
      }
    }
  }

  private CellNode getNode(int x, int y, short z)
  {
    int aX = x - this._baseX;
    if ((aX < 0) || (aX >= this._mapSize)) return null;

    int aY = y - this._baseY;
    if ((aY < 0) || (aY >= this._mapSize)) return null;

    CellNode result = this._buffer[aX][aY];
    if (result == null)
    {
      result = new CellNode(new NodeLoc(x, y, z));
      this._buffer[aX][aY] = result;
    }
    else if (!result.isInUse())
    {
      result.setInUse();

      if (result.getLoc() != null) ((NodeLoc)result.getLoc()).set(x, y, z); else {
        result.setLoc(new NodeLoc(x, y, z));
      }
    }
    return result;
  }

  private CellNode addNode(int x, int y, short z, boolean diagonal)
  {
    CellNode newNode = getNode(x, y, z);
    if (newNode == null) return null;
    if (newNode.getCost() >= 0.0F) return newNode;

    short geoZ = newNode.getLoc().getZ();

    int stepZ = Math.abs(geoZ - this._current.getLoc().getZ());
    float weight = diagonal ? Config.DIAGONAL_WEIGHT : Config.LOW_WEIGHT;

    if ((((NodeLoc)newNode.getLoc()).getNSWE() != 15) || (stepZ > 16)) weight = Config.HIGH_WEIGHT;
    else if (isHighWeight(x + 1, y, geoZ)) weight = Config.MEDIUM_WEIGHT;
    else if (isHighWeight(x - 1, y, geoZ)) weight = Config.MEDIUM_WEIGHT;
    else if (isHighWeight(x, y + 1, geoZ)) weight = Config.MEDIUM_WEIGHT;
    else if (isHighWeight(x, y - 1, geoZ)) weight = Config.MEDIUM_WEIGHT;

    newNode.setParent(this._current);
    newNode.setCost(getCost(x, y, geoZ, weight));

    CellNode node = this._current;
    int count = 0;
    while ((node.getNext() != null) && (count < 14000))
    {
      count++;
      if (node.getNext().getCost() > newNode.getCost())
      {
        newNode.setNext(node.getNext());
        break;
      }
      node = node.getNext();
    }
    if (count == 14000) {
      System.err.println("Pathfinding: too long loop detected, cost:" + newNode.getCost());
    }
    node.setNext(newNode);

    return newNode;
  }

  private boolean isHighWeight(int x, int y, short z)
  {
    CellNode result = getNode(x, y, z);
    if (result == null) return true;

    if (((NodeLoc)result.getLoc()).getNSWE() != 15) return true;
    return Math.abs(result.getLoc().getZ() - z) > 16;
  }

  private double getCost(int x, int y, short z, float weight)
  {
    int dX = x - this._targetX;
    int dY = y - this._targetY;
    int dZ = z - this._targetZ;

    double result = Math.sqrt(dX * dX + dY * dY + dZ * dZ / 256);
    if (result > weight) result += weight;

    if (result > 3.402823466385289E+038D) result = 3.402823466385289E+038D;

    return result;
  }
}