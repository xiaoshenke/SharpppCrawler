package wuxian.me.easyexecution.core.executor;

import wuxian.me.easyexecution.core.event.Event;
import wuxian.me.easyexecution.core.event.EventCreators;
import wuxian.me.easyexecution.core.event.EventType;
import wuxian.me.easyexecution.core.executor.id.JobIdFactory;

public class JobRunner extends EventCreators implements Runnable {

    public JobRunner(AbstractJob job) {
        this.job = job;
    }

    public AbstractJob getJob() {
        return this.job;
    }

    public void setExecId(long execId) {
        if (this.job != null) {
            this.job.setExecId(String.valueOf(execId));
        }
    }

    private boolean isCanceled = false;

    public boolean isCanceled() {
        return isCanceled;
    }

    public void canel() {
        this.isCanceled = true;

        if (job != null) {
            try {
                job.cancel();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void run() {
        try {
            realRun();

        } catch (Exception e) {
            ;
        }

        //Todo: logging
        if (isCanceled) {
            fireEvent(Event.create(EventType.JOB_CANCELED, this.job));
        } else {
            fireEvent(Event.create(EventType.JOB_FINISHED, this.job));
        }

    }

    private AbstractJob job;

    public String getExecId() {
        return job == null ? "-1" : job.getExecId();
    }

    private void realRun() throws Exception {
        job.setStartTime(System.currentTimeMillis());
        this.job.run();
        job.setEndTime(System.currentTimeMillis());
    }

}
