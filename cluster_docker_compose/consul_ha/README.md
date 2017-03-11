Dynamic RabbitMQ cluster using:

1. [Docker compose](https://docs.docker.com/compose/) 

2. [Consul](https://www.consul.io) as back-end and 

3. [HA proxy](https://github.com/docker/dockercloud-haproxy)


```
git clone https://github.com/Gsantomaggio/rabbitmqexample.git .
cd cluster_docker_compose/consul_ha
docker-compose up
docker-compose scale rabbit=3
```

you can customize the `rabbitmq.config` inside `conf/rabbitmq.config`
