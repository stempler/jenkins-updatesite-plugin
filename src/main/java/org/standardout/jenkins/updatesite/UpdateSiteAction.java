/**
 * 
 */
package org.standardout.jenkins.updatesite;

import hudson.model.ProminentProjectAction;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Run;

import java.io.File;

/**
 * @author Simon Templer
 *
 */
public class UpdateSiteAction extends BaseUpdateSiteAction implements ProminentProjectAction {
    private final AbstractItem project;

    public UpdateSiteAction(AbstractItem project) {
        this.project = project;
    }

    protected File dir() {
        // Would like to change AbstractItem to AbstractProject, but is
        // that a backwards compatible change?
        if (project instanceof AbstractProject) {
            AbstractProject<?, ?> abstractProject = (AbstractProject<?, ?>) project;

            Run<?, ?> run = abstractProject.getLastSuccessfulBuild();
            if (run != null) {
                File javadocDir = UpdateSitePublisher.getTarget(run);

                if (javadocDir.exists())
                    return javadocDir;
            }
        }

        return UpdateSitePublisher.getTarget(project);
    }

    protected String getTitle() {
        return project.getDisplayName() + " Update Site";
    }
}
