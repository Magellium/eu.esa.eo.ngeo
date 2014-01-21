package int_.esa.eo.ngeo.downloadmanager.notifications;

public enum NotificationLevel {
    FATAL(0),
    ERROR(1),
    WARNING(2),
    INFO(3);
    
    private final int notificationLevelValue;
    
    private NotificationLevel(int notificationLevelValue) {
        this.notificationLevelValue = notificationLevelValue;
    }

    public int getNotificationLevelValue() {
        return notificationLevelValue;
    }
}
