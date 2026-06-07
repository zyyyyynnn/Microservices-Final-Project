# =====================================================
# MallCloud 启动所有中间件 (Linux/macOS)
# 使用：bash scripts/start-middleware.sh
# =====================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/../deploy/docker"

cd "$DOCKER_DIR"

echo "========================================="
echo "  启动 MallCloud 中间件"
echo "========================================="

docker compose -f docker-compose.middleware.yml up -d

echo ""
echo "等待中间件启动完成..."

# 等待 Nacos
echo -n "等待 Nacos ... "
for i in {1..30}; do
  if curl -s http://localhost:8848/nacos/ > /dev/null 2>&1; then
    echo "OK"
    break
  fi
  sleep 2
  echo -n "."
done

# 等待 MySQL
echo -n "等待 MySQL ... "
for i in {1..30}; do
  if docker exec mall-mysql mysqladmin ping -uroot -proot > /dev/null 2>&1; then
    echo "OK"
    break
  fi
  sleep 2
  echo -n "."
done

# 等待 RocketMQ
echo -n "等待 RocketMQ ... "
sleep 10
echo "OK (启动较慢，请稍候)"

echo ""
echo "========================================="
echo "  中间件启动完成！"
echo "========================================="
echo ""
echo "  Nacos 控制台:    http://localhost:8848/nacos   (nacos/nacos)"
echo "  Sentinel 控制台: http://localhost:8080         (sentinel/sentinel)"
echo "  Zipkin:          http://localhost:9411"
echo "  Kibana:          http://localhost:5601"
echo "  RocketMQ 控制台: http://localhost:8180"
echo "  Seata 控制台:    http://localhost:7091"
echo ""
echo "下一步："
echo "  1. 初始化数据库: bash scripts/init-db.sh"
echo "  2. 启动微服务（IDE 或 docker compose -f docker-compose.all.yml up -d）"
echo ""
