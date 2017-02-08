# AEM Orchestrator - Integration Test Scenarios

This is a document to be used as a template for manually integration testing that the AEM Orchestrator application. To use, simply make a copy of this mark down file and add test results to the copy forming a test report.

## Scenario 1: Stack Startup
Ensure the stack is in a healthy state upon startup.

### Prerequisites 
AEM Stack was initially created and no further interaction has occured. The AEM Orchestrator is in a running state and has completed processing all messages on the SQS queue (queue should be empty).

### Checklist
| Confirm                                                           | Passed? | Comment |
|-------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                |         |         |
| Author Dispatcher instances running                               |         |         |
| Publish instances running                                         |         |         |
| Publish Dispatcher instances running                              |         |         |
| Author Dispatcher tags (AuthorHost)                               |         |         |
| Publish tags (PublishDispatcherHost, PairInstanceId, SnapshotId)  |         |         |
| Publish Dispatcher tags (PublishHost, PairInstanceId, SnapshotId) |         |         |
| AEM running on Publish instances                                  |         |         |
| Flush agents created and enabled for Author/Author Dispatchers    |         |         |
| Replication agents created and enabled for Author/Publish         |         |         |
| No errors in AEM Author log files                                 |         |         |
| No errors in AEM Publish log files                                |         |         |


## Scenario 2: Terminate One Author Dispatcher Instance
Ensure the AEM Orchestrator recovers from a terminated Author Dispatcher instance

### Prerequisites 
AEM stack was already in a healthy state and at least one Author Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Terminate an Author Dispatcher instance
2. Wait for SQS queue to empty

### Checklist
| Confirm                                               | Passed? | Comment |
|-------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                    |         |         |
| Previous Author/Author Dispatcher flush agent deleted |         |         |
| New flush agent created and enabled                   |         |         |
| No errors in Author AEM log                           |         |         |
| New Author Dispatcher instance running                |         |         |
| New Author instance has AuthorHost tag                |         |         |


## Scenario 3: Terminate One Publish Dispatcher Instance
Ensure the AEM Orchestrator recovers from a terminated Publish Dispatcher instance

### Prerequisites 
AEM stack was already in a healthy state and at least one Author, Publish and Publish Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Select a Publish Dispatcher to terminate
2. Record the value of PairInsanceId and PublishHost tags on the instance
3. Terminate the Publish Dispatcher instance
4. Wait for SQS queue to empty

### Checklist
| Confirm                                                                              | Passed? | Comment |
|--------------------------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                                   |         |         |
| Paired Publish instance terminated                                                   |         |         |
| Previous Author/Publish replication agent deleted                                    |         |         |
| New Publish instance running                                                         |         |         |
| New Publish Dispatcher instance running                                              |         |         |
| Publish instance has correct PublishDispatcherHost, PairInstanceId, SnapshotId tags  |         |         |
| Publish Dispatcher instance has correct PublishHost, PairInstanceId, SnapshotId tags |         |         |
| New Author/Publish replication agent created and enabled                             |         |         |
| AEM running and healthy on Publish instance                                          |         |         |
| No errors in Author AEM logs                                                         |         |         |
| No errors in Publish AEM logs                                                        |         |         |


## Scenario 4: Terminate One Publish Instance
Ensure the AEM Orchestrator recovers from a terminated Publish instance

### Prerequisites 
AEM stack was already in a healthy state and at least one Author, Publish and Publish Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Select a Publish instance to terminate
2. Record the value of PairInsanceId and PublishDispatcherHost tags on the instance
3. Terminate the Publish instance
4. Wait for SQS queue to empty

### Checklist
| Confirm                                                                              | Passed? | Comment |
|--------------------------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                                   |         |         |
| Paired Publish Dispatcher instance terminated                                        |         |         |
| Previous Author/Publish replication agent deleted                                    |         |         |
| New Publish instance running                                                         |         |         |
| New Publish Dispatcher instance running                                              |         |         |
| Publish instance has correct PublishDispatcherHost, PairInstanceId, SnapshotId tags  |         |         |
| Publish Dispatcher instance has correct PublishHost, PairInstanceId, SnapshotId tags |         |         |
| New Author/Publish replication agent created and enabled                             |         |         |
| AEM running and healthy on Publish instance                                          |         |         |
| No errors in Author AEM logs                                                         |         |         |
| No errors in Publish AEM logs                                                        |         |         |


## Scenario 5: Add One Author Dispatcher Instance
Ensure the AEM Orchestrator can handle scaling up the number of Author Dispatcher instances

