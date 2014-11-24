package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.*;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import jenkins.model.Jenkins;

import javax.naming.Context;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

@Extension
public class Test extends QueueListener {

    public static Test instance() {
        return Jenkins.getInstance().getExtensionList(QueueListener.class).get(Test.class);
    }

    @Override
    public void onEnterWaiting(Queue.WaitingItem wi) {


        String jobName = wi.task.getName();

        Job job = (Job) Jenkins.getInstance().getItemByFullName(jobName);

        AbstractProject project = (AbstractProject) job;

        if (project.getTrigger(CancelDownstreamQueueTrigger.class)!=null) {
            cancelDownstreamJobs(job);

        }
    }

    public void cancelDownstreamJobs(Job job) {

        AbstractProject project = (AbstractProject) job;
        //Get current date time with Calendar()
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        long currentTimeLong = currentDate.getTime();

        //Check if the job is not already building in an Executors
        //If it is already building -> Cancel the build
        if (job.isBuilding()) {
            Date date = job.getLastBuild().getTime();
            long buildDate = date.getTime();
            long diff= currentTimeLong - buildDate;
            if((diff)< 30*1000)
                job.getLastBuild().getExecutor().doStop();
        }

        //Next step is to cancel the downstream jobs
        List<AbstractProject> childProjects = project.getDownstreamProjects();
        Iterator childProjectsIterator = childProjects.iterator();

        while (childProjectsIterator.hasNext()) {
            AbstractProject childProject = (AbstractProject) childProjectsIterator.next();
            if (childProject.isBuilding())
                childProject.getLastBuild().getExecutor().doStop();
            else if (childProject.isInQueue()) {
                Queue.Item myItem = childProject.getQueueItem();
                Jenkins.getInstance().getQueue().cancel(myItem.task);
            }

        }
    }


}