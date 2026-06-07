# =====================================================
# MallCloud 一键停 (Linux/macOS)
# =====================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/../deploy/docker"

cd "$DOCKER_DIR"

echo "停止全栈..."
docker compose -f docker-compose.all.yml down 2>/dev/null || true

echo "停止中间件..."
docker compose -f docker-compose.middleware.yml down

echo ""
echo "是否同时删除数据卷？(y/N)"
read -r ans
if [[ "$ans" == "y" || "$ans" == "Y" ]]; then
  echo "删除数据卷..."
  docker compose -f docker-compose.middleware.yml down -v
  echo "完成。"
else
  echo "保留数据卷。"
fi
