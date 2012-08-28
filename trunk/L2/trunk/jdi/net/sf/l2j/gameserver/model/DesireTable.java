package net.sf.l2j.gameserver.model;

import java.util.Map;
import javolution.util.FastMap;

public class DesireTable
{
  public static final DesireType[] DEFAULT_DESIRES = { DesireType.FEAR, DesireType.DISLIKE, DesireType.HATE, DesireType.DAMAGE };
  private Map<L2Object, Desires> _objectDesireTable;
  private Desires _generalDesires;
  private DesireType[] _desireTypes;

  public DesireTable(DesireType[] desireList)
  {
    _desireTypes = desireList;
    _objectDesireTable = new FastMap();
    _generalDesires = new Desires(_desireTypes);
  }

  public float getDesireValue(DesireType type)
  {
    return _generalDesires.getDesireValue(type).getValue();
  }

  public float getDesireValue(L2Object object, DesireType type)
  {
    Desires desireList = (Desires)_objectDesireTable.get(object);
    if (desireList == null) return 0.0F;
    return desireList.getDesireValue(type).getValue();
  }

  public void addDesireValue(DesireType type, float value)
  {
    _generalDesires.addValue(type, value);
  }

  public void addDesireValue(L2Object object, DesireType type, float value)
  {
    Desires desireList = (Desires)_objectDesireTable.get(object);
    if (desireList != null) desireList.addValue(type, value);
  }

  public void createDesire(DesireType type)
  {
    _generalDesires.createDesire(type);
  }

  public void deleteDesire(DesireType type)
  {
    _generalDesires.deleteDesire(type);
  }

  public void createDesire(L2Object object, DesireType type)
  {
    Desires desireList = (Desires)_objectDesireTable.get(object);
    if (desireList != null) desireList.createDesire(type);
  }

  public void deleteDesire(L2Object object, DesireType type)
  {
    Desires desireList = (Desires)_objectDesireTable.get(object);
    if (desireList != null) desireList.deleteDesire(type);
  }

  public void addKnownObject(L2Object object)
  {
    if (object != null)
    {
      addKnownObject(object, new DesireType[] { DesireType.DISLIKE, DesireType.FEAR, DesireType.DAMAGE, DesireType.HATE });
    }
  }

  public void addKnownObject(L2Object object, DesireType[] desireList)
  {
    if (object != null) _objectDesireTable.put(object, new Desires(desireList));
  }

  class Desires
  {
    private Map<DesireTable.DesireType, DesireTable.DesireValue> _desireTable;

    public Desires(DesireTable.DesireType[] desireList)
    {
      _desireTable = new FastMap();

      for (DesireTable.DesireType desire : desireList)
      {
        _desireTable.put(desire, new DesireTable.DesireValue(DesireTable.this));
      }
    }

    public DesireTable.DesireValue getDesireValue(DesireTable.DesireType type)
    {
      return (DesireTable.DesireValue)_desireTable.get(type);
    }

    public void addValue(DesireTable.DesireType type, float value)
    {
      DesireTable.DesireValue temp = getDesireValue(type);
      if (temp != null)
      {
        temp.addValue(value);
      }
    }

    public void createDesire(DesireTable.DesireType type)
    {
      _desireTable.put(type, new DesireTable.DesireValue(DesireTable.this));
    }

    public void deleteDesire(DesireTable.DesireType type)
    {
      _desireTable.remove(type);
    }
  }

  class DesireValue
  {
    private float _value;

    DesireValue()
    {
      this(Float.valueOf(0.0F));
    }

    DesireValue(Float pValue)
    {
      _value = pValue.floatValue();
    }

    public void addValue(float pValue)
    {
      _value += pValue;
    }

    public float getValue()
    {
      return _value;
    }
  }

  public static enum DesireType
  {
    FEAR, DISLIKE, HATE, DAMAGE;
  }
}