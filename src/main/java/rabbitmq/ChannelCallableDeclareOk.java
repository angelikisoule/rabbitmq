package rabbitmq;

import java.io.IOException;
import java.util.Map;

import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.Channel;

public class ChannelCallableDeclareOk implements ChannelCallable<DeclareOk>{
	
	static final String USER_INBOXES_EXCHANGE = "user-inboxes";
	
	public String description;

	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description){this.description = description;}

	public DeclareOk call(final Channel channel) throws IOException {
		String exchange = USER_INBOXES_EXCHANGE;
		String type = "direct";
		// survive a server restart
		boolean durable = true;
		// keep it even if nobody is using it
		boolean autoDelete = false;
		// no special arguments
		Map<String, Object> arguments = null;
		return channel.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
	}

}
