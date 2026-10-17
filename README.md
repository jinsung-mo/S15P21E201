# 가볼래 (GABOLLE)

> 부산의 모든 여행, 가볼래?

SSAFY 15기 자율 프로젝트, 부울경 E201, 6인 팀

## 어떤 서비스인가

외국인이 부산 여행을 준비할 때는 유명한 장소를 찾는 것만으로 일정이 끝나지 않습니다. 며칠 동안 어디를 갈지, 얼마나 걸을 수 있는지, 식사 제약에 맞는지까지 따져야 합니다. 가볼래는 여행자의 취향과 예산, 이동 조건을 받아 장소를 고르고 날짜별 일정을 만드는 서비스입니다. 여행 중에는 지도와 현장 도구를 열어 계획을 확인할 수 있습니다.

이 README에는 시연 영상에서 확인한 화면과 공개 저장소에서 확인한 구현을 담았습니다. 팀이 함께 만든 서비스와 제가 직접 맡은 서버 작업은 아래에서 구분해 설명합니다.

## 사용 흐름

1. 홈에서 장소와 축제를 살펴봅니다.
2. 날짜, 인원, 출발지, 숙소, 교통수단, 예산을 입력하고 취향과 이동 조건을 고릅니다.
3. 추천 코스를 골라 날짜별 일정을 만들고 지도에서 이동 순서를 확인합니다.
4. 여행 중에는 필요한 한국어 문장과 현장 도구를 열고, AI 도우미에게 앱 사용 방법을 물어볼 수 있습니다.

아래 영상과 이미지는 모두 `가볼래_시연.mp4`의 앱 화면에서 가져왔습니다. 7분 46초 원본은 용량 때문에 저장소에 넣지 않았고, 핵심 장면을 잇는 36초 영상만 올렸습니다. 설계 문서에 적힌 기능이 모두 영상에서 시연된 것은 아닙니다.

## 앱 시연

홈에서 여행 조건을 입력하고 지도에서 경로를 확인하는 장면입니다. 아래 미리보기를 누르면 36초 MP4를 볼 수 있습니다.

<p align="center">
  <a href="docs/demo/app-highlight.mp4"><img src="docs/demo/app-highlight.gif" alt="가볼래 앱에서 날짜와 취향을 고르고 여행 경로를 확인하는 시연" width="280" /></a>
</p>

[시연 영상 보기 (MP4, 36초)](docs/demo/app-highlight.mp4)

원본의 홈(00:00~00:06), 날짜와 인원(01:58~02:08), 취향 선택(02:56~03:04), 경로 확인(05:11~05:23)을 배속 없이 연결했습니다. 음성은 제외했습니다.

## 시연 화면

| 홈 | 장소 상세 | 부산 축제 |
|:---:|:---:|:---:|
| ![홈 화면의 여행 시작과 기록 피드](docs/demo/home.jpg) | ![감천문화마을의 경사와 계단 정보](docs/demo/place.jpg) | ![부산 축제 목록과 일정 추가](docs/demo/festival.jpg) |

홈에서 여행을 시작하고 다른 여행 기록과 축제를 볼 수 있습니다. 장소 상세에는 주소와 경사, 계단 정보가 나옵니다. 확인하지 못한 정보는 **미확인**으로 표시합니다.

| 여행 기본 정보 | 취향 선택 | 조건 확인 |
|:---:|:---:|:---:|
| ![출발지 숙소 교통수단과 여행 시간 입력](docs/demo/plan-basic.jpg) | ![여행 카테고리와 여행 속도 선택](docs/demo/plan-taste.jpg) | ![트립 패스에서 날짜 예산과 보행 조건 확인](docs/demo/plan-pass.jpg) |

여행 만들기에서는 날짜와 인원부터 예산까지 차례로 입력합니다. 취향 선택은 건너뛸 수 있지만 알레르기, 식단, 이동 제약은 따로 확인합니다. 마지막 *트립 패스*에서 입력값을 고친 뒤 일정을 만듭니다.

| 일정 지도 | 현장에서 쓰는 한국어 | AI 도우미 |
|:---:|:---:|:---:|
| ![여섯 장소를 잇는 지도와 구간별 경사 표시](docs/demo/route-map.jpg) | ![관광지 식당 택시 숙소에서 쓸 한국어 문장](docs/demo/korean-phrases.jpg) | ![해운대 음식 질문을 여행 만들기로 연결하는 AI 도우미](docs/demo/ai-chat.jpg) |