### Prerequisites 
AEM stack was already in a healthy state and at least one Author Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Increase the desired capacity of the Author Dispatcher auto scaling group by 1
2. Wait for SQS queue to empty

### Checklist
| Confirm                                | Passed? | Comment |
|----------------------------------------|:-------:|---------|
| No errors in Orchestrator log file     |         |         |
| New flush agent created and enabled    |         |         |
| No errors in Author AEM log            |         |         |
| New Author Dispatcher instance running |         |         |
| New instance has AuthorHost tag        |         |         |


## Scenario 6: Add One Publish Dispatcher Instance
Ensure the AEM Orchestrator can handle scaling up the number of Publish Dispatcher instances

### Prerequisites 
AEM stack was already in a healthy state and at least one Author, Publish and Publish Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Increase the desired capacity of the Publish Dispatcher auto scaling group by 1
2. Wait for SQS queue to empty

### Checklist
| Confirm                                                                              | Passed? | Comment |
|--------------------------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                                   |         |         |
| New Publish instance running                                                         |         |         |
| New Publish Dispatcher instance running                                              |         |         |
| Publish instance has correct PublishDispatcherHost, PairInstanceId, SnapshotId tags  |         |         |
| Publish Dispatcher instance has correct PublishHost, PairInstanceId, SnapshotId tags |         |         |
| New Author/Publish replication agent created and enabled                             |         |         |
| AEM running and healthy on Publish instance                                          |         |         |
| No errors in Author AEM logs                                                         |         |         |
| No errors in Publish AEM logs                                                        |         |         |


## Scenario 7: Add One Publish Instance
Ensure the AEM Orchestrator can handle scaling up the number of Publish instances

### Prerequisites 
AEM stack was already in a healthy state and at least one Author, Publish and Publish Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Increase the desired capacity of the Publish auto scaling group by 1
2. Wait for SQS queue to empty

### Checklist
| Confirm                                                                              | Passed? | Comment |
|--------------------------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                                   |         |         |
| New Publish instance running                                                         |         |         |
| New Publish Dispatcher instance running                                              |         |         |
| Publish instance has correct PublishDispatcherHost, PairInstanceId, SnapshotId tags  |         |         |
| Publish Dispatcher instance has correct PublishHost, PairInstanceId, SnapshotId tags |         |         |
| New Author/Publish replication agent created and enabled                             |         |         |
| AEM running and healthy on Publish instance                                          |         |         |
| No errors in Author AEM logs                                                         |         |         |
| No errors in Publish AEM logs                                                        |         |         |


## Scenario 8: Terminate All Instances
Ensure the AEM Orchestrator can recover from the termination of all Author Dispatcher, Publish and Publish Dispatcher instances simultaneously.

### Prerequisites 
AEM stack was already in a healthy state and at least one Author, Publish and Publish Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Record the number of Author, Publish and Publish Dispatcher instances
2. Terminate all Author, Publish and Publish Dispatcher instances
3. Wait for SQS queue to empty

### Checklist
| Confirm                                                                               | Passed? | Comment |
|---------------------------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                                    |         |         |
| All previous Author/Publish replication agents deleted                                |         |         |
| All previous Author/Author Dispatcher flush agents deleted                            |         |         |
| Correct number of Author Dispatcher instances created and running                     |         |         |
| Correct number of Publish Dispatcher instances created and running                    |         |         |
| Correct number of Publish instances created and running                               |         |         |
| Author instances have correct AuthorHost tags                                         |         |         |
| Publish instances have correct PublishDispatcherHost, PairInstanceId, SnapshotId tags |         |         |
| Publish Dispatcher instances has correct PublishHost, PairInstanceId, SnapshotId tags |         |         |
| New Author/Publish replication agents created and enabled                             |         |         |
| New Author/Author Dispatcher flush agents created and enabled                         |         |         |
| AEM running and healthy on Publish instances                                          |         |         |
| No errors in Author AEM logs                                                          |         |         |
| No errors in Publish AEM logs                                                         |         |         |


## Scenario 9: Scale Up All Instances
Ensure the AEM Orchestrator can handle scaling up all Author Dispatcher, Publish and Publish Dispatcher instances by at least 2.

### Prerequisites 
AEM stack was already in a healthy state and at least one Author, Publish and Publish Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Record the number of Author, Publish and Publish Dispatcher instances
2. Increase the desired capacity of the Publish auto scaling group by 2
3. Increase the desired capacity of the Author Dispatcher auto scaling group by 2
4. Wait for SQS queue to empty

