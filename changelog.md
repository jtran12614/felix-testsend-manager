# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

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

