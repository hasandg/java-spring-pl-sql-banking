#!/bin/bash

# Banking API Test Execution Script
# This script runs both Postman and JMeter tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL=${BASE_URL:-"http://localhost:8081"}
THREAD_COUNT=${THREAD_COUNT:-50}
RAMP_UP=${RAMP_UP:-30}
DURATION=${DURATION:-300}
RESULTS_DIR="results"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if application is running
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
        log_error "Banking application is not running at $BASE_URL"
        log_info "Please start the application with: docker-compose up -d"
        exit 1
    fi
    
    # Check Newman (Postman CLI)
    if ! command -v newman &> /dev/null; then
        log_warning "Newman not found. Installing..."
        npm install -g newman
    fi
    
    # Check JMeter
    if ! command -v jmeter &> /dev/null; then
        log_error "JMeter not found. Please install Apache JMeter"
        exit 1
    fi
    
    log_success "Prerequisites check completed"
}

setup_results_directory() {
    log_info "Setting up results directory..."
    mkdir -p "$RESULTS_DIR"
    mkdir -p "$RESULTS_DIR/postman"
    mkdir -p "$RESULTS_DIR/jmeter"
    mkdir -p "$RESULTS_DIR/html-reports"
}

run_postman_tests() {
    log_info "Running Postman functional tests..."
    
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local collection_file="postman/Banking-API-Collection.json"
    local report_file="$RESULTS_DIR/postman/newman-report-$timestamp.html"
    local json_file="$RESULTS_DIR/postman/newman-results-$timestamp.json"
    
    if [ ! -f "$collection_file" ]; then
        log_error "Postman collection not found: $collection_file"
        return 1
    fi
    
    newman run "$collection_file" \
        --environment-var "baseUrl=$BASE_URL" \
        --reporters cli,html,json \
        --reporter-html-export "$report_file" \
        --reporter-json-export "$json_file" \
        --timeout-request 30000 \
        --delay-request 1000
    
    if [ $? -eq 0 ]; then
        log_success "Postman tests completed successfully"
        log_info "HTML Report: $report_file"
        log_info "JSON Results: $json_file"
    else
        log_error "Postman tests failed"
        return 1
    fi
}

run_jmeter_tests() {
    log_info "Running JMeter performance tests..."
    
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local test_plan="jmeter/Banking-Stress-Test.jmx"
    local results_file="$RESULTS_DIR/jmeter/jmeter-results-$timestamp.jtl"
    local html_report_dir="$RESULTS_DIR/html-reports/jmeter-report-$timestamp"
    
    if [ ! -f "$test_plan" ]; then
        log_error "JMeter test plan not found: $test_plan"
        return 1
    fi
    
    log_info "Test Parameters:"
    log_info "  - Base URL: $BASE_URL"
    log_info "  - Thread Count: $THREAD_COUNT"
    log_info "  - Ramp Up: $RAMP_UP seconds"
    log_info "  - Duration: $DURATION seconds"
    
    # Run JMeter test
    jmeter -n -t "$test_plan" \
        -Jbase.url="$BASE_URL" \
        -Jthread.count="$THREAD_COUNT" \
        -Jramp.up="$RAMP_UP" \
        -Jduration="$DURATION" \
        -l "$results_file" \
        -e -o "$html_report_dir"
    
    if [ $? -eq 0 ]; then
        log_success "JMeter tests completed successfully"
        log_info "Results File: $results_file"
        log_info "HTML Report: $html_report_dir/index.html"
        
        # Display summary
        if [ -f "$results_file" ]; then
            log_info "Test Summary:"
            awk -F',' 'NR>1 {
                total++; 
                if($8=="true") success++; 
                else failed++;
                time+=$2
            } 
            END {
                printf "  - Total Requests: %d\n", total;
                printf "  - Successful: %d (%.1f%%)\n", success, (success/total)*100;
                printf "  - Failed: %d (%.1f%%)\n", failed, (failed/total)*100;
                printf "  - Average Response Time: %.0f ms\n", time/total;
            }' "$results_file"
        fi
    else
        log_error "JMeter tests failed"
        return 1
    fi
}

