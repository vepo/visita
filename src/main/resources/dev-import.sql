-- quick_dev_data.sql
-- Quick data generation with 128-day range and 1-hour max view duration
-- Updated for normalized schema with domains and pages

DO $$
DECLARE
    i INTEGER;
    view_record RECORD;
    random_page_id BIGINT;
    length_seconds INTEGER;
    access_timestamp TIMESTAMP;
    referrer TEXT;
    user_agent TEXT;
    user_id TEXT;
    tab_id TEXT;
    screen_resolution TEXT;
    timezone TEXT;
BEGIN
    -- Step 1: Insert sample domains
    INSERT INTO tb_domains (hostname, token)
    VALUES 
        ('example.com', 'token-1'),
        ('blog.vepo.dev', 'token-2'),
        ('app.example.com', 'token-3'),
        ('shop.example.com', 'token-4')
    ON CONFLICT (hostname) DO NOTHING;

    -- Step 2: Insert sample pages with domain references
    WITH domain_ids AS (
        SELECT id, hostname FROM tb_domains
    )
    INSERT INTO tb_pages (path, domain_id)
    SELECT 
        page_path,
        d.id
    FROM (
        VALUES 
            -- Home pages
            ('/', (SELECT id FROM domain_ids WHERE hostname = 'example.com')),
            ('/', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            ('/', (SELECT id FROM domain_ids WHERE hostname = 'app.example.com')),
            ('/', (SELECT id FROM domain_ids WHERE hostname = 'shop.example.com')),
            
            -- Blog pages
            ('/post/some-post', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            ('/post/another-post', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            ('/post/getting-started', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            ('/post/advanced-topics', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            ('/post/docker-guide', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            ('/post/kubernetes-tips', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            
            -- App pages
            ('/dashboard', (SELECT id FROM domain_ids WHERE hostname = 'app.example.com')),
            ('/profile', (SELECT id FROM domain_ids WHERE hostname = 'app.example.com')),
            ('/settings', (SELECT id FROM domain_ids WHERE hostname = 'app.example.com')),
            ('/analytics', (SELECT id FROM domain_ids WHERE hostname = 'app.example.com')),
            ('/billing', (SELECT id FROM domain_ids WHERE hostname = 'app.example.com')),
            ('/notifications', (SELECT id FROM domain_ids WHERE hostname = 'app.example.com')),
            
            -- Shop pages
            ('/products', (SELECT id FROM domain_ids WHERE hostname = 'shop.example.com')),
            ('/cart', (SELECT id FROM domain_ids WHERE hostname = 'shop.example.com')),
            ('/checkout', (SELECT id FROM domain_ids WHERE hostname = 'shop.example.com')),
            ('/orders', (SELECT id FROM domain_ids WHERE hostname = 'shop.example.com')),
            ('/product/laptop', (SELECT id FROM domain_ids WHERE hostname = 'shop.example.com')),
            ('/product/phone', (SELECT id FROM domain_ids WHERE hostname = 'shop.example.com')),
            
            -- Additional pages
            ('/about', (SELECT id FROM domain_ids WHERE hostname = 'example.com')),
            ('/contact', (SELECT id FROM domain_ids WHERE hostname = 'example.com')),
            ('/privacy', (SELECT id FROM domain_ids WHERE hostname = 'example.com')),
            ('/terms', (SELECT id FROM domain_ids WHERE hostname = 'example.com')),
            ('/careers', (SELECT id FROM domain_ids WHERE hostname = 'example.com')),
            ('/blog', (SELECT id FROM domain_ids WHERE hostname = 'example.com'))
    ) AS pages(page_path, domain_id)
    INNER JOIN domain_ids d ON pages.domain_id = d.id
    ON CONFLICT (domain_id, path) DO NOTHING;

    -- Step 3: Generate 1000 view records with random page assignments
    FOR i IN 1..1000 LOOP
        -- Get a random page ID
        SELECT id INTO random_page_id 
        FROM tb_pages 
        ORDER BY RANDOM() 
        LIMIT 1;
        
        -- Generate random view data into individual variables
        access_timestamp := NOW() - (RANDOM() * INTERVAL '128 days');
        length_seconds := FLOOR(RANDOM() * 3600);
        
        -- Referrer
        referrer := CASE 
            WHEN RANDOM() < 0.05 THEN NULL  -- 5% null
            WHEN RANDOM() < 0.3 THEN 'direct'  -- 30% direct
            WHEN RANDOM() < 0.6 THEN 'https://google.com'  -- 30% google
            WHEN RANDOM() < 0.8 THEN 'https://facebook.com'  -- 20% facebook
            ELSE 'https://example.com'  -- 15% other
        END;
        
        -- User Agent
        user_agent := CASE 
            WHEN RANDOM() < 0.6 THEN  -- 60% Chrome on desktop
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
            WHEN RANDOM() < 0.8 THEN  -- 20% Safari on mobile
                'Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1'
            WHEN RANDOM() < 0.9 THEN  -- 10% Firefox
                'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0'
            ELSE  -- 10% Android Chrome
                'Mozilla/5.0 (Linux; Android 14; SM-S901U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.210 Mobile Safari/537.36'
        END;
        
        -- User ID
        user_id := CASE 
            WHEN RANDOM() > 0.3 THEN 'user_' || LPAD(FLOOR(RANDOM() * 100)::TEXT, 3, '0')
            ELSE NULL
        END;
        
        -- Tab ID
        tab_id := 'tab_' || LPAD(FLOOR(RANDOM() * 50)::TEXT, 3, '0');
        
        -- Screen resolution
        screen_resolution := CASE 
            WHEN RANDOM() < 0.4 THEN '1920x1080'  -- 40% Full HD
            WHEN RANDOM() < 0.6 THEN '1366x768'   -- 20% HD
            WHEN RANDOM() < 0.7 THEN '1280x720'   -- 10% HD Ready
            WHEN RANDOM() < 0.85 THEN '2560x1440' -- 15% 2K
            WHEN RANDOM() < 0.95 THEN '3840x2160' -- 10% 4K
            ELSE '1536x864'                       -- 5% Other
        END;
        
        -- Timezone
        timezone := (ARRAY['America/New_York', 'Europe/London', 'Asia/Tokyo', 'Australia/Sydney', 'America/Los_Angeles'])[FLOOR(RANDOM() * 5) + 1];
        
        -- Insert into tb_views
        INSERT INTO tb_views (
            "length",
            access_timestamp,
            end_timestamp,
            page_id,
            referrer,
            user_agent,
            user_id,
            tab_id,
            screen_resolution,
            timezone
        ) VALUES (
            length_seconds * 1000,  -- Convert to milliseconds
            access_timestamp,
            LEAST(
                access_timestamp + (length_seconds || ' seconds')::INTERVAL,
                NOW()
            ),
            random_page_id,
            referrer,
            user_agent,
            user_id,
            tab_id,
            screen_resolution,
            timezone
        );
    END LOOP;
    
    -- Show statistics (optional)
    RAISE NOTICE 'Data generation complete:';
    RAISE NOTICE '- Domains: %', (SELECT COUNT(*) FROM tb_domains);
    RAISE NOTICE '- Pages: %', (SELECT COUNT(*) FROM tb_pages);
    RAISE NOTICE '- Views: %', (SELECT COUNT(*) FROM tb_views);
    RAISE NOTICE '- Distinct pages used: %', (SELECT COUNT(DISTINCT page_id) FROM tb_views);
END $$;