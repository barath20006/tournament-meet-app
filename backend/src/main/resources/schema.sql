-- ========================================================
-- SportMeet Database Schema (PostgreSQL)
-- ========================================================

CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sports (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    icon_emoji VARCHAR(10) NOT NULL,
    is_popular BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS tournaments (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    sport_id VARCHAR(50) NOT NULL REFERENCES sports(id),
    tournament_type VARCHAR(50) DEFAULT 'Knockout',
    location VARCHAR(100) NOT NULL,
    venue_name VARCHAR(150) NOT NULL,
    ground_name VARCHAR(150),
    address VARCHAR(255),
    tournament_date VARCHAR(50) NOT NULL,
    start_time VARCHAR(20) NOT NULL,
    end_time VARCHAR(20),
    registration_deadline VARCHAR(50),
    entry_fee NUMERIC(10,2) DEFAULT 0.0,
    prize_money NUMERIC(10,2) DEFAULT 0.0,
    max_teams INT DEFAULT 16,
    registered_teams INT DEFAULT 0,
    players_per_team INT DEFAULT 11,
    age_category VARCHAR(50) DEFAULT 'Open',
    organizer_id VARCHAR(36) NOT NULL REFERENCES users(id),
    organizer_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    whatsapp_number VARCHAR(20) NOT NULL,
    description TEXT,
    poster_url VARCHAR(255),
    status VARCHAR(30) DEFAULT 'APPROVED',
    rejection_reason TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tournament_sport ON tournaments(sport_id);
CREATE INDEX IF NOT EXISTS idx_tournament_location ON tournaments(location);
CREATE INDEX IF NOT EXISTS idx_tournament_date ON tournaments(tournament_date);
CREATE INDEX IF NOT EXISTS idx_tournament_status ON tournaments(status);

CREATE TABLE IF NOT EXISTS registrations (
    id VARCHAR(36) PRIMARY KEY,
    tournament_id VARCHAR(36) NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    team_name VARCHAR(100) NOT NULL,
    captain_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    number_of_players INT DEFAULT 11,
    notes TEXT,
    status VARCHAR(30) DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_registrations_tournament ON registrations(tournament_id);
CREATE INDEX IF NOT EXISTS idx_registrations_user ON registrations(user_id);

CREATE TABLE IF NOT EXISTS favorites (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tournament_id VARCHAR(36) NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, tournament_id)
);

CREATE TABLE IF NOT EXISTS reports (
    id VARCHAR(36) PRIMARY KEY,
    tournament_id VARCHAR(36) NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    reporter_id VARCHAR(36) NOT NULL REFERENCES users(id),
    reporter_name VARCHAR(100) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(30) DEFAULT 'PENDING_REVIEW',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
