
## Безопасность

- ПИН **не хранится в открытом виде**: сохраняется **PBKDF2-хеш + соль** в `EncryptedSharedPreferences`.
- Биометрия реализована через `androidx.biometric.BiometricPrompt` с `BIOMETRIC_STRONG`.

## Как реализована биометрия (подробно)

Идея такая: **ПИН — базовый способ входа**, а биометрия — быстрый способ разблокировать приложение, если пользователь сам её включил и если устройство поддерживает “strong” биометрию.

### 1) Зависимости и разрешение

- Зависимость на библиотеку биометрии добавлена в [app/build.gradle.kts](app/build.gradle.kts): используется `androidx.biometric:biometric`.
- В манифест добавлено разрешение на биометрию: [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml).

### 2) Проверка, доступна ли биометрия на устройстве

Проверка делается через `BiometricManager.canAuthenticate(...)` с флагом `BIOMETRIC_STRONG`.

- Реализация: [app/src/main/java/ru/uni/mdnotes/auth/BiometricAuth.kt](app/src/main/java/ru/uni/mdnotes/auth/BiometricAuth.kt)
- При старте приложения доступность сохраняется в состоянии VM: [app/src/main/java/ru/uni/mdnotes/MainActivity.kt](app/src/main/java/ru/uni/mdnotes/MainActivity.kt)
- Если биометрия недоступна, переключатель “Разрешить биометрию” скрывается/отключается на экране входа: [app/src/main/java/ru/uni/mdnotes/ui/LockScreen.kt](app/src/main/java/ru/uni/mdnotes/ui/LockScreen.kt)

Зачем это нужно:

- чтобы не показывать пользователю кнопку биометрии там, где она не работает;
- чтобы не пытаться открыть prompt на устройствах без настроенной биометрии.

### 3) Включение/выключение биометрии пользователем

Биометрия **не включается автоматически** навсегда — пользователь сам выбирает:

- при первом запуске (после установки ПИН) можно включить/выключить биометрию;
- на экране входа есть тумблер “Разрешить биометрию”.

Флаг хранится в `EncryptedSharedPreferences`:

- Хранилище: [app/src/main/java/ru/uni/mdnotes/auth/AuthStore.kt](app/src/main/java/ru/uni/mdnotes/auth/AuthStore.kt)
- Управление флагом и состоянием: [app/src/main/java/ru/uni/mdnotes/auth/AuthViewModel.kt](app/src/main/java/ru/uni/mdnotes/auth/AuthViewModel.kt)

Почему так:

- пользователю проще явно контролировать способ входа;
- это совместимо с требованием “входить надо или по ПИН, или по биометрии”.

### 4) Как именно вызывается BiometricPrompt

Вход по биометрии происходит так:

1. Экран входа вызывает `onUnlockWithBiometric(activity)`.
2. ViewModel проверяет:
	- доступность биометрии на устройстве;
	- что пользователь разрешил биометрию.
3. Если всё ок — показывается `BiometricPrompt`.
4. При успехе выставляется `isUnlocked = true`, и UI переключается на экран заметок.

Ключевые места:

- Запуск prompt + обработчики success/error: [app/src/main/java/ru/uni/mdnotes/auth/BiometricAuth.kt](app/src/main/java/ru/uni/mdnotes/auth/BiometricAuth.kt)
- Логика “можно ли сейчас входить по биометрии”: [app/src/main/java/ru/uni/mdnotes/auth/AuthViewModel.kt](app/src/main/java/ru/uni/mdnotes/auth/AuthViewModel.kt)

Параметры prompt важные:

- `setAllowedAuthenticators(BIOMETRIC_STRONG)` — акцент на более “сильной” биометрии.
- `setNegativeButtonText("Ввести ПИН")` — явный fallback, чтобы пользователь мог перейти к ПИН, если биометрия не сработала/не хочет использовать.

### 5) Автозапрос биометрии при открытии экрана входа

Чтобы сделать “упор на биометрию”, на экране входа есть автозапуск prompt:

- Если биометрия доступна и включена пользователем, то при открытии экрана выполняется `onUnlockWithBiometric(...)`.
- Это реализовано через `LaunchedEffect` в UI: [app/src/main/java/ru/uni/mdnotes/ui/LockScreen.kt](app/src/main/java/ru/uni/mdnotes/ui/LockScreen.kt)

### 6) Что происходит при неуспехе

- `onAuthenticationFailed()` (палец/лицо не совпали) не ломает поток — пользователь может попробовать ещё раз.
- `onAuthenticationError(...)` (например, отмена/слишком много попыток) показывает текст ошибки, и остаётся вариант входа по ПИН.

Таким образом, вход всегда возможен: **либо биометрия (если включена), либо ПИН**.
