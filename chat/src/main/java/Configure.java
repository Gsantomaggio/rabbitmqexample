import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Created by gabriele on 18/09/2017.
 */
public class Configure {

    public static final String EXCHANGE_CHAT = "ex_chat";
    public static final String QUEUE_DATABASE = "queue_data_base";


    public static String get_queue_name_private(String user_name) {
        return "queue_private_" + user_name.toLowerCase();
    }

    public static String get_queue_name_public(String user_name) {
        return "queue_public_" + user_name.toLowerCase();
    }

    public static void init(Connection connection) throws Exception {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_CHAT, BuiltinExchangeType.TOPIC, true);
        channel.close();
    }


    public static void bind_user_queue(Connection connection, String user_name) throws Exception {
        Channel channel = connection.createChannel();
        channel.queueDeclare(get_queue_name_private(user_name), true, false, false, null);
        channel.queueDeclare(get_queue_name_public(user_name), true, false, false, null);
        channel.queueBind(get_queue_name_private(user_name), EXCHANGE_CHAT, user_name.toLowerCase());
        channel.queueBind(get_queue_name_public(user_name), EXCHANGE_CHAT, "public");
        channel.close();
    }





}
