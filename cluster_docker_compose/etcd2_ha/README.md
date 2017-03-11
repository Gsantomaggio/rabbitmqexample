RabbitMQ cluster using:

1. [Docker compose](https://docs.docker.com/compose/) 

2. [etcd2](https://github.com/coreos/etcd) as back-end  

3. [HA proxy](https://github.com/docker/dockercloud-haproxy)

```
git clone https://github.com/Gsantomaggio/rabbitmqexample.git .
cd cluster_docker_compose/etcd2_ha
docker-compose up
docker-compose scale rabbit=3
```

You can customize the `rabbitmq.config` inside `conf/rabbitmq.config`
