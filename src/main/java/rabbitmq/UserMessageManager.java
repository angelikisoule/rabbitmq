package rabbitmq;

import java.io.IOException;

import javax.inject.Inject;

import com.rabbitmq.client.AMQP.Exchange.BindOk;
import com.rabbitmq.client.Channel;

public class UserMessageManager {
	
	static final String USER_INBOXES_EXCHANGE = "user-inboxes";

	@Inject
	RabbitMqManager rabbitMqManager;

	public void onApplicationStart() {
		ChannelCallableDeclareOk callableImpl = new ChannelCallableDeclareOk();
		callableImpl.setDescription("Declaring direct exchange: " + USER_INBOXES_EXCHANGE);
		rabbitMqManager.call(callableImpl);
	}

	public void onUserLogin(final long userId){
	       final String queue = getUserInboxQueue(userId);
	       ChannelCallableImplBindOk bindOk = new ChannelCallableImplBindOk();
	       bindOk.setDescription("Declaring user queue: " + queue + ", binding it to exchange: " + USER_INBOXES_EXCHANGE);
	       rabbitMqManager.call(bindOk);
	               
	}

}
