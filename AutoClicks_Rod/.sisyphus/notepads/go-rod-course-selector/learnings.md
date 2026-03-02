# Learnings — go-rod-course-selector

## [2026-03-02] Session ses_353a9f02bffe57VoZfJ5HK9CmN — Initial Setup

### Python Reference Key Facts
- `py_ref/notification.py`: sctp pattern is `sctp(\d+)t` — extracts digits between "sctp" and "t"
- `py_ref/config.py`: Chrome args include `--headless=new` in Python, but Go version uses `launcher.New().Headless(bool)` instead
- Python's `forbidden_flag` (SelectionSelectors) and `select_button` (dict form) are DEAD CODE — do NOT migrate
- `span.text-error` and `span.text-success` are inline in Python's `_is_available()` — must go to YAML as SidebarSelectors.FullFlag / SidebarSelectors.SelectedFlag

### CSS Selector Conversions (Python → CSS)
- `{tag_name: input, name: username}` → `input[name='username']`
- `{tag_name: input, type: password}` → `input[type='password']`
- `{tag_name: button, type: submit, class_name: 'login-button ant-btn'}` → `button[type='submit'].login-button.ant-btn`
- `{tag_name: span, class_name: 'ant-table-column-title'}` → `span.ant-table-column-title`
- `{tag_name: tr, class_name: 'ant-table-row ant-table-row-level-0'}` → `tr.ant-table-row.ant-table-row-level-0`
- `sidebar_flag_css` → `div.ant-drawer-body tbody.ant-table-tbody` (already CSS)
- `{tag_name: input, type: checkbox}` → `input[type='checkbox']`
- `close_button_css` → `div.drawer-close-wrap-right svg` (already CSS)
- `data_raw_css` → `div.ant-drawer-body tbody.ant-table-tbody tr.ant-table-row.ant-table-row-level-0` (already CSS)
- `span.text-error` (inline) → SidebarSelectors.FullFlag
- `span.text-success` (inline) → SidebarSelectors.SelectedFlag
- `select_button_css` → `div.ant-drawer-body button` (already CSS)
- `confirm_button_css` → `.ant-modal button.ant-btn.ant-btn-primary` (already CSS)

### Go Module
- Module path: `github.com/morethan987/AutoClicks_Rod`
- Key deps: `github.com/go-rod/rod`, `gopkg.in/yaml.v3`

### Architecture Rules (from Metis review)
- NO Must* API for element operations — use error-returning API throughout
- NO interfaces — direct calls to Rod and net/http
- NO per-operation retry — single top-level retry loop only
- Two-tier error strategy: infra errors → return err (retry); business results → log + continue polling
- time.Sleep ALLOWED only for: retry backoff, polling interval. NOT for page state waits.
