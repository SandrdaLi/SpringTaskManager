
CREATE FUNCTION GET_TASK_COUNT (v_status VARCHAR(20))
	
	RETURNS INTEGER
	READS SQL DATA
	
	BEGIN ATOMIC
	
		declare v_count INTEGER;
		
		CASE v_status
		
		WHEN 'All' THEN
			SELECT COUNT(*) INTO v_count FROM TASK;
		ELSE
			SELECT COUNT(*) INTO v_count FROM TASK WHERE STATUS = v_status;
		
		END CASE;
		
		RETURN v_count;
	
	END;
	
/
--
--
--
--CREATE FUNCTION get_user_id (in_name VARCHAR(200))
--RETURNS INTEGER READS SQL DATA 
--BEGIN ATOMIC
--  declare out_id INTEGER;
--  select id into out_id from USER where user_name = in_name;
--  RETURN out_id;
--END
 

