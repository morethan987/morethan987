package browser

import (
	"fmt"
	"log/slog"
	"slices"
	"time"

	"github.com/go-rod/rod"
	"github.com/go-rod/rod/lib/proto"
	"github.com/morethan987/AutoClicks_Rod/internal/config"
	"github.com/morethan987/AutoClicks_Rod/internal/notification"
)

// WaitForCoursePage waits until the course list page has finished rendering.
func WaitForCoursePage(page *rod.Page, cfg *config.Config) error {
	slog.Info("Waiting for course page to load", "selector", cfg.Selectors.Course.Flag)
	_, err := page.Timeout(40 * time.Second).Element(cfg.Selectors.Course.Flag)
	if err != nil {
		return fmt.Errorf("course: wait for course page flag %q: %w", cfg.Selectors.Course.Flag, err)
	}
	slog.Info("Course page loaded successfully")
	return nil
}

// FindTargetCourses scans the course list and returns elements for courses that
// match any target's name + ID. Teacher matching happens later in SelectCourse.
func FindTargetCourses(page *rod.Page, cfg *config.Config) ([]*rod.Element, error) {
	rows, err := page.Timeout(10 * time.Second).Elements(cfg.Selectors.Course.DataRow)
	if err != nil {
		return nil, fmt.Errorf("course: find data rows %q: %w", cfg.Selectors.Course.DataRow, err)
	}

	var matched []*rod.Element
	for _, row := range rows {
		linkEl, err := row.Element("a")
		if err != nil {
			continue // row has no link, skip
		}
		idEl, err := row.Element("div")
		if err != nil {
			continue
		}

		courseName, err := linkEl.Attribute("title")
		if err != nil || courseName == nil {
			continue
		}
		courseID, err := idEl.Text()
		if err != nil {
			continue
		}

		for _, target := range cfg.Courses {
			if *courseName == target.Name && courseID == target.ID {
				matched = append(matched, linkEl)
				slog.Info("Target course found", "name", *courseName, "id", courseID)
				break
			}
		}
	}

	slog.Info("Course scan complete",
		"targets", len(cfg.Courses),
		"found", len(matched),
	)
	return matched, nil
}

// SelectCourse opens the sidebar for a given course link and attempts to enroll
// in any matching teacher's row that is available.
func SelectCourse(page *rod.Page, courseLink *rod.Element, cfg *config.Config) error {
	if err := courseLink.Click(proto.InputMouseButtonLeft, 1); err != nil {
		return fmt.Errorf("course: click course link: %w", err)
	}

	// Wait for sidebar to appear
	_, err := page.Timeout(10 * time.Second).Element(cfg.Selectors.Sidebar.SidebarFlag)
	if err != nil {
		return fmt.Errorf("course: wait for sidebar %q: %w", cfg.Selectors.Sidebar.SidebarFlag, err)
	}

	// Pre-locate close button (needed if we don't enroll)
	closeBtn, err := page.Timeout(10 * time.Second).Element(cfg.Selectors.Sidebar.CloseButton)
	if err != nil {
		return fmt.Errorf("course: find sidebar close button %q: %w", cfg.Selectors.Sidebar.CloseButton, err)
	}

	slog.Info("Sidebar loaded, extracting teacher rows")

	rows, err := page.Timeout(10 * time.Second).Elements(cfg.Selectors.Sidebar.DataRow)
	if err != nil {
		return fmt.Errorf("course: find sidebar data rows %q: %w", cfg.Selectors.Sidebar.DataRow, err)
	}
	slog.Info("Teacher rows found", "count", len(rows))

	selectFlag := false // tracks whether enrollment succeeded
	for _, row := range rows {
		cells, err := row.Elements("td")
		if err != nil || len(cells) < 4 {
			continue
		}
		teacherName, err := cells[3].Text()
		if err != nil {
			continue
		}
		slog.Info("Checking teacher", "name", teacherName)

		// Match teacher against all targets
		matched := false
		for _, target := range cfg.Courses {
			if slices.Contains(target.Teachers, teacherName) {
				matched = true
			}
			if matched {
				break
			}
		}
		if !matched {
			continue
		}

		slog.Info("Teacher matched, checking availability", "teacher", teacherName)

		available, err := isAvailable(row, cfg)
		if err != nil {
			return fmt.Errorf("course: check availability: %w", err)
		}
		if !available {
			slog.Info("Course is full or already selected", "teacher", teacherName)
			continue
		}

		// Tick the checkbox
		checkbox, err := row.Timeout(10 * time.Second).Element(cfg.Selectors.Sidebar.Checkbox)
		if err != nil {
			return fmt.Errorf("course: find checkbox: %w", err)
		}
		if err := checkbox.Click(proto.InputMouseButtonLeft, 1); err != nil {
			return fmt.Errorf("course: click checkbox: %w", err)
		}

		// Confirm enrollment
		if err := confirmSelection(page, cfg); err != nil {
			return fmt.Errorf("course: confirm selection: %w", err)
		}
		selectFlag = true
	}

	// If enrollment succeeded, sidebar closes automatically; otherwise close manually.
	if !selectFlag {
		slog.Info("No enrollment this round, closing sidebar manually")
		if err := closeBtn.Click(proto.InputMouseButtonLeft, 1); err != nil {
			return fmt.Errorf("course: close sidebar: %w", err)
		}
	}
	slog.Info("Sidebar closed, continuing to next course")
	return nil
}

