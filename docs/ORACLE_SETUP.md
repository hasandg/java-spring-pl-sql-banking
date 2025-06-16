# Oracle Database 19c Enterprise Setup Guide

## Prerequisites

### 1. Oracle Container Registry Access

To use the official Oracle Database Enterprise image, you need to:

1. **Create Oracle Account**: Sign up at [Oracle Container Registry](https://container-registry.oracle.com)
2. **Accept License Agreement**: Navigate to Database → Enterprise and accept the license
3. **Docker Login**: Authenticate with Oracle Container Registry

```bash
docker login container-registry.oracle.com
# Enter your Oracle account credentials
```

### 2. System Requirements

Oracle Database 19c Enterprise requires significant system resources:

- **Memory**: Minimum 4GB RAM (8GB recommended)
- **CPU**: Minimum 2 cores (4 cores recommended)  
- **Disk Space**: Minimum 20GB free space
- **Docker**: Version 20.10+ with BuildKit support

## Quick Start

### 1. Start the Database

```bash
# Start Oracle 19c Enterprise and Redis
docker-compose up -d oracle-db redis

# Monitor startup logs (first startup takes 10-15 minutes)
docker-compose logs -f oracle-db
```

### 2. Wait for Database Initialization

The Oracle container will show:
```
DATABASE IS READY TO USE!
```

### 3. Connect to Database

```bash
# Connect using SQL*Plus
docker exec -it oracle-19c-db sqlplus system/Oracle123@ORCLPDB1

# Or connect from host
sqlplus system/Oracle123@localhost:1522/ORCLPDB1
```

## Database Configuration

### Connection Details

- **Host**: localhost
- **Port**: 1522
- **Service Name**: ORCLPDB1 (Pluggable Database)
- **Container Database**: ORCLCDB
- **Username**: system
- **Password**: Oracle123

### Environment Variables

| Variable | Value | Description |
|----------|-------|-------------|
| ORACLE_SID | ORCLCDB | Container Database SID |
| ORACLE_PDB | ORCLPDB1 | Pluggable Database Name |
| ORACLE_PWD | Oracle123 | System Password |
| ORACLE_EDITION | enterprise | Database Edition |
| ORACLE_CHARACTERSET | AL32UTF8 | Character Set |
| ENABLE_ARCHIVELOG | true | Archive Log Mode |

## Application Configuration

### Spring Boot Properties

```properties
# Oracle 19c Enterprise Connection
spring.datasource.url=jdbc:oracle:thin:@localhost:1522/ORCLPDB1
spring.datasource.username=system
spring.datasource.password=Oracle123
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# JPA Configuration for Oracle 19c
spring.jpa.database-platform=org.hibernate.dialect.Oracle12cDialect
spring.jpa.hibernate.ddl-auto=update
```

### Environment Variables Override

```bash
# For different environments
export DB_HOST=your-oracle-host
export DB_PORT=1522
export DB_SERVICE=ORCLPDB1
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
```

## Database Schema Setup

### 1. Create Banking User

```sql
-- Connect as system user
sqlplus system/Oracle123@localhost:1522/ORCLPDB1

-- Create banking user
CREATE USER banking_user IDENTIFIED BY banking_password;
GRANT CONNECT, RESOURCE, CREATE VIEW TO banking_user;
GRANT UNLIMITED TABLESPACE TO banking_user;

-- Grant additional privileges for sequences
GRANT CREATE SEQUENCE TO banking_user;
GRANT CREATE TRIGGER TO banking_user;
```

### 2. Run Initialization Scripts

```bash
# Copy and run init scripts
docker cp ./init-scripts/ oracle-19c-db:/opt/oracle/scripts/startup/
docker exec oracle-19c-db /opt/oracle/scripts/startup/01-create-sequences.sql
```

## Troubleshooting

### Common Issues

#### 1. Container Won't Start
```bash
# Check system resources
docker system df
docker system prune

# Increase Docker memory limit to 4GB+
```

#### 2. Connection Refused
```bash
# Check if database is ready
docker exec oracle-19c-db lsnrctl status

# Wait for "DATABASE IS READY TO USE!" message
docker-compose logs oracle-db | grep "DATABASE IS READY"
```

#### 3. Authentication Failed
```bash
# Reset password
docker exec -it oracle-19c-db ./setPassword.sh Oracle123
```

#### 4. Slow Startup
```bash
# Monitor startup progress
docker-compose logs -f oracle-db

# First startup can take 10-15 minutes
# Subsequent startups are faster (2-3 minutes)
```

### Performance Tuning

#### 1. Memory Settings
```yaml
# docker-compose.yml
deploy:
  resources:
    limits:
      memory: 6G  # Increase for better performance
      cpus: '4.0'
```

#### 2. SGA/PGA Tuning
```sql
-- Connect as system
ALTER SYSTEM SET sga_target=2G SCOPE=SPFILE;
ALTER SYSTEM SET pga_aggregate_target=1G SCOPE=SPFILE;
SHUTDOWN IMMEDIATE;
STARTUP;
```

## Production Considerations

### 1. Security
- Change default passwords
- Use Oracle Wallet for password management
- Enable SSL/TLS connections
- Configure proper user privileges

### 2. Backup Strategy
```bash
# Volume backup
docker run --rm -v oracle_data:/data -v $(pwd):/backup alpine tar czf /backup/oracle-backup.tar.gz /data
```

### 3. Monitoring
- Enable Oracle Enterprise Manager
- Configure database alerts
- Monitor tablespace usage
- Set up log rotation

### 4. High Availability
- Consider Oracle RAC for clustering
- Implement Data Guard for standby
- Use Oracle GoldenGate for replication

## Useful Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f oracle-db

# Connect to database
docker exec -it oracle-19c-db sqlplus system/Oracle123@ORCLPDB1

# Check database status
docker exec oracle-19c-db lsnrctl status

# Backup database
docker exec oracle-19c-db rman target /

# Monitor resources
docker stats oracle-19c-db
```

## License Information

Oracle Database Enterprise Edition requires a valid Oracle license for production use. 
Please ensure compliance with Oracle licensing terms before deploying to production environments.

For development and testing purposes, Oracle provides a free license with certain limitations. 