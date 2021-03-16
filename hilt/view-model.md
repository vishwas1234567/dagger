---
layout: default
title: View Models
---

**Note:** Examples on this page assume usage of the Gradle plugin. If you are **not**
using the plugin, please read this [page](gradle-setup.md#hilt-gradle-plugin) for
details.
{: .c-callouts__note }

## Hilt View Models

A Hilt View Model is a
[Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
that is constructor injected by Hilt. To enable injection of a ViewModel by
Hilt use the
[`@HiltViewModel`](https://dagger.dev/api/latest/dagger/hilt/android/lifecycle/HiltViewModel.html)
annotation:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>
```java
@HiltViewModel
public final class FooViewModel extends ViewModel {

  @Inject
  FooViewModel(SavedStateHandle handle, Foo foo) {
    // ...
  }
}
```
{: .c-codeselector__code .c-codeselector__code_java }
```kotlin
@HiltViewModel
class FooViewModel @Inject constructor(
  val handle: SavedStateHandle,
  val foo: Foo
) : ViewModel
```
{: .c-codeselector__code .c-codeselector__code_kotlin }

Then an activitiy or fragments annotated with
[`@AndroidEntryPoint`](android-entry-point.md) can get the `ViewModel` instance
as normal using `ViewModelProvider` or the `by viewModels()` KTX extension:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>
```java
@AndroidEntryPoint
public final class MyActivity extends AppCompatActivity {

  private FooViewModel fooViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fooViewModel = new ViewModelProvider(this).get(FooViewModel.class);
  }
}
```
{: .c-codeselector__code .c-codeselector__code_java }
```kotlin
@AndroidEntryPoint
class MyActivity : AppCompatActivity() {
  private val fooViewModel: FooViewModel by viewModels()
}
```
{: .c-codeselector__code .c-codeselector__code_kotlin }

**Warning:** Even though the view model has an `@Inject` constructor, it is an
error to request it from Dagger directly (for example, via field injection)
since that would result in multiple instances. View Models must be retrieved
through the `ViewModelProvider` API. This is checked at compile time by Hilt.
{: .c-callouts__warning}

[`SavedStateHandle`](https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate)
is a default binding available to all Hilt View Models. Only dependencies from
the
[`ViewModelComponent`](https://dagger.dev/api/latest/dagger/hilt/android/components/ViewModelComponent.html)
and its parent components can be provided into the `ViewModel`.

## View Model Scope

All Hilt View Models are provided by the `ViewModelComponent` which follows the
same lifecycle as a `ViewModel`, i.e. it survives configuration changes. To
scope a dependency to a `ViewModel` use the
[`@ViewModelScoped`](https://dagger.dev/api/latest/dagger/hilt/android/scopes/ViewModelScoped.html)
annotation.

A `@ViewModelScoped` type will make it so that a single instance of the scoped
type is provided across all dependencies injected into the Hilt View Model.
Other instances of a `ViewModel` that requests the scoped instance will receive
a different instance. Scoping to the `ViewModelComponent` allows for flexible
and granular scope since View Models surive configuration changes and their
lifecycle is controlled by the activity or fragment. If a single instance needs
to be shared across various View Models then it should be scoped using either
`@ActivityRetainedScoped` or `@Singleton`.

For example, we can scope a dependency to be shared within a single ViewModel as
such:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>

```java
@Module
@InstallIn(ViewModelComponent.class)
final class ViewModelMovieModule {
  @Provides
  @ViewModelScoped
  static MovieRepository provideRepo(SavedStateHandle handle) {
      return new MovieRepository(handle.getString("movie-id"));
  }
}

public final class MovieDetailFetcher {
  @Inject MovieDetailFetcher(MovieRepository movieRepo) {
      // ...
  }
}

public final class MoviePosterFetcher {
  @Inject MoviePosterFetcher(MovieRepository movieRepo) {
      // ...
  }
}

@HiltViewModel
public final class MovieViewModel extends ViewModel {
  @Inject
  MovieViewModel(MovieDetailFetcher detailFetcher, MoviePosterFetcher posterFetcher) {
      // Both detailFetcher and posterFetcher will contain the same instance of
      // the MovieRepository.
  }
}
```

{: .c-codeselector__code .c-codeselector__code_java }

```kotlin
@Module
@InstallIn(ViewModelComponent::class)
internal object ViewModelMovieModule {
  @Provides
  @ViewModelScoped
  fun provideRepo(handle: SavedStateHandle) =
      MovieRepository(handle.getString("movie-id"));
}

class MovieDetailFetcher @Inject constructor(
  val movieRepo: MovieRepository
)

class MoviePosterFetcher @Inject constructor(
  val movieRepo: MovieRepository
)

@HiltViewModel
class MovieViewModel @Inject constructor(
  val detailFetcher: MovieDetailFetcher,
  val posterFetcher: MoviePosterFetcher
) : ViewModel {
  init {
    // Both detailFetcher and posterFetcher will contain the same instance of
    // the MovieRepository.
  }
}
```

{: .c-codeselector__code .c-codeselector__code_kotlin }
