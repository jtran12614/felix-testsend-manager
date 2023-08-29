# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## 1.3.0 - 2023-MM-dd
### Changed
- Support inline addresses for triggerTestSend

## 1.2.6 - 2022-11-30
### Changed
- FELIX-5680 Add endpoint for updating test send history

## 1.2.5 - 2022-09-29
### Changed
- FELIX-5306 Add headers for backward compatible
- FELIX-5306 Add httptrace configuration and git auto tag
- FELIX-5776 Proxy migration changes

## 1.2.4 - 2022-09-15
### Fixed
- FELIX-4409 Gradle downgrade due to incompatibility with JAR publish script
### Added
- FELIX-5306 New api in Trigger Test Send Manager to register the workflow for test experiment

## 1.2.3 - 2021-11-29
### Added
- Metadata for SBA
- FELIX-4151 Test send history initialization API
- [FELIX-3715](https://jira.rakuten-it.com/jira/browse/FELIX-3715) Add trace log
### Changed
- FELIX-4291 Update SBA endpoints
- Update dependencies, deprecate object mapper wrapper and JSONObject

## 1.2.2 - 2020-10-22
### Changed
- Ansible script for changing git repository url

## 1.2.2
### Fixed
- Changed info object to map

## 1.2.1
### Fixed
- Reverting Unsubscribe URL for mail content

## 1.2.0
### Added
- Endpoints:
  - Get histories by bundle_id, bundle_type (info is not included)
  - Get history detail by history_id.

## 1.1.8
### Fixed
- Unique mail recipients addresses in info for mail bundle type. 

## 1.1.7
### Added
- Logback-spring
- Fix for mail channel test sending
- Revert MU attribute tag to original one 

## 1.1.6 - 2020-05-12
### Added
- Add Hikari configuration

## 1.1.5 - 2020-01-14
### Added
- Added controller for line

## 1.1.4 - 2018-06-25
### Fixed
- Ansible script for deployment

## 1.1.3 - 2018-06-21
### Fixed
- Ansible script for app template config
- Reserve campaign response json parameter

## 1.1.1 - 2018-06-01
### Fixed
- Ansible script for deployment

## 1.1.0 - 2018-05-30
### Changed
- Support for Job processor feature
- Ansible app default configuration files

## 1.0.1 - 2018-05-23
### Fixed
- Ansible app default configuration files
  - Stating api proxy port

## 1.0.0 - 2018-04-25
### Changed
- Update spring version 1.5 to 2.0

## 0.1.0 - 2017-12-05
### Added
- HTTP Endpoint
  - Get test send history by id.
  - Get test send history by job id.
  - Get test send histories by bundle id and bundle type and pageable parameter.  
  - Kick mail test job with bundle id and bundle type and mail job object.
- MESSAGING Endpoint
  - Listening channels:
    - Kicking test send is finished
    - Test send is finished
    - Test send is finished on error

