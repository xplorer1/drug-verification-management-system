CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS telemetry_readings (
    id BIGSERIAL,
    batch_id BIGINT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    location VARCHAR(255),
    device_id VARCHAR(100),
    PRIMARY KEY (id, timestamp)
);

SELECT create_hypertable('telemetry_readings', 'timestamp', if_not_exists => TRUE);
