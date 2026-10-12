# Изменения для Practice4

Распаковать архив в корень существующего проекта — в папку, где лежит `settings.gradle.kts`. Все пути ниже полные относительно этой папки. Внешней папки с названием проекта в архиве нет.

## Изменены

- `app/build.gradle.kts`
- `app/src/androidTest/java/com/example/itunes_search/AppNavigationTest.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/itunes_search/MainActivity.kt`
- `app/src/main/java/com/example/itunes_search/data/model/ItunesResponse.kt`
- `app/src/main/java/com/example/itunes_search/data/model/Track.kt`
- `app/src/main/java/com/example/itunes_search/data/repository/MockTrackRepository.kt`
- `app/src/main/java/com/example/itunes_search/data/repository/TrackRepository.kt`
- `app/src/main/java/com/example/itunes_search/ui/ITunesApp.kt`
- `app/src/main/java/com/example/itunes_search/ui/catalog/CatalogScreen.kt`
- `app/src/main/java/com/example/itunes_search/ui/catalog/CatalogUiState.kt`
- `app/src/main/java/com/example/itunes_search/ui/catalog/CatalogViewModel.kt`
- `app/src/main/java/com/example/itunes_search/ui/components/AlbumArtwork.kt`
- `app/src/main/java/com/example/itunes_search/ui/details/TrackDetailsScreen.kt`
- `app/src/main/java/com/example/itunes_search/ui/details/TrackDetailsViewModel.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/example/itunes_search/data/MockTrackRepositoryTest.kt`
- `gradle/libs.versions.toml`

## Добавлены

- `CHANGED_FILES_PRACTICE4.md`
- `PRACTICE4_README.md`
- `app/src/main/java/com/example/itunes_search/data/mapper/TrackMapper.kt`
- `app/src/main/java/com/example/itunes_search/data/remote/ItunesApi.kt`
- `app/src/main/java/com/example/itunes_search/data/remote/NetworkModule.kt`
- `app/src/main/java/com/example/itunes_search/data/repository/NetworkTrackRepository.kt`
- `app/src/main/java/com/example/itunes_search/di/AppContainer.kt`
- `app/src/main/java/com/example/itunes_search/domain/model/Track.kt`
- `app/src/main/java/com/example/itunes_search/domain/model/TrackError.kt`
- `app/src/main/java/com/example/itunes_search/domain/model/TrackResult.kt`
- `app/src/main/java/com/example/itunes_search/domain/repository/TrackRepository.kt`
- `app/src/main/java/com/example/itunes_search/domain/usecase/GetTrackDetailsUseCase.kt`
- `app/src/main/java/com/example/itunes_search/domain/usecase/SearchTracksUseCase.kt`
- `app/src/main/java/com/example/itunes_search/ui/components/NetworkStateContent.kt`
- `app/src/main/java/com/example/itunes_search/ui/details/TrackDetailsUiState.kt`
- `app/src/main/res/drawable/ic_refresh.xml`
- `app/src/test/java/com/example/itunes_search/data/NetworkTrackRepositoryTest.kt`
- `app/src/test/java/com/example/itunes_search/testutil/FakeTrackRepository.kt`
- `app/src/test/java/com/example/itunes_search/testutil/MainDispatcherRule.kt`
- `app/src/test/java/com/example/itunes_search/ui/CatalogViewModelTest.kt`
- `app/src/test/java/com/example/itunes_search/ui/TrackDetailsViewModelTest.kt`

## Удалены

Нет. Ничего вручную удалять не нужно.

## Что реализовано

- Retrofit-запросы `search` с `term`/`limit` и `lookup` с `id`.
- Асинхронная загрузка на IO-диспетчере, управляемая ViewModel через UseCase.
- Слои domain/data/ui, отдельные DTO и доменная модель.
- Загрузка, ошибка, пустой ответ, повтор; отмена старого поиска.
- Сетевой репозиторий в приложении; моки только для тестов.
- Тесты сетевого контракта, состояний ViewModel и навигации.

Инструкция запуска, проверки и записи для сдачи — в `PRACTICE4_README.md`.

Сборка и выполнение тестов в среде подготовки не подтверждены: Gradle не скачивается (`Network is unreachable`), Android SDK отсутствует.
