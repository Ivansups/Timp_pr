# Swing Bank — Tech Notes

## Требования
- JDK 17+
- Maven 3.8+
- SQLite JDBC (подтягивается через Maven)

## Сборка
```bash
mvn clean package
```
- Fat JAR: `target/swing-bank-1.0.0-shaded.jar`
- БД: `bank.db` в корне. При первом запуске создаётся и сидится (admin/user/alice/bob с 1–2 счетами).

## Запуск
- IDE: Main `com.bank.App`
- CLI: `java -jar target/swing-bank-1.0.0-shaded.jar`
- Сброс данных: удалить `bank.db` и запустить снова.

## Упаковка в exe (Windows, JDK 17+)
```bat
scripts\jpackage-win.cmd
```
Выход: `target\installer\SwingBank-1.0.0.exe` (вложенный runtime через jpackage/jlink).

## Архитектура (кратко)
- `db/Database` — init SQLite, схемы, сиды.
- `dao/*` — User/Account DAO через JDBC.
- `service/*` — Auth, Account (депозит/снятие/перевод).
- `ui/screens` — Login, Dashboard, User, Admin, MainFrame (навигация).
- `ui/components` — градиенты, кнопки, карточки, метрики.
- Тема: FlatLaf Dark + собственные цвета/градиенты (Palette, GradientPanel).

## Роли и сценарии
- USER: видит свои счета (<3), пополнение, снятие, перевод на чужие счета (свои скрыты).
- ADMIN: видит все счета, корректирует балансы (+/-).

## Известное
- Транзакции упрощены: перевод — одна БД-транзакция; прочие операции — простой update.
- Для сборки exe на macOS/Linux нужен Windows-хост (или используйте JAR).

