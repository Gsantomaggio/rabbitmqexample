# /bin/python
import pika
from threading import Thread
import os
import sys
import datetime

TEAM_EXCHANGE_NAME = "my_company"
ROOMS = ["tech.programming", "tech.networking", "marketing"]


def cls():
    os.system('cls' if os.name == 'nt' else 'clear')


class ConsoleInfo:
    def __init__(self):
        self.user_name = ""
        self.messages = []
        self.routing_keys = []
        self.broker_info = ""


console_info = ConsoleInfo()


def threaded_rmq(user_name, consume_channel, routing_keys):
    q_declare = consume_channel.queue_declare(exclusive=True, auto_delete=True)
    consume_channel.queue_bind(exchange=TEAM_EXCHANGE_NAME,
                               queue=q_declare.method.queue,
                               routing_key=user_name)

    for routing_key in routing_keys:
        consume_channel.queue_bind(exchange=TEAM_EXCHANGE_NAME,
                                   queue=q_declare.method.queue,
                                   routing_key=routing_key)

    consume_channel.basic_consume(on_message, queue=q_declare.method.queue,
                                  no_ack=True)
    consume_channel.start_consuming()


def on_message(ch, method, properties, body):
    msg = "%s - %s - %s   - %s" % (
        str(method.exchange), str(method.routing_key), body, properties.headers)
    console_info.messages.append(msg)
    print_console()


def start_rabbitmq_subscriber(channel, input_user_name, routing_keys):
    thread_rmq = Thread(target=threaded_rmq,
                        args=(input_user_name, channel, routing_keys))
    thread_rmq.start()


def print_console():
    cls()
    print "Server:" + console_info.broker_info
    print ""
    print "User name: " + console_info.user_name
    print ""
    print "Team " + TEAM_EXCHANGE_NAME + " - Rooms: " + str(ROOMS)
    print ""
    print "Joined to : " + str(console_info.routing_keys)
    print ""
    print "==================== Messages Received ======================================="
    print ""
    print "Exchange    Routing Key    Body   Message Header"
    print "------------------------------------------------------------------------------"
    for message in console_info.messages:
        print message
    print ""
    print "=============================================================================="
    print "- press q to terminate \n- return to send a message \n"


def publish_message(channel, routing_key, message, is_private):
    t = datetime.datetime.now()
    headers = {
        'sender_user': console_info.user_name,
        'sent': t.strftime('%m/%d/%Y %H:%M:%S'),
        'is_private': is_private

    }

    properties = pika.BasicProperties(
            headers=headers,
            delivery_mode=2
    )
    channel.publish(exchange=TEAM_EXCHANGE_NAME, routing_key=routing_key, properties=properties,
                    body=message)


def main(host, port, user, password, vhost):
    print "Welcome to " + TEAM_EXCHANGE_NAME + " - Rooms: " + str(ROOMS)
    print ""

    console_info.broker_info = host
    console_info.user_name = raw_input("Insert username: \n")
    for room in ROOMS:
        if raw_input("join to " + room + "? y/n \n") == "y":
            console_info.routing_keys.append(room)

    # setup rabbitmq connection
    credentials = pika.PlainCredentials(user, password)
    connection = pika.BlockingConnection(pika.ConnectionParameters(host, port, vhost, credentials))

    channel = connection.channel()
    channel.exchange_declare(exchange=TEAM_EXCHANGE_NAME, exchange_type="topic", durable=True)
    # start subscriber
    start_rabbitmq_subscriber(channel, console_info.user_name,
                              console_info.routing_keys)

    publish_channel = connection.channel()

    print_console()
    while raw_input() != "q":

        message_to_send = raw_input("Message to send: \n")
        sent = False
        if raw_input("private message ?") == "y":
            to = raw_input("user: \n")
            publish_message(publish_channel, to, message_to_send, True)
            pass
        else:
            for routing_key in console_info.routing_keys:
                if raw_input("send to " + routing_key + " ?") == "y":
                    publish_message(publish_channel, routing_key, message_to_send, False)
                    sent = True
                    break
        if not sent:
            print_console()

    def kill():
        channel.stop_consuming()

    connection.add_timeout(0, kill)
    print "Goodbye"


if __name__ == "__main__":
    print 'Argument List:', str(sys.argv)
    rabbitmq_host = sys.argv[1];
    rabbitmq_port = int(sys.argv[2]);
    rabbitmq_user = sys.argv[3];
    rabbitmq_password = sys.argv[4];
    rabbitmq_vhost = sys.argv[5];
    main(rabbitmq_host, rabbitmq_port, rabbitmq_user, rabbitmq_password, rabbitmq_vhost)
