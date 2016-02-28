# /bin/python
import pika
from threading import Thread
import os
import sys
import datetime

TEAM_EXCHANGE_NAME = "my_company"
ROOMS = ["tech.programming", "tech.networking", "marketing"]


def publish_message(channel, routing_key, message, is_private):
    t = datetime.datetime.now()
    headers = {
        'sender_user': "simulation user",
        'sent': t.strftime('%m/%d/%Y %H:%M:%S'),
        'is_private': is_private

    }

    properties = pika.BasicProperties(
            headers=headers,
            delivery_mode=2
    )
    print "sending.. " + message
    channel.publish(exchange=TEAM_EXCHANGE_NAME, routing_key=routing_key, properties=properties,
                    body=message)


def main(host, port, user, password, vhost):
    print "Welcome to " + TEAM_EXCHANGE_NAME + " - Rooms: " + str(ROOMS)
    print ""

    # setup rabbitmq connection
    credentials = pika.PlainCredentials(user, password)
    connection = pika.BlockingConnection(pika.ConnectionParameters(host, port, vhost, credentials))

    channel = connection.channel()
    channel.exchange_declare(exchange=TEAM_EXCHANGE_NAME, exchange_type="topic")

    for i in range(1, 10):
        publish_message(channel, "tech.programming", "Simulation message n:" + str(i), False)

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
