# 📔 그때의 나 (Diary of That Time)

> 감정을 기록하고, 일주일 뒤의 내가 다시 돌아보는 **감정 회고 일기 Android 앱**
> 2026 GURU2 · 8팀

| 항목 | 내용 |
|---|---|
| 기간 | 2026.01.14 ~ 2026.01.27 |
| 플랫폼 | Android (Kotlin) |
| 팀 구성 | 5인 |
| 내 기여 | 전체 125커밋 중 **53커밋** (팀 내 최다) |

<br>

## 📖 서비스 소개

감정은 그 순간에는 크게 느껴지지만, 시간이 지나 돌아보면 다르게 보이기도 합니다.
**그때의 나**는 지금의 감정과 그 이유를 기록해 두고, **일주일 뒤 알림**으로 그 일기를 다시 꺼내 회고하게 해 줍니다.
회고를 마친 일기에는 **극복 도장**이 찍히고, 행복했던 기억은 가끔 다시 알림으로 찾아옵니다.

<br>

## ✨ 주요 기능

| 기능 | 설명 |
|---|---|
| ✍️ 감정 일기 작성 | 제목, 감정(행복·슬픔 등), 감정의 이유, **사실인지 추측인지** 기록 |
| ⏰ 일주일 뒤 회고 알림 | 일기마다 `AlarmManager`로 7일 뒤 알림 예약 → 알림을 누르면 회고 화면으로 이동 |
| 🔁 회고 | 지금도 사실이라고 생각하는지, 어떻게 극복했는지, 나에게 한마디 작성 |
| 🟥 극복 도장 | 회고를 마친 일기 상세 화면에 도장이 찍히는 애니메이션 표시 |
| 😊 행복 리마인드 | `WorkManager`로 '행복' 일기 중 하나를 골라 알림으로 다시 보여주기 |
| 🗂 감정 아카이브 | 월별 감정 통계, 감정별 일기 모아보기, 극복 사례 모아보기 |

<br>

## 🛠 Tech Stack

| 분류 | 기술 |
|---|---|
| Language | Kotlin 2.0 |
| UI | XML Layout, ViewBinding, Material Components, RecyclerView, Fragment |
| Local DB | Room 2.6 (KSP), Kotlin Coroutines, Flow |
| Background | AlarmManager + BroadcastReceiver, WorkManager |
| Architecture | ViewModel (아카이브 화면) |
| Build | Gradle (Kotlin DSL), Version Catalog · minSdk 24 / targetSdk 36 |

<br>

## 🙋 내가 한 일 (박세빈)

커밋 기록 기준으로 **데이터 계층, 일기 작성, 회고와 알림 흐름**을 맡고, 마지막에 팀원 코드 통합을 담당했습니다.

- **Room DB 설계 및 세팅** — `Diary` 엔티티, `AppDatabase`, `DiaryDao`
  - 기본 CRUD, 날짜 범위 조회
  - `strftime`을 이용한 **월별 감정 통계** 쿼리 (`EmotionCount`)
  - 월별·감정별 일기 목록 조회 (`Flow`로 화면 자동 갱신)
- **감정 일기 작성 화면** (`WriteActivity`) — 감정, 이유, 사실/추측 입력 후 저장
- **회고 페이지 구현** (`ReviewActivity`) — 일기 다시 보기, 극복 방법·나에게 한마디 저장
- **회고 알림** — `AlarmManager`로 일기마다 7일 뒤 알림 예약, `AlarmReceiver`에서 알림을 띄우고 회고 화면으로 연결
- **행복 리마인드 알림** — `WorkManager` 기반 `HappyReminderWorker`
- **극복 도장** — 회고 완료 여부에 따라 일기 상세 화면에 도장 애니메이션 표시
- **통합 및 안정화** — 팀원 코드 병합, 라이브러리 충돌 해결, 앱이 튕기는 오류 수정

<br>

## 📁 구조

```
app/src/main/java/com/example/diaryofthattime
├── data/            # Room: Diary, DiaryDao, AppDatabase, EmotionCount
├── receiver/        # AlarmReceiver (회고 알림)
├── util/            # AlarmHelper (7일 뒤 알림 예약)
├── review/          # ReviewActivity (회고)
├── ui/main          # 메인 화면
├── ui/archive       # 아카이브 (ViewModel, Adapter)
├── WriteActivity.kt         # 일기 작성
├── DiaryDetailActivity.kt   # 일기 상세 + 극복 도장
├── HappyReminder.kt         # 행복 리마인드 Worker
└── Archive_main.kt / Archive_detail.kt  # 감정 아카이브
```

<br>

## 👥 Team

| 이름 | GitHub |
|---|---|
| 박세빈 | [@sebin0204](https://github.com/sebin0204) |
| 최윤정 | [@dw261](https://github.com/dw261) |
| 이혜원 | [@hyeweooon](https://github.com/hyeweooon) |
| - | [@ojin4477](https://github.com/ojin4477) |
