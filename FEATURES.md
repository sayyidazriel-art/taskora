# Taskora — Feature Guide

This document explains the main screens and features available in Taskora. Exact behavior can vary with the installed build and its current settings.

## 1. Home Dashboard

The Home dashboard is the starting point for reviewing daily activity.

| Component | Purpose |
| --- | --- |
| Profile Greeting | Displays the user's profile greeting and picture. |
| Level | Shows the user's current progression level. |
| XP Progress | Shows progress toward the next level. |
| Daily Streak | Displays the current activity streak according to the app's streak rules. |
| Today's Tasks | Shows tasks whose deadline date is today. |
| Coming Up | Shows upcoming tasks in a scrollable list. |
| Monthly Calendar | Provides a compact view of scheduled dates. |
| Daily Challenge | Shows progress toward the daily goal. |
| Weekly Challenge | Shows progress toward the weekly goal. |
| Monthly Challenge | Shows progress toward the monthly goal. |
| Focus Tip | Displays a productivity suggestion. |

Today's Tasks is date-based rather than simply hiding a task when its deadline time passes. A task whose deadline time has passed can remain visible for the rest of its deadline date.

## 2. My Tasks

My Tasks is the main area for creating and managing activities.

### Create a Task

A task can include:
- Title.
- Description.
- Start date.
- Deadline date and time.
- Difficulty.
- XP reward.

### Difficulty and Base XP

| Difficulty | Base XP |
| --- | ---: |
| Easy | 10 XP |
| Medium | 20 XP |
| Hard | 35 XP |
| Expert | 55 XP |

### Task Status and Completion

- Incomplete tasks are shown before completed tasks.
- Completed tasks are locked to protect their completion record.
- Completing a task updates the applicable XP and progression systems.
- The task's completion result is reflected in relevant history and statistics.

### Late and Missed Tasks

- Completing a task after its deadline time but on the same deadline date gives 50% of its base XP.
- An unfinished task can incur a penalty after its deadline date has ended. The specified penalty is 50% of its base XP and is recorded in XP History.

## 3. Calendar

Calendar provides a monthly overview of task dates.

Users can:
- Browse months.
- Identify dates with scheduled tasks.
- Select a date.
- Review tasks associated with that date.

## 4. Weekly Routines

Weekly Routines lets users create reusable templates for activities that repeat on selected days.

### New Weekly Template

A template can define:
- Template name.
- Description.
- Difficulty.
- Scheduled time.
- Days of the week.

### Generate Next Week

Generate Next Week creates tasks from a selected template for the following week. Generation is intended to be limited to once per template for the target week to prevent duplicate task creation.

## 5. Focus Mode

Focus Mode is a timer-based workspace for concentrating on an activity.

Available controls and information can include:
- Focus duration presets.
- Optional task association.
- Start, pause, resume, and finish controls.
- Saved focus-session history.

A focus session can be used without linking it to a task. When linked, it can help the user review time spent on a particular activity.

## 6. Achievements

Achievements recognize milestones reached through app activity.

An achievement can show:
- Name and icon.
- Description.
- Reward XP.
- Locked or unlocked state.
- Unlock history.

Examples of achievement categories include task completion, XP milestones, level progression, streaks, focus activity, and Hidden Quest completion.

## 7. Hidden Quests

Hidden Quests are optional surprise activities.

Their intended rules include:
- Random appearance.
- No simultaneous pending quests.
- Avoiding back-to-back appearances.
- No penalty for skipping or missing a quest.
- Double XP reward when successfully completed.
- Appearance window between 05:00 and 22:00.

Quest availability and timing may depend on the app's local scheduling behavior.

## 8. Challenges

Challenges provide goals over different periods.

- Daily Challenge: a goal for the current day.
- Weekly Challenge: a goal for the current week.
- Monthly Challenge: a goal for the current month.

The challenge area tracks progress based on qualifying activity. Reward claiming, challenge history, and advanced reward mechanics should be checked against the actual installed build.

## 9. Statistics

Statistics provides an overview of productivity patterns.

| Metric or View | Purpose |
| --- | --- |
| Completed Tasks | Counts completed tasks. |
| Total XP | Summarizes XP activity. |
| XP History | Lists XP gains and penalties. |
| Productivity Trend | Shows changes in productivity over time. |
| Weekly Analytics | Reviews activity by week. |
| Monthly Analytics | Reviews activity by month. |
| Yearly Analytics | Reviews activity by year. |
| Task Distribution | Shows how tasks are distributed. |
| Difficulty Distribution | Shows task difficulty breakdown. |
| Focus Statistics | Summarizes focus sessions and duration. |
| Productivity Score | Provides an app-defined productivity indicator. |
| Consistency Score | Provides an app-defined consistency indicator. |

Scores are indicators calculated by the application; they should not be interpreted as clinical or scientifically validated measurements.

## 10. Profile

Profile contains personal information and application preferences.

### Personal Profile
- View and update profile information.
- Change the profile picture.
- Review progression information.

### Appearance
- Light theme.
- Dark theme.
- System theme.
- Accent color options, where available.

### Dashboard Preferences
Control the visibility of supported Home dashboard sections.

### Language
- Bahasa Indonesia.
- English.

### Notifications
Configure supported local notification preferences.

### Quiet Hours
Quiet Hours defines a period when notifications should be suppressed or limited. For example, setting 22:00–06:00 can help avoid interruptions overnight. Exact behavior depends on notification permissions and Android system settings.

### Storage Manager
Review local storage information and available data-management controls.

### Backup and Restore
Create or restore a backup. If encrypted backup is enabled, protect the password carefully. Keep an exported copy outside the app's private storage.

## 11. Systems Behind the Screens

| System | Responsibility |
| --- | --- |
| Room / SQLite | Stores structured app data locally. |
| XP System | Applies XP rewards and penalties. |
| Level System | Calculates progression from XP. |
| Streak System | Tracks qualifying activity over time. |
| Achievement System | Checks and records achievement unlocks. |
| Challenge System | Tracks progress toward challenge goals. |
| Notification Manager | Schedules supported local reminders. |
| Backup System | Exports and restores supported app data. |
| Localization | Provides supported language strings. |
| Theme Manager | Applies appearance preferences. |

## 12. Example of an Integrated Workflow

1. Create a task in My Tasks.
2. Review the task on Home or Calendar.
3. Start a Focus Mode session, optionally linked to the task.
4. Complete the task.
5. Receive the applicable XP reward.
6. Progress toward levels, streaks, achievements, and challenges.
7. Review activity in Statistics.
8. Create a backup to preserve important data.

Taskora is designed so these features work together as one productivity workflow rather than as unrelated tools.
