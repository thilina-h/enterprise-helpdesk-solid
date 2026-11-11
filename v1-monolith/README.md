# Helpdesk v1

Intentionally problematic monolithic design: one servlet has many reasons to change. Local educational use only. No authentication, authorization or CSRF protection; do not publish publicly. Storage lasts only until restart/redeployment. Notifications are simulated; no destinations or real delivery.

From the repository root in PowerShell (Maven 3.9+ installed and on PATH):

```powershell
$env:JAVA_HOME = 'C:\dev\java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version
Set-Location .\v1-monolith
mvn clean test package
Copy-Item .\target\helpdesk-v1.war 'C:\dev\tomcat\apache-tomcat-10.1\webapps\helpdesk-v1.war' -Force
& 'C:\dev\tomcat\apache-tomcat-10.1\bin\startup.bat'
curl.exe -i http://localhost:8080/helpdesk-v1/request
curl.exe -i --data-urlencode "name=Ada" --data-urlencode "type=IT" --data-urlencode "description=Cannot sign in" --data-urlencode "channel=EMAIL" http://localhost:8080/helpdesk-v1/request
curl.exe -i --data-urlencode "name= " --data-urlencode "type=IT" --data-urlencode "description=Help" --data-urlencode "channel=EMAIL" http://localhost:8080/helpdesk-v1/request
curl.exe -i http://localhost:8080/helpdesk-v1/requests
```

Browser: http://localhost:8080/helpdesk-v1/request and http://localhost:8080/helpdesk-v1/requests. Valid POST returns 200/HIGH; invalid returns 400 without saving/notifying. Try HR/SMS for MEDIUM. Duplicate submissions create distinct IDs. Inputs use parameters `name`, `type`, `description`, `channel`. Only POST /request submits; POST /requests returns 405. GET lists may reflect concurrent writes; IDs are atomic and storage ordered/thread-safe.

Servlet 6.0 mappings are explicit in `src/main/webapp/WEB-INF/web.xml`; no annotations. Java 21 and Tomcat 10.1 are required. Maven Wrapper is not included: install Maven 3.9+ and verify `mvn -version` reports JDK 21. See [troubleshooting](../docs/06-deployment-and-troubleshooting.md) for 404/405/400/500, stale WAR and namespace issues.
