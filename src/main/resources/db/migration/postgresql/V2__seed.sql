INSERT INTO "user" (id, username, password_hash, name, user_type, is_active,
                    creator_id, creator_name, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin',
        '$2a$10$3ZA1Tl136kecqQorijpEcOhLEzMjW8zX7vURFhJpGi1r2ipcSpuFq',
        '系统管理员', 'admin', TRUE, 'init', 'init',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
