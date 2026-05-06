-- V2__remove_streak_fields_from_users.sql
-- streaks table is canonical source of truth for streak data
-- users.streak_count and users.longest_streak are removed to avoid dual source of truth

ALTER TABLE users DROP COLUMN streak_count;
ALTER TABLE users DROP COLUMN longest_streak;
