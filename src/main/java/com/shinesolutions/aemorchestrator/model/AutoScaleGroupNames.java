package com.shinesolutions.aemorchestrator.model;

public class AutoScaleGroupNames {
    private String authorDispatcher;
    private String publisher;
    private String publisherDispatcher;

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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public AutoScaleGroupNames withPublisher(String publisher) {
        this.setPublisher(publisher);
        return this;
    }

    public String getPublisherDispatcher() {
        return publisherDispatcher;
    }

    public void setPublisherDispatcher(String publisherDispatcher) {
        this.publisherDispatcher = publisherDispatcher;
    }
    
    public AutoScaleGroupNames withPublisherDispatcher(String publisherDispatcher) {
        this.setPublisherDispatcher(publisherDispatcher);
        return this;
    }

}
