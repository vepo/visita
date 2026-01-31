-- quick_dev_data.sql
-- Quick data generation with 128-day range and 1-hour max view duration

WITH view_data AS (
    SELECT
        -- Random timestamp within last 128 days
        NOW() - (RANDOM() * INTERVAL '128 days') AS access_timestamp,
        
        -- Random length up to 1 hour (3600 seconds)
        FLOOR(RANDOM() * 3600) AS length_seconds,
        
        -- Page
        (ARRAY['/home', '/products', '/about', '/contact', '/dashboard'])[FLOOR(RANDOM() * 5) + 1] AS page,
        
        -- Referrer
        CASE WHEN RANDOM() > 0.1 
             THEN (ARRAY['https://google.com', 'https://facebook.com', 'direct', 'https://example.com'])[FLOOR(RANDOM() * 4) + 1]
             ELSE NULL
        END AS referrer,
        
        -- User Agent
        (ARRAY[
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            'Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15'
        ])[FLOOR(RANDOM() * 2) + 1] AS user_agent,
        
        -- User ID
        'user_' || LPAD(FLOOR(RANDOM() * 100)::TEXT, 3, '0') AS user_id,
        
        -- Tab ID
        'tab_' || LPAD(FLOOR(RANDOM() * 50)::TEXT, 3, '0') AS tab_id,
        
        -- Screen resolution
        (ARRAY['1920x1080', '1366x768', '1280x720'])[FLOOR(RANDOM() * 3) + 1] AS screen_resolution,
        
        -- Timezone
        (ARRAY['America/New_York', 'Europe/London', 'Asia/Tokyo'])[FLOOR(RANDOM() * 3) + 1] AS timezone
    FROM generate_series(1, 1000)
)
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
    -- Length in milliseconds
    length_seconds::BIGINT,
    access_timestamp,
    
    -- End timestamp = access_timestamp + length (ensured to be ≤ 1 hour difference)
    -- Also ensure end_timestamp doesn't exceed current time
    LEAST(
        access_timestamp + (length_seconds || ' seconds')::INTERVAL,
        NOW()
    ),
    page,
    referrer,
    user_agent,
    user_id,
    tab_id,
    screen_resolution,
    timezone
FROM view_data;