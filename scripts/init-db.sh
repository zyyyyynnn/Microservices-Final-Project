#!/bin/bash
# =====================================================
# MallCloud 初始化数据库 (Linux/macOS)
# =====================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR/.."
DB_DIR="$PROJECT_DIR/db/init"

MYSQL_HOST=${MYSQL_HOST:-127.0.0.1}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PWD=${MYSQL_PWD:-root}

echo "========================================="
echo "  初始化 MallCloud 数据库"
echo "  Host: $MYSQL_HOST:$MYSQL_PORT"
echo "========================================="

# 1. 创建库 + 表
echo ">> 执行 00-create-databases.sql ..."
mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PWD < "$DB_DIR/00-create-databases.sql"

# 2. 种子数据
echo ">> 执行 seed.sql ..."
mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PWD < "$DB_DIR/seed.sql"

# 3. 验证
echo ""
echo "========================================="
echo "  数据统计"
echo "========================================="
mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PWD -t <<'EOF'
SELECT 'mall_user.user' AS tbl, COUNT(*) AS cnt FROM mall_user.user
UNION ALL SELECT 'mall_user.address', COUNT(*) FROM mall_user.address
UNION ALL SELECT 'mall_auth.sys_user_auth', COUNT(*) FROM mall_auth.sys_user_auth
UNION ALL SELECT 'mall_product.category', COUNT(*) FROM mall_product.category
UNION ALL SELECT 'mall_product.spu', COUNT(*) FROM mall_product.spu
UNION ALL SELECT 'mall_product.sku', COUNT(*) FROM mall_product.sku
UNION ALL SELECT 'mall_inventory.stock', COUNT(*) FROM mall_inventory.stock
UNION ALL SELECT 'mall_seckill.seckill_activity', COUNT(*) FROM mall_seckill.seckill_activity;
EOF

echo ""
echo "========================================="
echo "  数据库初始化完成！"
echo "========================================="
echo ""
echo "测试账号："
echo "  USER:     zhangsan    / P@ssw0rd123"
echo "  MERCHANT: merchant01  / P@ssw0rd123"
echo "  ADMIN:    admin       / Admin@123"
echo ""
