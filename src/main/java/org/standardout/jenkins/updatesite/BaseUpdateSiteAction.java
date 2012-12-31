/**
 * 
 */
package org.standardout.jenkins.updatesite;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author Simon Templer
 * 
 */
public abstract class BaseUpdateSiteAction implements Action {
	
	public String getUrlName() {
		return "updatesite";
	}

	public String getDisplayName() {
		return "Update Site";
	}

	public String getIconFileName() {
		File dir = dir();
		if (dir != null && dir.exists())
			//TODO change
			return "help.png";
		else
			// hide it since we don't have an Update Site yet.
			return null;
	}

	/**
	 * Serves the Update Site.
	 */
	public void doDynamic(StaplerRequest req, StaplerResponse rsp)
			throws IOException, ServletException {
		new DirectoryBrowserSupport(this, new FilePath(dir()), getTitle(),
				"help.png", false).generateResponse(req, rsp, this);
	}

	protected abstract String getTitle();

	/**
	 * @return the directory where the Update Site was archived
	 */
	protected abstract File dir();
	
}
