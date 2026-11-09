# Testing notes – notifyhub

Pointers collected while writing the first tests. Grouped by topic.

## Why and what

- A unit test protects **one rule** of your code (e.g. "already stored → don't save again").
- It tests **your logic**, not Postgres, Spring or OpenSearch. Whether SQL, JPA mapping and Flyway work is a different
  test (`@DataJpaTest`).
- **Load as little as possible:** logic → plain unit test with mocks, talking to something external → slice test, only a
  few tests load the whole app.
- Tests describe **what** the result must be, not **how** it's built. Then they are a safety net for refactoring (
  `MappingBuilder` changed completely, the tests stayed the same).
- The simplest code is a good place to learn the tools; mocks pay off most where a wrong decision is dangerous (alias
  decisions).

## Structure

- Use `// given`, `// when`, `// then`. With `assertThrows`, "when" and "then" are one line: `// when + then`.
- Stubs (`when(...)`) belong in **given**: they prepare the situation.
- `@DisplayName`: *situation → expected behaviour*, e.g. "alias already points to the expected index → alias is left
  unchanged".
- One scenario per test.
- Make the scenario visible in the test itself: stub values that name the scenario, even if they equal the default (
  `indexExists → false`).
- Don't hide important values in `setUp` (e.g. `failOnStartup = false`). Create them in the test, or at least comment
  where they come from.
- Use a **local variable** when a test needs its own instance, instead of overwriting a shared field.
- Split nested assertions into variables: one line, one thing. Easier to read, easier to see what failed.

## Mockito basics

- `@ExtendWith(MockitoExtension.class)` turns on Mockito annotations. **No Spring.** No Spring banner in the output
  proves no context was started.
- `@Mock` = a fake. Without stubbing it returns defaults: `null`, `0`, `false`, `Optional.empty()`, empty collections.
- `@Spy` = a **real** object that Mockito can inject and watch.
- Don't mock helpers like `ObjectMapper`; use the real one.
- `@InjectMocks` creates the real class under test and passes **only mocks and spies** into its constructor.
- `@InjectMocks` runs **before** `@BeforeEach`. Fields set in `@BeforeEach` are not seen by it.
- A real dependency (e.g. the `OpenSearchProperties` record) → don't use `@InjectMocks`; build the class yourself with
  `new` in `@BeforeEach`.
- The constructor parameter order from `@RequiredArgsConstructor` = the order of the `private final` fields (Ctrl+P in
  IntelliJ shows it).
- A class's `final` field and the test's field are two different variables; setting the test field later doesn't change
  the class.

## Stubbing

- `when(x).thenReturn(value)` – fixed answer.
- `when(x).thenAnswer(invocation -> ...)` – run code, e.g. pretend to be the database and give the entity an id.
  `invocation.getArgument(0)` = first argument.
- `when(x).thenThrow(new SomeException(...))` – the call throws.
- **Stub only what the scenario is about**, plus what keeps the code away from side effects (e.g. `indexExists → true`
  to skip "create index").
- Strict stubs: `UnnecessaryStubbingException` means "this stub isn't needed for this scenario".
- Calling another mock inside `when(...)` (e.g. `definition.settings()`) silently produces `null` and can match by
  accident. Use matchers instead.

## Verifying

- `verify(mock).method(...)` = called **exactly once** (`times(1)` is the default; writing it is fine for readability).
- `verify(mock, never()).method(...)` = never called.
- Prefer `never().addAlias(any(), any())` over `never().addAlias(index, alias)`: specific arguments let a call with
  *other* arguments slip through.
- `ArgumentCaptor` catches what was passed, so you can check **what** was saved, not just **that** something was saved.

## Matchers

- Matching with `alias` or `eq(alias)` is the same (`equals()`).
- **All or nothing:** once one argument is a matcher (`any()`), all must be → wrap real values in `eq(...)`. Otherwise:
  `InvalidUseOfMatchersException`.

## Test data

- Only what the test needs. A mocked service never reads the entity's fields → `new IndexDefinitionEntity()` is enough.
- **Marker objects** show where a value came from, e.g. a body `{"from":"db"}`.
- Use the test's variables (`alias`, `index`), not literals. With a literal `"notifications-v2"`, raising `VERSION`
  turned scenario 2 silently into scenario 3 – and it still passed.
- Use realistic values (`http://localhost:9200`, `notifications`): failure messages become readable.
- Entities without their own `equals` (`@Getter`/`@Setter`, no `@Data`) compare by **identity** → pass the same instance
  to stub and code.

## Exceptions

- Find out who throws what in real life: the HTTP client throws `ResourceAccessException`, the initializer **translates
  ** it into `IllegalStateException`.
- Let the mock throw the **cause**; assert the **result**.
- If the mock throws the expected exception directly, the test passes without the `catch` ever running.
- `assertThrows(Type.class, () -> ...)` returns the exception. A result you don't use checks nothing (`.getCause()`
  alone does nothing).
- Check the cause is kept: `assertInstanceOf(ResourceAccessException.class, exception.getCause())`.
- `assertDoesNotThrow(() -> ...)` for "nothing is thrown".

## The break check

- After every test: **break the code on purpose** and make sure the test fails. A test that can't fail protects nothing.
- Examples: comment out `addAlias`, call `addAlias` in the "other index" branch, swap `settings`/`mappings`, comment out
  the `catch`, remove the cause `e`, always throw regardless of `failOnStartup`.

## JUnit and imports

- Use JUnit 5: `org.junit.jupiter.api.Assertions`, not JUnit 4's `org.junit.Assert`.
- `assertThrows`, `assertEquals` etc. are **static** → `import static org.junit.jupiter.api.Assertions.assertThrows;` (
  IntelliJ: Alt+Enter → "Import static method").
- Calling `run(...)` of an `ApplicationRunner` yourself: pass `new DefaultApplicationArguments()` (real, empty) rather
  than `null` or a mock.

## JSON in tests

- `assertEquals` on two `JsonNode`s compares **structure**, not text: field order and spacing don't matter.
- Text blocks (`"""`): watch for **trailing commas** before `}`; indent by nesting level so mistakes are visible.
- `objectMapper.stringNode("{...}")` is a JSON **string**, not an object. Use `createObjectNode()` or `readTree(...)`.
- Log the exact value you compared, not a similar one – otherwise the log lies (the "same mapping, different order"
  confusion).