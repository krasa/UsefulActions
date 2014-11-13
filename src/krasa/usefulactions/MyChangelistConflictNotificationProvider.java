/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package krasa.usefulactions;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.changes.ChangeListManagerImpl;
import com.intellij.openapi.vcs.changes.conflicts.ChangelistConflictTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.Nullable;

public class MyChangelistConflictNotificationProvider extends EditorNotifications.Provider<MyChangelistConflictNotificationPanel> {

    private static final Key<MyChangelistConflictNotificationPanel> KEY = Key.create("changelistConflicts");

    private final ChangelistConflictTracker myConflictTracker;

    public MyChangelistConflictNotificationProvider(ChangeListManagerImpl changeListManager) {
        myConflictTracker = changeListManager.getConflictTracker();
    }

    public Key<MyChangelistConflictNotificationPanel> getKey() {
        return KEY;
    }

    public MyChangelistConflictNotificationPanel createNotificationPanel(VirtualFile file, FileEditor fileEditor) {
        return myConflictTracker.hasConflict(file) ? MyChangelistConflictNotificationPanel.create(myConflictTracker, file) : null;

    }
}
