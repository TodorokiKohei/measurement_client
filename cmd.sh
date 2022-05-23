#!/bin/bash

check_current(){
	home_dir="$HOME/measurement_client"
	if [[ "$(pwd)" != "${home_dir}" ]]; then
		echo "Pleasse move to ${home_dir}"
		return 1
	fi
	return 0
}

build_client(){
	if ! check_current; then
		return 1
	fi
	docker build -f build/Dockerfile -t todoroki182814/measurement-client .
	docker push todoroki182814/measurement-client
}


rm_data(){
	if [[ $# != 1 ]]; then
		echo "usage: rm_data dir"
		return 1
	fi
	root_dir=$1
	sudo find "./${root_dir}" -name "data" -type d | xargs sudo rm -r
}