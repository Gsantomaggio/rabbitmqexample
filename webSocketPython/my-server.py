#!/usr/bin/env python
import os
import tornado.ioloop
import tornado.web
import tornado.websocket
import pika
from threading import Thread
import logging
logging.basicConfig()

clients = []
connection = pika.BlockingConnection()
print 'Connected:localhost'
channel = connection.channel()

def threaded_rmq():
	channel.queue_declare(queue="my_queue")
	print 'consumer ready, on my_queue'
	channel.basic_consume(consumer_callback, queue="my_queue", no_ack=True) 
	channel.start_consuming()
	

def disconnect_to_rabbitmq():
	channel.stop_consuming()
	connection.close()
	print 'Disconnected from Rabbitmq'

	
def consumer_callback(ch, method, properties, body):
		print " [x] Received %r" % (body,)
		for itm in clients:
			itm.write_message(body)

class SocketHandler(tornado.websocket.WebSocketHandler):
	def open(self):
		print "WebSocket opened"
		clients.append(self)
	
	def on_close(self):
		print "WebSocket closed"
		clients.remove(self)


class MainHandler(tornado.web.RequestHandler):
    def get(self):
    	print 'get page'
        self.render("websocket.html")
	

application = tornado.web.Application([
    (r'/ws', SocketHandler),
    (r"/", MainHandler),
])

def startTornado():
    application.listen(8888)
    tornado.ioloop.IOLoop.instance().start()

def stopTornado():
    tornado.ioloop.IOLoop.instance().stop()

if __name__ == "__main__":
	threadRMQ = Thread(target=threaded_rmq)
	threadRMQ.start()

   	threadTornado = Thread(target=startTornado)
   	threadTornado.start()
	try:
		raw_input("Press enter to stop\n")
	except SyntaxError:
		pass
	try:
		disconnect_to_rabbitmq()
	except Exception, e:
		pass
	stopTornado(); 
	
	print 'end'
	
	