run_load_profiles() {
    log_info "Running different load profiles..."
    
    # Light Load
    log_info "Running Light Load Test (10 users, 2 minutes)..."
    THREAD_COUNT=10 RAMP_UP=10 DURATION=120 run_jmeter_tests
    
    sleep 30
    
    # Medium Load  
    log_info "Running Medium Load Test (50 users, 5 minutes)..."
    THREAD_COUNT=50 RAMP_UP=30 DURATION=300 run_jmeter_tests
    
    sleep 30
    
    # Heavy Load
    log_info "Running Heavy Load Test (100 users, 10 minutes)..."
    THREAD_COUNT=100 RAMP_UP=60 DURATION=600 run_jmeter_tests
}

generate_summary_report() {
    log_info "Generating summary report..."
    
    local summary_file="$RESULTS_DIR/test-summary-$(date +%Y%m%d_%H%M%S).md"
    
    cat > "$summary_file" << EOF
# Banking API Test Summary

**Test Date:** $(date)
**Base URL:** $BASE_URL

## Test Configuration
- Thread Count: $THREAD_COUNT
- Ramp Up: $RAMP_UP seconds  
- Duration: $DURATION seconds

## Results Location
- Postman Results: \`$RESULTS_DIR/postman/\`
- JMeter Results: \`$RESULTS_DIR/jmeter/\`
- HTML Reports: \`$RESULTS_DIR/html-reports/\`

## Quick Access
- Latest JMeter Report: [Open HTML Report]($(ls -t $RESULTS_DIR/html-reports/jmeter-report-*/index.html | head -1))
- Latest Postman Report: [Open HTML Report]($(ls -t $RESULTS_DIR/postman/newman-report-*.html | head -1))

## System Information
- OS: $(uname -s)
- Architecture: $(uname -m)
- Java Version: $(java -version 2>&1 | head -1)
- JMeter Version: $(jmeter --version 2>&1 | head -1)

EOF

    log_success "Summary report generated: $summary_file"
}

show_help() {
    echo "Banking API Test Runner"
    echo ""
    echo "Usage: $0 [OPTIONS] [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  postman     Run Postman functional tests only"
    echo "  jmeter      Run JMeter performance tests only"
    echo "  load        Run multiple load profiles"
    echo "  all         Run all tests (default)"
    echo ""
    echo "Options:"
    echo "  -u, --url URL          Base URL (default: http://localhost:8081)"
    echo "  -t, --threads COUNT    Thread count for JMeter (default: 50)"
    echo "  -r, --ramp-up SECONDS  Ramp up time (default: 30)"
    echo "  -d, --duration SECONDS Test duration (default: 300)"
    echo "  -h, --help            Show this help"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Run all tests with defaults"
    echo "  $0 -t 100 -d 600 jmeter             # Run JMeter with 100 users for 10 minutes"
    echo "  $0 -u http://staging:8081 postman   # Run Postman tests against staging"
    echo "  $0 load                              # Run multiple load profiles"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--url)
            BASE_URL="$2"
            shift 2
            ;;
        -t|--threads)
            THREAD_COUNT="$2"
            shift 2
            ;;
        -r|--ramp-up)
            RAMP_UP="$2"
            shift 2
            ;;
        -d|--duration)
            DURATION="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        postman|jmeter|load|all)
            COMMAND="$1"
            shift
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Default command
COMMAND=${COMMAND:-"all"}

# Main execution
main() {
    log_info "Starting Banking API Test Suite"
    log_info "Command: $COMMAND"
    
    check_prerequisites
    setup_results_directory
    
    case $COMMAND in
        postman)
            run_postman_tests
            ;;
        jmeter)
            run_jmeter_tests
            ;;
        load)
            run_load_profiles
            ;;
        all)
            run_postman_tests
            sleep 10
            run_jmeter_tests
            ;;
        *)
            log_error "Unknown command: $COMMAND"
            show_help
            exit 1
            ;;
    esac
    
    generate_summary_report
    log_success "Test execution completed!"
}

# Run main function
main "$@" 