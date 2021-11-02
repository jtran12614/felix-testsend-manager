DROP TABLE IF EXISTS `test_send_histories`;
CREATE TABLE `test_send_histories`
(
    `id`          int(11)    NOT NULL AUTO_INCREMENT,
    `job_id`      int(11)  DEFAULT NULL,
    `bundle_type` tinyint(4) NOT NULL,
    `bundle_id`   int(11)    NOT NULL,
    `status`      tinyint(4) NOT NULL,
    `info`        text,
    `started`     datetime   NOT NULL,
    `finished`    datetime DEFAULT NULL,
    `version`     int(11)    NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `job_id` (`job_id`),
    KEY `idx_job_id` (`job_id`) USING BTREE,
    KEY `idx_bundle` (`bundle_type`, `bundle_id`) USING BTREE
);
