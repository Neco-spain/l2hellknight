package l2m.gameserver.network.telnet;

import java.nio.charset.Charset;
import l2m.gameserver.Config;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

public class TelnetPipelineFactory
  implements ChannelPipelineFactory
{
  private final ChannelHandler handler;

  public TelnetPipelineFactory(ChannelHandler handler)
  {
    this.handler = handler;
  }

  public ChannelPipeline getPipeline()
    throws Exception
  {
    ChannelPipeline pipeline = Channels.pipeline();

    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
    pipeline.addLast("decoder", new StringDecoder(Charset.forName(Config.TELNET_DEFAULT_ENCODING)));
    pipeline.addLast("encoder", new StringEncoder(Charset.forName(Config.TELNET_DEFAULT_ENCODING)));

    pipeline.addLast("handler", handler);

    return pipeline;
  }
}