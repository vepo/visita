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
    blog_home_id BIGINT;
    blog_getting_started_id BIGINT;
    blog_some_post_id BIGINT;
    blog_advanced_id BIGINT;
    blog_docker_id BIGINT;
    blog_about_id BIGINT;
    blog_contact_id BIGINT;
    shop_products_id BIGINT;
    shop_laptop_id BIGINT;
    shop_phone_id BIGINT;
    shop_cart_id BIGINT;
    shop_checkout_id BIGINT;
    app_home_id BIGINT;
    app_dashboard_id BIGINT;
    app_analytics_id BIGINT;
    app_settings_id BIGINT;
    sankey_user_id TEXT;
    sankey_tab_id TEXT;
    sankey_base_ts TIMESTAMP;
    chain_step INTEGER;
BEGIN
    -- Step 1: Insert sample domains
    INSERT INTO tb_domains (hostname, token)
    VALUES 
        ('example.com', 'token-1'),
        ('blog.vepo.dev', 'token-2'),
        ('app.example.com', 'token-3'),
        ('shop.example.com', 'token-4'),
        ('localhost', 'local-dev')
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
            ('/about', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            ('/contact', (SELECT id FROM domain_ids WHERE hostname = 'blog.vepo.dev')),
            
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

    UPDATE tb_domains
    SET ignored_path_patterns = E'/favicon\\.ico\n/health'
    WHERE hostname = 'localhost';

    -- Step 3: Deterministic Sankey funnels (origin flow + page drill-down)
    -- Referrers use full URLs on in-site navigation, matching document.referrer in visita.js.
    SELECT p.id INTO blog_home_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'blog.vepo.dev' AND p.path = '/';

        SELECT p.id INTO blog_getting_started_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'blog.vepo.dev' AND p.path = '/post/getting-started';

        SELECT p.id INTO blog_some_post_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'blog.vepo.dev' AND p.path = '/post/some-post';

        SELECT p.id INTO blog_advanced_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'blog.vepo.dev' AND p.path = '/post/advanced-topics';

        SELECT p.id INTO blog_docker_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'blog.vepo.dev' AND p.path = '/post/docker-guide';

        SELECT p.id INTO blog_about_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'blog.vepo.dev' AND p.path = '/about';

        SELECT p.id INTO blog_contact_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'blog.vepo.dev' AND p.path = '/contact';

        SELECT p.id INTO shop_products_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'shop.example.com' AND p.path = '/products';

        SELECT p.id INTO shop_laptop_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'shop.example.com' AND p.path = '/product/laptop';

        SELECT p.id INTO shop_phone_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'shop.example.com' AND p.path = '/product/phone';

        SELECT p.id INTO shop_cart_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'shop.example.com' AND p.path = '/cart';

        SELECT p.id INTO shop_checkout_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'shop.example.com' AND p.path = '/checkout';

        SELECT p.id INTO app_home_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'app.example.com' AND p.path = '/';

        SELECT p.id INTO app_dashboard_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'app.example.com' AND p.path = '/dashboard';

        SELECT p.id INTO app_analytics_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'app.example.com' AND p.path = '/analytics';

        SELECT p.id INTO app_settings_id
        FROM tb_pages p JOIN tb_domains d ON p.domain_id = d.id
        WHERE d.hostname = 'app.example.com' AND p.path = '/settings';

        -- 3a. blog.vepo.dev: Google -> / -> posts/about (click "/" then drill into next pages)
        FOR i IN 1..35 LOOP
            sankey_user_id := 'sankey_blog_' || LPAD(i::TEXT, 3, '0');
            sankey_tab_id := 'sankey_blog_tab_' || LPAD(i::TEXT, 3, '0');
            sankey_base_ts := NOW() - ((i % 14) || ' days')::INTERVAL;

            INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
            VALUES (45, sankey_base_ts, sankey_base_ts + INTERVAL '45 seconds', blog_home_id, 'https://google.com',
                    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');

            chain_step := i % 5;
            IF chain_step = 0 THEN
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (60, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', blog_getting_started_id, 'https://blog.vepo.dev/',
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (50, sankey_base_ts + INTERVAL '4 minutes', sankey_base_ts + INTERVAL '5 minutes', blog_advanced_id, 'https://blog.vepo.dev/post/getting-started',
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            ELSIF chain_step = 1 THEN
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (55, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', blog_getting_started_id, 'https://blog.vepo.dev/',
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (40, sankey_base_ts + INTERVAL '4 minutes', sankey_base_ts + INTERVAL '5 minutes', blog_docker_id, 'https://blog.vepo.dev/post/getting-started',
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            ELSIF chain_step = 2 THEN
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (50, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', blog_some_post_id, 'https://blog.vepo.dev/',
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            ELSIF chain_step = 3 THEN
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (35, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', blog_about_id, 'https://blog.vepo.dev/',
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (30, sankey_base_ts + INTERVAL '4 minutes', sankey_base_ts + INTERVAL '5 minutes', blog_contact_id, 'https://blog.vepo.dev/about',
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            ELSE
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (40, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', blog_about_id, 'https://blog.vepo.dev/',
                        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            END IF;
        END LOOP;

        -- 3b. blog.vepo.dev: Facebook + direct entry points
        FOR i IN 1..12 LOOP
            sankey_user_id := 'sankey_blog_fb_' || LPAD(i::TEXT, 3, '0');
            sankey_tab_id := 'sankey_blog_fb_tab_' || LPAD(i::TEXT, 3, '0');
            sankey_base_ts := NOW() - ((i % 10) || ' days')::INTERVAL;

            INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
            VALUES (40, sankey_base_ts, sankey_base_ts + INTERVAL '40 seconds', blog_home_id, 'https://facebook.com',
                    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');

            INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
            VALUES (35, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', blog_some_post_id, 'https://blog.vepo.dev/',
                    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
        END LOOP;

        FOR i IN 1..8 LOOP
            sankey_user_id := 'sankey_blog_direct_' || LPAD(i::TEXT, 3, '0');
            sankey_tab_id := 'sankey_blog_direct_tab_' || LPAD(i::TEXT, 3, '0');
            sankey_base_ts := NOW() - ((i % 7) || ' days')::INTERVAL;

            INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
            VALUES (30, sankey_base_ts, sankey_base_ts + INTERVAL '30 seconds', blog_getting_started_id, 'direct',
                    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
        END LOOP;

        -- 3c. shop.example.com: Facebook -> products -> product -> cart -> checkout
        FOR i IN 1..20 LOOP
            sankey_user_id := 'sankey_shop_' || LPAD(i::TEXT, 3, '0');
            sankey_tab_id := 'sankey_shop_tab_' || LPAD(i::TEXT, 3, '0');
            sankey_base_ts := NOW() - ((i % 12) || ' days')::INTERVAL;

            INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
            VALUES (35, sankey_base_ts, sankey_base_ts + INTERVAL '35 seconds', shop_products_id, 'https://facebook.com',
                    'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');

            IF i % 2 = 0 THEN
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (50, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', shop_laptop_id, 'https://shop.example.com/products',
                        'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            ELSE
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (45, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', shop_phone_id, 'https://shop.example.com/products',
                        'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            END IF;

            IF i % 3 = 0 THEN
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (25, sankey_base_ts + INTERVAL '4 minutes', sankey_base_ts + INTERVAL '5 minutes', shop_cart_id,
                        CASE WHEN i % 2 = 0 THEN 'https://shop.example.com/product/laptop' ELSE 'https://shop.example.com/product/phone' END,
                        'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (20, sankey_base_ts + INTERVAL '6 minutes', sankey_base_ts + INTERVAL '7 minutes', shop_checkout_id, 'https://shop.example.com/cart',
                        'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            END IF;
        END LOOP;

        -- 3d. app.example.com: LinkedIn -> home -> dashboard -> analytics/settings
        FOR i IN 1..15 LOOP
            sankey_user_id := 'sankey_app_' || LPAD(i::TEXT, 3, '0');
            sankey_tab_id := 'sankey_app_tab_' || LPAD(i::TEXT, 3, '0');
            sankey_base_ts := NOW() - ((i % 9) || ' days')::INTERVAL;

            INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
            VALUES (40, sankey_base_ts, sankey_base_ts + INTERVAL '40 seconds', app_home_id, 'https://linkedin.com',
                    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');

            INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
            VALUES (55, sankey_base_ts + INTERVAL '2 minutes', sankey_base_ts + INTERVAL '3 minutes', app_dashboard_id, 'https://app.example.com/',
                    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');

            IF i % 2 = 0 THEN
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (45, sankey_base_ts + INTERVAL '4 minutes', sankey_base_ts + INTERVAL '5 minutes', app_analytics_id, 'https://app.example.com/dashboard',
                        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            ELSE
                INSERT INTO tb_views ("length", access_timestamp, end_timestamp, page_id, referrer, user_agent, user_id, tab_id, timezone)
                VALUES (35, sankey_base_ts + INTERVAL '4 minutes', sankey_base_ts + INTERVAL '5 minutes', app_settings_id, 'https://app.example.com/dashboard',
                        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', sankey_user_id, sankey_tab_id, 'America/Sao_Paulo');
            END IF;
        END LOOP;

    -- Step 4: Generate additional random view records
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
            length_seconds,
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