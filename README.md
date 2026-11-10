# 📔 그때의 나 (Diary of That Time)

> 순간적으로 치솟는 감정을 기록하고, **일주일 뒤의 내가 이성적으로 다시 돌아보는** 감정 회고 일기 Android 앱
> 서울여자대학교 2025 겨울 Programming GURU · **GURU2 8조**

| 항목 | 내용 |
|---|---|
| 기간 | 2026.01.14 ~ 2026.01.27 |
| 플랫폼 | Android (Kotlin) |
| 팀 구성 | 4인 |
| 내 역할 | **아이디어 제안(최종 채택)**, 감정일기장 · 회고일기장 개발, 기능명세서 작성 |
| 기여 | 전체 125커밋 중 **53커밋** (팀 내 최다) |

<br>

## 📖 서비스 소개

감정이 극대화되는 순간에는 객관적인 판단이 어렵습니다.
**그때의 나**는 감정을 억누르거나 부정하지 않고,

1. 지금 느끼는 **감정을 인식**하고
2. 감정의 **원인을 분리**하며
3. 그 이유가 **사실인지 추측인지 구분**하도록 돕습니다.

그리고 **일주일 뒤 알림**으로 기록을 다시 꺼내 보게 해, 감정을 이겨낸 경험을 '성공 사례'로 쌓아 갑니다.

> **예시**
> - 즉시 작성: 슬픔 / "답장이 늦어서 마음이 떠난 것 같다" / **추측**
> - 일주일 후: "가족과 식사 중이라 연락이 안 됐고, 다음 날 먼저 연락이 왔다" / 이겨낸 방법: "핸드폰을 멀리 두고 내 할 일을 했다"

<br>

## ✨ 주요 기능

| 기능 | 설명 |
|---|---|
| 📅 메인 달력 · 출석 도장 | 일기를 쓴 날에는 감정별로 다른 발자국 도장 표시 |
| ✍️ 감정일기장 | 제목, 감정(행복·무기력·슬픔·불안·화남), 이유, 사실/추측 기록 → 감정별 극복 추천 활동 팝업 |
| ⏰ 일주일 뒤 회고 알림 | 일기마다 7일 뒤 알림 예약, 알림을 누르면 해당 일기의 회고일기장으로 이동 |
| 🔁 회고일기장 | "극복했어요" → 사실/추측 재확인, 극복 방법, 나에게 한마디 작성 / "아직 못했어요" → 일주일 뒤 다시 알림 |
| 🟥 극복 완료 도장 | 회고를 마치면 원본 일기에 극복 도장, 극복 방법, 나에게 한마디(포스트잇)가 붙음 |
| 🗂 감정 보관함 | 월별 감정 통계, 감정별 일기 목록, 극복 방법만 모아 보는 성공 사례 리스트 |
| 😊 랜덤 리마인드 | '행복' 일기 중 하나를 골라 알림으로 다시 보여주기 |

<br>

## 🛠 Tech Stack

| 분류 | 기술 |
|---|---|
| Language | Kotlin 2.0 |
| UI | XML Layout, ViewBinding, Material Components, RecyclerView, Fragment |
| Local DB | Room 2.6 (KSP), Kotlin Coroutines, Flow |
| Background | AlarmManager + BroadcastReceiver, WorkManager |
| Build | Gradle (Kotlin DSL), Version Catalog · minSdk 24 / targetSdk 36 |
| 협업 | GitHub, Notion |

<br>

## 🙋 내가 한 일 (박세빈)

**기획 · 협업 환경**
- 감정 일기장 아이디어를 제안해 팀 프로젝트 주제로 **최종 채택**
- 노션 워크스페이스와 GitHub 레포지토리 생성, 기능명세서 작성

**감정일기장** (`WriteActivity`)
- Room DB 세팅 (`Diary` 엔티티, `AppDatabase`, `DiaryDao`)
- 감정 선택(RadioGroup), 일기 · 사실/추측 입력, `lifecycleScope`로 백그라운드에서 DB 저장
- 입력하지 않은 칸에 오류 메시지 표시
- 기록 완료 시 선택한 감정에 맞는 **극복 추천 활동 팝업**

**회고일기장** (`ReviewActivity`)
- 알림에서 전달받은 일기 ID로 해당 일기를 불러와 회고 화면 구성
- "극복했어요 / 아직 못했어요" 분기 — 아직 못했으면 일주일 뒤 회고 알림 재예약
- 회고 완료 시 원본 일기에 **극복 완료 도장**과 극복 방법이 반영되도록 구현
- 회고 알림을 회고 화면과 연결 (알림 로직 담당 팀원과 협업)

**통합**
- 팀원 코드 병합, 라이브러리 충돌 해결, 앱이 튕기는 오류 수정

<br>

## 👥 Team

| 이름 | 담당 |
|---|---|
| 박세빈 [@sebin0204](https://github.com/sebin0204) | 아이디어 · 기능명세서, 감정일기장, 회고일기장, 코드 통합 |
| 최어진 [@ojin4477](https://github.com/ojin4477) | GitHub 협업 환경, 회고 알림 로직 · 알림 권한, 발표 자료 |
| 이혜원 [@hyeweooon](https://github.com/hyeweooon) | 메인 화면 · 달력 출석 도장, UI 디자인 · 로고, 발표 영상 |
| 최윤정 [@dw261](https://github.com/dw261) | 감정 보관함, 일기 상세 · 수정/삭제, 성공 사례 모아보기, 랜덤 리마인드 |

<br>

## 📁 구조

```
app/src/main/java/com/example/diaryofthattime
├── data/            # Room: Diary, DiaryDao, AppDatabase, EmotionCount
├── receiver/        # AlarmReceiver (회고 알림)
├── util/            # AlarmHelper (7일 뒤 알림 예약)
├── review/          # ReviewActivity (회고일기장)
├── ui/main          # 메인 화면 (달력 · 출석 도장)
├── ui/archive       # 감정 보관함 (ViewModel, Adapter)
├── WriteActivity.kt         # 감정일기장
├── DiaryDetailActivity.kt   # 일기 상세 + 극복 도장
├── HappyReminder.kt         # 랜덤 리마인드 Worker
└── Archive_main.kt / Archive_detail.kt  # 감정 보관함
```
