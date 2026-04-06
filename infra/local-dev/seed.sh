#!/bin/bash
# =============================================================================
# OTA 로컬 개발 환경 시드 데이터 주입 스크립트
# Docker Compose로 띄운 MySQL에 시드 데이터를 삽입한다.
#
# 사용법:
#   프로젝트 루트에서 실행: ./infra/local-dev/seed.sh
# =============================================================================

set -e

SEED_DIR="adapter-out/persistence-mysql/src/main/resources/db/seed"
CONTAINER_NAME="otatoy-mysql"
DB_USER="root"
DB_PASS="root"
DB_NAME="ota"

echo "=== OTA 시드 데이터 주입 ==="
echo ""

# 시드 디렉토리 존재 확인
if [ ! -d "$SEED_DIR" ]; then
    echo "[오류] 시드 디렉토리를 찾을 수 없습니다: $SEED_DIR"
    echo "프로젝트 루트 디렉토리에서 실행해주세요."
    exit 1
fi

# Docker 컨테이너 실행 확인
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "[오류] Docker 컨테이너가 실행 중이 아닙니다: $CONTAINER_NAME"
    echo "먼저 docker-compose up -d 를 실행해주세요."
    exit 1
fi

# 시드 파일 순서대로 실행
for f in $(ls "$SEED_DIR"/V999_*.sql 2>/dev/null | sort); do
    filename=$(basename "$f")
    echo "실행: $filename"
    docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$f"
done

echo ""
echo "=== 시드 데이터 주입 완료 ==="
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
