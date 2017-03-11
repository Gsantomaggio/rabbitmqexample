Static RabbitMQ cluster using [Docker compose](https://docs.docker.com/compose/) and [Consul](consul.io/)

```
git clone https://github.com/Gsantomaggio/rabbitmqexample.git .
cd cluster_docker_compose/consul
sudo docker-compose up
```

It creates a cluster with 2 nodes and the UI enabled.

You can customize the `rabbitmq.config` inside `conf/rabbitmq.config`




