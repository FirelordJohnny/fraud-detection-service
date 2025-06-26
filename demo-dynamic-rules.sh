#!/bin/bash

# Dynamic Fraud Detection Rule System Demo Script
# Demonstrates how to dynamically manage rules through API without code changes

echo "=== Dynamic Fraud Detection Rule System Demo ==="
echo

BASE_URL="http://localhost:8080/api/v1/fraud-rules"

echo "1. View all current rules"
echo "GET $BASE_URL"
curl -s "$BASE_URL" | jq '.' || echo "Please ensure the service is running and jq is installed"
echo

echo "2. Quick create amount threshold rule"
echo "POST $BASE_URL/quick-create"
curl -X POST "$BASE_URL/quick-create" \
  -d "ruleType=AMOUNT&ruleName=DEMO_HIGH_AMOUNT&threshold=25000&description=Demo: High amount transaction detection" \
  -H "Content-Type: application/x-www-form-urlencoded"
echo

echo "3. Quick create frequency limit rule"
echo "POST $BASE_URL/quick-create"
curl -X POST "$BASE_URL/quick-create" \
  -d "ruleType=FREQUENCY&ruleName=DEMO_HIGH_FREQUENCY&threshold=8&description=Demo: High frequency transaction detection" \
  -H "Content-Type: application/x-www-form-urlencoded"
echo

echo "4. Create custom rule (via JSON)"
echo "POST $BASE_URL"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "DEMO_WEEKEND_RULE",
    "ruleType": "CUSTOM",
    "description": "Demo: Weekend transaction rule",
    "thresholdValue": 15000,
    "riskWeight": 0.35,
    "priority": 2,
    "enabled": true,
    "ruleConfig": "{\"pattern\": \"weekend\", \"multiplier\": 1.5}"
  }'
echo

echo "5. View newly created rules"
echo "GET $BASE_URL"
curl -s "$BASE_URL" | jq '.[] | select(.ruleName | contains("DEMO"))' || echo "Rules created successfully"
echo

echo "6. Simulate transaction detection request"
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
  }' || echo "Detection service may not be configured yet"
echo

echo "7. Disable demo rule"
# Get the first demo rule ID and disable it
RULE_ID=$(curl -s "$BASE_URL" | jq -r '.[] | select(.ruleName == "DEMO_HIGH_AMOUNT") | .id' 2>/dev/null)
if [ "$RULE_ID" != "null" ] && [ -n "$RULE_ID" ]; then
    echo "PATCH $BASE_URL/$RULE_ID/toggle"
    curl -X PATCH "$BASE_URL/$RULE_ID/toggle"
    echo
fi

echo "8. Delete demo rules"
DEMO_RULES=$(curl -s "$BASE_URL" | jq -r '.[] | select(.ruleName | contains("DEMO")) | .id' 2>/dev/null)
for rule_id in $DEMO_RULES; do
    if [ "$rule_id" != "null" ] && [ -n "$rule_id" ]; then
        echo "DELETE $BASE_URL/$rule_id"
        curl -X DELETE "$BASE_URL/$rule_id"
        echo
    fi
done

echo
echo "=== Demo Complete ==="
echo "Key Advantages:"
echo "✅ Add new rules without code changes"
echo "✅ Real-time effect, no service restart required"
echo "✅ Support for complex rule configurations"
echo "✅ Flexible risk weights and priorities"
echo "✅ Complete rule lifecycle management"
echo
echo "To view all available API endpoints, visit:"
echo "http://localhost:8080/swagger-ui.html" 