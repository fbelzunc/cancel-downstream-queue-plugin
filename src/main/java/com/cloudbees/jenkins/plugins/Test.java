package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Items;
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

        if (jobName.equals("job-1")) {

            Job jobTriggered = (Job) Jenkins.getInstance().getItemByFullName(jobName);
            AbstractProject jobTriggered2 = (AbstractProject) jobTriggered;
            List<AbstractProject> childJobs = jobTriggered2.getDownstreamProjects();
            Iterator childJobsIterator = childJobs.iterator();

            //TODO need to check timestamp of first job in case it was already built
            if (jobTriggered2.getTrigger(CancelDownstreamQueueTrigger.class) != null)
                while (childJobsIterator.hasNext()) {
                    AbstractProject child = (AbstractProject) childJobsIterator.next();

                    Queue.Item[] childs = Jenkins.getInstance().getQueue().getItems();
                        for(int i=-0; i<childs.length; i++) {
                            Queue.Item myItem = childs[i];
                            System.out.println("Cancelling " + myItem.task.getDisplayName());
                            Jenkins.getInstance().getQueue().cancel(myItem.task);
                        }
                }
        }
    }
}