-- 初始化数据库表结构
-- V1__init_schema.sql

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),
    level INT DEFAULT 1,
    streak_count INT DEFAULT 0,
    longest_streak INT DEFAULT 0,
    last_active_date DATETIME,
    theme_preference VARCHAR(20) DEFAULT 'light',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    title VARCHAR(255) NOT NULL,
    content_json JSON,
    infographic_url VARCHAR(500),
    difficulty INT DEFAULT 1,
    node_type VARCHAR(20) DEFAULT 'point',
    prerequisite_ids JSON,
    related_node_ids JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES knowledge_nodes(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    node_id BIGINT NOT NULL,
    question_type VARCHAR(20) DEFAULT 'choice',
    content_json JSON NOT NULL,
    answer_json JSON NOT NULL,
    explanation TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (node_id) REFERENCES knowledge_nodes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    node_id BIGINT NOT NULL,
    mastery_level INT DEFAULT 0,
    next_review_date DATETIME,
    review_count INT DEFAULT 0,
    last_reviewed_at DATETIME,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (node_id) REFERENCES knowledge_nodes(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_node (user_id, node_id)
);

CREATE TABLE IF NOT EXISTS user_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer TEXT,
    is_correct BOOLEAN,
    answered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    time_spent_ms BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS streaks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    current_streak INT DEFAULT 0,
    longest_streak INT DEFAULT 0,
    last_streak_date DATETIME,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    criteria_json JSON
);

CREATE TABLE IF NOT EXISTS user_badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_badge (user_id, badge_id)
);

CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100),
    cover_url VARCHAR(500),
    chapters_json JSON,
    linked_node_ids JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_book_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    current_chapter INT DEFAULT 0,
    total_chapters INT DEFAULT 0,
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    finished_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_book (user_id, book_id)
);

CREATE TABLE IF NOT EXISTS analytics_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    topics_studied INT DEFAULT 0,
    questions_answered INT DEFAULT 0,
    accuracy_rate DECIMAL(5,2) DEFAULT 0,
    study_duration_minutes INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_date (user_id, session_date)
);

-- 索引
CREATE INDEX idx_nodes_parent ON knowledge_nodes(parent_id);
CREATE INDEX idx_questions_node ON questions(node_id);
CREATE INDEX idx_progress_user ON user_progress(user_id);
CREATE INDEX idx_progress_next_review ON user_progress(next_review_date);
CREATE INDEX idx_answers_user ON user_answers(user_id);
CREATE INDEX idx_answers_question ON user_answers(question_id);

-- 初始化徽章数据
INSERT INTO badges (name, description, icon_url, criteria_json) VALUES
('初学者', '完成第一个知识点的学习', '/badges/beginner.png', '{"type": "node_count", "value": 1}'),
('连续3天', '连续学习3天', '/badges/3day.png', '{"type": "streak", "value": 3}'),
('连续7天', '连续学习7天', '/badges/7day.png', '{"type": "streak", "value": 7}'),
('连续30天', '连续学习30天', '/badges/30day.png', '{"type": "streak", "value": 30}'),
('答题100', '累计答对100道题', '/badges/100answers.png', '{"type": "correct_answers", "value": 100}'),
('掌握10节点', '掌握10个知识点', '/badges/10nodes.png', '{"type": "mastered_nodes", "value": 10}');
