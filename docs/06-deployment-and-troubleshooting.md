# Deployment and troubleshooting (Windows PowerShell)

Start in `enterprise-helpdesk-solid`. Install Maven 3.9+ separately and add its `bin` directory to PATH (installation location may vary). No Maven Wrapper is included. Install JDK 21 and Tomcat 10.1.x at the classroom paths below. Do not copy Servlet API into Tomcat or the WAR; the container provides it.

```powershell
$env:JAVA_HOME = 'C:\dev\java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version
& 'C:\dev\tomcat\apache-tomcat-10.1\bin\version.bat'
Set-Location .\v1-monolith
mvn clean test package
if ($LASTEXITCODE -ne 0) { throw 'V1 build failed' }
Copy-Item .\target\helpdesk-v1.war 'C:\dev\tomcat\apache-tomcat-10.1\webapps\helpdesk-v1.war' -Force
Set-Location ..\v2-solid
mvn clean test package
if ($LASTEXITCODE -ne 0) { throw 'V2 build failed' }
Copy-Item .\target\helpdesk-v2.war 'C:\dev\tomcat\apache-tomcat-10.1\webapps\helpdesk-v2.war' -Force
Set-Location ..
& 'C:\dev\tomcat\apache-tomcat-10.1\bin\startup.bat'
```

If Tomcat is already running, do not start a second copy. Check deployment logs and wait for both contexts to deploy. `pom.xml` finalName is `helpdesk-v1` / `helpdesk-v2`, so renaming a WAR changes its context path.

## Manual acceptance checks

```powershell
curl.exe -i http://localhost:8080/helpdesk-v1/request
curl.exe -i http://localhost:8080/helpdesk-v2/request
curl.exe -i --data-urlencode "name=Ada" --data-urlencode "type=IT" --data-urlencode "description=Cannot sign in" --data-urlencode "channel=EMAIL" http://localhost:8080/helpdesk-v1/request
curl.exe -i --data-urlencode "name=Ada" --data-urlencode "type=IT" --data-urlencode "description=Cannot sign in" --data-urlencode "channel=EMAIL" http://localhost:8080/helpdesk-v2/request
curl.exe -i --data-urlencode "name=Lin" --data-urlencode "type=HR" --data-urlencode "description=Timetable query" --data-urlencode "channel=SMS" http://localhost:8080/helpdesk-v1/request
curl.exe -i --data-urlencode "name=Lin" --data-urlencode "type=HR" --data-urlencode "description=Timetable query" --data-urlencode "channel=SMS" http://localhost:8080/helpdesk-v2/request
curl.exe -i --data-urlencode "name= " --data-urlencode "type=IT" --data-urlencode "description=Help" --data-urlencode "channel=EMAIL" http://localhost:8080/helpdesk-v1/request
curl.exe -i --data-urlencode "name= " --data-urlencode "type=IT" --data-urlencode "description=Help" --data-urlencode "channel=EMAIL" http://localhost:8080/helpdesk-v2/request
curl.exe -i --data-urlencode "name=<script>alert(1)</script>" --data-urlencode "type=IT" --data-urlencode "description=<script>text</script>" --data-urlencode "channel=EMAIL" http://localhost:8080/helpdesk-v1/request
curl.exe -i --data-urlencode "name=<script>alert(1)</script>" --data-urlencode "type=IT" --data-urlencode "description=<script>text</script>" --data-urlencode "channel=EMAIL" http://localhost:8080/helpdesk-v2/request
curl.exe -i http://localhost:8080/helpdesk-v1/requests
curl.exe -i http://localhost:8080/helpdesk-v2/requests
```

Observe 200 for valid requests, HIGH for IT, MEDIUM for HR, 400 for blank name, escaped script-like name and no saved invalid row. Open listing pages in the browser: the script-like name must appear as literal text with no alert. Description is stored but never displayed; it must not appear in logs either. Logs contain only simulated transport and ID, with no real delivery. Start with empty contexts to test the empty state; restart to verify data loss. Repeat a valid submission to confirm distinct IDs.

## Problems and remedies

| Symptom | Check / action |
|---|---|
| `mvn` unknown | Install Maven, add its actual `bin` directory to PATH, reopen PowerShell; no hardcoded Maven location is assumed |
| Wrong JDK or `release version 21 not supported` | `mvn -version` and `java -version` must both report 21; set JAVA_HOME and prepend its bin as above |
| Unsupported class version 65 | Tomcat is running on an older JVM; check `version.bat`, service JVM configuration and JAVA_HOME |
| Maven download/DNS/proxy failure | Confirm access to Maven Central and configure Maven settings for your environment; rerun `mvn clean test package` in each module. If the cache is unwritable, use `mvn "-Dmaven.repo.local=$env:TEMP\helpdesk-m2" clean test package` |
| Port 8080 already in use | Use `Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue` to identify the listener. Use one Tomcat instance; stop the known classroom instance before starting another. If intentionally changing connector port in `conf/server.xml`, update all URLs |
| 404 | Include `/helpdesk-v1/request` or `/helpdesk-v2/request`; `/` has no welcome page. Confirm matching WAR filename, successful deployment and `WEB-INF/web.xml` mappings |
| 405 | Use GET for forms/lists and POST only for `/request`; POST `/requests` is deliberately rejected |
| 400 | Check exact parameter names, trim/length constraints and uppercase allowlisted options; consult the HTML error |
| 500 | Read Tomcat `logs` files for the first exception; inspect deployment, classpath and JVM version. Do not assume invalid input caused an internal error |
| Old UI/stale WAR | Stop Tomcat; remove only the matching exploded `webapps/helpdesk-v1` or `helpdesk-v2` folder, copy the freshly built matching WAR, restart; local data will reset |
| `javax.servlet` linkage error | Tomcat 10.1 uses `jakarta.servlet.*`; remove old javax dependencies/classes, run clean build, redeploy fresh WAR. Servlet 6 API is provided scope |
| Intentional LSP test fails | This is expected only with `-Dhelpdesk.badLsp=true`; omit that property for normal test runs |

For a visible Tomcat startup log, use `& 'C:\dev\tomcat\apache-tomcat-10.1\bin\catalina.bat' run` instead of startup.bat. Do not run both concurrently.

The source targets Java 21. The pinned compiler 3.13.0, Surefire 3.5.2 and WAR 3.4.0 plugins are explained in each POM. No `javax.servlet`, database driver or real notification library is needed.
