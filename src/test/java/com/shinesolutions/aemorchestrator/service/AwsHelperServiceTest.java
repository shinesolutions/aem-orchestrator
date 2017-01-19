package com.shinesolutions.aemorchestrator.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.TagDescription;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;


@RunWith(MockitoJUnitRunner.class)
public class AwsHelperServiceTest {
    
    @Mock
    private AmazonEC2 amazonEC2Client;
    
    @Mock
    private AmazonElasticLoadBalancing amazonElbClient;
    
    @InjectMocks
    private AwsHelperService awsHelperService;

    @Test
    public void testGetTags() throws Exception {
        TagDescription tag1 = new TagDescription().withKey("key1").withValue("value1");
        TagDescription tag2 = new TagDescription().withKey("key2").withValue("value2");
        
        List<TagDescription> tagList = new ArrayList<TagDescription>();
        tagList.add(tag1);
        tagList.add(tag2);
        DescribeTagsResult describeTagResult = new DescribeTagsResult();
        describeTagResult.setTags(tagList);
        
        when(amazonEC2Client.describeTags(any(DescribeTagsRequest.class))).thenReturn(describeTagResult);
        
        Map<String, String> tagMap = awsHelperService.getTags("testInstanceId");
        
        assertThat(tagMap.get(tag1.getKey()), equalTo(tag1.getValue()));
        assertThat(tagMap.get(tag2.getKey()), equalTo(tag2.getValue()));
    }

}
