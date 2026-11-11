# Feature parity

All paths below are relative to `/helpdesk-v1` or `/helpdesk-v2`. Both use `text/html;charset=UTF-8`. Parameter names are `name`, `type`, `description`, `channel`; values are trimmed server-side. Options are case-sensitive. Baseline UI and response HTML match exactly.

| Endpoint/input | V1 expected | V2 expected |
|---|---|---|
| GET `/request` | 200, native form, IT/HR and EMAIL/SMS | Same |
| GET `/requests` before submission | 200, “No requests yet.” | Same |
| POST `/request`, IT, EMAIL or SMS | 200, HIGH, generated ID, trimmed name, type, chosen channel, SIMULATED | Same |
| POST `/request`, HR, EMAIL or SMS | 200, MEDIUM and same confirmation fields | Same |
| Name missing/blank or >50 after trim | 400, helpful name error | Same |
| Name 1 or 50 after trim | Accepted when other fields valid | Same |
| Description missing/blank or >200 after trim | 400, helpful description error | Same |
| Description 1 or 200 after trim | Accepted when other fields valid | Same |
| Type missing, lowercase, unknown or FINANCE | 400, choose IT/HR | Same |
| Channel missing, lowercase or unknown | 400, choose EMAIL/SMS | Same |
| Multiple invalid fields | First error in name/type/description/channel order | Same |
| Any invalid submission | No ID consumed, saved request or notification | Same |
| Valid duplicate submission | New ID and new entry; no deduplication | Same |
| GET `/requests` after valid submissions | ID/name/type/priority, ascending numeric ID | Same |
| POST `/requests` | 405, Allow: GET | Same |
| `<script>` in valid name/description | Stored as text; name escaped in confirmation/list; description not rendered | Same |
| Quotes, apostrophes, ampersands in displayed values | HTML escaped | Same |
| Restart or redeploy | Data and sequence reset | Same |

There are no real recipients, no full descriptions in logs, and no outbound notification calls. Notification logs contain only SIMULATED, transport and ID. Each application's sequence is independent. IDs may differ when histories differ; `REQ-0001` is unique within one running application instance, not across restarts or multiple Tomcat nodes.

`HttpContractTest` is the same fixture in both modules and tests statuses, output escaping, validation/list side effects and concurrent IDs. `tools/ParityCheck.java` loads each implementation in an isolated class loader and compares 12 complete response snapshots: form, empty list, four valid combinations, four invalid inputs, populated list and POST to the listing route. It compares actual status, content type and HTML, not just expected priority constants. It does not replace deployment testing.

Finance is a V2 test-only extension. Do not enable it in the baseline factory or form during comparison. A real Finance rollout would also update UI/options, validation guidance and documentation; either teach it on a separate branch or extend V1 too before claiming feature parity.
