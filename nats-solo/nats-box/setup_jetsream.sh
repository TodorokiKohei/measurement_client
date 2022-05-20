#!/bin/sh

# Stream生成
nats -s nats://nats-1 str add --config /tmp/configs/stream.json

# Consumer生成
# nats -s nats://nats-1 con add STREAM --config /tmp/configs/pull-consumer.json

while :; do sleep 10; done