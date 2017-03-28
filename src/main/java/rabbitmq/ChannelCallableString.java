package rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.Channel;

public class ChannelCallableString implements ChannelCallable<String>{
	
	public String description;

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}

	public String call(Channel channel) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
