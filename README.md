## Run the tests

```bash
./gradlew test
```

Or run VersionControlTest directly from IntelliJ

You should expect 2 runs in total

## Code details and decisions

- Check the [setup PR](https://github.com/JoanMVPopov/project-j/pull/2)
- Check the [test implementation PR](https://github.com/JoanMVPopov/project-j/pull/3)

## Debug the tests

1. Open the Registry in IntelliJ (Ctrl+Shift+A → "Registry")
2. Enable debugger.auto.attach.from.console
3. Hit Debug on the test instead of Run

## First run

The first run will take longer because the framework:
1. Downloads IntelliJ IDEA Community Edition (ideaIC-252.28539.54.exe)
2. Extracts it to out/ide-tests/cache/builds/
3. Clones the GitHub project to out/ide-tests/cache/projects/

Subsequent runs reuse the cached IDE and project.

If a test crashes, kill any stale IDE processes and clean the test state, then re-run. The stale PID will appear in the error log as owner pid: <number>.

## Test report

Gradle generates an HTML report at:

build/reports/tests/test/index.html
