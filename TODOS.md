# TODOS

## Post-MVP Security Hardening

- [ ] **JWT_SECRET/DB_PASSWORD production hardening**: Remove hardcoded fallback defaults from `application.yml`. Add fail-fast validation at startup if required env vars are missing. Currently `${JWT_SECRET:your-256-bit-secret...}` falls back to an insecure default if env var is unset.

## Phase 1 Prerequisites

- [ ] **RedisConfig class**: Add `RedisConfig.java` with connection pooling (Lettuce) and JSON serialization for Spring Data Redis. Stack includes Redis but no config class exists yet. Required before streak/badge services use Redis for caching.

## Phase 4 Prerequisites

- [ ] **knowledge_edges table**: The `docs/plan-gstack.md` defines a `knowledge_edges` table for prerequisite/related relationships but `V1__init_schema.sql` doesn't include it. Add to V3 migration before knowledge graph visualization (Phase 4).

## Implementation Notes

### SM-2 Algorithm Parameters (confirmed 2026-05-06)
- EF initial = 2.5, min = 1.3
- First review: interval = 1 day if quality ≥ 3, else 0 (review tomorrow)
- Subsequent reviews:
  - q < 3: interval = 1, mastery = max(0, mastery - 1)
  - q = 3: interval = max(1, round(interval × EF))
  - q = 4: interval = round(interval × EF)
  - q = 5: interval = round(interval × EF × 1.3)
- mastery_level = min(5, mastery + 1) on success
- next_review_date = today + interval

### Streak Canonical Source (confirmed 2026-05-06)
- `streaks` table is canonical source of truth for streak data
- `users.streak_count` and `users.longest_streak` are dead columns (removed in V2 migration)
- `StreakService` owns all streak logic

### Badge N+1 Fix (confirmed 2026-05-06)
- `BadgeService.getAllBadgesWithStatus()` uses single JOIN query to avoid N+1
- LEFT JOIN user_badges on (badge_id, user_id) in one query
- No per-badge EXISTS queries

### picture.js (confirmed 2026-05-06 — NOT ADDED TO TODOS)
- Skipped: implementer to define infographic generation approach during Phase 2 content population
