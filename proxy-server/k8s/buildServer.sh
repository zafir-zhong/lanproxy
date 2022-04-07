#!/usr/bash
docker build -t tool/lan-proxy:1.0.01-release .
# 带filebeat的版本
kubectl create -n tool configmap lan-proxy-config --from-file=proxy-server-0.1/conf
kubectl apply -f proxy.yml
# 不带filebeat基本版
# kubectl apply -f proxy-base.yml
kubectl get pod -n tool -o wide
