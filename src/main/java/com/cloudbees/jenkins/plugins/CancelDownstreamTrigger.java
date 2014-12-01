package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.trimToNull;


public class CancelDownstreamTrigger extends Trigger<AbstractProject> {

    public Long numOfMilliSeconds;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CancelDownstreamTrigger(Long numOfMilliSeconds) {
        this.numOfMilliSeconds = numOfMilliSeconds;
    }
    public Long getNumOfMilliSeconds(){
        return numOfMilliSeconds;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {

        public Long numOfMilliSeconds;

        public Long getNumOfMilliSeconds() {
            return numOfMilliSeconds;
        }

        public void setNumOfMilliSeconds(Long numOfMilliSeconds) {
            this.numOfMilliSeconds = numOfMilliSeconds;
        }

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof AbstractProject;
        }

        @Override
        public String getDisplayName() {
            return "Build immediately cancelling downstream";
        }
    }
}

