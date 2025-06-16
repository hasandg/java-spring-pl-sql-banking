# 🧪 Banking API Testing Suite

Complete testing solution for the Java Spring Boot Banking Application with **Postman** functional tests and **JMeter** performance/stress tests.

## 📁 Project Structure

```
├── postman/
│   ├── Banking-API-Collection.json    # Postman collection with dynamic variables
│   └── environment.json               # Environment variables
├── jmeter/
│   └── Banking-Stress-Test.jmx        # JMeter stress test plan
├── scripts/
│   └── run-tests.sh                   # Automated test execution script
├── docs/
│   └── API-TESTING-GUIDE.md           # Comprehensive testing guide
└── results/                           # Test results (auto-generated)
    ├── postman/                       # Newman HTML/JSON reports
    ├── jmeter/                        # JMeter JTL files
    └── html-reports/                  # JMeter HTML reports
```

## 🚀 Quick Start

### 1. Prerequisites

```bash
# Install Node.js and Newman (Postman CLI)
npm install -g newman newman-reporter-html

# Install Apache JMeter
# Download from: https://jmeter.apache.org/download_jmeter.cgi
# Or via Homebrew: brew install jmeter

# Start the Banking Application
docker-compose up -d
```

### 2. Run All Tests

```bash
# Make script executable (first time only)
chmod +x scripts/run-tests.sh

# Run all tests with default settings
./scripts/run-tests.sh

# Run with custom parameters
./scripts/run-tests.sh -t 100 -d 600 -u http://localhost:8081
```

### 3. Run Specific Tests

```bash
# Functional tests only
./scripts/run-tests.sh postman

# Performance tests only  
./scripts/run-tests.sh jmeter

# Multiple load profiles
./scripts/run-tests.sh load
```

## 📊 Test Coverage

### Functional Tests (Postman)

✅ **Account Management**
- Create accounts with validation
- Retrieve account details
- Handle invalid account scenarios

✅ **Transaction Operations**
- Deposit money with random amounts
- Withdraw money with balance checks
- Transfer between accounts
- Transaction history retrieval

✅ **Error Handling**
- Invalid account numbers
- Insufficient funds scenarios
- Negative amount validation
- Missing parameter handling

✅ **Dynamic Variables**
- Auto-generated account numbers
- Random transaction amounts
- Timestamp-based descriptions
- Cross-request data sharing

### Performance Tests (JMeter)

⚡ **Load Testing**
- 50 concurrent users (default)
- 5-minute duration
- Mixed transaction types
- Response time monitoring

⚡ **Stress Testing**
- Up to 500 concurrent users
- System breaking point detection
- Resource utilization monitoring
- Error rate analysis

⚡ **Test Scenarios**
- Random transaction controller (40% deposits, 30% withdrawals, 30% queries)
- Throughput limiting (60 req/min)
- Dynamic account creation
- Realistic data patterns

## 🎯 Usage Examples

### Basic Testing

```bash
# Quick functional test
./scripts/run-tests.sh postman

# Quick performance test (light load)
./scripts/run-tests.sh -t 10 -d 120 jmeter
```

### Production-Like Testing

```bash
# Heavy load test
./scripts/run-tests.sh -t 200 -d 600 jmeter

# Spike test
./scripts/run-tests.sh -t 500 -r 10 -d 60 jmeter

# Extended endurance test
./scripts/run-tests.sh -t 100 -d 1800 jmeter
```

### Environment Testing

```bash
# Test against staging
./scripts/run-tests.sh -u http://staging.banking.com:8081 all

# Test against production (read-only)
./scripts/run-tests.sh -u https://api.banking.com postman
```

## 📈 Performance Benchmarks

### Expected Response Times (95th Percentile)

| Operation | Target | Good | Acceptable |
|-----------|--------|------|------------|
| Account Creation | < 500ms | < 1s | < 2s |
| Deposit/Withdrawal | < 300ms | < 500ms | < 1s |
| Transfer | < 800ms | < 1s | < 2s |
| Balance Inquiry | < 200ms | < 300ms | < 500ms |

### Throughput Targets

| Metric | Target | Good | Acceptable |
|--------|--------|------|------------|
| Transactions/Second | 100+ | 50+ | 25+ |
| Concurrent Users | 200+ | 100+ | 50+ |
| Error Rate | < 0.1% | < 1% | < 5% |

