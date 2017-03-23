package rabbitmq;

import java.io.IOException;
import java.util.Map;

import com.rabbitmq.client.AMQP.Queue.BindOk;
import com.rabbitmq.client.Channel;

public class ChannelCallableImplBindOk implements ChannelCallable<BindOk> {
	
	static final String USER_INBOXES_EXCHANGE = "user-inboxes";

	public String description;

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BindOk call(Channel channel) throws IOException {
		return null;
	}

	public BindOk call(Channel channel, String queue) throws IOException {
		return declareUserMessageQueue(queue, channel);
	}

	private BindOk declareUserMessageQueue(final String queue, final Channel channel) throws IOException {
		// survive a server restart
		boolean durable = true;
		// keep the queue
		boolean autoDelete = false;
		// can be consumed by another connection
		boolean exclusive = false;
		// no special arguments
		Map<String, Object> arguments = null;
		channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
		// bind the addressee's queue to the direct exchange
		String routingKey = queue;
		return channel.queueBind(queue, USER_INBOXES_EXCHANGE, routingKey);
	}

}
