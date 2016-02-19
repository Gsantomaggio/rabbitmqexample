# /bin/python
import pika
from threading import Thread
import os
import sys
import time

TEAM_EXCHANGE_NAME = "my_company"
ROOMS = ["tech.code", "tech.network", "marketing"]


def cls():
    os.system('cls' if os.name == 'nt' else 'clear')


class ConsoleInfo:
    def __init__(self):
        self.user_name = ""
        self.messages = []
        self.routing_keys = []


consoleInfo = ConsoleInfo()


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
    is_private = 'Yes' if method.routing_key == consoleInfo.user_name else 'No'
    msg = "%s - %s - %s    - %s - %s" % (
        str(method.exchange), str(method.routing_key), is_private, body, properties.headers)
    consoleInfo.messages.append(msg)
    print_console()


def start_rabbitmq_subscriber(channel, input_user_name, routing_keys):
    thread_rmq = Thread(target=threaded_rmq,
                        args=(input_user_name, channel, routing_keys))
    thread_rmq.start()


def print_console():
    cls()
    print "User name: " + consoleInfo.user_name
    print ""
    print "Team " + TEAM_EXCHANGE_NAME + " - Rooms: " + str(ROOMS)
    print ""
    print "Joined to : " + str(consoleInfo.routing_keys)
    print ""
    print "==================== Messages Received ======================================="
    print ""
    print "Exchange    Routing Key  Priv.  Body - Message Header"
    print "------------------------------------------------------------------------------"
    for message in consoleInfo.messages:
        print message
    print ""
    print "=============================================================================="
    print "* press q for terminate \n* return to send a message \n"


def main(host, user, password):
    print "Welcome to " + TEAM_EXCHANGE_NAME + " - Rooms: " + str(ROOMS)
    print ""
    consoleInfo.user_name = raw_input("Insert username: \n")
    for room in ROOMS:
        if raw_input("join to " + room + "? y/n \n") == "y":
            consoleInfo.routing_keys.append(room)

    credentials = pika.PlainCredentials(user, password)
    connection = pika.BlockingConnection(pika.ConnectionParameters(host, 5672, "/", credentials))

    print_console()
    channel = connection.channel()
    start_rabbitmq_subscriber(channel, consoleInfo.user_name,
                              consoleInfo.routing_keys)
    publish_channel = connection.channel()
    while raw_input() != "q":
        headers = {
            'sender_user': consoleInfo.user_name,
            'created': int(time.time())
        }

        properties = pika.BasicProperties(
                headers=headers
        )
        message_to_send = raw_input("Message to send: \n")
        sent = False
        if raw_input("private message ?") == "y":
            to = raw_input("user: \n")
            publish_channel.publish(exchange=TEAM_EXCHANGE_NAME, routing_key=to, properties=properties,
                                    body=message_to_send)
            pass
        else:
            for routing_key in consoleInfo.routing_keys:
                if raw_input("send to " + routing_key + " ?") == "y":
                    publish_channel.publish(exchange=TEAM_EXCHANGE_NAME, routing_key=routing_key, properties=properties,
                                            body=message_to_send)
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
    rabbitmq_user = sys.argv[2];
    rabbitmq_password = sys.argv[3];
    main(rabbitmq_host, rabbitmq_user, rabbitmq_password)
