package l2rt.extensions.network;

@SuppressWarnings("unchecked")
public interface IClientFactory<T extends MMOClient>
{
	public T create(MMOConnection<T> con);
}