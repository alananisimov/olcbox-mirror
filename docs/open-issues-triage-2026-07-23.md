# Разбор открытых issues Olcbox

Дата среза: 23 июля 2026 года.

Проверено:

- все 18 открытых issues в `alananisimov/olcbox`, включая комментарии и приложенный к #73 лог;
- `olcbox` на `main`, commit `14a00da`;
- актуальный `openlibrecommunity/olcrtc` на `origin/master`, commit `2f2db04`;
- release/PR workflows и граница между UI, platform-кодом Olcbox и ядром olcrtc.

Здесь оставлены только предложения, у которых есть понятная пользовательская ценность, технически честный путь реализации и проверяемый результат. «Добавить всё», неподтверждённые обходы блокировок и уже исправленные задачи не превращены в backlog.

## Статус app-only реализации

После разбора в Olcbox реализованы изменения, не требующие нового API или поведения olcrtc:

- #121: ручной редактируемый ввод URL/URI и типизированные ошибки импорта;
- повторный импорт уже известного `subscriptionUrl` обновляет его группу без дублей, сохраняя стабильные IDs и локальные overrides;
- #123: интервалы `s/m/h/d`, precedence server/manual значений, ближайший due timestamp, foreground check и retry backoff; override находится в импорте и `Settings → Subscriptions & Sharing`, а не в отдельной location;
- #102: единый точный SHA olcrtc для всех release jobs и SHA в About/diagnostic logs/release notes;
- личный запрос: `Jitsi + VP8` доступен как Experimental;
- #71/#47: сохраняемый desktop selector `Auto / TUN / System proxy / Local SOCKS`;
- часть #8: валидируемый per-location custom DNS через существующие mobile setters и desktop YAML;
- #90: удалены неработающие и небезопасные безусловные Jitsi `insecure` YAML keys;
- #81/#119: первый iOS onboarding с готовыми параметрами локального SOCKS5 и кнопкой копирования;
- подписку можно удалить целиком вместе со всеми импортированными из неё locations;
- mobile UI больше не предлагает SEI, которого нет в bundled mobile API; старые SEI URI совместимо переводятся в VP8 вместо нерабочего WB Stream DataChannel.

Не реализованы «псевдофиксы», которые лишь скрыли бы отсутствие core capability: UDP/Telegram calls, настоящий mobile failover, self-hosted Jitsi custom CA/pin и основная оптимизация VP8 idle power. Они остаются core-задачами из разделов ниже.

## Короткий вывод

В ближайший backlog Olcbox разумно взять:

