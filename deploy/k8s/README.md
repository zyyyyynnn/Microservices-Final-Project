# MallCloud K8s 部署快速参考
# 适用：minikube 演示 / 真实 K8s 集群

## 1. 准备
```bash
minikube start --driver=docker --cpus=4 --memory=8192
minikube addons enable ingress
minikube addons enable metrics-server
eval $(minikube docker-env)
```

## 2. 批量构建镜像
```bash
bash scripts/build-images.sh
# 或 PowerShell
.\scripts\build-images.ps1
```

## 3. 部署中间件
```bash
kubectl apply -f deploy/k8s/00-namespace.yaml
kubectl apply -f deploy/k8s/01-mysql.yaml
kubectl apply -f deploy/k8s/02-redis.yaml
kubectl apply -f deploy/k8s/03-nacos.yaml
kubectl apply -f deploy/k8s/04-rocketmq.yaml
kubectl apply -f deploy/k8s/05-elasticsearch.yaml
kubectl apply -f deploy/k8s/06-seata.yaml
```

## 4. 部署业务服务
```bash
for f in deploy/k8s/services/*.yaml; do kubectl apply -f $f; done
```

## 5. 部署 Ingress
```bash
kubectl apply -f deploy/k8s/ingress.yaml
```

## 6. 访问
```bash
minikube ip
# 添加 host：<minikube-ip> mallcloud.local
# 浏览器打开 http://mallcloud.local
```

## 7. 扩缩容
```bash
kubectl scale deployment mall-seckill --replicas=5 -n mallcloud
kubectl scale deployment mall-order --replicas=5 -n mallcloud
```

## 8. 监控
```bash
# K8s Dashboard
minikube dashboard

# 业务 Dashboard
kubectl port-forward svc/mall-sentinel 8080:8080 -n mallcloud
kubectl port-forward svc/mall-nacos 8848:8848 -n mallcloud
kubectl port-forward svc/mall-zipkin 9411:9411 -n mallcloud
```

## 9. 清理
```bash
kubectl delete namespace mallcloud
```
