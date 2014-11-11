package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import jenkins.model.Jenkins;

@Extension
public class Test extends QueueListener {

    public static Test instance() {
        return Jenkins.getInstance().getExtensionList(QueueListener.class).get(Test.class);
    }

    @Override
    public void onEnterWaiting(Queue.WaitingItem wi) {
        System.out.println("Job waiting on queue");
    }
}

