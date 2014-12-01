package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.*;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import jenkins.model.Jenkins;

import java.util.*;
import java.util.List;
import java.util.logging.Logger;



@Extension
public class CancelDownstreamQueueListener extends QueueListener {

    List<AbstractProject> currentStreamJobs = new ArrayList<AbstractProject>();
    AbstractProject rootProject = null;
    long DEFAULT_WINDOWS_TIME = 0;

    public static CancelDownstreamQueueListener instance() {
        return Jenkins.getInstance().getExtensionList(QueueListener.class).get(CancelDownstreamQueueListener.class);
    }

    @Override
    public void onEnterWaiting(Queue.WaitingItem wi) {
        String jobName = wi.task.getName();
        Job job = (Job) Jenkins.getInstance().getItemByFullName(jobName);
        AbstractProject project = (AbstractProject) job;

        if (project.getTrigger(CancelDownstreamTrigger.class) != null) {
            rootProject = project;
            cancelDownstreamJobs(job);
        }
        //Now start cancelling the builds/queue of the downstream jobs
        if (currentStreamJobs.size() != 0) {
            //Next step is to cancel the downstream jobs
            Iterator downStreamJobsIterator = currentStreamJobs.iterator();

            while (downStreamJobsIterator.hasNext()) {
                AbstractProject downStreamJob = (AbstractProject) downStreamJobsIterator.next();
                cancelDownstreamJobs((Job) downStreamJob);
            }
            currentStreamJobs.clear();
        }
    }

    public void cancelDownstreamJobs(Job job) {
        AbstractProject project = (AbstractProject) job;

        //Get current date time with Calendar()
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        long currentTimeLong = currentDate.getTime();
        //Windows time to cancel the downstream
        long windowsTime = DEFAULT_WINDOWS_TIME;
        //Check if the job is not already building in an Executors
        //If it is already building -> Cancel the build
        if (job.isBuilding()) {
            Date date = job.getLastBuild().getTime();
            long buildDate = date.getTime();
            long diff = currentTimeLong - buildDate;

            for (AbstractProject<?, ?> jenkinsJob : Hudson.getInstance().getAllItems(AbstractProject.class)) {
                if (rootProject.getName().equals(jenkinsJob.getName())) {
                    CancelDownstreamTrigger trigger = jenkinsJob.getTrigger(CancelDownstreamTrigger.class);
                    windowsTime = trigger.getNumOfMilliSeconds();
                }
            }
            if ((diff) < windowsTime)
                job.getLastBuild().getExecutor().doStop();
        }
        //Next step is to cancel the downstream jobs
        List<AbstractProject> childProjects = project.getDownstreamProjects();
        Iterator childProjectsIterator = childProjects.iterator();

        while (childProjectsIterator.hasNext()) {
            AbstractProject childProject = (AbstractProject) childProjectsIterator.next();
            currentStreamJobs.addAll(childProject.getDownstreamProjects());
            if (childProject.isBuilding()) {
                LOGGER.info("Cancelling build execution of job: " + childProject.getName());
                childProject.getLastBuild().getExecutor().doStop();
            } else if (childProject.isInQueue()) {
                Queue.Item myItem = childProject.getQueueItem();
                LOGGER.info("Removing job " + childProject.getName() + " from the queue");
                Jenkins.getInstance().getQueue().cancel(myItem.task);
            }
        }
    }
    private static final Logger LOGGER = Logger.getLogger(CancelDownstreamQueueListener.class.getName());

}