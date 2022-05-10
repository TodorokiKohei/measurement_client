#!/bin/sh

# Stream生成
nats -s nats://nats-1,nats://nats-2,nats://nats-3 str add --config /tmp/configs/stream.json

# Consumer生成
nats -s nats://nats-1,nats://nats-2,nats://nats-3 con add STREAM --config /tmp/configs/pull-consumer.json