#!/usr/bash
# 带filebeat的版本
kubectl delete -f proxy.yml
kubectl delete -n tool configmap lan-proxy-config
# 不带filebeat的基本版
# kubectl delete -f proxy-base.yml

kubectl get pod -n tool -o wide