-- 1. TRUNCATE tables and reset IDs
TRUNCATE TABLE ticket_attachments, ticket_comments, ticket_dynamic_values, ticket_history, tickets RESTART IDENTITY CASCADE;

-- 2. Insert tickets
-- Ticket 1: CRM System Malfunction (IN_PROGRESS)
-- Creator: user (3), Executor: admin (1)
-- Category: SOFTWARE, Importance: HIGH, Urgency: CRITICAL, Impact: COMPANY
-- Priority score: 4.0
-- Created: 2 hours ago
-- SLA: Created + 4 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, priority_score)
VALUES (
    1,
    'Сбой в работе CRM-системы',
    'При попытке открыть карточку клиента в CRM-системе возникает ошибка 500. Не можем работать с заказами. Проблема наблюдается у всех сотрудников отдела продаж с самого утра.',
    'SOFTWARE',
    'COMPANY',
    'HIGH',
    'CRITICAL',
    'IN_PROGRESS',
    3,
    1,
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '2 hours' + INTERVAL '4 hours',
    4.0
);

-- Ticket 2: BSOD on work PC (IN_PROGRESS)
-- Creator: vip (4), Executor: it (2)
-- Category: HARDWARE, Importance: HIGH, Urgency: CRITICAL, Impact: USER
-- Priority score: 4.0
-- Created: 30 minutes ago
-- SLA: Created + 4 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, priority_score)
VALUES (
    2,
    'Синий экран смерти (BSOD) на рабочем компьютере',
    'Компьютер в кабинете генерального директора (каб. 501) перезагружается каждые 10 минут с синим экраном. Ошибка указывает на nvlddmkm.sys. Срочно нужна помощь, срывается важная видеоконференция с инвесторами!',
    'HARDWARE',
    'USER',
    'HIGH',
    'CRITICAL',
    'IN_PROGRESS',
    4,
    2,
    NOW() - INTERVAL '30 minutes',
    NOW() - INTERVAL '30 minutes' + INTERVAL '4 hours',
    4.0
);

-- Ticket 3: Wi-Fi in 'London' meeting room (NEW)
-- Creator: vip (4)
-- Category: NETWORK, Importance: HIGH, Urgency: HIGH, Impact: DEPARTMENT
-- Priority score: 3.0 (Urgency) + 50.0 (Location MAIN) = 53.0
-- Created: 4 hours ago
-- SLA: Created + 8 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, priority_score)
VALUES (
    3,
    'Не работает Wi-Fi в переговорной "Лондон"',
    'Wi-Fi сеть "Corp-Guest" в переговорной "Лондон" (4 этаж) не раздает интернет. Устройства подключаются к точке доступа, но пишут "Без доступа к интернету". Через час там запланирована встреча с ключевыми партнерами.',
    'NETWORK',
    'DEPARTMENT',
    'HIGH',
    'HIGH',
    'NEW',
    4,
    NULL,
    NOW() - INTERVAL '4 hours',
    NOW() - INTERVAL '4 hours' + INTERVAL '8 hours',
    53.0
);

-- Ticket 4: VPN Connection Issue (NEW)
-- Creator: user (3)
-- Category: NETWORK, Importance: HIGH, Urgency: HIGH, Impact: USER
-- Priority score: 3.0 (Urgency) + 1.0 (Location 313) = 4.0
-- Created: 1 hour ago
-- SLA: Created + 8 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, priority_score)
VALUES (
    4,
    'Проблема с подключением к VPN',
    'После вчерашнего обновления операционной системы на домашнем ноутбуке (macOS Sonoma) не удается подключиться к корпоративному VPN. Ошибка: "Сервер VPN недоступен или соединение разорвано". Не могу получить доступ к рабочим файлам.',
    'NETWORK',
    'USER',
    'HIGH',
    'HIGH',
    'NEW',
    3,
    NULL,
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour' + INTERVAL '8 hours',
    4.0
);

-- Ticket 5: Network Printer on 2nd floor not working (IN_PROGRESS)
-- Creator: user (3), Executor: it (2)
-- Category: HARDWARE, Importance: MEDIUM, Urgency: HIGH, Impact: DEPARTMENT
-- Priority score: 3.0
-- Created: 3 hours ago
-- SLA: Created + 8 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, priority_score)
VALUES (
    5,
    'Не работает сетевой принтер на 2-м этаже',
    'Принтер в кабинете 204 не реагирует на отправленные на печать документы. На экране устройства горит красный индикатор "ошибка замятия бумаги", но визуально внутри замятой бумаги нет. Просьба помочь, так как нужно срочно распечатать квартальные отчеты для бухгалтерии.',
    'HARDWARE',
    'DEPARTMENT',
    'MEDIUM',
    'HIGH',
    'IN_PROGRESS',
    3,
    2,
    NOW() - INTERVAL '3 hours',
    NOW() - INTERVAL '3 hours' + INTERVAL '8 hours',
    3.0
);

