package rabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.AMQP.Queue.BindOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

public class UserMessageManager {

	static final String USER_INBOXES_EXCHANGE = "user-inboxes";
	static final String MESSAGE_CONTENT_TYPE = "application/vnd.ccm.pmsg.v1+json";
	static final String MESSAGE_ENCODING = "UTF-8";

	@Inject
	RabbitMqManager rabbitMqManager;
	
	public UserMessageManager(RabbitMqManager rabbitMqManager){
		this.rabbitMqManager = rabbitMqManager;
	}

	public void onApplicationStart() {
		rabbitMqManager.call(new ChannelCallable<DeclareOk>() {
			@Override
			public String getDescription() {
				return "Declaring direct exchange: " + USER_INBOXES_EXCHANGE;
			}

			@Override
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
		});
	}

	public void onUserLogin(final long userId) {
		final String queue = getUserInboxQueue(userId);
		rabbitMqManager.call(new ChannelCallable<BindOk>() {
			@Override
			public String getDescription() {
				return "Declaring user queue: " + queue + ", binding it to exchange: " + USER_INBOXES_EXCHANGE;
			}

			@Override
			public BindOk call(final Channel channel) throws IOException {
				return declareUserMessageQueue(queue, channel);
			}
		});
	}

	public String getUserInboxQueue(long userId) {
		return "user-inbox." + userId;
	}

	public String sendUserMessage(final long userId, final String jsonMessage) {

		return rabbitMqManager.call(new ChannelCallable<String>() {
			@Override
			public String getDescription() {
				return "Sending message to user: " + userId;
			}

			@Override
			public String call(final Channel channel) throws IOException {
				String queue = getUserInboxQueue(userId);
				// it may not exist so declare it
				declareUserMessageQueue(queue, channel);
				String messageId = UUID.randomUUID().toString();
				BasicProperties props = new BasicProperties.Builder().contentType(MESSAGE_CONTENT_TYPE)
						.contentEncoding(MESSAGE_ENCODING).messageId(messageId).deliveryMode(2).build();
				String routingKey = queue;
				// publish the message to the direct exchange
				channel.basicPublish(USER_INBOXES_EXCHANGE, routingKey, props, jsonMessage.getBytes(MESSAGE_ENCODING));
				return messageId;
			}
		});
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

	public List<String> fetchUserMessages(final long userId) {
		return rabbitMqManager.call(new ChannelCallable<List<String>>() {
			@Override
			public String getDescription() {
				return "Fetching messages for user: " + userId;
			}

			@Override
			public List<String> call(final Channel channel) throws IOException {
				List<String> messages = new ArrayList<>();
				String queue = getUserInboxQueue(userId);
				boolean autoAck = true;
				GetResponse getResponse;
				while ((getResponse = channel.basicGet(queue, autoAck)) != null) {
					final String contentEncoding = getResponse.getProps().getContentEncoding();
					messages.add(new String(getResponse.getBody(), contentEncoding));
				}
				return messages;
			}
		});
	}
	
	public static void main(String[] args) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("quest");
		factory.setPassword("quest");
		factory.setVirtualHost("/");
		factory.setHost("localhost");
		factory.setPort(5672);

		RabbitMqManager connectionManager = new RabbitMqManager(factory);
		connectionManager.start();
		connectionManager.createChannel();
		long userId = 1;
		UserMessageManager userMessageManager = new UserMessageManager(connectionManager);
		userMessageManager.onUserLogin(userId);
		userMessageManager.onApplicationStart();
		userMessageManager.sendUserMessage(userId, "hello");
		userMessageManager.fetchUserMessages(userId);
	}
	
}
