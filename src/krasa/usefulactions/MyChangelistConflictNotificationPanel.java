/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.conflicts.ChangelistConflictTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import org.jetbrains.annotations.Nullable;

public class MyChangelistConflictNotificationPanel extends EditorNotificationPanel {

    private final ChangeList myChangeList;
    private final VirtualFile myFile;
    private final ChangelistConflictTracker myTracker;

    @Nullable
    public static MyChangelistConflictNotificationPanel create(ChangelistConflictTracker tracker, VirtualFile file) {
        final ChangeListManager manager = tracker.getChangeListManager();
        final Change change = manager.getChange(file);
        if (change == null) return null;
        final LocalChangeList changeList = manager.getChangeList(change);
        if (changeList == null) return null;
        return new MyChangelistConflictNotificationPanel(tracker, file, changeList);
    }

    private MyChangelistConflictNotificationPanel(ChangelistConflictTracker tracker, VirtualFile file, LocalChangeList changeList) {
        myTracker = tracker;
        myFile = file;
        myChangeList = changeList;
        myLabel.setText("File from non-active changelist is modified");
        
        createActionLabel("Move current file to active changelist", new Runnable() {
            public void run() {
                for (Change change : myChangeList.getChanges()) {
                    if (myFile.equals(change.getVirtualFile())) {
                        ChangeListManagerImpl manager = (ChangeListManagerImpl) ChangeListManager.getInstance(myTracker.getProject());
                        manager.moveChangesTo(manager.getDefaultChangeList(), new Change[]{change});
                    }
                }
            }
        }).setToolTipText("Move current file to active changelist");

//        createActionLabel("Move changes", new Runnable() {
//            public void run() {
//                ChangelistConflictResolution.MOVE.resolveConflict(myTracker.getProject(), myChangeList.getChanges());
//            }
//        }).setToolTipText("Move changes to active changelist (" + manager.getDefaultChangeList().getName() + ")");
//
//        createActionLabel("Switch changelist", new Runnable() {
//            public void run() {
//                List<Change> changes = Collections.singletonList(myTracker.getChangeListManager().getChange(myFile));
//                ChangelistConflictResolution.SWITCH.resolveConflict(myTracker.getProject(), changes);
//            }
//        }).setToolTipText("Set active changelist to '" + myChangeList.getName() + "'");
//
//        createActionLabel("Ignore", new Runnable() {
//            public void run() {
//                myTracker.ignoreConflict(myFile, true);
//            }
//        }).setToolTipText("Hide this notification");

//    setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

//        myLinksPanel.add(new InplaceButton("Show options dialog", IconLoader.getIcon("/general/ideOptions.png"), new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//
//                ShowSettingsUtil.getInstance().editConfigurable(myTracker.getProject(),
//                        new ChangelistConflictConfigurable((ChangeListManagerImpl) manager));
//            }
//        }));
    }
}