지도에는 장소 순서, 예상 이동 시간, 구간별 경사 적합도가 나옵니다. 한국어 문장 카드와 환율 계산, 주변 버스, 긴급 도움 화면도 앱에서 열 수 있습니다.

### 영상 속 AI 도우미

시연 영상에서 해운대 근처 음식을 묻자 AI 도우미는 맛집 위주의 일정을 만들 수 있다고 안내하고 `여행 만들기` 버튼을 보여줍니다. 특정 식당을 지어내 추천하지는 않습니다.

화면과 식단, 이동 조건의 기준은 [팀 공유 문서](docs/gabolle/README.md)에 있습니다.

## 팀 구성과 역할

여섯 명이 맡은 주 영역입니다. 기능 하나를 한 사람만 만든 것은 아니어서, 아래 표는 팀의 작업 구분을 보여줍니다.

| 이름 | 주 담당 | 맡은 영역 |
|---|---|---|
| 박재현 | 백엔드 | Spring 서버와 공통 API |
| 진미리 | 프런트엔드 | 여행 화면과 사용자 흐름 |
| 모진성 (저) | 데이터와 DB | PostgreSQL 저장과 검증, 여행 및 일정 서버 기능, AI 도우미 서버 기능 |
| 장효준 | 추천 알고리즘 | 장소 제약 판정과 추천 로직 |
| 고지혁 | 개인화와 MLOps | 개인화 실험, 모델과 배치 작업 환경 |
| 이예승 | 인프라, 배포, QA | 서비스 배포와 품질 점검 |

저는 여행 생성 요청이 중복될 때 데이터가 늘어나지 않도록 고쳤고, 동행자가 같은 일정을 동시에 수정할 때 한 사람의 변경이 사라지지 않게 했습니다. 실제 PostgreSQL 테스트에서 발견한 오류와 이벤트 저장 경로도 다뤘습니다. 화면과 추천 알고리즘, 배포는 각 담당 팀원의 작업입니다.

## 기술 구성

| 영역 | 사용 기술 |
|---|---|
| 앱 | Expo, React Native, TypeScript, Expo Router |
| 서버 | Java 17, Spring Boot, Spring Security |
| 저장소 | PostgreSQL, Spring Data JPA, Flyway |
| 데이터와 추천 | Node.js와 Python으로 장소 데이터를 가공, Spring에서 추천 후보 선정과 순위 계산 |
| 운영 | Docker Compose, Jenkins, Nginx |

앱 코드는 `frontend/`, 서버 코드는 `backend/`, 장소와 교통, 지형 데이터 작업은 `bigData/`에 있습니다. 추천 후보 선정과 순위 계산은 현재 저장소의 Spring 서버 안에 있습니다. 기능별 실제 구현과 실행 조건은 각 폴더의 README를 확인해 주세요.

### 이 기술들이 프로젝트에 맞았던 이유

아래 비교는 모든 기술을 도입할 때 실제로 다른 제품을 놓고 평가했다는 뜻은 아닙니다. **Expo와 PostgreSQL은 팀 문서에 선택 근거가 남아 있고**, 나머지는 현재 코드와 배포 구성을 보고 이 프로젝트에서 얻는 이점을 정리했습니다. 어느 기술이 무조건 더 좋다는 비교도 아닙니다.

