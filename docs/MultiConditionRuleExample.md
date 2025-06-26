# Multi-Condition Rule Hybrid Solution Usage Guide

## Overview

Our hybrid solution supports two types of rule configuration:
1. **Single Condition Rules** - Simple field comparisons
2. **Multi-Condition Group Rules** - Support for condition grouping and logical combinations

## 1. Single Condition Rules

### Use Cases
Suitable for simple single-field validation, such as amount thresholds, currency type checks, etc.

### Configuration Method
```java
FraudRule rule = FraudRule.builder()
    .ruleName("HIGH_AMOUNT_RULE")
    .ruleType("SIMPLE")
    .conditionField("amount")         // Field name
    .conditionOperator("GT")          // Operator
    .conditionValue("10000")          // Threshold
    .riskWeight(BigDecimal.valueOf(0.7))
    .build();
```

### Supported Operators
- `GT`, `>` - Greater than
- `LT`, `<` - Less than  
- `EQ`, `=` - Equal to
- `NE`, `!=` - Not equal to
- `GTE`, `>=` - Greater than or equal to
- `LTE`, `<=` - Less than or equal to
- `IN` - Contains in list (comma-separated)
- `NOT_IN` - Not contains in list
- `CONTAINS` - String contains
- `TIME_IN_RANGE` - Within time range (format: HH:mm-HH:mm)

## 2. Multi-Condition Group Rules

### Use Cases
Suitable for complex business logic requiring multiple condition combinations.

### Configuration Structure
```
Rule = ConditionGroup1 AND/OR ConditionGroup2 AND/OR ConditionGroup3
Where:
ConditionGroup1 = (Condition1 AND/OR Condition2 AND/OR Condition3)
ConditionGroup2 = (Condition4 AND/OR Condition5)
```

### Configuration Examples

#### Example 1: Simple Multi-Condition Combination
**Business Requirement**: High amount AND USD transaction
```java
// Build condition configuration
MultiConditionConfig config = MultiConditionConfig.builder()
    .groupLogicalOperator("AND")  // Use AND between condition groups
    .conditionGroups(Arrays.asList(
        // Condition group: amount and currency check
        MultiConditionConfig.ConditionGroup.builder()
            .groupId("amount_currency_check")
            .intraGroupOperator("AND")  // Use AND within group conditions
            .conditions(Arrays.asList(
                MultiConditionConfig.RuleCondition.builder()
                    .field("amount")
                    .operator("GT")
                    .value("10000")
                    .build(),
                MultiConditionConfig.RuleCondition.builder()
                    .field("currency")
                    .operator("EQ")
                    .value("USD")
                    .build()
            ))
            .build()
    ))
    .build();

// Create rule
FraudRule rule = FraudRule.builder()
    .ruleName("HIGH_AMOUNT_USD_RULE")
    .ruleType("MULTI_CONDITION")
    .ruleConfig(objectMapper.writeValueAsString(config))
    .riskWeight(BigDecimal.valueOf(0.8))
    .build();
```

#### Example 2: Complex Multi-Condition Combination
**Business Requirement**: (High amount AND Sensitive currency) AND (Suspicious region OR Suspicious payment method)
```java
MultiConditionConfig config = MultiConditionConfig.builder()
    .groupLogicalOperator("AND")  // AND relationship between two condition groups
    .conditionGroups(Arrays.asList(
        // Condition Group 1: Amount and currency check
        MultiConditionConfig.ConditionGroup.builder()
            .groupId("amount_currency_group")
            .intraGroupOperator("AND")
            .conditions(Arrays.asList(
                MultiConditionConfig.RuleCondition.builder()
                    .field("amount")
                    .operator("GT")
                    .value("15000")
                    .build(),
                MultiConditionConfig.RuleCondition.builder()
                    .field("currency")
                    .operator("IN")
                    .value("USD,EUR")
                    .build()
            ))
            .build(),
        
        // Condition Group 2: Location or payment method check
        MultiConditionConfig.ConditionGroup.builder()
            .groupId("location_payment_group")
            .intraGroupOperator("OR")  // Use OR within group conditions
            .conditions(Arrays.asList(
                MultiConditionConfig.RuleCondition.builder()
                    .field("ipAddress")
                    .operator("CONTAINS")
                    .value("192.168")
                    .build(),
                MultiConditionConfig.RuleCondition.builder()
                    .field("country")
                    .operator("IN")
                    .value("US,CN")
                    .build(),
                MultiConditionConfig.RuleCondition.builder()
                    .field("paymentMethod")
                    .operator("EQ")
                    .value("CREDIT_CARD")
                    .build()
            ))
            .build()
    ))
    .build();
```

