# Smart Factory Event Hub

> **배터리 제조 공정 실시간 이벤트 수집·통합·모니터링 플랫폼**  
> 배터리 생산 공정을 모델로 구현한 제조 미들웨어 아키텍처 포트폴리오

---

## 프로젝트 개요

| 항목 | 내용 |
|------|------|
| **목적** | 제조 설비 데이터를 실시간으로 수집·연동하고, 이상 감지 및 공정 현황을 통합 모니터링 |
| **모델 도메인** | 배터리 제조 공정 모델 (전극 → 조립 → 화성 → 검사/출하) |
| **적용 아키텍처** | Event-Driven Architecture (EDA) + 메시지 브로커 기반 시스템 통합 |
| **구현 기간** | 2025.06 |

---

## 직무 연관성

이 프로젝트는 제조 솔루션(미들웨어) 분야 전문가 직무에서 요구하는
시스템 통합, 이벤트 기반 아키텍처, 메시징 처리, 제조 데이터 모니터링 역량을 보여주기 위해 구현했습니다.

- 제조 설비 이벤트를 메시지 브로커 기반으로 수집·라우팅
- MES Service에서 이벤트 수신, 이상 판별, 데이터 저장 처리
- 공장/공정/라인/설비 계층 기반 통합 모니터링
- RabbitMQ 구조를 TibRV, IBM MQ 등 엔터프라이즈 메시징 시스템으로 확장 가능한 형태로 설계
- Docker Compose 기반으로 전체 서비스를 일괄 기동 가능하게 구성

---

## 시스템 아키텍처

```
┌─────────────────────┐     AMQP      ┌──────────────────┐     JPA      ┌────────────┐
│  Equipment          │  ──────────▶  │   MES Service    │  ──────────▶ │ PostgreSQL │
│  Simulator          │   RabbitMQ    │  (EAI 미들웨어)   │              │            │
│  (설비 데이터 생성)  │  Topic Exchange│  이상 감지·분류   │              │            │
└─────────────────────┘               └──────────────────┘              └────────────┘
                                               │
                                          REST API
                                               │
                                      ┌────────────────┐
                                      │   Dashboard    │
                                      │   (Vue 3 SPA)  │
                                      │  실시간 모니터링 │
                                      └────────────────┘
```

### 컨테이너 구성 (Docker Compose)

| 컨테이너 | 역할 | 기술 |
|----------|------|------|
| `rabbitmq` | 메시지 브로커 | RabbitMQ 3 (management UI 포함) |
| `postgresql` | 이벤트·공정 데이터 저장 | PostgreSQL 15 |
| `equipment-simulator` | 설비 데이터 발행 (Publisher) | Spring Boot 3, AMQP |
| `mes-service` | 이벤트 수신·처리·API 제공 (EAI) | Spring Boot 3, Spring Data JPA |
| `dashboard` | 공정 모니터링 대시보드 | Vue 3, Nginx |

```bash
docker compose up -d   # 5개 컨테이너 일괄 기동
```

---

## 핵심 구현 내용

### 1. Event-Driven 메시지 통합 (EAI 미들웨어)

- **TopicExchange** (`factory.events`) + **라우팅 키** (`equipment.#`) 패턴으로 설비 유형별 이벤트 라우팅
- `Jackson2JsonMessageConverter` + `JavaTimeModule`을 적용한 **JSON 직렬화/역직렬화**
- `@RabbitListener`로 비동기 이벤트 수신 후 즉시 이상 판별 처리

```
설비 → Simulator가 3초마다 TEMPERATURE / OPERATION_RATE / DEFECT_RATE 이벤트 발행
      → RabbitMQ가 mes.queue로 라우팅
      → MES Service가 수신 → 스펙 기반 이상 판별 → PostgreSQL 저장
```

### 2. 스펙 기반 3단계 이상 감지

설비별·이벤트 유형별 경계값(threshold)과 허용 오차율(tolerance%)을 DB에서 관리하며, 수신 데이터를 실시간 분류합니다.

| 상태 | 판정 기준 | 예시 (온도, 경계 140℃, 오차 5%) |
|------|----------|--------------------------------|
| **NORMAL** | 정상 범위 | < 133℃ |
| **WARNING** | 경계 근접 (사전 이상 감지) | 133℃ ~ 140℃ |
| **ALERT** | 경계값 초과 | > 140℃ |

```java
// EventProcessingService.java - 온도 기준 판별 로직
double margin = threshold * (tolerancePct / 100.0);
if (value > threshold)              return "ALERT";
if (value > threshold - margin)     return "WARNING";
return "NORMAL";
```

### 3. 생산 계층 구조 (4-Depth Hierarchy)

```
Site (공장)
 └─ ProcessMain (대공정)
      └─ ProductionLine (라인)
           └─ ProcessSub (소공정)
                └─ Equipment (설비)
```

- **서산 공장** : 전극(양극/음극 라인) → 조립 → 화성 → 검사/출하
- **충주 공장** : 전극(양극/음극 라인) → 조립 → 화성 → 검사/출하
- Hibernate `MultipleBagFetchException` 해결 → 레벨별 단계 쿼리 분리

### 4. 공정 모니터링 대시보드

**대시보드 (일별 통계)**
- 날짜 선택 → 사이트별 평균 가동률·수율·ALERT/WARNING 건수 집계
- 대공정 단위 상세 breakdown 및 바 차트
<img width="1912" height="914" alt="image" src="https://github.com/user-attachments/assets/b9a16035-16ce-42ee-a5ac-303133423ab9" />


