#!/usr/bin/env python

import pika
import logging
logging.basicConfig()

connection = pika.BlockingConnection()
print 'Connected:localhost'
channel = connection.channel()
channel.queue_declare(queue="my_queue")
channel.basic_publish(exchange='',
                      routing_key='my_queue',
                      body='Test Message')
connection.close()