1. нормальный ввод и диагностику ссылок подписки — [#121](https://github.com/alananisimov/olcbox/issues/121);
2. реальное соблюдение `#refresh` — [#123](https://github.com/alananisimov/olcbox/issues/123);
3. воспроизводимые сборки с точным SHA ядра — вместо формулировки [#102](https://github.com/alananisimov/olcbox/issues/102);
4. экспериментальный `Jitsi + VP8` — личный запрос, актуальное ядро его уже поддерживает;
5. выбор desktop-режима `TUN / System proxy / Local SOCKS` — [#71](https://github.com/alananisimov/olcbox/issues/71) и практическая часть [#47](https://github.com/alananisimov/olcbox/issues/47);
6. безопасную модель доверия к self-hosted Jitsi — [#90](https://github.com/alananisimov/olcbox/issues/90);
7. оптимизацию энергопотребления VP8 — [#114](https://github.com/alananisimov/olcbox/issues/114);
8. UDP для звонков — [#105](https://github.com/alananisimov/olcbox/issues/105), но только как совместную задачу olcrtc + Olcbox;
9. настоящий failover профилей — [#76](https://github.com/alananisimov/olcbox/issues/76), а не просто импорт YAML;
10. понятный iOS onboarding — [#81](https://github.com/alananisimov/olcbox/issues/81) и [#119](https://github.com/alananisimov/olcbox/issues/119).

Issues #29, #65, #75 и #95 уже исправлены в текущем `main`. #73 относится к старой версии без достаточного текущего воспроизведения. #52 пока не содержит проверенного альтернативного Telemost API, поэтому реализацию по нему начинать рано.

## Матрица решений

| Issue | Решение | Без изменения olcrtc | Изменение olcrtc |
|---|---|---:|---:|
| [#123 `#refresh`](https://github.com/alananisimov/olcbox/issues/123) | Реализовать | Да | Нет |
| [#121 вставка ссылки подписки](https://github.com/alananisimov/olcbox/issues/121) | Реализовать | Да | Нет |
| [#119 инструкция для iPhone](https://github.com/alananisimov/olcbox/issues/119) | Объединить с #81 как onboarding/docs | Да | Нет |
| [#114 расход батареи](https://github.com/alananisimov/olcbox/issues/114) | Реализовать после профилирования | Частично, только preset/UX | Да, для основного эффекта |
| [#105 Telegram calls](https://github.com/alananisimov/olcbox/issues/105) | Реализовать как отдельный UDP milestone | Нет | Да |
| [#102 latest olcrtc](https://github.com/alananisimov/olcbox/issues/102) | Закрыть исходную задачу; сделать pin + SHA | Да, CI/Olcbox | Нет |
| [#95 iOS background SOCKS](https://github.com/alananisimov/olcbox/issues/95) | Проверить на текущей сборке и закрыть | Уже исправлено | Нет |
| [#90 Jitsi TLS](https://github.com/alananisimov/olcbox/issues/90) | Реализовать безопасное per-location trust | Нет | Да |
| [#81 Olcbox на iOS](https://github.com/alananisimov/olcbox/issues/81) | Сначала onboarding; настоящий VPN — отдельный epic | Да | Не для onboarding |
| [#76 YAML/failover](https://github.com/alananisimov/olcbox/issues/76) | Реализовать настоящий failover | Только упрощённый импорт | Да, на mobile |
| [#75 много пунктов Settings](https://github.com/alananisimov/olcbox/issues/75) | Закрыть после проверки текущей сборки | Уже исправлено | Нет |
| [#73 Telemost 1.0.92](https://github.com/alananisimov/olcbox/issues/73) | Не делать фикс без нового repro | — | — |
| [#71 системный proxy](https://github.com/alananisimov/olcbox/issues/71) | Реализовать selector desktop-режима | Да | Нет |
| [#65 updater без proxy](https://github.com/alananisimov/olcbox/issues/65) | Закрыть после проверки текущей сборки | Уже исправлено | Нет |
| [#52 Telemost/T-Bank](https://github.com/alananisimov/olcbox/issues/52) | Отложить до подтверждения endpoint | Нет | Да, если endpoint подтвердится |
| [#47 конфликт с другими VPN](https://github.com/alananisimov/olcbox/issues/47) | Объединить практический фикс с #71 | Да | Нет |
| [#29 автообновления](https://github.com/alananisimov/olcbox/issues/29) | Закрыть | Уже реализовано | Нет |
| [#8 все параметры](https://github.com/alananisimov/olcbox/issues/8) | Разделить; не реализовывать как raw-конфиг | Частично | Частично |
| Личный запрос `Jitsi + VP8` | Реализовать как Experimental | Да, с актуальным ядром | Нет; pin проверенного ядра |

## Приоритетный backlog

### P0 — небольшие изменения с большим эффектом

#### 1. Надёжный импорт подписки — #121

Сейчас экран предлагает «Paste link or URI», но фактически читает только системный clipboard. Пользователь не может ввести или исправить URL вручную. Любая ошибка сети, TLS, HTTP или парсинга сводится к одному сообщению `No valid Olcbox config found`.

По приложенному кейсу сам формат ответа панели корректный: обычный text response с `#refresh: 10m` и строками `olcrtc://...`. Вероятный источник ошибки — HTTPS по IP-адресу и связанная с этим проверка сертификата, но текущий Olcbox скрывает реальную причину. Это вывод по коду и скриншоту, а не подтверждённый диагноз сервера.

Что изменить только в Olcbox:

- добавить редактируемое поле URL/URI; clipboard оставить как кнопку;
- заменить `null` из download/parse path на типизированный результат: TLS error, timeout/DNS, HTTP status, пустой ответ, неподдерживаемый формат, невалидная строка конфигурации;
- показывать короткую безопасную причину в UI, не писать полный subscription body, ключи или URL с credentials в обычный лог;
- отдельно валидировать `http/https` URL и `olcrtc://` URI;
- добавить тесты на plain text subscription, redirect, non-2xx, TLS failure, пустой body и malformed config.

Не надо лечить #121 глобальным `skip TLS verify`. Для HTTPS по IP правильное решение — сертификат с корректным SAN, hostname или явно настроенный custom CA.

Критерий готовности: пользователь может вручную вставить URL и получает конкретную причину неудачи; корректный ответ панели импортируется.

#### 2. Реальное расписание `#refresh` — #123

`#refresh` сейчас сохраняется в `SubscriptionMetadata.refresh` и отображается, но scheduler использует только целое `updateIntervalHours`. Проверка due subscriptions запускается раз в час. Поэтому `#refresh: 10m` технически невозможно соблюсти.

Что изменить только в Olcbox:

- хранить интервал как duration/milliseconds, а не как целое число часов;
- парсить минимум `s`, `m`, `h`, `d`, с разумной нижней границей, например 5 минут;
- зафиксировать precedence: ручной override пользователя → `profile-update-interval` header → `#refresh` в body → default;
- хранить ручной override отдельно, иначе его невозможно отличить от значения сервера;
- заменить фиксированный часовой poll на вычисление ближайшего due timestamp;
- выполнять due-check при cold start и возвращении приложения в foreground;
- хранить `lastAttempt`, `lastSuccess` и применять backoff после сетевых ошибок, не удаляя старую рабочую конфигурацию;
- мигрировать существующий `updateIntervalHours` без потери настроек.

Force-refresh при каждом foreground не нужен: это создаст лишнюю нагрузку. Достаточно немедленно проверять due state и оставить ручную кнопку принудительного обновления.

#### 3. Воспроизводимый olcrtc в релизах — замена #102 и часть диагностики #73

Исходная #102 фактически выполнена: release workflow берёт `openlibrecommunity/olcrtc` с `master`. Но floating branch создаёт более серьёзную проблему: разные matrix jobs одного релиза теоретически могут собрать разные commits ядра, если `master` сдвинется между checkout.

Что изменить в CI/Olcbox:

- один раз в начале workflow разрешать выбранный ref в точный commit SHA;
- передавать этот SHA во все Windows/macOS/Linux/Android/iOS jobs;
- использовать один и тот же upstream в release и PR checks; сейчас PR checks указывают `alananisimov/olcrtc`, а release — `openlibrecommunity/olcrtc`;
- встраивать SHA ядра в `AppInfo`, экран About, diagnostic log и release notes;
- по возможности добавить protocol/build identifier в стартовый лог обеих сторон.

Это не требует изменения протокола olcrtc. Если build identifier понадобится в handshake для жёсткой проверки совместимости, это уже отдельное изменение ядра.

Критерий готовности: по одному логу однозначно видны версии Olcbox и olcrtc; все assets одного релиза собраны из одного SHA.

### P1 — продуктовые функции

#### 4. `Jitsi + VP8` как Experimental

Запрос здравый. Текущий Olcbox искусственно разрешает для Jitsi только `datachannel`: `supportedTransportsForProvider(JITSI)` возвращает один вариант, а import normalization молча превращает Jitsi+VP8 в DataChannel.

Актуальный olcrtc уже содержит примеры `client/server.jitsi.vp8channel.yaml`. Commit `255f82f` от 21 июля 2026 года исправил video transports на Jitsi-инсталляциях, где вместо `colibri-ws` используется SCTP bridge: добавлены запрос video forwarding, корректные `sendrecv` transceivers и публикация source. Текущий `2f2db04` также включает более позднюю изоляцию co-located VP8 sessions из `6fa08e7`.

Что изменить в Olcbox:

- разрешить `vp8channel` для Jitsi в модели, UI, import/export и connection checker;
- перестать молча заменять импортированный `Jitsi + VP8` на DataChannel;
- пометить вариант `Experimental`, DataChannel пока оставить рекомендуемым/default;
- показывать полный Jitsi room URL и проверять одинаковую transport-конфигурацию на клиенте и сервере;
- добавить unit tests normalization/import/export и smoke tests с актуальным olcrtc минимум на Jitsi с `colibri-ws` и с SCTP fallback;
- для первого выпуска pin exact протестированного `2f2db04` либо более нового commit, прошедшего тот же test matrix; `255f82f` считать только функциональным минимумом.

Изменять актуальное ядро для первого релиза функции не требуется. Сначала нужен реальный interop/soak test: документация olcrtc всё ещё считает Jitsi video transports менее стабильными, чем DataChannel.

#### 5. Выбор desktop routing mode — #71 + #47

Сейчас режим жёстко выбран по ОС:

- Linux → TUN;
- Windows → TUN;
- macOS → System Proxy/PAC.

Это объясняет часть конфликтов с WireGuard, Amnezia и Clash: два приложения одновременно меняют routes, DNS или system proxy. По #47 недостаточно данных для исправления конкретного Amnezia bug, но есть полезный общий продуктовый фикс.

Что изменить только в Olcbox:

- persisted selector `TUN / System proxy / Local SOCKS only`;
- показывать только поддерживаемые на конкретной ОС режимы;
- в `Local SOCKS only` запускать olcrtc без TUN, PAC и изменения системных настроек;
- хранить фактически активный режим, а не вычислять его заново по ОС при stop;
- восстанавливать proxy/routes/DNS только если их менял текущий запуск Olcbox;
- делать transactional cleanup после crash и показывать diagnostic summary изменённых routes/proxy;
- добавить тесты переключения режимов и совместного запуска с уже существующим system proxy/VPN.

Это эффективный app-only фикс. Изменение olcrtc не требуется: локальный SOCKS listener уже существует.

#### 6. Custom DNS как ограниченная, честно описанная функция — часть #8

В Olcbox DNS сейчас в основном захардкожен (`1.1.1.1:53`), на Linux добавлено определение текущего resolver. В mobile API olcrtc есть `SetDNS`, поэтому обычный per-location resolver для служебных запросов carrier можно добавить без нового core API.

Но запрос из комментария #8 про локальный AdGuard Home нельзя обещать решить одним полем. Android TUN использует mapped DNS: приложение получает синтетический адрес, SOCKS передаёт доменное имя, а конечное разрешение выполняет серверная сторона olcrtc. Приватный DNS, доступный только из домашней LAN клиента, сервер не увидит.

Разумный scope:

- добавить `DNS: Auto / Custom host:port` в location;
- передавать значение в существующий mobile API и desktop YAML;
- не менять виртуальный `mapdns` address Android TUN на пользовательский resolver;
- в UI явно объяснить, что resolver должен быть доступен той стороне, которая выполняет DNS resolution;
- проверить IPv4/IPv6 endpoint, hostname и port;
- для настоящего client-local/private DNS создать отдельную задачу: local resolution до SOCKS либо полноценная передача DNS/UDP через туннель. Это уже потребует изменений TUN path и, вероятно, olcrtc.

#### 7. Безопасное доверие к self-hosted Jitsi — #90

Commit `3539e4a` добавил в desktop YAML:

```yaml
tls:
  insecure_skip_verify: true
jitsi:
  insecure: true
```

В актуальной схеме config olcrtc таких полей нет, поэтому YAML keys игнорируются и проблему не решают. Кроме того, безусловное отключение TLS-проверки для всего Jitsi-трафика было бы небезопасным default.

Нужные изменения в olcrtc:

- явная TLS trust config, которая действительно доходит до HTTP/WebSocket/XMPP dialers Jitsi;
- предпочтительный вариант: custom CA PEM и/или SHA-256 SPKI/certificate pin;
- опциональный `insecure_skip_verify` только как явный expert per-profile flag с предупреждением;
- эквивалентные setters/arguments в mobile API;
- тесты: trusted public certificate, self-signed с imported CA/pin, wrong pin, expired certificate и explicit insecure mode.

Нужные изменения в Olcbox:

- убрать неработающие безусловные YAML keys;
- добавить per-location trust mode и безопасный импорт CA/pin;
- не включать insecure автоматически для Jitsi;
- не логировать приватные key/certificate contents.

#### 8. Энергопотребление VP8 — #114

Жалоба правдоподобна: `vp8channel` создаёт ticker с частотой FPS, по умолчанию 60, даже когда нет пользовательского трафика. В idle он просыпается на каждом tick и отправляет keepalive примерно каждые 100 мс. Каждый отправленный sample маскируется под валидный VP8 keyframe. Сам WebRTC media path тоже существенно дороже обычного TCP/DataChannel.

App-only mitigation:

- preset `Performance / Balanced / Battery saver` с измеренными, а не случайными FPS/batch значениями;
- предупреждать о trade-off throughput/latency;
- по возможности рекомендовать DataChannel там, где carrier его стабильно поддерживает.

Основное исправление в olcrtc:

- сделать idle writer event-driven: ждать outbound data либо отдельный keepalive deadline, не будить coroutine 60 раз в секунду без работы;
- не отправлять больше RTP samples, чем требует carrier liveness;
- проверить адаптивное pacing/batching при burst-трафике;
- добавить counters для RTP samples, bytes, wakeups/queue pressure;
- прогнать Android Battery Historian/Perfetto отдельно для idle, browsing и video-heavy трафика на Telemost, WB Stream и Jitsi.

Снижать default FPS вслепую не стоит: обе стороны и SFU чувствительны к pacing. Сначала нужны baseline и soak tests, затем отдельный core PR.

#### 9. UDP и Telegram calls — #105 + комментарии #8

Это реальный missing capability, а не настройка tun2socks.

Android пишет для HEV `udp: 'tcp'`, то есть ожидает UDP-in-TCP/UDP ASSOCIATE совместимость от локального SOCKS. Текущий olcrtc SOCKS server принимает только команду `CONNECT`; `UDP ASSOCIATE` отсутствует. Поэтому произвольный UDP до Telegram на server egress не появляется.

В olcrtc уже была серия commits с encrypted datagrams/udpwire, relay lifecycle и SOCKS5 UDP, но она была полностью откачена commit `37539d3` 6 июля 2026 года. Это полезный прототип, но не основание просто вернуть revert.

Нужный core milestone:

- определить один поддерживаемый контракт с HEV: стандартный SOCKS5 UDP ASSOCIATE либо документированный UDP-in-TCP framing;
- отдельный authenticated/encrypted datagram wire format с flow ID;
- bounded flow table, idle expiry, ограничения payload/rate и защита от spoofing/amplification;
- server-side UDP egress и ответы к правильному клиентскому flow;
- явная capability transports: native lossy datagrams для DataChannel и корректный fallback для VP8, без притворства, что надёжный KCP равен realtime UDP;
- unit, fuzz, loss/reordering и end-to-end tests, включая DNS и Telegram-like bidirectional RTP.

Изменения Olcbox после появления стабильного core API:

- выбрать HEV mode, совместимый с реализацией ядра;
- включать UDP только если bundled core сообщает capability;
- добавить diagnostics/counters и kill switch;
- проверить split tunnel и Android `protect()` для всех UDP sockets.

App-only фикса здесь нет. Переключение `udp: tcp/udp` без server-side поддержки создаст только другой вид поломки.

### P2 — большие функции и onboarding

#### 10. Настоящий profile failover — #76

CLI olcrtc уже поддерживает YAML `profiles[]` и supervisor. Olcbox умеет импортировать собственный JSON/text/URI, но не общий YAML. На mobile API есть только одиночный `Start/StartWithTransport`; supervisor наружу не экспортирован.

Просто распарсить YAML в несколько независимых locations можно без ядра, но это не даст автоматический failover и не выполнит основную ценность issue.

Нужные изменения olcrtc:

- mobile API вида `StartWithProfiles` или безопасная обёртка над supervisor;
- единый стабильный local SOCKS endpoint при переключении profile;
- callback состояния: active profile, причина отказа, retry/backoff, exhausted;
- stop/reconnect semantics и отсутствие параллельных конфликтующих sessions;
- поддержка transport-specific options каждого profile.

Нужные изменения Olcbox:

- отдельная сущность `ProfileGroup`, порядок и enabled state;
- импорт только документированной части схемы olcrtc, с понятными ошибками для неподдержанных server-only fields;
- UI active/fallback status, ручной переход и reorder;
- миграция обычной location в одноэлементную группу;
- end-to-end tests с принудительным падением первого profile.

До этого стоит переименовать file picker/help text так, чтобы он не обещал YAML, который фактически не разбирается.

#### 11. iOS onboarding — #81 + #119

Текущий iOS Olcbox поднимает локальный SOCKS, а не системный VPN/TUN. Поэтому пользователю нужен другой клиент, например Karing, который подключается к локальному SOCKS. Это можно сделать понятным без изменений ядра:

- отдельный first-run экран «На iOS Olcbox предоставляет local SOCKS»;
- показать host, port, username, password и кнопки copy/copy all;
- пошаговая настройка Karing с актуальными скриншотами;
- status check «SOCKS доступен» и тестовый HTTP request;
- честно описать background limitations;
- объединить #119 в документацию #81, а не поддерживать две параллельные инструкции.

Issue #95 уже закрыта в коде commit `e704948`: добавлены silent `AVAudioEngine` keepalive и reconnect watchdog. После проверки текущей сборки issue можно закрыть.

Архитектурно правильный системный VPN для iOS — отдельный epic на `NetworkExtension/NEPacketTunnelProvider`, entitlements, packet flow → tun2socks и lifecycle extension. Это в первую очередь platform-задача Olcbox; изменение wire protocol olcrtc не требуется, хотя mobile embedding API может понадобиться адаптировать под extension lifecycle.

## Что делать с остальными issues

### Уже исправлены

- [#29](https://github.com/alananisimov/olcbox/issues/29): есть автоматическая проверка обновлений при старте, интервалы 1/6/12/24 часа и сравнение release identity/version. Проверка SHA при каждом запуске не добавит полезной гарантии.
- [#65](https://github.com/alananisimov/olcbox/issues/65): commit `995f5c6` передаёт active subscription SOCKS proxy и в update check, и в APK download.
- [#75](https://github.com/alananisimov/olcbox/issues/75): Settings уже разбиты на hub/subroutes.
- [#95](https://github.com/alananisimov/olcbox/issues/95): исправлено commit `e704948`, нужен только reporter validation на свежей сборке.

Их разумно закрыть со ссылкой на commit/build, а не писать ещё один фикс.

### Недостаточно данных для разработки

- [#73](https://github.com/alananisimov/olcbox/issues/73): лог версии 1.0.92 показывает, что RTC и KCP стартуют, после чего client ждёт server welcome и получает timeout. Это больше похоже на несовместимые client/server core/config или неготовую серверную сторону, но это лишь вывод по старому логу. Нужен новый repro на текущем build с одинаковым SHA ядра. Без него issue закрыть как stale, разрешив reopen с парой логов.
- [#52](https://github.com/alananisimov/olcbox/issues/52): Telemost API сейчас захардкожен в olcrtc как `https://cloud-api.yandex.ru/telemost_front/v2/telemost`. В issue нет подтверждённого альтернативного API endpoint, который одновременно доступен под тарифом T-Bank и протокольно совместим. До такого подтверждения делать поле `api_base` бессмысленно. Если endpoint будет найден легальным наблюдением собственного трафика и проверен, тогда нужны core config/mobile setter, per-location expert field и contract tests.
- [#47](https://github.com/alananisimov/olcbox/issues/47): для отдельного Amnezia/WireGuard bug нужны routes, DNS, OS/build и логи до/после. Общую полезную часть следует реализовать в selector из #71.

## Как разделить #8

Issue «все параметры подключений» не должна реализовываться буквально. Raw args/config в основном UI создадут неподдерживаемые комбинации и затруднят миграции.

Здравые части:

- `Jitsi + VP8` — реализовать Experimental без изменения актуального ядра;
- custom DNS — реализовать ограниченный per-location field, отдельно описав private DNS limitation;
- UDP — вести как core milestone #105;
- VP8 FPS/batch — уже представлены в модели, оставить presets плюс expert tuning;
- `seichannel` — сначала исправить несоответствие платформ;
- `videochannel` — не включать в обычную mobile-сборку: FFmpeg заметно увеличит размер, а подтверждённого преимущества над VP8/SEI для мобильного продукта нет.

Отдельно обнаружено: Olcbox показывает `seichannel` для части providers на Android/iOS, но текущий `olcrtc/mobile.normalizeTransport()` поддерживает только `datachannel` и `vp8channel`; любое другое значение молча становится VP8. Немедленный app-only фикс — скрыть SEI на mobile и не принимать его импорт без предупреждения. Полноценный фикс требует расширить mobile API olcrtc: зарегистрировать SEI, передавать `seichannel.Options` (`fps`, `batch`, `fragment_size`, `ack_timeout_ms`) и возвращать ошибку для неизвестного transport вместо silent fallback.

## Рекомендуемый порядок работ

1. #121, #123 и exact olcrtc SHA: небольшие, хорошо тестируемые задачи.
2. `Jitsi + VP8` за feature flag/Experimental и desktop mode selector #71/#47.
3. Убрать ложную mobile SEI поддержку; затем решить, нужен ли полноценный core API.
4. Custom Jitsi trust #90.
5. Профилирование и core-оптимизация #114.
6. UDP milestone #105 — отдельная ветка/эпик с security и loss tests.
7. Failover #76 после стабилизации mobile core API.
8. iOS onboarding сразу; Network Extension — только как отдельное продуктовое решение.

Такой порядок сначала убирает ложное поведение и проблемы диагностики, затем добавляет востребованные комбинации, и только после этого берётся за изменения wire/runtime-архитектуры olcrtc.
