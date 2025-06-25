#!/bin/bash

# 动态欺诈检测规则系统演示脚本
# 演示如何通过API动态管理规则，无需修改代码

echo "=== 动态欺诈检测规则系统演示 ==="
echo

BASE_URL="http://localhost:8080/api/v1/fraud-rules"

echo "1. 查看当前所有规则"
echo "GET $BASE_URL"
curl -s "$BASE_URL" | jq '.' || echo "请确保服务已启动且安装了jq"
echo

echo "2. 快速创建金额阈值规则"
echo "POST $BASE_URL/quick-create"
curl -X POST "$BASE_URL/quick-create" \
  -d "ruleType=AMOUNT&ruleName=DEMO_HIGH_AMOUNT&threshold=25000&description=演示：高额交易检测" \
  -H "Content-Type: application/x-www-form-urlencoded"
echo

echo "3. 快速创建频率限制规则"
echo "POST $BASE_URL/quick-create"
curl -X POST "$BASE_URL/quick-create" \
  -d "ruleType=FREQUENCY&ruleName=DEMO_HIGH_FREQUENCY&threshold=8&description=演示：高频交易检测" \
  -H "Content-Type: application/x-www-form-urlencoded"
echo

echo "4. 创建自定义规则（通过JSON）"
echo "POST $BASE_URL"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "DEMO_WEEKEND_RULE",
    "ruleType": "CUSTOM",
    "description": "演示：周末交易规则",
    "thresholdValue": 15000,
    "riskWeight": 0.35,
    "priority": 2,
    "enabled": true,
    "ruleConfig": "{\"pattern\": \"weekend\", \"multiplier\": 1.5}"
  }'
echo

echo "5. 查看新创建的规则"
echo "GET $BASE_URL"
curl -s "$BASE_URL" | jq '.[] | select(.ruleName | contains("DEMO"))' || echo "规则创建成功"
echo

echo "6. 模拟交易检测请求"
echo "POST http://localhost:8080/api/v1/fraud-detection/analyze"
curl -X POST "http://localhost:8080/api/v1/fraud-detection/analyze" \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "DEMO_TXN_001",
    "userId": "DEMO_USER_123",
    "amount": 30000,
    "currency": "USD",
    "ipAddress": "192.168.1.100",
    "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S)'"
  }' || echo "检测服务可能尚未配置"
echo

echo "7. 禁用演示规则"
# 获取第一个演示规则的ID并禁用
RULE_ID=$(curl -s "$BASE_URL" | jq -r '.[] | select(.ruleName == "DEMO_HIGH_AMOUNT") | .id' 2>/dev/null)
if [ "$RULE_ID" != "null" ] && [ -n "$RULE_ID" ]; then
    echo "PATCH $BASE_URL/$RULE_ID/toggle"
    curl -X PATCH "$BASE_URL/$RULE_ID/toggle"
    echo
fi

echo "8. 删除演示规则"
DEMO_RULES=$(curl -s "$BASE_URL" | jq -r '.[] | select(.ruleName | contains("DEMO")) | .id' 2>/dev/null)
for rule_id in $DEMO_RULES; do
    if [ "$rule_id" != "null" ] && [ -n "$rule_id" ]; then
        echo "DELETE $BASE_URL/$rule_id"
        curl -X DELETE "$BASE_URL/$rule_id"
        echo
    fi
done

echo
echo "=== 演示完成 ==="
echo "关键优势："
echo "✅ 新增规则无需修改代码"
echo "✅ 实时生效，无需重启服务"
echo "✅ 支持复杂规则配置"
echo "✅ 灵活的风险权重和优先级"
echo "✅ 完整的规则生命周期管理"
echo
echo "要查看所有可用的API端点，请访问："
echo "http://localhost:8080/swagger-ui.html" 