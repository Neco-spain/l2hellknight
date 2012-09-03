package l2rt.gameserver;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import java.util.Date;
import java.util.Map;

class PacketHistory
{
	protected static final XMLFormat<PacketHistory> PACKET_HISTORY_XML = new XMLFormat<PacketHistory>(PacketHistory.class){
		/**
		 * @see javolution.xml.XMLFormat#read(javolution.xml.XMLFormat.InputElement,java.lang.Object)
		 */
		@Override
		public void read(InputElement xml, PacketHistory packetHistory) throws XMLStreamException
		{
			packetHistory.timeStamp = xml.getAttribute("time-stamp", 0);
			packetHistory.info = xml.<Map<Class<?>, Long>> get("info");
		}

		/**
		 * @see javolution.xml.XMLFormat#write(java.lang.Object,javolution.xml.XMLFormat.OutputElement)
		 */
		@Override
		public void write(PacketHistory packetHistory, OutputElement xml) throws XMLStreamException
		{
			xml.setAttribute("time-stamp", new Date(packetHistory.timeStamp).toString());

			for(Class<?> cls : packetHistory.info.keySet())
				xml.setAttribute(cls.getSimpleName(), packetHistory.info.get(cls));
		}
	};

	public Map<Class<?>, Long> info;
	public long timeStamp;
}