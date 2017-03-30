package rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConnectorrabbitMQ {
	public static final String QUEUE_NAME = "hello.soulee.queue";

	public static void main(String[] args) throws IOException, TimeoutException {

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
		UserMessageManager userMessageManager = new UserMessageManager();
		userMessageManager.onApplicationStart();
		userMessageManager.onUserLogin(userId);
		userMessageManager.sendUserMessage(userId, "hello");
		userMessageManager.fetchUserMessages(userId);
		

		// Connection connection = factory.newConnection();
		// Channel channel = connection.createChannel();
		// channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		// String message = "Hello World!";
		// channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
		// System.out.println(" [x] Sent '" + message + "'");
		//
		// channel.close();
		// connection.close();
	}

}