| 사용한 기술 | 다른 선택지와 비교해 이 프로젝트에 맞는 점 |
|---|---|
| **Expo, React Native** | 웹과 모바일 화면을 각각 만드는 방식보다 화면 코드를 함께 관리하기 좋았습니다. 팀 개발 환경이 Windows라 iOS를 로컬에서 빌드하기 어려웠는데, Expo의 클라우드 빌드로 iOS 빌드를 진행할 수 있었습니다. 네이티브 기능이 복잡해지면 별도 설정이 필요하다는 부담은 있습니다. [프런트엔드의 선택 기록](frontend/README.md), [Expo 빌드 설명](https://docs.expo.dev/build/) |
| **TypeScript** | JavaScript만 쓸 때보다 여행 조건과 API 응답의 형태를 코드에서 확인하기 쉽습니다. 다만 타입 검사는 서버 응답이 실제로 맞는지까지 보장하지 않으므로 별도 테스트가 필요합니다. [앱 설정과 타입 검사 명령](frontend/package.json) |
| **Expo Router** | 화면 주소를 일일이 등록하는 방식보다 파일 경로에 맞춰 관리하기 편합니다. 웹과 앱에서 같은 화면 구조를 다루는 이 프로젝트에 맞지만, 파일 배치가 곧 주소가 되므로 구조를 바꿀 때 주소도 확인해야 합니다. [화면 폴더](frontend/app/), [Expo Router 설명](https://docs.expo.dev/router/introduction/) |
| **Java 17, Spring Boot, Spring Security** | 인증과 DB 저장이 많은 서버를 Express처럼 미들웨어부터 직접 조립하는 방식과 비교하면, 트랜잭션(여러 저장 작업을 한 번에 성공시키거나 취소하는 경계)과 로그인 검사를 같은 틀에서 다루기 편합니다. 팀 PC와 자동 빌드 환경의 Java 버전도 17로 맞췄습니다. 대신 설정과 실행 환경은 더 무겁습니다. [서버 실행 기준](backend/README.md), [인증 설정](backend/src/main/java/com/gabolle/backend/auth/config/SecurityConfig.java) |
| **PostgreSQL** | MySQL이나 SQLite로도 여행 데이터를 저장할 수 있지만, 여기서는 중복 요청을 막는 고유 제약과 `ON CONFLICT`(중복 저장 시 오류 대신 정해 둔 동작을 하는 SQL)를 실제 저장 코드에서 씁니다. 백엔드와 개인화 작업의 표를 스키마(한 DB 안의 표 묶음)로 나누는 운영 기준도 있습니다. 이는 다른 DB가 동시성 문제를 해결하지 못한다는 뜻이 아니라, 현재 구현에 PostgreSQL 기능이 직접 쓰인다는 뜻입니다. [DB 선택 기록](docs/DB-STANDARD.md), [여행 저장 코드](backend/src/main/java/com/gabolle/backend/trip/infra/JpaTripRepository.java) |
| **Spring Data JPA** | 모든 조회와 저장을 손으로 SQL로 작성하는 대신, 일반적인 데이터 저장은 JPA에 맡겼습니다. 다만 중복 요청과 일정 충돌처럼 DB의 실제 동작이 중요한 곳은 SQL을 직접 사용했습니다. [여행 저장 코드](backend/src/main/java/com/gabolle/backend/trip/infra/JpaTripRepository.java) |
| **Flyway** | 서버가 테이블을 그때그때 자동 생성하는 방식보다, 버전별 SQL 파일로 구조 변경을 남기는 편이 여러 팀원이 같은 DB 구조를 유지하기 좋습니다. 개발과 테스트, 배포 환경에 적용할 변경을 코드와 함께 추적할 수 있습니다. [DB 변경 기준](docs/DB-STANDARD.md), [변경 파일](backend/src/main/resources/db/migration/) |
| **Node.js, Python 데이터 작업** | 장소 수집과 가공을 사용자 요청을 받는 Spring 서버 안에서 모두 처리하지 않고 별도 작업으로 뒀습니다. 한 언어로 통일하면 관리할 도구는 줄지만, 이미 작성된 수집 스크립트와 분석 코드를 각 환경에서 실행하는 편이 이 저장소에서는 자연스럽습니다. 두 언어를 섞어서 처리 속도가 더 빠르다는 뜻은 아닙니다. [데이터 작업 목록](bigData/package.json), [Python 의존성](bigData/requirements.txt) |
| **Docker Compose** | 로컬에서 DB와 서버를 각각 설치하고 주소를 맞추는 대신, 두 컨테이너와 연결 관계를 한 파일로 띄웁니다. 개인 PC의 설정 차이를 줄이지만 Docker 실행 환경은 필요합니다. [로컬 실행 설정](backend/docker-compose.yml) |
| **Jenkins** | GitHub Actions로 새 배포 경로를 만드는 대신, 팀 GitLab 브랜치와 서버 자격 증명에 연결된 작업을 사용합니다. 자동 빌드 뒤 실제 컨테이너의 상태를 확인하도록 짜여 있습니다. 다만 서버와 작업 설정을 팀이 직접 관리해야 합니다. [서버 배포 작업](backend/Jenkinsfile), [웹 배포 작업](frontend/Jenkinsfile) |
| **Nginx** | 웹 파일을 Spring 서버에서 함께 제공하는 방식과 달리, 웹 파일 제공과 API 요청 전달을 나눴습니다. 웹 컨테이너는 파일과 새로고침 경로를 처리하고, 서버 앞의 Nginx는 `/api/` 요청을 백엔드로 보냅니다. 설정이 두 곳에 있어 경로를 잘못 맞추면 정상 화면 뒤에서 API만 실패할 수 있습니다. [웹 컨테이너 설정](frontend/nginx.conf), [서버 앞단 설정](docs/SERVER-SETUP.md) |

### 전체 아키텍처

![앱과 웹에서 Nginx, Spring Boot, PostgreSQL로 이어지는 서비스와 별도로 배치한 데이터 가공 및 개인화 실험 환경](docs/architecture/system-overview.svg)

위쪽은 사용자 요청이 지나는 경로이고, 아래쪽은 따로 운영하는 데이터 가공과 개인화 실험 환경입니다. PostgreSQL의 Outbox는 업무 데이터와 함께 이벤트를 먼저 저장하는 테이블입니다.

현재 공개 코드의 이벤트 발행자는 실제로 메시지를 보내지 않는 `NoOpEventPublisher`라 Kafka로 이어지는 경로는 그리지 않았습니다. 데이터 가공 결과를 DB에 자동으로 적재하는 경로와 개인화 실험 결과를 온라인 추천에 연결하는 경로도 이 브랜치에서 확인하지 못했습니다. Python 경로 최적화 스크립트는 있지만, 실제 추천 요청에서 호출하는 연결은 찾지 못했습니다.

관련 코드: [웹 배포 설정](frontend/Jenkinsfile), [서버 배포 설정](backend/Jenkinsfile), [추천 서비스](backend/src/main/java/com/gabolle/backend/recommendation/application/RecommendationService.java), [데이터 수집](bigData/collect/), [데이터 가공](bigData/process/), [개인화 실험 환경](infra/personalization/compose.yaml)

### 여행 생성 API가 중복 요청을 처리하는 방식

삐용 프로젝트에서 썼던 [Archify](https://github.com/tt-a1i/archify)로 여행 생성 API의 호출 순서를 그렸습니다. 앱이 같은 요청에 동일한 `Idempotency-Key`(재요청을 알아보는 키)를 붙여 보내면, 서버는 PostgreSQL에서 키를 먼저 확보합니다. 새 키일 때는 여행과 소유자, 제약, 취향 정보를 같은 트랜잭션에 저장하고 `201`(새로 생성됨)을 반환합니다. 이미 사용한 키라면 요청 내용의 지문을 비교해, 내용이 같을 때 기존 여행을 `200`(성공)으로 돌려주고 다를 때는 `409`(충돌)로 거절합니다. **여행 생성과 추천 일정 계산은 별도 API**입니다.

<a href="docs/architecture/trip-create.html"><img src="docs/architecture/trip-create.png" alt="여행 앱, TripController, TripCreationService, JpaTripRepository, PostgreSQL 사이의 생성과 재요청 흐름" width="860" /></a>

[Archify 다이어그램 HTML](docs/architecture/trip-create.html)은 내려받아 브라우저에서 열 수 있습니다. [다이어그램 원본 JSON](docs/architecture/trip-create.sequence.json)과 [앱 요청 코드](frontend/src/api/tripApi.ts), [여행 생성 API](backend/src/main/java/com/gabolle/backend/trip/presentation/TripController.java), [DB 저장 코드](backend/src/main/java/com/gabolle/backend/trip/infra/JpaTripRepository.java)를 함께 남겼습니다.

## 현지 데이터 수집과 협업

장소 정보는 관광지 목록만으로 채우기 어려웠습니다. 팀은 장소를 방문하기 좋은 시간처럼 현지에서 알 수 있는 정보를 모으기 위해 [현지인 설문](https://j15e201.p.ssafy.io/survey/)을 진행했습니다. 장소 상세 화면의 `좋은 때` 항목은 낮, 밤, 언제든 좋다는 응답을 집계하고, 응답이 없으면 표시하지 않습니다. 이 링크는 **진행 당시 설문 주소이며 현재는 404**입니다. [화면의 집계 코드](frontend/src/place/detailExtras.ts)

장소 정보가 없다는 이유로 안전하다고 판정하지 않는 것도 중요했습니다. [팀 상세설계서](docs/gabolle/GABOLLE_기능_화면_상세설계서.md)는 제약을 `PASS / FAIL / UNKNOWN`(충족, 위반, 확인 필요)으로 나누고, 확인되지 않은 조건은 경고로 남기도록 정했습니다. 추천은 제약 판정 뒤 개인화 순위, 다양성 조정, 동선 계산으로 이어지는 구조입니다. 이 내용은 **설계 범위**이며, 시연 화면만으로 모든 단계의 운영 여부를 확인했다는 뜻은 아닙니다.

현지 장소 설문과 별도로, [짝비교 분석 코드](bigData/process/choice-fit.mjs)는 사용자가 두 경로 중 하나를 고른 응답에서 걷는 시간, 경사, 계단, 환승의 가중치를 추정합니다. 원본 응답과 분석 결과 수치는 이 공개 저장소에 없어 README에는 적지 않았습니다.

삐용 프로젝트에서 쓰던 Jira 협업 방식을 가볼래에도 맞춰 적용했습니다. [Jira 보드](https://ssafy.atlassian.net/jira/software/c/projects/S15P21E201/boards/15310)의 업무는 Epic, Story, Task로 나눴고, 브랜치와 커밋 메시지에 Jira 키를 넣어 관련 코드를 찾기 쉽게 했습니다. 기능 브랜치는 담당 파트의 개발 브랜치로 올리고, 병합 대상은 자동 검사로 확인했습니다. 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)에 남겨 두었습니다.

[Notion 회의록](https://app.notion.com/p/434b996e22f182abb32881507c548f8a?source=copy_link)에는 팀 회의와 피드백을, [요구사항 정의서](https://app.notion.com/p/313ffa9ff7524f75bb29a295653d0b1e?v=49950c1f190347ae8531dcbc8021682e&source=copy_link)에는 기능별 구현 상태와 담당자, 구현 위치를 정리했습니다. [API 명세서](https://app.notion.com/p/API-3e4240be9cfd80d988a7e1f45146abc2?source=copy_link)는 당시 백엔드 코드 기준의 공통 헤더, 응답 형식, API 목록을 담고 있어, 현재 동작은 코드와 테스트로 다시 확인해야 합니다. [와이어프레임](https://claude.ai/artifact/LUW6tRsDPiozewVLYTxcMw)도 기획에 사용했습니다. 저장소에서 바로 볼 수 있는 문서는 아래 `docs/gabolle`에 연결했습니다.

## 개발하며 겪은 문제

아래는 제가 맡은 여행, 일정, 데이터 저장 작업에서 실제로 부딪힌 문제입니다. 팀 전체의 추천 알고리즘이나 배포 작업을 제 구현으로 적지 않았습니다.

### 같은 요청으로 여행이 두 번 만들어질 수 있었다

휴대폰이 서버 응답을 받지 못하면 사용자는 여행 만들기를 다시 누를 수 있습니다. 처음 구현은 요청 키가 이미 있는지 조회하고, 없으면 여행을 만든 뒤 키를 저장했습니다. 두 요청이 거의 동시에 들어오면 둘 다 “키가 없다”는 결과를 보고 여행을 만들 수 있었습니다.

순서를 바꿔 **DB에서 요청 키를 먼저 확보한 요청만 여행을 생성**하게 했습니다. 같은 사용자와 같은 키를 한 번만 저장하도록 PostgreSQL에 `UNIQUE` 제약을 두고, 이미 키가 있으면 요청 내용을 비교합니다. 내용까지 같으면 기존 여행을 돌려주고, 다르면 충돌로 거절합니다. 이처럼 같은 요청을 다시 보내도 새 결과가 생기지 않게 처리하는 것을 *멱등성*이라고 합니다.

[여행 생성 서비스](backend/src/main/java/com/gabolle/backend/trip/application/TripCreationService.java), [DB 저장 코드](backend/src/main/java/com/gabolle/backend/trip/infra/JpaTripRepository.java), [8개 동시 요청 테스트](backend/src/test/java/com/gabolle/backend/trip/TripCreationTest.java), [PostgreSQL 통합 테스트](backend/src/test/java/com/gabolle/backend/trip/TripPersistenceIntegrationTest.java)를 남겼습니다. 동시 요청 테스트는 메모리 저장소를 쓰며, 실제 DB 제약은 별도 통합 테스트에서 확인합니다.

### 동행자의 일정 수정이 서로 덮어쓸 수 있었다

두 사람이 모두 일정의 5번 버전을 열었다고 가정해 보겠습니다. 한 사람이 6번 버전을 저장한 뒤 다른 사람도 옛 내용을 바탕으로 저장하면 앞사람의 변경이 사라질 수 있습니다. 서버에서 저장 전 최신 버전을 읽는 것만으로는 부족했습니다. 읽고 저장하는 사이에 다른 요청이 들어오기 때문입니다.

수정 요청에는 사용자가 보고 있던 버전 번호를 넣었습니다. DB에서는 같은 일정에 같은 버전이 두 번 생기지 않도록 막고, 최신 버전 번호도 사용자가 읽은 값에서 바뀌지 않았을 때만 갱신합니다. 늦게 도착한 요청에는 HTTP 409를 돌려줘 다시 확인하도록 했습니다.

[8개 동시 수정 테스트](backend/src/test/java/com/gabolle/backend/itinerary/ItineraryVersionConflictTest.java)에서는 모두 5번 버전에서 출발해 1개만 성공하고 7개가 거절됩니다. 7번 버전도 생기지 않습니다. 이 테스트는 메모리 저장소를 사용합니다. [PostgreSQL 통합 테스트](backend/src/test/java/com/gabolle/backend/itinerary/ItineraryPersistenceIntegrationTest.java)는 DB의 중복 저장과 최신 버전 갱신 충돌을 따로 확인합니다. [일정 저장 코드](backend/src/main/java/com/gabolle/backend/itinerary/infra/JpaItineraryRepository.java)

### 단위 테스트는 통과했는데 실제 DB에서 실패했다

일정 버전이 충돌하면 DB 작업의 일부만 되돌리려고 `SAVEPOINT`를 사용했습니다. 하지만 PostgreSQL 통합 테스트에서는 중첩 트랜잭션을 지원하지 않는다는 예외가 났습니다. 메모리 저장소 테스트만으로는 드러나지 않던 문제였습니다.

충돌을 예외로 받은 뒤 복구하는 대신, `INSERT ... ON CONFLICT DO NOTHING`으로 중복 저장을 건너뛰게 바꿨습니다. 저장된 행이 0개면 충돌로 처리합니다. 최신 버전 번호를 바꿀 때도 조건부 `UPDATE`를 사용합니다. 이 방식은 실패한 트랜잭션을 되살리기 위한 별도 복구 절차가 필요 없습니다. [저장 코드](backend/src/main/java/com/gabolle/backend/itinerary/infra/JpaItineraryRepository.java), [DB 검증 테스트](backend/src/test/java/com/gabolle/backend/itinerary/ItineraryPersistenceIntegrationTest.java)

### 이벤트 API는 성공했지만 기록이 남지 않았다

사용자 행동 이벤트 API는 정상 응답을 보냈지만, 서비스가 DB 저장소 대신 메모리 저장소를 참조하고 있었습니다. 서버를 다시 시작하면 기록이 사라졌습니다. 어느 저장소 구현을 호출하는지 추적해 DB의 **Outbox** 저장 경로로 연결했습니다. Outbox는 업무 데이터와 이벤트를 같은 DB에 먼저 기록하는 방식입니다.

제가 수정한 범위는 이벤트 수집 경로입니다. Outbox 전체 설계는 팀 작업이며, 현재 공개 코드의 발행자는 실제로 메시지를 보내지 않는 `NoOpEventPublisher`입니다. 따라서 Kafka로 이벤트가 전달된다고 주장하지 않습니다. [이벤트 수집 서비스](backend/src/main/java/com/gabolle/backend/event/application/EventIngestService.java), [Outbox 저장 서비스](backend/src/main/java/com/gabolle/backend/event/application/OutboxService.java)

### AI 도우미는 답할 수 있는 범위를 좁혔다

존재하지 않는 식당을 알려주거나 환율, 버스 도착시간을 임의로 말하면 여행자에게 피해가 갈 수 있습니다. 제가 맡은 서버 기능에서는 AI가 장소나 실시간 수치를 확정하기보다 앱의 적절한 화면으로 안내하게 했습니다. 이동할 수 있는 화면 주소도 서버의 허용 목록으로 검사하고, 여행 조건은 적용 전에 사용자가 확인합니다.

시연 영상의 해운대 음식 질문도 특정 식당 추천이 아니라 `여행 만들기`로 연결됩니다. AI 도우미 서버 코드는 팀의 `back/dev` 브랜치에 있고 이 GitHub `main`에는 아직 포함되지 않았습니다. 위 설명은 시연 화면과 팀 브랜치에서 확인한 범위입니다.

## 돌아보며

가장 어려웠던 부분은 **같은 코드가 메모리 테스트와 실제 PostgreSQL에서 다르게 동작한다는 점**이었습니다. 일정 수정 충돌을 처리할 때 메모리 저장소에서는 문제가 보이지 않았지만, DB 통합 테스트에서 `SAVEPOINT` 오류가 났습니다. 그 뒤로는 동시 요청 테스트와 실제 DB 테스트를 따로 두고, 어떤 환경에서 검증한 결과인지 함께 적으려고 합니다. 여행 생성에서도 서비스 코드의 사전 조회보다 DB의 고유 제약을 먼저 믿어야 했습니다.

아쉬운 점도 있습니다. 현재 공개된 브랜치만으로는 데이터 가공 결과가 온라인 추천까지 자동으로 전달되는지, Outbox 이벤트가 Kafka까지 발행되는지 확인할 수 없습니다. AI 도우미 서버 코드와 설문 원본도 이 GitHub `main`에 없습니다. 그래서 README에서 설계 문서의 목표와 영상에서 본 화면, 공개 코드로 확인한 동작을 나눠 적었습니다. 이 연결과 자료가 정리되기 전에는 추천 성능이나 서비스 운영 성과를 숫자로 주장하기 어렵습니다.

다음에는 실제 DB를 사용하는 동시 요청 검증과 이벤트 발행 경로를 먼저 보강하고 싶습니다. 설문 원본을 공개할 수 없는 경우에도 개인정보를 제거한 집계와 재현 가능한 분석 절차는 남겨야 합니다. 이번 작업을 하며 기능을 만드는 것만큼 **어디까지 확인했고 무엇은 아직 확인하지 못했는지** 설명하는 일이 중요하다는 것을 배웠습니다.

## 로컬 실행

프론트엔드는 Node.js 20 이상이 필요합니다.

```bash
cd frontend
cp .env.example .env
npm install
npm run web
```

백엔드는 Java 17과 Docker가 필요합니다. `.env`의 비밀번호와 비밀키를 설정한 뒤 실행합니다.

```bash
cd backend
cp .env.example .env
docker compose up --build -d
```

환경변수와 OAuth 설정은 [백엔드 실행 안내](backend/README.md)를 참고해 주세요. 비밀값은 저장소에 올리지 않습니다.

## 기획과 설계 문서

[`docs/gabolle`](docs/gabolle/README.md)에 있는 v1.1 문서 6종이 팀의 확정 기준입니다. `.docx`가 원본이며, 아래 내용은 그 문서와 시연 영상을 대조해 작성했습니다.

- [통합 서비스 기획서](docs/gabolle/GABOLLE_통합_서비스_기획서_v1.1.docx): 문제 정의와 서비스 범위
- [요구사항 명세서](docs/gabolle/GABOLLE_요구사항_명세서_v1.1.docx): 기능별 요구사항
- [API 명세서](docs/gabolle/GABOLLE_API_명세서_v1.1.docx): 앱과 서버의 요청 및 응답 형식
- [도메인 온톨로지 명세서](docs/gabolle/GABOLLE_도메인_온톨로지_명세서_v1.1.docx): 장소와 제약의 판정 기준
- [화면 흐름과 정보 구조 명세서](docs/gabolle/GABOLLE_화면흐름_및_정보구조_명세서_v1.1.docx): 화면과 이동 흐름
- [사용자 시나리오와 인수 기준](docs/gabolle/GABOLLE_사용자_시나리오_및_인수기준_v1.1.docx): 완료 판정 기준

라이선스는 [LICENSE](LICENSE)를 참고해 주세요.
