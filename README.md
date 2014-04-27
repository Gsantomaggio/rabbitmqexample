rabbitmqexample
===============

Here some RabbitMQ example. 
1. webSocketPython : How to redicted RabbitMQ to web-page using http://www.tornadoweb.org/. To get ready you need pika and tornado in this way: 
pip install pika 
pip install tornado 
Then execute my-server.py and open a web page on http://localhost:8888/ 
Try to send a message to "my_queue" on rabbitmq (or simply use sendtest.py)  , it will be redirected to all connected clients. 


