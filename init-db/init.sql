-- =============================================
-- 계층 구조 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS site (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    location VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS process_main (
    id      BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL REFERENCES site(id),
    name    VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS production_line (
    id              BIGSERIAL PRIMARY KEY,
    process_main_id BIGINT NOT NULL REFERENCES process_main(id),
    name            VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS process_sub (
    id                 BIGSERIAL PRIMARY KEY,
    production_line_id BIGINT NOT NULL REFERENCES production_line(id),
    name               VARCHAR(100) NOT NULL
);

-- =============================================
-- 설비 마스터
-- =============================================
CREATE TABLE IF NOT EXISTS equipment (
    equipment_id   VARCHAR(50) PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    process_sub_id BIGINT REFERENCES process_sub(id)
);

-- =============================================
-- 스펙 테이블 (경계값 + 오차율)
-- =============================================
CREATE TABLE IF NOT EXISTS equipment_spec (
    id            BIGSERIAL PRIMARY KEY,
    equipment_id  VARCHAR(50)   NOT NULL,
    event_type    VARCHAR(50)   NOT NULL,
    threshold     NUMERIC(10,2) NOT NULL,
    tolerance_pct NUMERIC(5,2)  NOT NULL DEFAULT 5.0,
    UNIQUE (equipment_id, event_type)
);

-- =============================================
-- 이벤트 이력
-- =============================================
CREATE TABLE IF NOT EXISTS equipment_event (
    id           BIGSERIAL PRIMARY KEY,
    equipment_id VARCHAR(50)    NOT NULL,
    event_type   VARCHAR(50)    NOT NULL,
    value        NUMERIC(10, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_event_equipment_id ON equipment_event (equipment_id);
CREATE INDEX IF NOT EXISTS idx_event_status       ON equipment_event (status);
CREATE INDEX IF NOT EXISTS idx_event_created_at   ON equipment_event (created_at DESC);

-- =============================================
-- 시드: SITE
-- SK On 국내 주요 공장 2곳
-- =============================================
INSERT INTO site (name, location) VALUES
    ('서산 공장', '충남 서산'),
    ('충주 공장', '충북 충주')
ON CONFLICT DO NOTHING;

-- =============================================
-- 시드: 대공정
-- 배터리 셀 제조 4대 공정
-- =============================================
INSERT INTO process_main (site_id, name) VALUES
    -- 서산 공장
    (1, '전극 공정'),   -- 1
    (1, '조립 공정'),   -- 2
    (1, '화성 공정'),   -- 3
    (1, '검사/출하'),   -- 4
    -- 충주 공장
    (2, '전극 공정'),   -- 5
    (2, '조립 공정')    -- 6
ON CONFLICT DO NOTHING;

-- =============================================
-- 시드: 라인
-- =============================================
INSERT INTO production_line (process_main_id, name) VALUES
    -- 서산 전극 공정
    (1, '양극 라인'),   -- 1
    (1, '음극 라인'),   -- 2
    -- 서산 조립 공정
    (2, '권취 라인'),   -- 3
    (2, '케이스 라인'), -- 4
    -- 서산 화성 공정
    (3, '충방전 라인'), -- 5
    -- 서산 검사/출하
    (4, '검사 라인'),   -- 6
    -- 충주 전극 공정
    (5, '양극 라인'),   -- 7
    (5, '음극 라인')    -- 8
ON CONFLICT DO NOTHING;

-- =============================================
-- 시드: 소공정
-- =============================================
INSERT INTO process_sub (production_line_id, name) VALUES
    -- 양극 라인 (서산)
    (1, '믹싱'),       -- 1  NMP 용매 + 양극재 혼합
    (1, '코팅'),       -- 2  집전체(알루미늄박) 도포
    (1, '압연'),       -- 3  전극 두께/밀도 조정
    (1, '슬리팅'),     -- 4  전극 폭 절단
    -- 음극 라인 (서산)
    (2, '믹싱'),       -- 5  흑연 + 바인더 혼합
    (2, '코팅'),       -- 6  집전체(구리박) 도포
    (2, '압연'),       -- 7
    (2, '슬리팅'),     -- 8
    -- 권취 라인 (서산)
    (3, '노칭'),       -- 9  탭 형성
    (3, '권취/적층'),  -- 10 젤리롤 제조
    -- 케이스 라인 (서산)
    (4, '탭 용접'),    -- 11
    (4, '전해액 주입'),-- 12
    (4, '밀봉'),       -- 13
    -- 충방전 라인 (서산)
    (5, '초기 충전'),  -- 14
    (5, '에이징'),     -- 15
    (5, '디가싱'),     -- 16
    -- 검사 라인 (서산)
    (6, 'OCV 검사'),   -- 17
    (6, '용량 검사'),  -- 18
    (6, '외관 검사'),  -- 19
    -- 양극 라인 (충주)
    (7, '믹싱'),       -- 20
    (7, '코팅'),       -- 21
    -- 음극 라인 (충주)
    (8, '믹싱'),       -- 22
    (8, '코팅')        -- 23
ON CONFLICT DO NOTHING;

-- =============================================
-- 시드: 설비 마스터
-- =============================================
INSERT INTO equipment (equipment_id, name, process_sub_id) VALUES
    -- 서산 공장
    ('EQ-A', '양극 믹서 #1',   1),   -- 서산 양극 믹싱
    ('EQ-B', '양극 코터 #1',   2),   -- 서산 양극 코팅
    ('EQ-C', '압연기 #1',      3),   -- 서산 양극 압연
    -- 충주 공장
    ('EQ-D', '양극 믹서 #1',  20),   -- 충주 양극 믹싱
    ('EQ-E', '양극 코터 #1',  21),   -- 충주 양극 코팅
    ('EQ-F', '음극 믹서 #1',  22),   -- 충주 음극 믹싱
    ('EQ-G', '음극 코터 #1',  23)    -- 충주 음극 코팅
ON CONFLICT DO NOTHING;

-- =============================================
-- 시드: 스펙값 (공정별 실제 기준에 근거)
-- =============================================
INSERT INTO equipment_spec (equipment_id, event_type, threshold, tolerance_pct) VALUES
    -- 서산 EQ-A 양극 믹서
    ('EQ-A', 'TEMPERATURE',    30.0, 5.0),
    ('EQ-A', 'OPERATION_RATE', 90.0, 5.0),
    ('EQ-A', 'DEFECT_RATE',     0.5, 5.0),
    -- 서산 EQ-B 양극 코터 (건조 오븐 온도 기준)
    ('EQ-B', 'TEMPERATURE',   140.0, 5.0),
    ('EQ-B', 'OPERATION_RATE', 85.0, 5.0),
    ('EQ-B', 'DEFECT_RATE',     0.3, 5.0),
    -- 서산 EQ-C 압연기
    ('EQ-C', 'TEMPERATURE',    50.0, 5.0),
    ('EQ-C', 'OPERATION_RATE', 88.0, 5.0),
    ('EQ-C', 'DEFECT_RATE',     0.3, 5.0),
    -- 충주 EQ-D 양극 믹서 (서산과 동일 공정, 동일 기준)
    ('EQ-D', 'TEMPERATURE',    30.0, 5.0),
    ('EQ-D', 'OPERATION_RATE', 90.0, 5.0),
    ('EQ-D', 'DEFECT_RATE',     0.5, 5.0),
    -- 충주 EQ-E 양극 코터
    ('EQ-E', 'TEMPERATURE',   140.0, 5.0),
    ('EQ-E', 'OPERATION_RATE', 85.0, 5.0),
    ('EQ-E', 'DEFECT_RATE',     0.3, 5.0),
    -- 충주 EQ-F 음극 믹서 (음극은 흑연 기반 - 온도 기준 다름)
    ('EQ-F', 'TEMPERATURE',    35.0, 5.0),
    ('EQ-F', 'OPERATION_RATE', 90.0, 5.0),
    ('EQ-F', 'DEFECT_RATE',     0.5, 5.0),
    -- 충주 EQ-G 음극 코터 (구리박 코팅 - 건조 온도 낮음)
    ('EQ-G', 'TEMPERATURE',   110.0, 5.0),
    ('EQ-G', 'OPERATION_RATE', 85.0, 5.0),
    ('EQ-G', 'DEFECT_RATE',     0.3, 5.0)
ON CONFLICT (equipment_id, event_type) DO NOTHING;
