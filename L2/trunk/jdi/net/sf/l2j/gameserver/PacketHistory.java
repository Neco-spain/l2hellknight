package net.sf.l2j.gameserver;

import java.util.Date;
import java.util.Map;
import javolution.xml.XMLFormat;
import javolution.xml.XMLFormat.InputElement;
import javolution.xml.XMLFormat.OutputElement;
import javolution.xml.stream.XMLStreamException;

class PacketHistory
{
  protected Map<Class, Long> _info;
  protected long _timeStamp;
  protected static final XMLFormat<PacketHistory> PACKET_HISTORY_XML = new XMLFormat()
  {
    public void read(XMLFormat.InputElement xml, PacketHistory packetHistory)
      throws XMLStreamException
    {
      packetHistory._timeStamp = xml.getAttribute("time-stamp", 0);
      packetHistory._info = ((Map)xml.get("info"));
    }

    public void write(PacketHistory packetHistory, XMLFormat.OutputElement xml)
      throws XMLStreamException
    {
      xml.setAttribute("time-stamp", new Date(packetHistory._timeStamp).toString());

      for (Class cls : packetHistory._info.keySet())
        xml.setAttribute(cls.getSimpleName(), (Long)packetHistory._info.get(cls));
    }
  };
}