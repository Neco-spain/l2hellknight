package org.mmocore.network;

import java.nio.ByteOrder;

public class SelectorConfig<T extends MMOClient>
{
  private final UDPHeaderHandler<T> UDP_HEADER_HANDLER;
  private final IPacketHandler<T> UDP_PACKET_HANDLER;
  private final TCPHeaderHandler<T> TCP_HEADER_HANDLER;
  private final IPacketHandler<T> TCP_PACKET_HANDLER;
  private int READ_BUFFER_SIZE = 65536;
  private int WRITE_BUFFER_SIZE = 65536;
  private int MAX_SEND_PER_PASS = 1;
  private int SLEEP_TIME = 10;
  private HeaderSize HEADER_TYPE = HeaderSize.SHORT_HEADER;
  private int HELPER_BUFFER_SIZE = 65536;
  private int HELPER_BUFFER_COUNT = 20;
  private ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

  public SelectorConfig(UDPHeaderHandler<T> udpHeaderHandler, IPacketHandler<T> udpPacketHandler, TCPHeaderHandler<T> tcpHeaderHandler, IPacketHandler<T> tcpPacketHandler)
  {
    UDP_HEADER_HANDLER = udpHeaderHandler;
    UDP_PACKET_HANDLER = udpPacketHandler;

    TCP_HEADER_HANDLER = tcpHeaderHandler;
    TCP_PACKET_HANDLER = tcpPacketHandler;
  }

  public int getReadBufferSize()
  {
    return READ_BUFFER_SIZE;
  }

  public int getWriteBufferSize()
  {
    return WRITE_BUFFER_SIZE;
  }

  public int getHelperBufferSize()
  {
    return HELPER_BUFFER_SIZE;
  }

  public int getHelperBufferCount()
  {
    return HELPER_BUFFER_COUNT;
  }

  public ByteOrder getByteOrder()
  {
    return BYTE_ORDER;
  }

  public HeaderSize getHeaderType()
  {
    return HEADER_TYPE;
  }

  public UDPHeaderHandler<T> getUDPHeaderHandler()
  {
    return UDP_HEADER_HANDLER;
  }

  public IPacketHandler<T> getUDPPacketHandler()
  {
    return UDP_PACKET_HANDLER;
  }

  public TCPHeaderHandler<T> getTCPHeaderHandler()
  {
    return TCP_HEADER_HANDLER;
  }

  public IPacketHandler<T> getTCPPacketHandler()
  {
    return TCP_PACKET_HANDLER;
  }

  public void setMaxSendPerPass(int maxSendPerPass)
  {
    MAX_SEND_PER_PASS = maxSendPerPass;
  }

  public int getMaxSendPerPass()
  {
    return MAX_SEND_PER_PASS;
  }

  public void setSelectorSleepTime(int sleepTime)
  {
    SLEEP_TIME = sleepTime;
  }

  public int getSelectorSleepTime()
  {
    return SLEEP_TIME;
  }

  public static enum HeaderSize
  {
    BYTE_HEADER, 
    SHORT_HEADER, 
    INT_HEADER;
  }
}