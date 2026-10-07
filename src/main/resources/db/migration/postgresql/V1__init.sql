CREATE TABLE "user" (
  id              VARCHAR(36)   PRIMARY KEY,
  username        VARCHAR(50)   NOT NULL UNIQUE,
  password_hash   VARCHAR(100)  NOT NULL,
  name            VARCHAR(100)  NOT NULL,
  phone           VARCHAR(20),
  email           VARCHAR(100),
  note            VARCHAR(500),
  user_type       VARCHAR(20)   NOT NULL,
  is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
  last_login_time TIMESTAMP,
  creator_id      VARCHAR(36),
  creator_name    VARCHAR(100),
  updator_id      VARCHAR(36),
  updator_name    VARCHAR(100),
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_user_type ON "user" (user_type);
CREATE INDEX idx_user_is_active ON "user" (is_active);

CREATE TABLE operation_log (
  id             VARCHAR(36)  PRIMARY KEY,
  user_id        VARCHAR(36),
  operation      VARCHAR(200),
  details        TEXT,
  ip_address     VARCHAR(50),
  user_agent     VARCHAR(500),
  execution_time INTEGER,
  status         VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
  created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_operation_log_user_id ON operation_log (user_id);
CREATE INDEX idx_operation_log_created_at ON operation_log (created_at DESC);

CREATE TABLE option (
  id    VARCHAR(36)  PRIMARY KEY,
  type  VARCHAR(100) NOT NULL,
  name  VARCHAR(100) NOT NULL,
  value VARCHAR(500),
  note  VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX idx_option_type_name ON option (type, name);
