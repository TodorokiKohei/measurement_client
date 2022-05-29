#!/bin/sh

# JetStreamが有効になっていなければ終了する
nats -s nats://nats-1 account info | sed -E "s/^ *//g" | grep "JetStream is not supported"
if [ "${?}" = 1 ]; then
	return 1;
fi

# Stream生成
nats -s nats://nats-1 str add STREAM --config /tmp/configs/stream.json

# Consumer生成
nats -s nats://nats-1 con add STREAM --config /tmp/configs/pull-consumer.json
nats -s nats://nats-1 con add STREAM --config /tmp/configs/push-consumer.json

while :; do sleep 10; done