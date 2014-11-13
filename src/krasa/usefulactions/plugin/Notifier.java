package krasa.usefulactions.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

/**
 * @author Vojtech Krasa
 */
public class Notifier {

    public static void notify(Project project, String content, NotificationType warning) {
        Notification notification = new Notification("UsefulActions", "",
                content, warning);
        showNotification(notification, project);
    }


    static void showNotification(final Notification notification, final Project project) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Notifications.Bus.notify(notification, project);
            }
        });
    }


}
