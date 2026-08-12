DELETE FROM user_roles
WHERE user_id IN (
    SELECT id FROM users
    WHERE email IN (
        'admin@campussphere.local',
        'student@campussphere.local',
        'coordinator@campussphere.local'
    )
);

DELETE FROM users
WHERE email IN (
    'admin@campussphere.local',
    'student@campussphere.local',
    'coordinator@campussphere.local'
);

DELETE FROM institutions
WHERE institution_code = 'CS-001';
