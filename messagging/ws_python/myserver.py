#!/usr/bin/env python

# How to send an web socket message using http://www.tornadoweb.org/.
# to get ready you have to install pika and tornado
# 1. pip install pika
# 2. pip install tornado

import os
import tornado.ioloop
import tornado.web
import tornado.websocket
import pika
from threading import Thread
import logging

logging.basicConfig(level=logging.INFO)

# web socket clients connected.
clients = []
TEAM_EXCHANGE_NAME = "my_company"
connection = pika.BlockingConnection()
logging.info('Connected:localhost')
channel = connection.channel()


def threaded_rmq():
    channel.exchange_declare(exchange=TEAM_EXCHANGE_NAME, exchange_type="topic", durable=True)
    q_declare = channel.queue_declare(exclusive=True, auto_delete=True)
    channel.queue_bind(exchange=TEAM_EXCHANGE_NAME,
                       queue=q_declare.method.queue,
                       routing_key="#")

    logging.info('consumer ready: ' + q_declare.method.queue)
    channel.basic_consume(consumer_callback, queue=q_declare.method.queue, no_ack=True)
    channel.start_consuming()


def disconnect_from_rabbitmq():
    def kill():
        channel.stop_consuming()

    connection.add_timeout(0, kill)
    logging.info('Disconnected from RabbitMQ')


def consumer_callback(ch, method, properties, body):
    logging.info("[x] Received %r" % (body,))
    msg = "%s ; %s ; %s  ; %s " % (
        str(method.exchange), str(method.routing_key), body, properties.headers)

    for itm in clients:
        itm.write_message(msg)


class SocketHandler(tornado.websocket.WebSocketHandler):
    def data_received(self, chunk):
        pass

    def on_message(self, message):
        pass

    def open(self):
        logging.info('WebSocket opened')
        clients.append(self)

    def on_close(self):
        logging.info('WebSocket closed')
        clients.remove(self)


class StompHandler(tornado.web.RequestHandler):
    def data_received(self, chunk):
        pass

    def get(self):
        self.render("static/webstomp.html")


class MainHandler(tornado.web.RequestHandler):
    def data_received(self, chunk):
        pass

    def get(self):
        self.render("static/monitoring.html")


settings = {'debug': True}

static_path = os.path.join(os.path.dirname(__file__), 'static')

application = tornado.web.Application([
    (r'/static/(.*)', tornado.web.StaticFileHandler, {'path': static_path}),
    (r'/ws', SocketHandler),
    (r"/stomp", StompHandler),
    (r"/", MainHandler)
], **settings)


def start_tornado():
    application.listen(8888)
    tornado.ioloop.IOLoop.instance().start()


def stop_tornado():
    tornado.ioloop.IOLoop.instance().stop()


if __name__ == "__main__":
    logging.info('Starting thread RabbitMQ')
    threadRMQ = Thread(target=threaded_rmq)
    threadRMQ.start()

    logging.info('Starting thread Tornado')

    threadTornado = Thread(target=start_tornado)
    threadTornado.start()
    try:
        raw_input("Server ready. Press enter to stop\n")
    except SyntaxError:
        pass
    try:
        logging.info('Disconnecting from RabbitMQ..')
        disconnect_from_rabbitmq()
    except Exception, e:
        logging.info('See you...aaaaa')
        pass
    stop_tornado()

    logging.info('See you...')
