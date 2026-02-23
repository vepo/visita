ALTER TABLE tb_views
RENAME COLUMN referrer to referer;

CREATE VIEW tb_views_original_referer AS 
    SELECT id as view_id, 
           (SELECT referer 
            FROM tb_views original_view
            LEFT JOIN tb_pages original_page ON original_view.page_id = original_page.id 
            LEFT JOIN tb_domains original_domain ON original_page.domain_id = original_domain.id 
            WHERE original_view.user_id = view.user_id AND 
                  original_view.tab_id = view.tab_id AND  
                  NOT original_view.referer like CONCAT('%', original_domain.hostname, '%') 
                  AND original_view.access_timestamp <= view.access_timestamp  LIMIT 1) as original_referer 
    FROM tb_views view;