**공정 모니터링 (실시간)**
- 아코디언 트리(Site > 대공정 > 라인 > 소공정)로 계층 탐색
- 선택 범위 내 설비 카드(상태·위치·최근 이벤트) 실시간 갱신 (3초 폴링)
- WARNING 목록 / ALERT 목록 위치 정보 포함 표시
<img width="1897" height="918" alt="image" src="https://github.com/user-attachments/assets/329c9f88-0496-4bc4-8220-4a44c920e76d" />

**스펙 관리**
- 설비별·이벤트 유형별 경계값·오차율 조회 및 수정
- 신규 설비 등록 시 공정 계층에 자동 연결 (equipment 테이블 자동 생성)
<img width="1897" height="918" alt="image" src="https://github.com/user-attachments/assets/e1bfced7-a6c4-4b54-9921-26d68113c11a" />


---

## 기술 스택

### Backend
| 기술 | 용도 |
|------|------|
| **Spring Boot 3** | MES Service, Equipment Simulator |
| **Spring AMQP / RabbitMQ** | 이벤트 메시지 발행·구독 |
| **Spring Data JPA / Hibernate** | 공정 데이터 ORM |
| **PostgreSQL 15** | 이벤트·공정·스펙 데이터 저장 |
| **Jackson (JavaTimeModule)** | LocalDateTime JSON 직렬화 |

### Frontend
| 기술 | 용도 |
|------|------|
| **Vue 3 (Composition API)** | 대시보드 SPA |
| **Vue Router 4** | 페이지 라우팅 |
| **Axios** | REST API 폴링 (3초) |
| **Nginx** | 프론트엔드 서빙 |

### Infra
| 기술 | 용도 |
|------|------|
| **Docker / Docker Compose** | 5개 서비스 컨테이너화·오케스트레이션 |
| **Multi-stage Dockerfile** | Maven 빌드 → JRE 런타임 / Node 빌드 → Nginx |

---

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/api/stats/daily?date=YYYY-MM-DD` | 일별 공정별 가동률·수율·이상 통계 |
| `GET` | `/api/equipments/status` | 전체 설비 실시간 상태 |
| `GET` | `/api/equipments/status/by-site/{id}` | 사이트별 설비 상태 |
| `GET` | `/api/equipments/status/by-process-main/{id}` | 대공정별 설비 상태 |
| `GET` | `/api/equipments/status/by-line/{id}` | 라인별 설비 상태 |
| `GET` | `/api/equipments/status/by-process-sub/{id}` | 소공정별 설비 상태 |
| `GET` | `/api/events/alerts` | ALERT 이벤트 목록 |
| `GET` | `/api/events/warnings` | WARNING 이벤트 목록 |
| `GET` | `/api/hierarchy` | 공정 계층 전체 구조 |
| `GET` | `/api/specs` | 설비 스펙 목록 |
| `POST` | `/api/specs` | 설비 스펙 등록·수정 |

---

## 로컬 실행 방법

**사전 요구사항**: Docker Desktop

```bash
git clone https://github.com/fwangjuwon/smart-factory-event-hub.git
cd smart-factory-event-hub
docker compose up -d
```

| 서비스 | URL |
|--------|-----|
| 대시보드 | http://localhost:3000 |
| MES API | http://localhost:8081 |
| RabbitMQ 관리 콘솔 | http://localhost:15672 (guest/guest) |

---

## 폴더 구조

```
smart-factory-event-hub/
├── docker-compose.yml
├── init-db/
│   └── init.sql                  # 공정 계층·설비·스펙 초기 데이터
├── equipment-simulator/          # Spring Boot - 설비 이벤트 발행
│   └── src/main/java/com/factory/simulator/
│       ├── config/RabbitMQConfig.java
│       └── service/EquipmentPublisher.java
├── mes-service/                  # Spring Boot - EAI 미들웨어
│   └── src/main/java/com/factory/mes/
│       ├── controller/           # REST API
│       ├── service/              # 이상 감지, 통계, 계층, 스펙
│       ├── repository/           # Spring Data JPA
│       ├── entity/               # JPA 엔티티 (5-depth 계층)
│       └── dto/                  # 응답 DTO
└── dashboard/                    # Vue 3 SPA
    └── src/
        ├── views/                # DashboardView, HierarchyView, SpecManagementView
        ├── components/           # EquipmentCard, AlertList, WarningList, HierarchyTree
        └── api/factoryApi.js
```

---

## 설계 의도 및 기술적 고려사항

### EAI 미들웨어 관점
실제 제조 현장에서 설비(EMS)와 MES 사이에는 다양한 프로토콜·데이터 포맷 차이가 존재합니다. 이 프로젝트에서는 RabbitMQ를 메시지 브로커(EAI 미들웨어)로 두어 설비 시뮬레이터와 MES 서비스를 느슨하게 결합(Loose Coupling)시켰습니다. 실제 환경에서 TibRV·IBM MQ 등 엔터프라이즈 MQ로 대체하더라도 Consumer 측 코드 변경을 최소화할 수 있는 구조입니다.

### 이상 감지 로직
단순 임계값 초과 감지를 넘어, 허용 오차율(tolerance%) 기반의 WARNING 사전 감지 구간을 두어 실제 공정에서 선제 대응이 가능한 구조를 설계했습니다.

### 확장성
- 설비 ID 목록 추가만으로 신규 설비 모니터링 즉시 가능
- 공정 계층을 DB로 관리하므로 공장·라인 추가 시 코드 수정 불필요
- 스펙(경계값·오차율) 런타임 변경 가능 (재배포 불필요)