## 🔧 Configuration

### Script Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `-u, --url` | `http://localhost:8081` | Base URL |
| `-t, --threads` | `50` | Concurrent users |
| `-r, --ramp-up` | `30` | Ramp-up time (seconds) |
| `-d, --duration` | `300` | Test duration (seconds) |

### Environment Variables

```bash
# Override defaults with environment variables
export BASE_URL="http://localhost:8081"
export THREAD_COUNT=100
export RAMP_UP=60
export DURATION=600

./scripts/run-tests.sh
```

## 📋 Test Reports

### Postman Reports

- **HTML Report**: Visual test results with pass/fail status
- **JSON Report**: Detailed test data for CI/CD integration
- **Console Output**: Real-time test execution feedback

### JMeter Reports

- **HTML Dashboard**: Comprehensive performance analysis
- **JTL Files**: Raw test data for custom analysis
- **Summary Statistics**: Key performance metrics
- **Response Time Graphs**: Visual performance trends

### Sample Report Locations

```
results/
├── postman/
│   ├── newman-report-20241201_143022.html
│   └── newman-results-20241201_143022.json
├── jmeter/
│   └── jmeter-results-20241201_143500.jtl
└── html-reports/
    └── jmeter-report-20241201_143500/
        └── index.html
```

## 🐛 Troubleshooting

### Common Issues

**Application Not Running**
```bash
# Check application status
curl http://localhost:8081/actuator/health

# Start services
docker-compose up -d

# Check logs
docker-compose logs banking-app
```

**Newman Not Found**
```bash
# Install Newman globally
npm install -g newman newman-reporter-html

# Verify installation
newman --version
```

**JMeter Memory Issues**
```bash
# Increase JMeter heap size
export JVM_ARGS="-Xms1g -Xmx4g"
jmeter -n -t jmeter/Banking-Stress-Test.jmx
```

**Connection Timeouts**
```bash
# Increase timeout values
./scripts/run-tests.sh -u http://slow-server:8081 postman
```

### Performance Issues

**High Response Times**
1. Check database connection pool settings
2. Monitor Oracle database performance
3. Verify Redis cache hit rates
4. Check system resource utilization

**High Error Rates**
1. Review application logs
2. Check database connection limits
3. Verify network connectivity
4. Monitor memory usage

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: API Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Start Services
        run: docker-compose up -d
        
      - name: Wait for Services
        run: sleep 30
        
      - name: Install Newman
        run: npm install -g newman newman-reporter-html
        
      - name: Run Functional Tests
        run: ./scripts/run-tests.sh postman
        
      - name: Run Performance Tests
        run: ./scripts/run-tests.sh -t 20 -d 60 jmeter
        
      - name: Upload Reports
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: results/
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    
    stages {
        stage('Setup') {
            steps {
                sh 'docker-compose up -d'
                sh 'sleep 30'
            }
        }
        
        stage('Functional Tests') {
            steps {
                sh './scripts/run-tests.sh postman'
            }
        }
        
        stage('Performance Tests') {
            steps {
                sh './scripts/run-tests.sh -t 50 -d 300 jmeter'
            }
        }
        
        stage('Reports') {
            steps {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'results/html-reports',
                    reportFiles: '*/index.html',
                    reportName: 'Performance Report'
                ])
            }
        }
    }
    
    post {
        always {
            sh 'docker-compose down'
        }
    }
}
```

## 📚 Additional Resources

- **[API Testing Guide](API-TESTING-GUIDE.md)**: Comprehensive testing documentation
- **[Postman Documentation](https://learning.postman.com/)**: Official Postman learning resources
- **[JMeter Documentation](https://jmeter.apache.org/usermanual/)**: Apache JMeter user manual
- **[Newman CLI](https://github.com/postmanlabs/newman)**: Postman command-line runner

## 🤝 Contributing

1. **Add New Tests**: Extend Postman collection or JMeter test plan
2. **Improve Scripts**: Enhance automation scripts
3. **Update Documentation**: Keep guides current
4. **Report Issues**: Submit bugs or feature requests

## 📄 License

This testing suite is part of the Banking API project and follows the same license terms.

---

**Happy Testing! 🎉**

For questions or support, please refer to the comprehensive [API Testing Guide](API-TESTING-GUIDE.md) or create an issue in the project repository. 