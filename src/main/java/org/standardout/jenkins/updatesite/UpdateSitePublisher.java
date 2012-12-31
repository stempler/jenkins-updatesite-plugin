/**
 * 
 */
package org.standardout.jenkins.updatesite;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.model.Run;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Simon Templer
 *
 */
@SuppressWarnings("unchecked")
public class UpdateSitePublisher extends Recorder {
	
	/**
     * Path to the Update Site directory or archive in the workspace.
     */
    private final String updateSite;
    
    /**
     * If true, retain Update Sites for all the successful builds.
     */
    private final boolean keepAll;

    @DataBoundConstructor
    public UpdateSitePublisher(String updateSite, boolean keepAll) {
        this.updateSite = updateSite;
        this.keepAll = keepAll;
    }
    
    public String getUpdateSiteSource() {
        return updateSite;
    }

    public boolean isKeepAll() {
        return keepAll;
    }

    /**
     * Gets the directory where the Update Site is stored for the given project.
     */
    public static File getTarget(AbstractItem project) {
        return new File(project.getRootDir(),"updatesite");
    }

    /**
     * Gets the directory where the Update Site is stored for the given build.
     */
    public static File getTarget(Run<?, ?> run) {
        return new File(run.getRootDir(),"updatesite");
    }
    
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Publishing Update Site");

        EnvVars env = build.getEnvironment(listener);
        
        FilePath source = build.getWorkspace().child(env.expand(updateSite));
        FilePath target = new FilePath(keepAll ? getTarget(build) : getTarget(build.getProject()));

        if (!source.isDirectory()) {
        	if (!source.exists()) {
        		if(build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) {
                    // If the build failed, don't complain that there was no Update Site.
                    // The build probably didn't even get to the point where it produces the Update Site.
                    listener.error("Update Site to publish does not exist.");
                }
                build.setResult(Result.FAILURE);
                return true;
        	}
        	else {
	        	try {
	        		//TODO remove old target?
	                // extract to target
	        		source.unzip(target);
	            } catch (IOException e) {
	                Util.displayIOException(e,listener);
	                e.printStackTrace(listener.fatalError("Failed to unzip update site archive."));
	                build.setResult(Result.FAILURE);
	                return true;
	            }
        	}
        }
        else {
        	try {
                if (source.copyRecursiveTo("**/*",target)==0) {
                    if(build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) {
                    	// If the build failed, don't complain that there was no Update Site.
                        // The build probably didn't even get to the point where it produces the Update Site.
                        listener.error("Update Site folder is empty.");
                    }
                    build.setResult(Result.FAILURE);
                    return true;
                }
            } catch (IOException e) {
                Util.displayIOException(e,listener);
                e.printStackTrace(listener.fatalError("Failed to copy update site."));
                build.setResult(Result.FAILURE);
                return true;
            }
        }
        
        // add build action, if javadoc is recorded for each build
        if(keepAll)
            build.addAction(new UpdateSiteBuildAction(build));
        
        return true;
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public Collection<Action> getProjectActions(AbstractProject project) {
        return Collections.<Action>singleton(new UpdateSiteAction(project));
    }

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	@Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		
        public String getDisplayName() {
            return "Publish Update Site";
        }

//        /**
//         * Performs on-the-fly validation on the file mask wildcard.
//         */
//        @SuppressWarnings("rawtypes")
//		public FormValidation doCheckJavadocDir(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException, ServletException {
//            FilePath ws = project.getSomeWorkspace();
//            return ws != null ? ws.validateRelativeDirectory(value) : FormValidation.ok();
//        }

        @SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return req.bindJSON(UpdateSitePublisher.class, formData);
		}
        
    }

}
