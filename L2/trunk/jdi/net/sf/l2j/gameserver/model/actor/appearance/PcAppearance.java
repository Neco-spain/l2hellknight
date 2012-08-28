package net.sf.l2j.gameserver.model.actor.appearance;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PcAppearance
{
  private L2PcInstance _owner;
  private byte _face;
  private byte _hairColor;
  private byte _hairStyle;
  private boolean _sex;
  private String _visibleName;
  private boolean _invisible = false;

  private int _nameColor = 16777215;
  private int _old_nameColor = 16777215;
  private boolean _isMarried = false;

  private int _titleColor = 16777079;

  public PcAppearance(byte Face, byte HColor, byte HStyle, boolean Sex)
  {
    _face = Face;
    _hairColor = HColor;
    _hairStyle = HStyle;
    _sex = Sex;
  }

  public final byte getFace()
  {
    return _face;
  }

  public final void setFace(int value) {
    _face = (byte)value;
  }
  public final byte getHairColor() { return _hairColor; }

  public final void setHairColor(int value)
  {
    _hairColor = (byte)value;
  }
  public final byte getHairStyle() { return _hairStyle; }

  public final void setHairStyle(int value)
  {
    _hairStyle = (byte)value;
  }
  public final boolean getSex() { return _sex; }

  public final void setSex(boolean isfemale)
  {
    _sex = isfemale;
  }

  public void setInvisible() {
    _invisible = true;
  }

  public void setVisible()
  {
    _invisible = false;
  }

  public final void setVisibleName(String visibleName)
  {
    _visibleName = visibleName;
  }

  public final String getVisibleName()
  {
    if (_visibleName == null)
    {
      _visibleName = getOwner().getName();
    }
    return _visibleName;
  }

  public boolean getInvisible()
  {
    return _invisible;
  }

  public int getNameColor() {
    return _nameColor;
  }

  public int getNameColorForSave() {
    return _isMarried ? _old_nameColor : _nameColor;
  }

  public void setNameColor(int nameColor, boolean married)
  {
    if (married)
    {
      _old_nameColor = _nameColor;
      _isMarried = true;
    }
    _nameColor = nameColor;
  }

  public void setNameColor(int red, int green, int blue)
  {
    _nameColor = ((red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16));
  }

  public int getTitleColor()
  {
    return _titleColor;
  }

  public void setTitleColor(int titleColor)
  {
    _titleColor = titleColor;
  }

  public void setTitleColor(int red, int green, int blue)
  {
    _titleColor = ((red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16));
  }

  public L2PcInstance getOwner()
  {
    return _owner;
  }
}