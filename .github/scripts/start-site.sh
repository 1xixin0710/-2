#!/usr/bin/env bash
set -euo pipefail

cd /workspaces/-2

if curl -fsS --max-time 3 http://127.0.0.1:8080/catalog >/dev/null; then
    exit 0
fi

if [ "$(sudo docker inspect -f '{{.State.Status}}' library-mysql 2>/dev/null || true)" != "running" ]; then
    if ! sudo docker start library-mysql >/dev/null 2>&1; then
        # Codespaces can recover an older MySQL container while the current layer is broken.
        running_mysql=$(sudo docker ps --filter 'publish=3306' --format '{{.Names}}' | head -n 1)
        if [ -n "$running_mysql" ]; then
            sudo docker rm library-mysql >/dev/null
            sudo docker rename "$running_mysql" library-mysql
        else
            sudo docker inspect -f '{{range .Config.Env}}{{println .}}{{end}}' library-mysql > /tmp/library-mysql.env
            sudo docker rename library-mysql "library-mysql-corrupt-$(date +%s)"
            sudo docker run -d \
                --name library-mysql \
                --restart unless-stopped \
                --env-file /tmp/library-mysql.env \
                -p 127.0.0.1:3306:3306 \
                -v /workspaces/-2/.data/mysql:/var/lib/mysql \
                mysql:8.0 >/dev/null
        fi
    fi
fi

for _ in $(seq 1 30); do
    if sudo docker exec library-mysql mysqladmin ping -h 127.0.0.1 --silent >/dev/null 2>&1; then
        break
    fi
    sleep 1
done

if ! pgrep -f '^java .*target/dagong-cloud-library-1.0.0.jar$' >/dev/null; then
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
        java -Dfile.encoding=UTF-8 -jar target/dagong-cloud-library-1.0.0.jar \
        >/tmp/dagong-cloud-library.log 2>&1 </dev/null &
fi

for _ in $(seq 1 60); do
    if curl -fsS --max-time 3 http://127.0.0.1:8080/catalog >/dev/null; then
        exit 0
    fi
    sleep 1
done

tail -n 50 /tmp/dagong-cloud-library.log
exit 1
