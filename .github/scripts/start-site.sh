#!/usr/bin/env bash
set -euo pipefail

cd /workspaces/-2

if curl -fsS --max-time 3 http://127.0.0.1:8080/catalog >/dev/null; then
    exit 0
fi

if [ "$(sudo docker inspect -f '{{.State.Status}}' library-mysql 2>/dev/null || true)" != "running" ]; then
    sudo docker start library-mysql >/dev/null
fi

for _ in $(seq 1 30); do
    if sudo docker exec library-mysql mysqladmin ping -h 127.0.0.1 --silent >/dev/null 2>&1; then
        break
    fi
    sleep 1
done

if ! pgrep -f '^java .*target/yunxu-books-1.0.0.jar$' >/dev/null; then
    DB_USERNAME=$(sudo docker exec library-mysql printenv MYSQL_USER)
    DB_PASSWORD=$(sudo docker exec library-mysql printenv MYSQL_PASSWORD)
    DB_DATABASE=$(sudo docker exec library-mysql printenv MYSQL_DATABASE)
    DB_URL="jdbc:mysql://127.0.0.1:3306/${DB_DATABASE}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"

    nohup env \
        DB_URL="$DB_URL" \
        DB_USERNAME="$DB_USERNAME" \
        DB_PASSWORD="$DB_PASSWORD" \
        SPRING_PROFILES_ACTIVE=cloud \
        DB_INIT_MODE=never \
        java -Dfile.encoding=UTF-8 -jar target/yunxu-books-1.0.0.jar \
        >/tmp/yunxu-books.log 2>&1 </dev/null &
fi

for _ in $(seq 1 60); do
    if curl -fsS --max-time 3 http://127.0.0.1:8080/catalog >/dev/null; then
        exit 0
    fi
    sleep 1
done

tail -n 50 /tmp/yunxu-books.log
exit 1
