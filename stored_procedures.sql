CREATE PROCEDURE delete_user(uid INT)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM question WHERE user_id = uid;
    DELETE FROM chat WHERE user_id = uid;
    DELETE FROM user_preferences WHERE user_id = uid;
    DELETE FROM users WHERE id = uid;
END;
$$;

call delete_user(6)