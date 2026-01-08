-- quick_dev_data.sql
-- Quick data generation without the detailed reporting

INSERT INTO tb_views (
    "length",
    access_timestamp,
    end_timestamp,
    "page",
    referrer,
    user_agent,
    user_id,
    tab_id,
    screen_resolution,
    timezone
)
SELECT
    FLOOR(RANDOM() * 600000)::BIGINT,
    NOW() - (RANDOM() * INTERVAL '30 days'),
    NOW() - (RANDOM() * INTERVAL '29 days'),
    (ARRAY['/home', '/products', '/about', '/contact', '/dashboard'])[FLOOR(RANDOM() * 5) + 1],
    CASE WHEN RANDOM() > 0.1 
         THEN (ARRAY['https://google.com', 'https://facebook.com', 'direct', 'https://example.com'])[FLOOR(RANDOM() * 4) + 1]
         ELSE NULL
    END,
    (ARRAY[
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        'Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15'
    ])[FLOOR(RANDOM() * 2) + 1],
    'user_' || LPAD(FLOOR(RANDOM() * 100)::TEXT, 3, '0'),
    'tab_' || LPAD(FLOOR(RANDOM() * 50)::TEXT, 3, '0'),
    (ARRAY['1920x1080', '1366x768', '1280x720'])[FLOOR(RANDOM() * 3) + 1],
    (ARRAY['America/New_York', 'Europe/London', 'Asia/Tokyo'])[FLOOR(RANDOM() * 3) + 1]
FROM generate_series(1, 1000);