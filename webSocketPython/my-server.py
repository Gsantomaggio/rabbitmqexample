#!/usr/bin/env python

# How to send an web socket message using http://www.tornadoweb.org/.
# to get ready you have to install pika and tornado
# 1. pip install pika
# 2. pip install tornado
import _thread
import asyncio
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

connection = pika.BlockingConnection()
logging.info('Connected:localhost')
channel = connection.channel()


def get_connection():
    credentials = pika.PlainCredentials('guest', 'guest')
    conn = pika.BlockingConnection(pika.ConnectionParameters(host="localhost", port=5672,
                                                             virtual_host="/",
                                                             credentials=credentials))
    return conn


def callback(ch, method, properties, body):
    print(" [x] %s" % (body))
    for itm in clients:
        itm.write_message(body)


def start_consumers():
    asyncio.set_event_loop(asyncio.new_event_loop())
    channel = get_connection().channel()
    channel.queue_declare(queue="my_queue")
    channel.basic_consume(
        queue="my_queue",
        on_message_callback=callback,
        auto_ack=True)

    channel.start_consuming()


def disconnect_to_rabbitmq():
    channel.stop_consuming()
    connection.close()
    logging.info('Disconnected from Rabbitmq')


class SocketHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        logging.info('WebSocket opened')
        clients.append(self)

    def on_close(self):
        logging.info('WebSocket closed')
        clients.remove(self)


class MainHandler(tornado.web.RequestHandler):
    def get(self):
        self.render("websocket.html")


def make_app():
    return tornado.web.Application([
        (r'/ws', SocketHandler),
        (r"/", MainHandler),
    ])


class WebServer(tornado.web.Application):

    def __init__(self):
        handlers = [(r'/ws', SocketHandler),
                    (r"/", MainHandler), ]
        settings = {'debug': True}
        super().__init__(handlers, **settings)

    def run(self, port=8888):
        self.listen(port)
        tornado.ioloop.IOLoop.instance().start()


class TestHandler(tornado.web.RequestHandler):
    def get(self):
        self.write("test success")


ws = WebServer()


def start_server():
    asyncio.set_event_loop(asyncio.new_event_loop())
    ws.run()


if __name__ == "__main__":

    logging.info('Starting thread Tornado')
    threadC = Thread(target=start_consumers)
    threadC.start()

    from threading import Thread

    t = Thread(target=start_server, args=())
    t.daemon = True
    t.start()

    t.join()
    try:
        input("Server ready. Press enter to stop\n")
    except SyntaxError:
        pass
    try:
        logging.info('Disconnecting from RabbitMQ..')
        disconnect_to_rabbitmq()
    except Exception:
        pass
    stopTornado();

    logging.info('See you...')