// isAvailable returns true if the row has neither a "full" nor "already selected" badge.
func isAvailable(row *rod.Element, cfg *config.Config) (bool, error) {
	hasFull, _, err := row.Has(cfg.Selectors.Sidebar.FullFlag)
	if err != nil {
		return false, fmt.Errorf("isAvailable: check full flag: %w", err)
	}
	if hasFull {
		return false, nil
	}

	hasSelected, _, err := row.Has(cfg.Selectors.Sidebar.SelectedFlag)
	if err != nil {
		return false, fmt.Errorf("isAvailable: check selected flag: %w", err)
	}
	if hasSelected {
		return false, nil
	}

	return true, nil
}

// confirmSelection clicks the "Select" button and then the confirmation dialog button.
// It treats a missing button as a business-logic condition (not enrollment window) and logs + returns nil.
func confirmSelection(page *rod.Page, cfg *config.Config) error {
	hasSelect, selectBtn, err := page.Has(cfg.Selectors.Selection.SelectButton)
	if err != nil {
		return fmt.Errorf("confirmSelection: check select button: %w", err)
	}
	if !hasSelect {
		slog.Info("Select button not found — may not be enrollment window")
		return nil
	}

	if err := selectBtn.Click(proto.InputMouseButtonLeft, 1); err != nil {
		return fmt.Errorf("confirmSelection: click select button: %w", err)
	}
	slog.Info("Select button clicked, waiting for confirmation dialog")

	hasConfirm, confirmBtn, err := page.Has(cfg.Selectors.Selection.ConfirmButton)
	if err != nil {
		return fmt.Errorf("confirmSelection: check confirm button: %w", err)
	}
	if !hasConfirm {
		slog.Warn("Confirm button not found — enrollment may have failed")
		return nil
	}

	if err := confirmBtn.Click(proto.InputMouseButtonLeft, 1); err != nil {
		return fmt.Errorf("confirmSelection: click confirm button: %w", err)
	}
	slog.Info("Enrollment confirmed successfully!")

	// Send WeChat notification if server key is configured
	if cfg.ServerKey != "" {
		if err := notification.Send(cfg.ServerKey, "选课成功通知", "成功选择了一门课程！"); err != nil {
			slog.Warn("Failed to send ServerChan notification", "error", err)
		}
	}

	return nil
}
