## TODO:

1. fix codacy issues
2. cropdb explorer
4. add p2p replications via jgroups
5. add lucene indexer
6. cropdb cluster via jgroups
7. spring data rest / graphql api over cropdb cluster
8. data views (like rdbms view)

## Other Articles

DataGate Server:

https://stackoverflow.com/questions/42644779/how-to-secure-a-websocket-endpoint-in-java-ee
https://github.com/ls1intum/jReto

https://github.com/eranyanay/1m-go-websockets
https://www.freecodecamp.org/news/million-websockets-and-go-cc58418460bb/

IPFS:

https://www.freecodecamp.org/news/how-to-build-mongodb-like-datastore-using-interplanetary-linked-data-in-5-minutes/

Queue message and run a timer task to pick message and send over websocket

## title DataGate Message Communication

    Replica (R1)->Server: Connect Message
    note left of Server: Validates Auth token in message
    
    note over Replica (R1)
        Somehow need to bar Replica from 
        sending DataGateFeed message to
        Server before receiving ConnectAck
    end note
    
    alt Valid token
        Server->Server: Stores replica id
        Server->Replica (R1): ConnectAck Message
        note left of Replica (R1):
            Replica can now receive DataGateFeed
        end note
    else Validation failed
        Server->Replica (R1): Error Message
        note left of Replica (R1): 
            Closes connection to server
            and will not receive any further
            communications from server
            until next successful Connect
        end note
    end
    
    Replica (R1)->Replica (R1): Find last sync time
    Replica (R1)->Replica (R1): Find changes since last sync
    
    Replica (R1)->Server: BatchChangeStart Message
    note right of Replica (R1)
        In replica keep the list of CropIds of
        the Documents sending to servers. Server
        would send back BatchChangeAck message with
        list of CropIds accepted by Server. Replica
        will remove those ids from it's list. Finally
        after exchange is completed this list size should
        be 0.
    end note
    
    note right of Replica (R1): 
        If changes exists sends changes 
        in chunks, size is sets in replication
        config
    end note
    
    Server->Replica (R1): BatchAck
    note left of Server
        BatchChangeAck will contain list of
        CropIds accepted by Server.
    end note
    
    Replica (R1)->Server: BatchChangeContinue Message
    Server->Replica (R1): BatchAck
    note left of Server:
        Server broadcast the BatchChangeContinue 
        to all connected peers
    end note
    
    Replica (R1)->Server: BatchChangeContinue Message
    Server->Replica (R1): BatchAck
    
    Server->Replica (R1): DataGateFeed
    Replica (R1)->Server: FeedAck
    
    note right of Replica (R1): 
        When there is no more changes to send
        send BatchChangeEnd with last sync time
    end note
    
    Replica (R1)->Server: BatchChangeEnd Message
    note right of Replica (R1): 
        BatchChangeEnd message will contain replicaId,
        batchSize and last sync time
    end note
    Server->Replica (R1): BatchEndAck
    Replica (R1)->Replica (R1): Check from journal for failed entries
    Replica (R1)->Server: Retry failed entries in DataGateFeed
    Server->Replica (R1): FeedAck
    
    Server->Replica (R1): BatchChangeStart Message
    Replica (R1)->Server: BatchAck
    note left of Server:
        Server should ensure all message sent to replica
        by keeping track of replicaId and cropId map
    end note
    
    Server->Replica (R1): BatchChangeContinue Message
    Replica (R1)->Server: BatchAck
    Server->Replica (R1): BatchChangeEnd Message
    Replica (R1)->Replica (R1): Save message header time as last sync time
    Replica (R1)->Server: BatchEndAck
    
    note right of Replica (R1):
        From now on Replica will store the
        timestamp of any ack or feed message
        as last sync time
    end note
    
    Replica (R1)->Server: DataGateFeed
    Server->Replica (R1): FeedAck
    Replica (R1)->Replica (R1): Save message header time as last sync time
    Replica (R1)->Replica (R1): Check from journal for failed entries
    Replica (R1)-->Server: Retry failed entries in DataGateFeed
    Server-->Replica (R1): FeedAck
    
    Server->Replica (R1): DataGateFeed
    Replica (R1)->Server: FeedAck
    Replica (R1)->Replica (R1): Save message header time as last sync time
    
    
    Replica (R1)->Server: Disconnect Message
    Server->Server: Removes replica id
    Replica (R1)->Replica (R1): Closes connection

https://blog.yugabyte.com/how-we-built-a-high-performance-document-store-on-rocksdb/

https://docs.yugabyte.com/latest/architecture/docdb/persistence/

https://blog.yugabyte.com/enhancing-rocksdb-for-speed-scale/

## Query Optimization

- https://www.javatpoint.com/dbms-transforming-relational-expressions
- http://www.cbcb.umd.edu/confcour/Spring2014/CMSC424/query_optimization.pdf
- https://www.tutorialcup.com/dbms/query-optimization.htm
- https://www.geeksforgeeks.org/query-optimization-in-relational-algebra/
 
 

