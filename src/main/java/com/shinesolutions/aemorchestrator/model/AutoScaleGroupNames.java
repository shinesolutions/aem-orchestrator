package com.shinesolutions.aemorchestrator.model;

public class AutoScaleGroupNames {
    private String authorDispatcher;
    private String publish;
    private String publishDispatcher;

    public String getAuthorDispatcher() {
        return authorDispatcher;
    }

    public void setAuthorDispatcher(String authorDispatcher) {
        this.authorDispatcher = authorDispatcher;
    }
    
    public AutoScaleGroupNames withAuthorDispatcher(String authorDispatcher) {
        this.setAuthorDispatcher(authorDispatcher);
        return this;
    }

    public String getPublish() {
        return publish;
    }

    public void setPublish(String publisher) {
        this.publish = publisher;
    }
    
    public AutoScaleGroupNames withPublish(String publisher) {
        this.setPublish(publisher);
        return this;
    }

    public String getPublishDispatcher() {
        return publishDispatcher;
    }

    public void setPublishDispatcher(String publisherDispatcher) {
        this.publishDispatcher = publisherDispatcher;
    }
    
    public AutoScaleGroupNames withPublishDispatcher(String publisherDispatcher) {
        this.setPublishDispatcher(publisherDispatcher);
        return this;
    }

}
