package com.example.PromptShieldAPI.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
    "activity.log.path=test-activity.log"
})
class ActivityLogServiceTest {

    private ActivityLogService activityLogService;
    
    @BeforeEach
    void setUp() {
        activityLogService = new ActivityLogService();
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup test log file
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("test-activity.log"));
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    void testLogActivity() {
        // Given
        String type = "TEST_ACTIVITY";
        String title = "Test Title";
        String description = "Test Description";
        String username = "testuser";

        // When
        activityLogService.logActivity(type, title, description, username);

        // Then
        List<Map<String, Object>> activities = activityLogService.getRecentActivity();
        assertFalse(activities.isEmpty());
        
        Map<String, Object> activity = activities.get(0);
        assertEquals(type, activity.get("type"));
        assertEquals(title, activity.get("title"));
        assertEquals(description, activity.get("description"));
        assertEquals(username, activity.get("username"));
        assertNotNull(activity.get("timestamp"));
    }

    @Test
    void testGetRecentActivity() {
        // Given
        activityLogService.logActivity("ACTIVITY1", "Title 1", "Desc 1", "user1");
        activityLogService.logActivity("ACTIVITY2", "Title 2", "Desc 2", "user2");
        activityLogService.logActivity("ACTIVITY3", "Title 3", "Desc 3", "user3");

        // When
        List<Map<String, Object>> activities = activityLogService.getRecentActivity();

        // Then
        assertEquals(3, activities.size());
        
        // Should be ordered from newest to oldest
        assertEquals("ACTIVITY3", activities.get(0).get("type"));
        assertEquals("ACTIVITY2", activities.get(1).get("type"));
        assertEquals("ACTIVITY1", activities.get(2).get("type"));
    }

    @Test
    void testGetLogStatistics() {
        // Given
        activityLogService.logActivity("TEST", "Test", "Test", "user");

        // When
        Map<String, Object> stats = activityLogService.getLogStatistics();

        // Then
        assertNotNull(stats);
        assertTrue((Integer) stats.get("totalEntries") > 0);
        assertNotNull(stats.get("fileSize"));
        assertNotNull(stats.get("lastModified"));
    }

    @Test
    void testEmptyLog() {
        // When
        List<Map<String, Object>> activities = activityLogService.getRecentActivity();

        // Then
        assertTrue(activities.isEmpty());
    }
} 