ALTER TABLE tb_views
RENAME COLUMN referer to referrer;

DROP VIEW IF EXISTS tb_views_original_referer;

CREATE OR REPLACE VIEW tb_views_original_referrer AS 
    SELECT id as view_id, 
           (SELECT referrer 
            FROM tb_views original_view
            LEFT JOIN tb_pages original_page ON original_view.page_id = original_page.id 
            LEFT JOIN tb_domains original_domain ON original_page.domain_id = original_domain.id 
            WHERE original_view.user_id = view.user_id AND 
                  NOT original_view.referrer like CONCAT('%', original_domain.hostname, '%') 
                  AND original_view.access_timestamp <= view.access_timestamp  
            ORDER BY original_view.access_timestamp DESC
            LIMIT 1) as original_referrer 
    FROM tb_views view;