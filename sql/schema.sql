CREATE TABLE IF NOT EXISTS test_send_histories (
  id          INT(11)    NOT NULL AUTO_INCREMENT,
  job_id      INT(11)    UNIQUE,
  bundle_type TINYINT(4) NOT NULL,
  bundle_id   INT(11)    NOT NULL,
  status      TINYINT(4) NOT NULL,
  info        MEDIUMTEXT NULL,
  started     DATETIME   NOT NULL,
  finished    DATETIME   NULL,
  `version`   INT(11)    NOT NULL,
  PRIMARY KEY (id),
  KEY idx_job_id (job_id) USING BTREE,
  KEY idx_bundle (bundle_type, bundle_id) USING BTREE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;