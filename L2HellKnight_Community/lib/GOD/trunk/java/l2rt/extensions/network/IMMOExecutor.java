package l2rt.extensions.network;

@SuppressWarnings("unchecked")
public interface IMMOExecutor<T extends MMOClient>
{
	public void execute(ReceivablePacket<T> packet);
}