-- Ticket 6: Forgot corporate email password (CLOSED)
-- Creator: user (3), Executor: it (2)
-- Category: ACCESS, Importance: MEDIUM, Urgency: HIGH, Impact: USER
-- Priority score: 3.0
-- Created: 2 days ago, Closed: 1 day ago
-- SLA: Created + 8 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, closed_at, resolution, priority_score)
VALUES (
    6,
    'Забыла пароль от корпоративной почты',
    'Не могу войти в почту Outlook с нового рабочего телефона, пишет "Неверный пароль". На компьютере сессия тоже сбросилась. Пожалуйста, сбросьте пароль или помогите вспомнить.',
    'ACCESS',
    'USER',
    'MEDIUM',
    'HIGH',
    'CLOSED',
    3,
    2,
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '2 days' + INTERVAL '8 hours',
    NOW() - INTERVAL '1 day',
    'Пароль успешно сброшен в Active Directory, новый временный пароль передан сотруднику лично в руки и успешно им изменен при первом входе.',
    3.0
);

-- Ticket 7: Request for Gitlab Access (CLOSED)
-- Creator: user (3), Executor: it (2)
-- Category: ACCESS, Importance: LOW, Urgency: MEDIUM, Impact: USER
-- Priority score: 2.0
-- Created: 1 day ago, Closed: 4 hours ago
-- SLA: Created + 24 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, closed_at, resolution, priority_score)
VALUES (
    7,
    'Запрос доступа к Gitlab',
    'Прошу предоставить доступ к репозиторию frontend-app в корпоративном Gitlab для нового разработчика. Роль - Developer. Руководитель отдела согласовал заявку.',
    'ACCESS',
    'USER',
    'LOW',
    'MEDIUM',
    'CLOSED',
    3,
    2,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day' + INTERVAL '24 hours',
    NOW() - INTERVAL '4 hours',
    'Доступ успешно предоставлен, новый пользователь добавлен в группу репозитория с ролью Developer. Приглашение отправлено на почту.',
    2.0
);

-- Ticket 8: Request for Photoshop Installation (NEW)
-- Creator: user (3)
-- Category: SOFTWARE, Importance: LOW, Urgency: LOW, Impact: USER
-- Priority score: 1.0
-- Created: 6 hours ago
-- SLA: Created + 72 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, priority_score)
VALUES (
    8,
    'Запрос на установку Photoshop',
    'Для подготовки маркетинговых материалов к летней кампании требуется установить Adobe Photoshop на рабочую станцию дизайнера. Лицензия уже приобретена и согласована с руководителем направления.',
    'SOFTWARE',
    'USER',
    'LOW',
    'LOW',
    'NEW',
    3,
    NULL,
    NOW() - INTERVAL '6 hours',
    NOW() - INTERVAL '6 hours' + INTERVAL '72 hours',
    1.0
);

-- Ticket 9: Toner Replacement in room 105 (NEW)
-- Creator: user (3)
-- Category: HARDWARE, Importance: LOW, Urgency: LOW, Impact: USER
-- Priority score: 1.0
-- Created: 5 hours ago
-- SLA: Created + 72 hours
INSERT INTO tickets (id, title, description, category, impact, importance, urgency, status, creator_id, executor_id, created_at, sla_deadline, priority_score)
VALUES (
    9,
    'Замена картриджа в принтере (кабинет 105)',
    'Закончился тонер в принтере HP LaserJet. Печатает с белой полосой посередине страницы, текст почти не читается. Просьба заменить картридж на новый.',
    'HARDWARE',
    'USER',
    'LOW',
    'LOW',
    'NEW',
    3,
    NULL,
    NOW() - INTERVAL '5 hours',
    NOW() - INTERVAL '5 hours' + INTERVAL '72 hours',
    1.0
);

-- Update tickets sequence to correct next value
SELECT setval('tickets_id_seq', 9, true);

-- 3. Insert Dynamic Values
INSERT INTO ticket_dynamic_values (id, filter_id, ticket_id, value_id) VALUES
(1, 1, 3, 2), -- Ticket 3 has Location: MAIN
(2, 1, 4, 1); -- Ticket 4 has Location: 313

SELECT setval('ticket_dynamic_values_id_seq', 2, true);

