/**
 * 
 */
package org.standardout.jenkins.updatesite;

import hudson.model.AbstractBuild;

import java.io.File;

/**
 * @author Simon Templer
 *
 */
public class UpdateSiteBuildAction extends BaseUpdateSiteAction {
	
	private final AbstractBuild<?,?> build;
	
	public UpdateSiteBuildAction(AbstractBuild<?,?> build) {
	    this.build = build;
	}

    protected String getTitle() {
        return build.getDisplayName() + " Update Site";
    }

    protected File dir() {
        return UpdateSitePublisher.getTarget(build);
    }
}