#### Example 3: Flexible Combination Logic
**Business Requirement**: Extremely high amount OR (Medium amount AND Suspicious behavior)
```java
MultiConditionConfig config = MultiConditionConfig.builder()
    .groupLogicalOperator("OR")  // OR relationship between two condition groups
    .conditionGroups(Arrays.asList(
        // Condition Group 1: Extremely high amount
        MultiConditionConfig.ConditionGroup.builder()
            .groupId("very_high_amount")
            .intraGroupOperator("AND")
            .conditions(Arrays.asList(
                MultiConditionConfig.RuleCondition.builder()
                    .field("amount")
                    .operator("GT")
                    .value("50000")
                    .build()
            ))
            .build(),
        
        // Condition Group 2: Medium amount + Suspicious behavior
        MultiConditionConfig.ConditionGroup.builder()
            .groupId("medium_amount_suspicious")
            .intraGroupOperator("AND")
            .conditions(Arrays.asList(
                MultiConditionConfig.RuleCondition.builder()
                    .field("amount")
                    .operator("GT")
                    .value("5000")
                    .build(),
                MultiConditionConfig.RuleCondition.builder()
                    .field("amount")
                    .operator("LT")
                    .value("20000")
                    .build(),
                MultiConditionConfig.RuleCondition.builder()
                    .field("ipAddress")
                    .operator("CONTAINS")
                    .value("192.168")
                    .build()
            ))
            .build()
    ))
    .build();
```

## 3. Frontend UI Integration Recommendations

### Single Condition Rule UI
```html
<!-- Simple form -->
<form>
  <select name="field">
    <option value="amount">Amount</option>
    <option value="currency">Currency</option>
    <option value="country">Country</option>
  </select>
  
  <select name="operator">
    <option value="GT">Greater Than</option>
    <option value="EQ">Equal To</option>
    <option value="IN">Contained In</option>
  </select>
  
  <input type="text" name="value" placeholder="Value" />
</form>
```

### Multi-Condition Rule UI
```html
<!-- Condition group management -->
<div class="rule-builder">
  <div class="condition-groups">
    <div class="condition-group" v-for="group in conditionGroups">
      <h4>Condition Group {{ group.groupId }}</h4>
      
      <!-- Intra-group logical operator -->
      <select v-model="group.intraGroupOperator">
        <option value="AND">AND (All conditions must be met)</option>
        <option value="OR">OR (Any condition is met)</option>
      </select>
      
      <!-- Condition list -->
      <div class="conditions">
        <div class="condition" v-for="condition in group.conditions">
          <select v-model="condition.field">
            <option value="amount">Amount</option>
            <option value="currency">Currency</option>
            <!-- More field options -->
          </select>
          
          <select v-model="condition.operator">
            <option value="GT">Greater Than</option>
            <option value="EQ">Equal To</option>
            <!-- More operators -->
          </select>
          
          <input v-model="condition.value" placeholder="Value" />
          
          <button @click="removeCondition(group, condition)">Remove</button>
        </div>
        
        <button @click="addCondition(group)">Add Condition</button>
      </div>
    </div>
    
    <!-- Inter-group logical operator -->
    <select v-model="groupLogicalOperator">
      <option value="AND">AND (All groups must be met)</option>
      <option value="OR">OR (Any group is met)</option>
    </select>
    
    <button @click="addConditionGroup()">Add Condition Group</button>
  </div>
</div>
```

## 4. Performance Optimization Recommendations

### Short-Circuit Evaluation
The system automatically implements short-circuit evaluation:
- AND operations: Stop evaluation immediately once any condition is false
- OR operations: Stop evaluation immediately once any condition is true

### Condition Ordering
It's recommended to place conditions most likely to fail first to improve short-circuit evaluation effectiveness:
- For AND combinations: Place the most restrictive conditions first
- For OR combinations: Place the most easily satisfied conditions first

### Rule Caching
Consider caching rule configurations to avoid repeated JSON parsing overhead.

## 5. Error Handling

The system provides comprehensive error handling:
- JSON parsing errors: Return failure result with error logging
- Field not found: Return failure result with warning logging
- Empty condition groups: Return failure result with clear error reason

## 6. Extensibility

Extension path for the current solution:
1. **Phase 1**: Current non-nested grouping solution (Implemented)
2. **Phase 2**: Introduce rule engines like Drools if needed
3. **Phase 3**: Consider nested grouping support

This progressive design ensures the system can evolve smoothly as business complexity grows. 