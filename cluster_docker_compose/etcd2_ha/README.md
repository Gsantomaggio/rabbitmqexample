RabbitMQ cluster using [Docker compose](https://docs.docker.com/compose/), [etcd2](https://github.com/coreos/etcd) as back-end and HA proxy

```
git clone https://github.com/Gsantomaggio/rabbitmqexample.git .
cd cluster_docker_compose/etcd2_ha
docker-compose up
docker-compose scale rabbit=3
```

It creates a cluster with 2 nodes and the UI enabled. 
you can customize the `rabbitmq.config` inside `conf/rabbitmq.config`