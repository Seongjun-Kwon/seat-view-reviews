# [목차](#index) <a name = "index"></a>

- [개요](#outline)
- [Git](#git)
- [API](#api)
- [ERD](#erd)
- [테스트](#test)
- [기술 사용 이유](#why)
- [문제 해결 과정](#trouble)
- [작성한 문서](#docs)

<br>

# 개요 <a name = "outline"></a>

<details>
   <summary> 본문 확인 (👈 Click)</summary>
<br />

### 기간
2023.04.21 ~ 진행 중

### 사용 기술
- Java17
- Spring Boot, Spring Data JPA, Spring REST Docs
- MySQL, Redis, Docker, Github Actions, AWS (EC2, S3)

### 목적
야구 경기 관람 시 안전 펜스나 구조물에 의해 시야를 방해 받는 좌석들이 있습니다. 좌석 예매 시 좌석에서 보이는 시야를 알 수 없어 직접 후기를 찾아야하고, 찾아도 나오지 않는 경우가 많아 불편함을 느꼈습니다. 그래서, 좌석의 시야 및 후기를 모아주고 서로 공유할 수 있는 서비스를 개발하였습니다.

</details>

<br>

# Git <a name = "git"></a>

<details>
   <summary> 본문 확인 (👈 Click)</summary>
<br />

### 브랜치 전략
<div align="center">
 <img src="https://github.com/Seongjun-Kwon/seat-view-reviews/assets/82152173/005d16c5-97cc-4e05-9af8-69b2777f3e88" alt="branch">
</div>

### 커밋 컨벤션
| Tag Name (태그) | Content (내용) |
| --- | --- |
| Chore: 내용  | 빌드 업무 수정, 패키지 매니저 수정과 같은 잡다한 것들 |
| Feat: 내용  | 새로운 기능 구현 |
| Add: 내용 | 클래스, 파일, 간단한 코드 추가 ex) 설정 파일, 이미지 등 |
| Fix: 내용 | 간단한 코드 수정 |
| Hotfix: 내용 | 버그 수정 || 오류로 인한 코드 수정 |
| Docs: 내용 | 문서 수정 ex) 리드미 |
| Style: 내용 | 코드 포맷팅, 세미 콜론 누락, 변수명 수정, 로직 변경이 없는 경우 |
| Refactor: 내용 | 코드 리펙토링, 코드 구조 || 성능 개선 |
| Test: 내용 | 테스트 코드, 리팩토링 테스트 코드 추가 |

</details>

<br>

# API <a name = "api"></a>

<details>
   <summary> 본문 확인 (👈 Click)</summary>
<br />

추가 예정
</details>

<br>

# ERD <a name = "erd"></a>

<details>
   <summary> 본문 확인 (👈 Click)</summary>
<br />

<div align="center">
 <img src="https://github.com/Seongjun-Kwon/seat-view-reviews/assets/82152173/fe184f52-be6a-43a4-8baa-90db1aeefba8" alt="erd">
</div>

</details>

<br>

# 테스트 <a name = "test"></a>

<details>
   <summary> 본문 확인 (👈 Click)</summary>
<br />

[//]: # (### 테스트 커버리지)

[//]: # (<div align="center">)

[//]: # ( <img src="" alt="test">)

[//]: # (</div>)
추가 예정

</details>

<br>

# 기술 사용 이유 <a name = "why"></a>

<details>
   <summary> 본문 확인 (👈 Click)</summary>
<br />

### API 문서화 툴
Swagger 와 REST docs 사이에서 고민하였습니다.

Swagger 를 사용할 시 아래와 같은 단점이 있습니다.
1. 문서화 작업을 위한 어노테이션으로 인해 비즈니스 코드의 가독성이 떨어진다.
2. 테스트 기반이 아니기에 기능이 100% 동작한다고 확신할 수 없다.
3. 모든 오류에 대한 여러 가지 응답을 문서화할 수 없다.

위와 같은 문제를 Spring REST docs 는 모두 해결할 수 있습니다.
다만, 개인적으로 Spring REST docs 로 만들어진 문서는 가독성이 떨어진다고 여겨서 고민하였습니다.

OpenAPI 스펙을 활용하여 Spring REST docs 로 만든 문서를 Swagger UI 로 볼 수 있도록 하였습니다.

<br>

### Redis
- 그 날 조회 수가 높은 글의 작성자에게 포인트를 제공하는 정책이 있어 조회 수의 신뢰성이 중요했습니다. 조회 수 중복 방지를 위해 사용자와 사용자가 본 게시글을 정보를 저장할 곳이 필요한데, 사용자가 본 게시글 식별 정보는 데이터가 실시간으로 빠르게 늘어나기에 DB 에 저장하기에는 부담이 컸습니다.
- 조회 수의 신뢰성을 위해 동시성 문제로 인해 분실되는 조회 수가 없어야 했습니다.
- 실시간으로 조회 수를 반영하면 DB 에 부담이 된다고 생각하여 일정 시간마다 DB 에 반영해주고 싶었습니다.

위 세 가지 고려사항을 Redis 도입을 통해 해결할 수 있었기에 도입하였습니다.


<br>

### 전체적인 배포 환경

<div align="center">
 <img src="https://github.com/Seongjun-Kwon/seat-view-reviews/assets/82152173/eb6fd2bf-026a-4ed0-96bd-5b4dfddd7110" alt="erd">
</div>

- 서버 - EC2
  - 개인이 가진 컴퓨터를 활용하여 서버를 구축할 수도 있겠지만, 여분의 컴퓨터가 없기에 고려하지 않았습니다. 클라우드 서버 중에서 Naver Cloud 와 AWS 중에 고민을 하였는데, 1년간 무료로 사용할 수 있는 AWS 를 사용하였습니다.


- CI 툴 - Github actions
  - Jenkins 와 Github actions 간에 고민을 했지만, Jenkins 는 추가적인 설치가 필요하고 Github actions 를 이용하면 소스 코드와 함께 Github 에서 한번에 관리할 수 있다는 점에서 관리 포인트를 줄이고자 Github actions 를 선택하였습니다. 또한 이미 사용해봤기에 추가적인 러닝 커브가 없다는 장점도 있었습니다.

  
- 배포 방식
  - 총 아래의 세 가지 방식 중에서 고민했습니다.
    1. 빌드 파일을 AWS S3 에 올리고 CodeDeploy 를 사용하는 방식
    2. 빌드 파일을 DockerHub 에 올리고 CodeDeploy 를 사용하는 방식
    3. 빌드 파일을 DockerHub 에 올리고 EC2 에서 pull 받아서 사용하는 방식
  - 결론을 먼저 말하자면, 제일 마지막인 빌드 파일을 DockerHub 에 올리고 EC2 에서 pull 받아서 사용하는 방식을 선택하였습니다. 크게 2가지 이유로, 비용 문제와 AWS 의존성을 낮추기 위함입니다.
    - 첫번째 비용 문제는 현재 AWS 프리티어를 이용하고 있는데 S3 가 월별 표준 스토리지 5GB까지, GET 요청 20,000건, PUT 요청 2,000건 무료입니다. 생각보다 넉넉한 양이 아니고, 특히 용량 5GB 가 여러 jar 파일을 보관하기에는 부족하다고 생각하였습니다. 그리고 진행하고 있는 개인 프로젝트 특성 상 이미지 처리를 많이 하고 이 또한 S3 에서 하고 있기에 여러모로 S3 를 사용하는 것은 부담스러웠습니다.
    - 두번째 AWS 의존성을 낮추고자 한 이유는 추후에 온프레미스 환경 혹은 다른 클라우드로 바꿀 수도 있기 때문입니다. 프리티어 혜택이 끝나거나 프리티어 EC2 로 아쉬운 경우에 변경 가능성이 있기에 의존성을 낮추고 싶었습니다.

</details>

<br>

# 문제 해결 과정 <a name = "trouble"></a>
<details>
   <summary> 본문 확인 (👈 Click)</summary>
<br />

- 사용자의 조회 수 중복 증가를 방지
  - 중복 증가를 방지하기 위해 사용자와 사용자가 본 게시글을 식별할 수 있어야 했습니다. IP, 쿠키, 세션 방식을 고려하였고 각 방식의 장점을 결합하여 쿠키로 사용자를 식별하고, Redis 에 게시글 데이터를 저장하였습니다.
- 조회 수 동시성 문제
  - 조회 수가 높은 글에 포인트를 제공하는 정책이 있기에 조회 수의 신뢰성을 보장해야했습니다. Pub-Sub 방식으로 락을 획득하는 Redisson 을 사용하여 서버 부하를 줄이고 동시성 문제를 해결했습니다.
- 조회 수를 실시간으로 반영하는 문제
  - Redis 에 반영하고 일정 주기로 DB 에 반영하도록 하여 DB 부하를 줄이고 새로고침 공격을 방지하였습니다.
- EC2 micro 의 메모리 부족 문제
  - 비용 문제로 서버를 업그레이드 할 수 없는 상황에서 스왑 메모리를 사용하여 부족한 메모리를 충당했습니다.
- Docker MySQL 컨테이너에서 발생한 인코딩 오류 문제
  - 컨테이너의 MySQL 클라이언트 인코딩 셋 문제임을 파악하였습니다. 컨테이너 생성 시 인코딩 정보를 포함한 설정 파일을 사용하도록 하였습니다.


</details>

<br>

# 작성한 문서 <a name = "docs"></a>
<details>
   <summary> 본문 확인 (👈 Click)</summary>
<br />

- [spring.config.import 설정으로 env 파일을 읽을 때 생길 수 있는 오류](https://jun-codinghistory.tistory.com/732)
- [docker-compose 로 개발 환경의 DB 구축하기](https://jun-codinghistory.tistory.com/782)
- [Docker MySQL 컨테이너에서 발생한 인코딩 오류 문제](https://jun-codinghistory.tistory.com/744)
- [API 분리를 통해 재사용성 높이기](https://jun-codinghistory.tistory.com/754)
- [이미지가 포함된 게시글 관련 API 설계](https://jun-codinghistory.tistory.com/773)
- [Github actions, Docker image, Docker hub 를 활용한 CI/CD 과정](https://jun-codinghistory.tistory.com/782)
- [AWS EC2 메모리 부족을 스왑 메모리로 해결하기](https://jun-codinghistory.tistory.com/794)
- [도커 컨테이너 로그 설정하기](https://jun-codinghistory.tistory.com/796)

</details>

<br>