-- 4. Insert Ticket Comments
INSERT INTO ticket_comments (id, created_at, text, author_id, ticket_id) VALUES
(1, NOW() - INTERVAL '1 hour', 'Логи сервера показывают утечку соединений с базой данных CRM. Перезапускаю пул соединений и анализирую зависшие транзакции.', 1, 1),
(2, NOW() - INTERVAL '20 minutes', 'Уже на месте, выполняю откат драйверов видеокарты Nvidia к стабильной версии.', 2, 2),
(3, NOW() - INTERVAL '2 hours', 'Проверил логи принтера, действительно есть ложное срабатывание датчика замятия. Иду на место с инструментами для чистки датчика.', 2, 5),
(4, NOW() - INTERVAL '4 hours 30 minutes', 'Запрос выполнен. Доступ выдан, роль Developer настроена.', 2, 7);

SELECT setval('ticket_comments_id_seq', 4, true);

-- 5. Insert Ticket History
INSERT INTO ticket_history (id, action, created_at, description, ticket_id, user_id) VALUES
-- Ticket 1
(1, 'CREATED', NOW() - INTERVAL '2 hours', 'Заявка создана', 1, 3),
(2, 'ASSIGNED', NOW() - INTERVAL '1 hour 45 minutes', 'Назначена исполнителю: admin', 1, 3),
(3, 'STATUS_CHANGED', NOW() - INTERVAL '1 hour 45 minutes', 'Статус изменен с Новый на В работе', 1, 3),
(4, 'COMMENT_ADDED', NOW() - INTERVAL '1 hour', 'Добавлен комментарий', 1, 1),

-- Ticket 2
(5, 'CREATED', NOW() - INTERVAL '30 minutes', 'Заявка создана', 2, 4),
(6, 'ASSIGNED', NOW() - INTERVAL '25 minutes', 'Назначена исполнителю: it', 2, 2),
(7, 'STATUS_CHANGED', NOW() - INTERVAL '25 minutes', 'Статус изменен с Новый на В работе', 2, 2),
(8, 'COMMENT_ADDED', NOW() - INTERVAL '20 minutes', 'Добавлен комментарий', 2, 2),

-- Ticket 3
(9, 'CREATED', NOW() - INTERVAL '4 hours', 'Заявка создана', 3, 4),

-- Ticket 4
(10, 'CREATED', NOW() - INTERVAL '1 hour', 'Заявка создана', 4, 3),

-- Ticket 5
(11, 'CREATED', NOW() - INTERVAL '3 hours', 'Заявка создана', 5, 3),
(12, 'ASSIGNED', NOW() - INTERVAL '2 hours 15 minutes', 'Назначена исполнителю: it', 5, 2),
(13, 'STATUS_CHANGED', NOW() - INTERVAL '2 hours 15 minutes', 'Статус изменен с Новый на В работе', 5, 2),
(14, 'COMMENT_ADDED', NOW() - INTERVAL '2 hours', 'Добавлен комментарий', 5, 2),

-- Ticket 6
(15, 'CREATED', NOW() - INTERVAL '2 days', 'Заявка создана', 6, 3),
(16, 'ASSIGNED', NOW() - INTERVAL '1 day 22 hours', 'Назначена исполнителю: it', 6, 2),
(17, 'STATUS_CHANGED', NOW() - INTERVAL '1 day 22 hours', 'Статус изменен с Новый на В работе', 6, 2),
(18, 'STATUS_CHANGED', NOW() - INTERVAL '1 day', 'Статус изменен с В работе на Закрыт', 6, 2),

-- Ticket 7
(19, 'CREATED', NOW() - INTERVAL '1 day', 'Заявка создана', 7, 3),
(20, 'ASSIGNED', NOW() - INTERVAL '23 hours', 'Назначена исполнителю: it', 7, 2),
(21, 'STATUS_CHANGED', NOW() - INTERVAL '23 hours', 'Статус изменен с Новый на В работе', 7, 2),
(22, 'COMMENT_ADDED', NOW() - INTERVAL '4 hours 30 minutes', 'Добавлен комментарий', 7, 2),
(23, 'STATUS_CHANGED', NOW() - INTERVAL '4 hours', 'Статус изменен с В работе на Закрыт', 7, 2),

-- Ticket 8
(24, 'CREATED', NOW() - INTERVAL '6 hours', 'Заявка создана', 8, 3),

-- Ticket 9
(25, 'CREATED', NOW() - INTERVAL '5 hours', 'Заявка создана', 9, 3);

SELECT setval('ticket_history_id_seq', 25, true);
