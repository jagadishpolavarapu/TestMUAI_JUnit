# JUnit + Selenium Grid on TestMu AI — Assignment Project

This project implements the two scenarios described in your **JUnit assignment** and runs them against a Selenium Grid (TestMu AI cloud). It uses **JUnit 5**, **Selenium 4**, explicit waits, assertions, parallel execution, and captures screenshots and console logs. The tests are data-driven to cover the required browser/OS/version combinations. 

## Prerequisites
- Java 11+
- Maven 3.8+
- A TestMu AI account (username + access key) and Grid URL
- Update any vendor-specific capability keys using TestMu AI's **Capabilities Generator**

## Configure credentials and Grid URL
Set the following environment variables before running:

```bash
# Example (Linux/macOS)
export TESTMU_USERNAME="<your_username>"
export TESTMU_ACCESS_KEY="<your_access_key>"
export TESTMU_GRID_URL="https://<your-grid-domain>/wd/hub"

# On PowerShell (Windows)
$env:TESTMU_USERNAME="<your_username>"
$env:TESTMU_ACCESS_KEY="<your_access_key>"
$env:TESTMU_GRID_URL="https://<your-grid-domain>/wd/hub"
```

## Run tests
```bash
mvn -q -Dtest=*Tests test
```

Screenshots will be saved under `target/screenshots`. Each test prints the **Session ID** to the console, which you can use for submission.

## Notes
- Capability names under the `testMu:options` block are placeholders. Replace them with the exact keys from the TestMu AI **Capabilities Generator** (e.g., enable **network logs**, **video**, **screenshots**, **console logs**).  
- If Internet Explorer 11 is unavailable on your Grid, you can temporarily disable that row in `browserMatrix()`.
