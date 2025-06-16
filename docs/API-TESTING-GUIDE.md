# Banking API Testing Guide

## Overview

This guide provides comprehensive instructions for testing the Banking API using both **Postman** for functional testing and **JMeter** for performance/stress testing.

## 📋 Table of Contents

1. [Postman Collection Setup](#postman-collection-setup)
2. [JMeter Stress Testing](#jmeter-stress-testing)
3. [Test Scenarios](#test-scenarios)
4. [Performance Benchmarks](#performance-benchmarks)
5. [Troubleshooting](#troubleshooting)

---

## 🚀 Postman Collection Setup

### Prerequisites

- **Postman Desktop App** or **Postman Web**
- **Banking Application** running on `http://localhost:8081`
- **Oracle Database** and **Redis** services running

### Import Collection

1. **Download Collection**: `postman/Banking-API-Collection.json`
2. **Import in Postman**:
   - Open Postman
   - Click **Import** button
   - Select the JSON file
   - Click **Import**

### Collection Variables

The collection uses dynamic variables that are automatically managed:

| Variable | Description | Auto-Generated |
|----------|-------------|----------------|
| `baseUrl` | API base URL | `http://localhost:8081` |
| `accountNumber1` | First test account | ✅ From account creation |
| `accountNumber2` | Second test account | ✅ From account creation |
| `depositAmount` | Random deposit amount | ✅ $100-$1000 |
| `withdrawAmount` | Random withdrawal amount | ✅ $50-$500 |
| `transferAmount` | Random transfer amount | ✅ $50-$300 |

### Running Tests

#### 1. **Individual Requests**
```bash
# Run single request
Click on any request → Send
```

#### 2. **Collection Runner**
```bash
# Run entire collection
Collections → Banking API Collection → Run
```

#### 3. **Automated Test Sequence**
```bash
# Recommended order:
1. Create Account 1
2. Create Account 2  
3. Deposit Money
4. Withdraw Money
5. Transfer Money
6. Get Account Transactions
```

### Test Assertions

Each request includes automatic test assertions:

```javascript
// Status Code Validation
pm.test('Status code is 200', function () {
    pm.response.to.have.status(200);
});

// Response Structure Validation
pm.test('Response has required fields', function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('accountNumber');
});

// Dynamic Variable Assignment
const responseJson = pm.response.json();
pm.collectionVariables.set('accountNumber1', responseJson.accountNumber);
```

---

## ⚡ JMeter Stress Testing

### Prerequisites

- **Apache JMeter 5.6+** installed
- **Java 8+** runtime environment
- **Banking Application** running and accessible

### JMeter Test Plan Structure

```
Banking API Stress Test
├── Setup Thread Group (1 user)
│   └── Create Test Accounts
├── Load Test Thread Group (50 users)
│   ├── Random Transaction Controller
│   │   ├── Deposit Transaction (40% weight)
│   │   ├── Withdrawal Transaction (30% weight)
│   │   └── Get Account Details (30% weight)
│   └── Throughput Timer (60 req/min)
└── Result Collectors
    ├── Summary Report
    └── View Results Tree
```

### Running JMeter Tests

#### 1. **GUI Mode (Development)**
```bash
# Start JMeter GUI
jmeter -t jmeter/Banking-Stress-Test.jmx

# Configure test parameters in GUI
# Run test and view real-time results
```

#### 2. **Command Line Mode (Production)**
```bash
# Basic execution
jmeter -n -t jmeter/Banking-Stress-Test.jmx -l results/test-results.jtl

# With custom parameters
jmeter -n -t jmeter/Banking-Stress-Test.jmx \
  -Jthread.count=100 \
  -Jramp.up=60 \
  -Jduration=600 \
  -Jbase.url=http://localhost:8081 \
  -l results/stress-test-$(date +%Y%m%d_%H%M%S).jtl

# Generate HTML report
jmeter -g results/test-results.jtl -o results/html-report/
```

#### 3. **Docker Execution**
```bash
# Run JMeter in Docker
docker run --rm -v $(pwd):/jmeter justb4/jmeter:5.6.2 \
  -n -t /jmeter/jmeter/Banking-Stress-Test.jmx \
  -l /jmeter/results/docker-test-results.jtl
```

### Test Parameters

| Parameter | Default | Description | Range |
|-----------|---------|-------------|-------|
| `thread.count` | 50 | Concurrent users | 1-500 |
| `ramp.up` | 30 | Ramp-up time (seconds) | 10-300 |
| `duration` | 300 | Test duration (seconds) | 60-3600 |
| `base.url` | localhost:8081 | Target server URL | Any valid URL |

### Load Profiles

#### **Light Load**
```bash
jmeter -n -t Banking-Stress-Test.jmx \
  -Jthread.count=10 \
  -Jramp.up=10 \
  -Jduration=120
```

#### **Medium Load**
```bash
jmeter -n -t Banking-Stress-Test.jmx \
  -Jthread.count=50 \
  -Jramp.up=30 \
  -Jduration=300
```

#### **Heavy Load**
```bash
jmeter -n -t Banking-Stress-Test.jmx \
  -Jthread.count=200 \
  -Jramp.up=60 \
  -Jduration=600
```

#### **Spike Test**
```bash
jmeter -n -t Banking-Stress-Test.jmx \
  -Jthread.count=500 \
  -Jramp.up=10 \
  -Jduration=60
```

---

## 🎯 Test Scenarios

### Functional Test Scenarios (Postman)

#### **Happy Path Tests**
1. ✅ **Account Creation**: Create new accounts with valid data
2. ✅ **Deposit Operations**: Add money to accounts
3. ✅ **Withdrawal Operations**: Remove money from accounts
4. ✅ **Transfer Operations**: Move money between accounts
5. ✅ **Balance Inquiry**: Check account balances
6. ✅ **Transaction History**: Retrieve transaction lists

#### **Error Handling Tests**
1. ❌ **Invalid Account**: Access non-existent accounts
2. ❌ **Insufficient Funds**: Withdraw more than available balance
3. ❌ **Negative Amounts**: Use negative transaction amounts
4. ❌ **Invalid Data**: Send malformed JSON requests
5. ❌ **Missing Parameters**: Omit required fields

#### **Edge Case Tests**
1. 🔍 **Zero Amounts**: Test with zero-value transactions
2. 🔍 **Large Amounts**: Test with maximum allowed amounts
3. 🔍 **Special Characters**: Test with special characters in descriptions
4. 🔍 **Concurrent Operations**: Multiple operations on same account

### Performance Test Scenarios (JMeter)

#### **Load Testing**
- **Objective**: Verify normal operation under expected load
- **Users**: 50 concurrent users
- **Duration**: 5 minutes
- **Expected**: < 2 seconds response time, 0% error rate

#### **Stress Testing**
- **Objective**: Find breaking point of the system
- **Users**: 200+ concurrent users
- **Duration**: 10 minutes
- **Expected**: Graceful degradation, error handling

#### **Spike Testing**
- **Objective**: Test sudden load increases
- **Users**: 500 users in 10 seconds
- **Duration**: 1 minute
- **Expected**: System recovery after spike

#### **Volume Testing**
- **Objective**: Test with large amounts of data
- **Users**: 100 users
- **Duration**: 30 minutes
- **Expected**: Consistent performance over time

---

## 📊 Performance Benchmarks

### Expected Performance Metrics

#### **Response Times (95th Percentile)**
| Operation | Target | Good | Acceptable | Poor |
|-----------|--------|------|------------|------|
| Account Creation | < 500ms | < 1s | < 2s | > 2s |
| Deposit/Withdrawal | < 300ms | < 500ms | < 1s | > 1s |
| Transfer | < 800ms | < 1s | < 2s | > 2s |
| Balance Inquiry | < 200ms | < 300ms | < 500ms | > 500ms |
| Transaction History | < 400ms | < 600ms | < 1s | > 1s |

#### **Throughput Targets**
| Metric | Target | Good | Acceptable |
|--------|--------|------|------------|
| Transactions/Second | 100+ | 50+ | 25+ |
| Concurrent Users | 200+ | 100+ | 50+ |
| Error Rate | < 0.1% | < 1% | < 5% |

#### **Resource Utilization**
| Resource | Target | Warning | Critical |
|----------|--------|---------|----------|
| CPU Usage | < 70% | < 85% | > 90% |
| Memory Usage | < 80% | < 90% | > 95% |
| Database Connections | < 50% | < 75% | > 85% |
| Redis Memory | < 60% | < 80% | > 90% |

### Monitoring Commands

```bash
# Application Metrics
curl http://localhost:8081/actuator/metrics

# Database Performance
docker exec oracle-19c-db sqlplus system/Oracle123@ORCLPDB1 <<EOF
SELECT * FROM v\$session WHERE status = 'ACTIVE';
SELECT * FROM v\$sql WHERE executions > 100;
EOF

# Redis Performance
docker exec banking-redis redis-cli info stats

# System Resources
docker stats oracle-19c-db banking-redis
```

---

## 🔧 Troubleshooting

### Common Issues

#### **Postman Issues**

**Problem**: Variables not being set
```javascript
// Solution: Check test scripts
pm.test('Extract account number', function () {
    const responseJson = pm.response.json();
    pm.collectionVariables.set('accountNumber1', responseJson.accountNumber);
});
```

**Problem**: Connection refused
```bash
# Solution: Verify application is running
curl http://localhost:8081/actuator/health
docker-compose ps
```

#### **JMeter Issues**

**Problem**: Out of memory errors
```bash
# Solution: Increase JMeter heap size
export JVM_ARGS="-Xms1g -Xmx4g"
jmeter -n -t Banking-Stress-Test.jmx
```

**Problem**: Too many open files
```bash
# Solution: Increase file descriptor limit
ulimit -n 65536
```

**Problem**: Connection timeouts
```xml
<!-- Solution: Increase timeouts in JMeter -->
<stringProp name="HTTPSampler.connect_timeout">10000</stringProp>
<stringProp name="HTTPSampler.response_timeout">30000</stringProp>
```

#### **Application Issues**

**Problem**: Database connection pool exhausted
```properties
# Solution: Tune connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

**Problem**: Redis connection issues
```bash
# Solution: Check Redis connectivity
docker exec banking-redis redis-cli ping
```

**Problem**: High response times
```bash
# Solution: Enable application profiling
java -jar -Dspring.profiles.active=dev \
  -XX:+FlightRecorder \
  -XX:StartFlightRecording=duration=60s,filename=banking-profile.jfr \
  banking-app.jar
```

### Performance Tuning Tips

#### **Application Level**
1. **Enable Connection Pooling**: Configure HikariCP properly
2. **Cache Configuration**: Tune Redis cache TTL and eviction policies
3. **JVM Tuning**: Optimize garbage collection settings
4. **Database Indexing**: Ensure proper indexes on frequently queried columns

#### **Database Level**
1. **Oracle SGA/PGA**: Tune memory allocation
2. **Connection Limits**: Increase max connections if needed
3. **Query Optimization**: Analyze slow queries
4. **Tablespace Management**: Monitor space usage

#### **Infrastructure Level**
1. **Docker Resources**: Allocate sufficient CPU/memory
2. **Network Configuration**: Optimize network settings
3. **Disk I/O**: Use SSD storage for better performance
4. **Load Balancing**: Consider multiple application instances

---

## 📈 Reporting

### JMeter HTML Reports

Generate comprehensive HTML reports:

```bash
# Generate report from existing results
jmeter -g results/test-results.jtl -o results/html-report/

# View report
open results/html-report/index.html
```

### Custom Metrics Dashboard

Create monitoring dashboard with:
- **Application Metrics**: Response times, throughput, error rates
- **System Metrics**: CPU, memory, disk usage
- **Database Metrics**: Connection pool, query performance
- **Cache Metrics**: Hit/miss ratios, eviction rates

### Automated Testing Pipeline

```yaml
# CI/CD Integration Example
test:
  stage: test
  script:
    - docker-compose up -d
    - sleep 30  # Wait for services
    - newman run postman/Banking-API-Collection.json
    - jmeter -n -t jmeter/Banking-Stress-Test.jmx -l results.jtl
    - jmeter -g results.jtl -o html-report/
  artifacts:
    reports:
      junit: results.jtl
    paths:
      - html-report/
```

This comprehensive testing setup ensures your Banking API is thoroughly validated for both functionality and performance under various load conditions. 