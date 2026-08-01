@ECHO OFF
SET DIR=%~dp0
IF NOT EXIST "%DIR%gradle\wrapper\gradle-wrapper.jar" (
  ECHO Missing gradle\wrapper\gradle-wrapper.jar.
  ECHO Open this project in Android Studio and Sync Project, or run gradle wrapper --gradle-version 8.9.
  EXIT /B 1
)
java -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