### Checklist
| Confirm                                                                               | Passed? | Comment |
|---------------------------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                                    |         |         |
| Correct number of Author Dispatcher instances created and running                     |         |         |
| Correct number of Publish Dispatcher instances created and running                    |         |         |
| Correct number of Publish instances created and running                               |         |         |
| Author instances have correct AuthorHost tags                                         |         |         |
| Publish instances have correct PublishDispatcherHost, PairInstanceId, SnapshotId tags |         |         |
| Publish Dispatcher instances has correct PublishHost, PairInstanceId, SnapshotId tags |         |         |
| New Author/Publish replication agents created and enabled                             |         |         |
| New Author/Author Dispatcher flush agents created and enabled                         |         |         |
| AEM running and healthy on Publish instances                                          |         |         |
| No errors in Author AEM logs                                                          |         |         |
| No errors in Publish AEM logs                                                         |         |         |


## Scenario 10: Scale Down All Instances
Ensure the AEM Orchestrator can handle scaling down all Author Dispatcher, Publish and Publish Dispatcher instances by at least 2.

### Prerequisites 
AEM stack was already in a healthy state and at least 3 Author, Publish and Publish Dispatcher instances are running. The AEM Orchestrator is running.

### Steps
1. Record the number of Author, Publish and Publish Dispatcher instances
2. Decrease the desired capacity of the Publish auto scaling group by 2
3. Decrease the desired capacity of the Author Dispatcher auto scaling group by 2
4. Wait for SQS queue to empty

### Checklist
| Confirm                                                                                          | Passed? | Comment |
|--------------------------------------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                                               |         |         |
| The terminated Author/Publish instance replication agents are deleted                            |         |         |
| The terminated Author/Author Dispatcher instance flush agents are deleted                        |         |         |
| Correct number of Author Dispatcher instances created and running                                |         |         |
| Correct number of Publish Dispatcher instances created and running                               |         |         |
| Correct number of Publish instances created and running                                          |         |         |
| Remaining Author instances have correct AuthorHost tags                                          |         |         |
| Remaining Publish instances have correct PublishDispatcherHost, PairInstanceId, SnapshotId tags  |         |         |
| Remaining Publish Dispatcher instances have correct PublishHost, PairInstanceId, SnapshotId tags |         |         |
| Remaining Author/Publish replication agents still existing and enabled                           |         |         |
| Remaining Author/Author Dispatcher flush agents still existing and enabled                       |         |         |
| AEM running and healthy on Publish instances                                                     |         |         |
| No errors in Author AEM logs                                                                     |         |         |
| No errors in Publish AEM logs                                                                    |         |         |


## Scenario 11: Terminate AEM Orchestrator and All Instances
Ensure the AEM Orchestrator can recover from the termination of all Author Dispatcher, Publish and Publish Dispatcher instances from a shutdown state.

### Prerequisites
AEM stack was already in a healthy state and at least one Author, Publish and Publish Dispatcher instance is running. The AEM Orchestrator is running.

### Steps
1. Record the number of Author, Publish and Publish Dispatcher instances
2. Shut down the AEM Orchestrator
2. Terminate all Author, Publish and Publish Dispatcher instances
3. Start the AEM Orchestrator
3. Wait for SQS queue to empty

### Checklist
| Confirm                                                                               | Passed? | Comment |
|---------------------------------------------------------------------------------------|:-------:|---------|
| No errors in Orchestrator log file                                                    |         |         |
| All previous Author/Publish replication agents deleted                                |         |         |
| All previous Author/Author Dispatcher flush agents deleted                            |         |         |
| Correct number of Author Dispatcher instances created and running                     |         |         |
| Correct number of Publish Dispatcher instances created and running                    |         |         |
| Correct number of Publish instances created and running                               |         |         |
| Author instances have correct AuthorHost tags                                         |         |         |
| Publish instances have correct PublishDispatcherHost, PairInstanceId, SnapshotId tags |         |         |
| Publish Dispatcher instances has correct PublishHost, PairInstanceId, SnapshotId tags |         |         |
| New Author/Publish replication agents created and enabled                             |         |         |
| New Author/Author Dispatcher flush agents created and enabled                         |         |         |
| AEM running and healthy on Publish instances                                          |         |         |
| No errors in Author AEM logs                                                          |         |         |
| No errors in Publish AEM logs                                                         |         |         |
