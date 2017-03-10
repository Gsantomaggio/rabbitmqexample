RabbitMQ cluster using [Docker compose](https://docs.docker.com/compose/), [Consul](https://www.consul.io) as back-end and HA proxy

```
git clone https://github.com/Gsantomaggio/rabbitmqexample.git .
cd cluster_docker_compose/consul_ha
docker-compose up
docker-compose scale rabbit=3
```

you can customize the `rabbitmq.config` inside `conf/rabbitmq.config`