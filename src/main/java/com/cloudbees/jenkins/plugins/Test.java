package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import jenkins.model.Jenkins;

import javax.naming.Context;
import java.util.*;

@Extension
public class Test extends QueueListener {

    private final Map<Queue.WaitingItem, Context> waiting = new WeakHashMap<Queue.WaitingItem, Context>();

    public static Test instance() {
        return Jenkins.getInstance().getExtensionList(QueueListener.class).get(Test.class);
    }

    @Override
    public void onEnterWaiting(Queue.WaitingItem wi) {

        String jobName = wi.task.getName();
        Job jobTriggered = (Job) Jenkins.getInstance().getItemByFullName(jobName);
        AbstractProject jobTriggered2 = (AbstractProject) jobTriggered;
        List<AbstractProject> childJobs = jobTriggered2.getDownstreamProjects();
        Iterator childJobsIterator = childJobs.iterator();

        if(jobTriggered2.getTrigger(CancelDownstreamQueueTrigger.class)!=null) {
        //TODO need to check timestamp of first job in case it was already built

            while (childJobsIterator.hasNext()) {
                AbstractProject downstreamJob = (AbstractProject) childJobsIterator.next();
                System.out.println("Downstream job " + downstreamJob.getName());

                Jenkins.getInstance().getQueue().cancel(downstreamJob.getOwnerTask());
                System.out.println("Job was cancelled " + downstreamJob.getOwnerTask().getName());
            }
        }

    }
}