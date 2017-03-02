Static RabbitMQ cluster using [Docker compose](https://docs.docker.com/compose/)

```
git clone https://github.com/Gsantomaggio/rabbitmqexample.git .
cd cluster_docker_compose/consul
sudo docker-compose up
```

It creates a cluster with 2 nodes and the UI enabled. 
you can customize the `rabbitmq.config` inside `conf/rabbitmq.config`




