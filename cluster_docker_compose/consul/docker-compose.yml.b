version: "2"
services:
  rabbit_node_1:
    environment:
      - RABBITMQ_ERLANG_COOKIE='secret_cookie'
    networks:
      - back
    hostname: rabbit_node_1
    image: gsantomaggio/rabbitmq-autocluster
    ports:
      - "15672:15672"
      - "5672:5672"
    tty: true
    volumes:
      - rabbit1:/var/lib/rabbitmq
      - ./conf/:/etc/rabbitmq/
  rabbit_node_2:
    environment:
      - RABBITMQ_ERLANG_COOKIE='secret_cookie'
    networks:
      - back
    hostname: rabbit_node_2
    image: gsantomaggio/rabbitmq-autocluster
    depends_on:
      - rabbit_node_1
    ports:
      - "15673:15672"
      - "5673:5672"
    tty: true
    volumes:
      - rabbit2:/var/lib/rabbitmq
      - ./conf_2/:/etc/rabbitmq/
 
volumes:
  rabbit1:
    driver: local
  rabbit2:
    driver: local

networks:
  back:
