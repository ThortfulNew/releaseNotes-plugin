package com.thortful.tools.releaseNotes;
import hudson.Launcher;
import hudson.Extension;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.FilePath;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Release Notes builder
 *
 * Very Rough Cut attempt for now but does the job
 * Project skeleton generated with maven
 *
 * @author Eric Genet
 */
public class ReleaseNotesBuilder extends Builder {

    private final String filename;
    private final String noChangesText;

    @DataBoundConstructor
    public ReleaseNotesBuilder(String filename, String noChangesText) {
        this.filename = filename;

        if (noChangesText == null) {
            noChangesText = "";
        }
        this.noChangesText = noChangesText;
    }

    public String getFilename() {
        return filename;
    }

    public String getNoChangesText() {
        return noChangesText;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        PrintStream console = listener.getLogger();
        console.println("Gathering Changes for this build");

        // Aggregate the Changes for this build
        // TODO :  User driven conditional formatting of the output
        String changeSet = "";
        if (build.getChangeSet() != null)  {
            if (build.getChangeSet().isEmptySet()) {
                changeSet = noChangesText;
            } else {
                ChangeLogSet<Entry> changes = build.getChangeSet();
                for (Entry entry : changes) {
                    changeSet += " - " + entry.getMsg() + "\n";
                }
            }
        }

        // Write all this in the configured file
        try {

            String outputFileName = build.getWorkspace().getRemote();

            // Check the filename
            if (this.filename == null || this.filename.equalsIgnoreCase("")) {
                outputFileName += "/ReleaseNotes.txt";
            } else if (this.filename.startsWith("/")) {
                outputFileName += this.filename;
            } else {
                outputFileName += "/" + this.filename;
            }

            console.println("Writing changes log to " + outputFileName);

            PrintWriter writer = new PrintWriter(outputFileName, "UTF-8");
            writer.println(changeSet);
            writer.close();

            console.println("Done.");

        } catch (Exception e) {
            console.println("Something went wrong : " + e.getMessage());
        }

        return true;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        // Basic checks on filename
        public FormValidation doCheckFilename(@QueryParameter String value)
                throws IOException, ServletException {

            if (value.length() == 0 || value.equalsIgnoreCase("")) {
                return FormValidation.error("Please set a filename");
            }

            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {

            return true;
        }


        public String getDisplayName() {
            return "Output Changes log in a file";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // no configuration for now
            save();
            return super.configure(req,formData);
        }
    }
}

