# Enterprise Helpdesk: teaching SOLID through comparison

Two independent Java 21 / Jakarta Servlet 6 WAR applications for Tomcat 10.1. Version 1 deliberately mixes responsibilities while remaining functional and safe for this local exercise. Version 2 preserves the HTTP contract and separates the reasons to change. Both use only Servlet API at runtime; JUnit 5 is a test dependency. No Spring, database, JSP or external services.

**Local educational use only.** No authentication, authorization or CSRF protection. Do not expose publicly. All data disappears on restart or redeployment. EMAIL/SMS are simulations; no real destinations or delivery.

## Explore in order

1. Build and use [Version 1](v1-monolith/README.md); inspect its `RequestServlet` and `TEACHING` comments.
2. Read the [feature parity table](docs/01-feature-parity.md).
3. Build [Version 2](v2-solid/README.md); trace `RequestServlet → RequestService` and `ApplicationFactory`.
4. Follow the [SOLID map](docs/02-solid-map.md), [live demos](docs/03-classroom-demo-script.md) and [investigation](docs/04-student-investigation.md).
5. Check [verification results](docs/07-verification-results.md) for what was actually executed.

```text
enterprise-helpdesk-solid/
  README.md
  docs/00-instructor-overview.md ... 07-verification-results.md
  docs/examples/InterfaceSegregation.java
  tools/ParityCheck.java
  tools/verify.ps1
  tools/verify-direct.ps1
  v1-monolith/  pom.xml, README.md, src/main, src/test
  v2-solid/     pom.xml, README.md, src/main, src/test
```

For macOS Terminal instructions, explicit installation directories, dependency setup and run commands, see the [macOS setup guide](macos-setup/README.md).

## Build and deploy (PowerShell)

Install JDK 21, Maven 3.9+ and Tomcat 10.1.x. Maven Wrapper is not included; Maven must be on PATH. Run these commands from `enterprise-helpdesk-solid` (from the workspace root first run `Set-Location .\enterprise-helpdesk-solid`).

```powershell
$env:JAVA_HOME = 'C:\dev\java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version
Set-Location .\v1-monolith
mvn clean test package
if ($LASTEXITCODE -ne 0) { throw 'V1 verification failed' }
Copy-Item .\target\helpdesk-v1.war 'C:\dev\tomcat\apache-tomcat-10.1\webapps\helpdesk-v1.war' -Force
Set-Location ..\v2-solid
mvn clean test package
if ($LASTEXITCODE -ne 0) { throw 'V2 verification failed' }
Copy-Item .\target\helpdesk-v2.war 'C:\dev\tomcat\apache-tomcat-10.1\webapps\helpdesk-v2.war' -Force
& 'C:\dev\tomcat\apache-tomcat-10.1\bin\startup.bat'
Set-Location ..
```

Open http://localhost:8080/helpdesk-v1/request and http://localhost:8080/helpdesk-v1/requests; compare http://localhost:8080/helpdesk-v2/request and http://localhost:8080/helpdesk-v2/requests. The WAR filenames determine these context paths; explicit `web.xml` mappings provide `/request` and `/requests`.

Each module README has valid/invalid `curl.exe -i` commands. [Deployment and troubleshooting](docs/06-deployment-and-troubleshooting.md) covers setup and common errors. `tools/verify.ps1` builds in phase order and compares 12 actual servlet responses without Tomcat. `tools/verify-direct.ps1` is a reproducible Java/JUnit fallback when Maven cannot download dependencies; it also creates WARs without claiming a Maven build passed.

## Teaching materials

- [Instructor overview and 40-minute plan](docs/00-instructor-overview.md)
- [Parity and input contract](docs/01-feature-parity.md)
- [Exact SOLID code map](docs/02-solid-map.md)
- [Five live demos](docs/03-classroom-demo-script.md)
- [25-minute student investigation](docs/04-student-investigation.md)
- [Architecture diagrams](docs/05-architecture-before-after.md)
- [Deployment and troubleshooting](docs/06-deployment-and-troubleshooting.md)
- [Verification evidence and limitations](docs/07-verification-results.md)
